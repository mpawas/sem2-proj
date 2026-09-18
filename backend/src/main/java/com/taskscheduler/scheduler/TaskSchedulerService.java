package com.taskscheduler.scheduler;

import com.taskscheduler.common.TaskStatus;
import com.taskscheduler.model.StatsView;
import com.taskscheduler.model.TaskRecord;
import com.taskscheduler.model.TaskSubmitRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * The core asynchronous task scheduler.
 *
 * <p>Pipeline: producers submit into a thread-safe registry, a
 * {@link ScheduledExecutorService} fires delayed dispatches into a
 * {@link PriorityTaskQueue} (priority + FIFO), consumer threads hand work to a
 * fixed-size worker pool. Transitions are published to SSE listeners. Timeouts
 * come from scheduled guards; failures retry with exponential back-off;
 * cancellation is cooperative (volatile flag observed by jobs).</p>
 */
@Service
public class TaskSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerService.class);
    private static final Duration CONSUMER_POLL_TIMEOUT = Duration.ofMillis(500);
    private static final int DISPATCHER_THREADS = 4;
    private static final int MAX_BURST = 500;

    private final SchedulerProperties props;
    private final DemoJobFactory jobFactory;

    /** Thread-safe registry: taskId -> record. Reads are lock-free. */
    private final ConcurrentHashMap<String, TaskRecord> registry = new ConcurrentHashMap<>();
    private final PriorityTaskQueue inboundQueue;
    private final SchedulerMetrics metrics = new SchedulerMetrics();
    private final AtomicLong sequence = new AtomicLong();

    /** SSE/UI listeners; CopyOnWriteArrayList = safe to iterate while modified. */
    private final List<Consumer<TaskRecord>> listeners = new CopyOnWriteArrayList<>();

    private ScheduledExecutorService dispatcher; // delayed dispatch + timeout guards
    private ExecutorService jobWorkers;          // actually executes jobs
    private final List<Thread> consumerThreads = new CopyOnWriteArrayList<>();
    private volatile boolean running = true;

    /** Demo producer-consumer channel used by /api/tasks/burst. */
    private final LinkedBlockingQueue<TaskSubmitRequest> burstChannel = new LinkedBlockingQueue<>(256);

    public TaskSchedulerService(SchedulerProperties props, DemoJobFactory jobFactory) {
        this.props = props;
        this.jobFactory = jobFactory;
        this.inboundQueue = new PriorityTaskQueue(props.queueCapacity());
    }

    // ------------------------------------------------------------------
    // Life-cycle
    // ------------------------------------------------------------------
    @PostConstruct
    public void start() {
        dispatcher = Executors.newScheduledThreadPool(DISPATCHER_THREADS);
        jobWorkers = Executors.newFixedThreadPool(props.workers());

        for (int i = 0; i < props.workers(); i++) {
            Thread consumer = new Thread(this::consumeLoop, "scheduler-consumer-" + i);
            consumer.setDaemon(true);
            consumer.start();
            consumerThreads.add(consumer);
        }

        Thread burst = new Thread(this::drainBurstChannel, "burst-consumer");
        burst.setDaemon(true);
        burst.start();
        consumerThreads.add(burst);

        log.info("Scheduler started: workers={}, queueCapacity={}, timeoutMs={}, maxRetries={}",
                props.workers(), props.queueCapacity(), props.defaultTimeoutMs(), props.maxRetries());
    }

    @PreDestroy
    public void stop() {
        running = false;
        jobWorkers.shutdown();
        dispatcher.shutdown();
        try {
            if (!jobWorkers.awaitTermination(60, TimeUnit.SECONDS)) {
                jobWorkers.shutdownNow();
            }
            if (!dispatcher.awaitTermination(5, TimeUnit.SECONDS)) {
                dispatcher.shutdownNow();
            }
            for (Thread consumer : consumerThreads) {
                consumer.join(2_000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Scheduler stopped gracefully");
    }

// ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------
    /** Submit a task; it fires after delayMillis and runs fully asynchronously. */
    public TaskRecord submit(TaskSubmitRequest request) {
        String id = UUID.randomUUID().toString();
        TaskRecord task = new TaskRecord(
                id,
                request.name(),
                request.type(),
                request.priority(),
                request.delayMillis(),
                request.timeoutMillis(),
                request.maxRetries(),
                request.failRate(),
                request.params());

        registry.put(id, task);
        metrics.taskSubmitted();

        long fireAt = System.currentTimeMillis() + task.delayMillis();
        task.markSubmitted(fireAt);
        publish(task);   // PENDING goes out BEFORE any dispatch event
        task.setDispatchHandle(dispatcher.schedule(() -> dispatch(task),
                task.delayMillis(), TimeUnit.MILLISECONDS));
        return task;
    }

    /**
     * Marks a task CANCELLED. PENDING tasks also have their scheduled fire
     * attempt cancelled (best-effort); QUEUED/RUNNING tasks observe the volatile
     * flag and stop cooperatively. Returns {@code false} for terminal tasks.
     */
    public boolean cancel(String id) {
        TaskRecord task = require(id);
        if (!task.status().isCancellable()) {
            return false;
        }
        var handle = task.dispatchHandle();
        if (handle != null) {
            handle.cancel(false);   // no interrupt needed for PENDING
        }
        task.markCancelled("Cancelled by user");
        metrics.taskCancelled();
        publish(task);
        return true;
    }

    /** Manual retry of a FAILED / TIMED_OUT task (keeps its original priority). */
    public TaskRecord manualRetry(String id) {
        TaskRecord task = require(id);
        if (!task.status().isRetryable()) {
            throw new SchedulerException("Task '" + id + "' has status " + task.status()
                    + " and cannot be retried");
        }
        task.resetForManualRetry();
        enqueue(task);
        publish(task);
        return task;
    }

    /**
     * Evicts every task in a terminal state (COMPLETED, FAILED, CANCELLED,
     * TIMED_OUT). Active tasks (PENDING / QUEUED / RUNNING) are kept.
     *
     * <p>This is the "clear finished" action from the dashboard and the FR-11
     * requirement (bounded history eviction). The cumulative metric counters are
     * intentionally not reset — they are lifetime totals for the process.</p>
     *
     * @return the number of terminal records removed
     */
    public int clearFinished() {
        List<String> finished = registry.values().stream()
                .filter(t -> t.status().isTerminal())
                .map(TaskRecord::id)
                .toList();
        finished.forEach(registry::remove);
        if (!finished.isEmpty()) {
            log.info("Clear finished: removed {} terminal tasks", finished.size());
        }
        return finished.size();
    }

    /** Runnable for the priority queue; dispatcher path. */
    private void dispatch(TaskRecord task) {
        if (!running) {
            return;
        }
        if (task.isCancelRequested()) {
            return;                       // cancelled while still PENDING
        }
        task.markQueued();
        publish(task);
        enqueue(task);
    }

    /** Push into the priority queue with a fresh FIFO sequence number. */
    private void enqueue(TaskRecord task) {
        inboundQueue.put(new QueuedTask(task, sequence.incrementAndGet(),
                System.currentTimeMillis()));
    }
// ------------------------------------------------------------------
    // Consumer threads (pull from the priority queue, submit to the pool)
    // ------------------------------------------------------------------
    private void consumeLoop() {
        while (running) {
            try {
                QueuedTask item = inboundQueue.poll(CONSUMER_POLL_TIMEOUT.toMillis(),
                        TimeUnit.MILLISECONDS);
                if (item == null) {
                    continue;
                }
                TaskRecord task = item.task();
                if (task.isCancelRequested()) {
                    continue;             // already marked CANCELLED by cancel()
                }
                jobWorkers.submit(() -> execute(task));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!running) {
                    break;
                }
            } catch (RuntimeException e) {
                log.error("Consumer loop crashed", e);
            }
        }
    }

    /** Runs the job body on a worker thread with timeout-guard and post-checks. */
    private void execute(TaskRecord task) {
        task.startAttempt(Thread.currentThread().getName());

        // Timeout guard: marks TIMED_OUT if the job is still RUNNING when it fires.
        dispatcher.schedule(() -> {
            if (task.status() == TaskStatus.RUNNING) {
                task.markTimedOut("Execution exceeded " + task.timeoutMillis() + " ms");
                metrics.taskTimedOut();
                publish(task);
            }
        }, Math.max(1, task.timeoutMillis()), TimeUnit.MILLISECONDS);

        try {
            jobFactory.maybeFail(task);
            String result = jobFactory.execute(task, this::snapshot);
            if (task.status() == TaskStatus.RUNNING) {
                task.markCompleted(result);
                metrics.taskCompleted();
                publish(task);
            }                            // otherwise timeout/cancel won the race
        } catch (JobCancelledException ce) {
            task.markCancelled(ce.getMessage());
            metrics.taskCancelled();
            publish(task);
        } catch (JobFailureException fe) {
            retryOrFail(task, fe.getMessage());
        } catch (Throwable t) {
            log.error("Task {} crashed with unexpected exception", task.id(), t);
            retryOrFail(task, t.getMessage() == null
                    ? t.getClass().getSimpleName() : t.getMessage());
        }
    }

    /** Automatic retry with exponential back-off until maxRetries is exhausted. */
    private void retryOrFail(TaskRecord task, String message) {
        if (task.attempt() <= task.maxRetries()) {
            long backoffMillis = (long) (1_000L * Math.pow(2, task.attempt() - 1));
            task.markRetryAfterFailure(message, backoffMillis);
            metrics.taskFailed();
            publish(task);
            dispatcher.schedule(() -> {
                if (running && !task.isCancelRequested()
                        && task.status() == TaskStatus.QUEUED) {
                    enqueue(task);
                    publish(task);
                }
            }, backoffMillis, TimeUnit.MILLISECONDS);
        } else {
            task.markFailed(message);
            metrics.taskFailed();
            publish(task);
        }
    }
// ------------------------------------------------------------------
    // Burst demo: producer publishes into a channel; consumer submits + jitters
    // ------------------------------------------------------------------
    public int submitBurst(int count) {
        if (count < 1 || count > MAX_BURST) {
            throw new SchedulerException("count must be between 1 and " + MAX_BURST);
        }
        int accepted = 0;
        for (int i = 0; i < count; i++) {
            if (burstChannel.offer(burstRequest(i))) {
                accepted++;
            }
        }
        log.info("Burst: accepted {} of {} synthetic submissions", accepted, count);
        return accepted;
    }

    private TaskSubmitRequest burstRequest(int i) {
        return new TaskSubmitRequest(
                "burst-task-" + i,
                com.taskscheduler.common.TaskType.DELAY,
                com.taskscheduler.common.TaskPriority.fromRank(
                        ThreadLocalRandom.current().nextInt(0, 4)),
                ThreadLocalRandom.current().nextInt(50, 500),
                60_000,
                0,
                0,
                java.util.Map.of("durationMs",
                        ThreadLocalRandom.current().nextLong(300, 3_000)));
    }

    private void drainBurstChannel() {
        while (running) {
            try {
                TaskSubmitRequest req = burstChannel.poll(500, TimeUnit.MILLISECONDS);
                if (req == null) {
                    continue;
                }
                submit(req);
                // Small jitter: the queue depth becomes plainly visible on the UI.
                Thread.sleep(ThreadLocalRandom.current().nextLong(40, 250));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!running) {
                    break;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------
    public TaskRecord require(String id) {
        TaskRecord task = registry.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    public List<TaskRecord> snapshot() {
        return registry.values().stream()
                .sorted(Comparator.comparingLong(TaskRecord::submittedAtMillis).reversed())
                .toList();
    }

    public Statistics statsSnapshot() {
        return new Statistics(registry.values().stream().toList());
    }

    /** Record bundling counts + latency percentiles computed from a snapshot. */
    public record Statistics(Collection<TaskRecord> all) {
        public long count(TaskStatus status) {
            return all.stream().filter(t -> t.status() == status).count();
        }

        public long avgLatencyMillis() {
            return (long) latencies()
                    .boxed()
                    .collect(Collectors.summarizingLong(Long::longValue))
                    .getAverage();
        }

        public long p95LatencyMillis() {
            long[] sorted = latencies().toArray();
            return sorted.length == 0 ? 0
                    : sorted[(int) Math.ceil(0.95 * sorted.length) - 1];
        }

        private LongStream latencies() {
            return all.stream()
                    .filter(t -> t.status() == TaskStatus.COMPLETED)
                    .filter(t -> t.latencyMillis() > 0)
                    .mapToLong(TaskRecord::latencyMillis)
                    .sorted();
        }
    }

    public StatsView stats() {
        Statistics s = statsSnapshot();
        return new StatsView(
                s.all().size(),
                s.count(TaskStatus.PENDING),
                s.count(TaskStatus.QUEUED),
                s.count(TaskStatus.RUNNING),
                s.count(TaskStatus.COMPLETED),
                s.count(TaskStatus.FAILED),
                s.count(TaskStatus.CANCELLED),
                s.count(TaskStatus.TIMED_OUT),
                s.avgLatencyMillis(),
                s.p95LatencyMillis(),
                props.workers(),
                inboundQueue.depth(),
                metrics.submittedTotal());
    }

    // ------------------------------------------------------------------
    // Listeners (SSE)
    // ------------------------------------------------------------------
    public Runnable subscribe(Consumer<TaskRecord> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    private void publish(TaskRecord task) {
        if (listeners.isEmpty()) {
            return;
        }
        for (Consumer<TaskRecord> listener : listeners) {
            try {
                listener.accept(task);
            } catch (RuntimeException e) {
                log.warn("Removing a failing SSE listener", e);
                listeners.remove(listener);
            }
        }
    }
}