package com.taskscheduler.scheduler;

import com.taskscheduler.common.TaskStatus;
import com.taskscheduler.common.TaskType;
import com.taskscheduler.model.TaskRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Produces the workload functions for each {@link TaskType}.
 *
 * <p>The jobs themselves are written to be <em>cooperatively cancellable</em>:
 * loops regularly call {@link #checkCancelled(TaskRecord)} and exit cleanly, and
 * report progress through {@code TaskRecord#noteProgress(int)}.</p>
 */
@Component
public class DemoJobFactory {

    private static final Logger log = LoggerFactory.getLogger(DemoJobFactory.class);
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /** Roll the failRate dice - throws when the injected failure should happen. */
    public void maybeFail(TaskRecord task) {
        if (task.failRate() > 0 && ThreadLocalRandom.current().nextInt(100) < task.failRate()) {
            throw new JobFailureException("Injected random failure (failRate=" + task.failRate() + "%)");
        }
    }

    /**
     * Executes the job for a task. A snapshot supplier is passed in so that
     * {@link TaskType#BUILD_REPORT} can aggregate over the live registry without
     * creating a circular dependency (JobFactory -> SchedulerService).
     */
    public String execute(TaskRecord task, Supplier<Collection<TaskRecord>> snapshot) {
        return switch (task.type()) {
            case DELAY        -> runDelay(task);
            case COMPUTE      -> runCompute(task);
            case FETCH        -> runFetch(task);
            case BUILD_REPORT -> buildReport(snapshot.get());
        };
    }

    // ------------------------------------------------------------------
    // DELAY - simulated I/O, watches the cancel flag and reports progress
    // ------------------------------------------------------------------
    private String runDelay(TaskRecord task) {
        long duration = Math.max(100, Math.min(120_000, task.longParam("durationMs", 5_000)));
        long deadline = System.currentTimeMillis() + duration;

        while (System.currentTimeMillis() < deadline) {
            checkCancelled(task);
            long remaining = deadline - System.currentTimeMillis();
            nap(Math.min(100, Math.max(1, remaining)));   // ≤100 ms cooperative slices
            long done = duration - remaining;
            task.noteProgress((int) Math.min(99, done * 100 / duration));
        }
        task.noteProgress(100);
        return "Simulated I/O wait of " + duration + " ms finished at " + now();
    }

// ------------------------------------------------------------------
    // COMPUTE - CPU-bound sieve of Eratosthenes
    // ------------------------------------------------------------------
    private String runCompute(TaskRecord task) {
        long limit = Math.max(10_000, Math.min(50_000_000L, task.longParam("limit", 2_000_000L)));
        boolean[] composite = new boolean[(int) limit + 1];   // index == number
        long start = System.nanoTime();
        int found = 0;

        for (int i = 2; i < composite.length; i++) {
            if ((i & 0xFF) == 0) {                            // check every 256 iterations
                checkCancelled(task);
                task.noteProgress((int) (i * 100L / limit));
            }
            if (!composite[i]) {
                found++;
                if ((long) i * i <= limit) {
                    for (int j = i * i; j < composite.length; j += i) {
                        composite[j] = true;
                    }
                }
            }
        }
        task.noteProgress(100);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return "Sieve(limit=" + limit + ") found " + found + " primes in " + elapsedMs + " ms";
    }

    // ------------------------------------------------------------------
    // FETCH - simulated remote call
    // ------------------------------------------------------------------
    private String runFetch(TaskRecord task) {
        String url = task.stringParam("url", "https://example.com/api/v1/data");
        long latency = ThreadLocalRandom.current().nextLong(500, 3_501);
        nap(latency);
        checkCancelled(task);
        return "GET " + url + " -> 200 OK, 106496 bytes, in " + latency + " ms (simulated)";
    }

    // ------------------------------------------------------------------
    // BUILD_REPORT - parallel stream aggregation over the registry
    // ------------------------------------------------------------------
    private String buildReport(Collection<TaskRecord> all) {
        long startedAt = System.nanoTime();
        List<TaskRecord> tasks = new ArrayList<>(all);

        // toConcurrentMap is safe under parallel streams (no shared-state surgery).
        Map<TaskType, Long> byType = tasks.parallelStream()
                .collect(Collectors.toConcurrentMap(
                        TaskRecord::type, t -> 1L, Long::sum));

        long completed = tasks.parallelStream()
                .filter(t -> t.status() == TaskStatus.COMPLETED)
                .count();

        // LongSummaryStatistics gives us avg/stddev without double iteration.
        var latencyStats = tasks.parallelStream()
                .filter(t -> t.status() == TaskStatus.COMPLETED && t.latencyMillis() > 0)
                .mapToLong(TaskRecord::latencyMillis)
                .boxed()
                .collect(Collectors.summarizingLong(Long::longValue));

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        return "REPORT built in " + elapsedMs + " ms\n"
                + "  tasks in registry : " + tasks.size() + "\n"
                + "  completed         : " + completed + "\n"
                + "  avg latency       : " + (long) latencyStats.getAverage() + " ms\n"
                + "  byType            : " + byType;
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------
    private void checkCancelled(TaskRecord task) {
        if (task.isCancelRequested()) {
            throw new JobCancelledException("Cancelled cooperatively while running");
        }
    }

    /** Clean sleep that re-raises the interrupt flag instead of swallowing it. */
    private static void nap(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobCancelledException("Interrupted while sleeping");
        }
    }

    private static String now() {
        return CLOCK.format(LocalTime.now());
    }
}