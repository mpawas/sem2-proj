# Design Document — Async Task Scheduler

> Companion to the root [`README.md`](../README.md). This document explains **how**
> the scheduler works and **why** — the architecture, the threading model, the
> scheduling algorithm, the failure model, and a catalogue of every advanced Java
> facility used, with pointers to the exact code.

---

## 1. Problem statement & goals

Web applications routinely need to run work *in the background*: fire a delay,
queue an email, recompute a report, retry a flaky call — without blocking the
request that triggered it. This project builds a small but honest version of that
idea:

**Goals**

1. Submit a task with a **priority** and an optional **start delay**.
2. Run it **asynchronously** on a bounded worker pool — the caller never waits.
3. Observe state in **real time** (browser dashboard over SSE).
4. Tolerate failures: **automatic retries** with back-off, execution **timeouts**.
5. Keep the whole system **thread-safe** using *only* the advanced Java
   concurrency toolkit (no external job libraries).

**Non-goals**

- Persistence / durable queues (in-memory by design; see §9 Future work).
- Distributed scheduling (single-node).
- Production-grade auth.

---

## 2. High-level architecture

```
   Browser (React :5173)                       Spring Boot :8080
   ┌─────────────────────────┐      http       ┌────────────────────────────────────┐
   │ Vite dev server         │ ──────────────► │ TaskController  (REST /api/tasks)  │
   │  POST /api/tasks        │ ◄────────────── │ DashboardController (SSE /api/events)│
   │  GET  /api/events (SSE) │                 └───────────────┬────────────────────┘
   └─────────────────────────┘                                ▼
                      ┌──────────────── TaskSchedulerService ──────────────────────┐
                      │  submit() ──► ConcurrentHashMap<id,TaskRecord> (registry)  │
                      │       │                                                     │
                      │       │ dispatcher.schedule(delay)  (after delay elapses)   │
                      │       ▼                                                     │
                      │  dispatch() ──► PriorityTaskQueue (PriorityBlockingQueue)   │
                      │                      │ (CRITICAL first, FIFO tie-break)      │
                      │          consumers poll ──► jobWorkers pool                  │
                      │                      │      execute(job) + timeout guard     │
                      │                      ▼                                      │
                      │   publish(TaskRecord) ──► SSE listeners (the dashboard)      │
                      └──────────────────────────────────────────────────────────────┘
```

**Three independent thread pools (separation of concerns):**

| Pool | Type | Threads | Used for |
| --- | --- | --- | --- |
| `dispatcher` | `ScheduledExecutorService` | 4 | Delayed dispatch, retry back-off timers, execution-timeout guards |
| `jobWorkers` | `Executors.newFixedThreadPool` | `scheduler.workers` | Actually running jobs |
| consumer threads | raw `Thread`s | `scheduler.workers` | Pulling from the priority queue, handing work to the pool |

> Separating the *dispatcher* (timer logic) from the *workers* (job execution)
> means a slow job can never stall the timing machinery, and a tight timer loop
> can never starve a running job. The buffered `PriorityBlockingQueue` also
> decouples producers (fast REST calls) from consumers (slow jobs).

---

## 3. The task life-cycle (state machine)

```
        submit()
           │
           ▼
      ┌──── PENDING ────────────────────────────┐  delay elapses,
      │   (scheduled future saved)              │  dispatcher fires
      │        │                                ▼
      │        │ cancel(d)                  +────────── wait: QUEUED
      │        ✘                             │
      │                                      ▼
      │                                 RUNNING ──► timeout guard fires ──► TIMED_OUT
      │                                      │
      │                                      ├─ job returns OK ⇒ COMPLETED
      │                                      ├─ job throws ⇒ FAILED ── retry? ──► QUEUED
      │                                      └─ cancel flag ⇒ CANCELLED
      └────────────────────────────────────────
```

- **PENDING** — registered, waiting out its `delayMillis`.
- **QUEUED** — in the priority queue (or between retries).
- **RUNNING** — executing on a worker.
- **COMPLETED / FAILED / TIMED_OUT / CANCELLED** — terminal.

**Atomicity.** Every transition is a `synchronized` method on `TaskRecord`;
`status` is `volatile` so reads stay lock-free, and the executor **re-checks the
status after the job body returns** (`if (task.status() == RUNNING)`) so a
## 4. Threading model, step by step

**Submit** (REST thread, e.g. `tomcat-http-*`):

```java
registry.put(id, task);                              // CHM put
task.setDispatchHandle(dispatcher.schedule(
    () -> dispatch(task), task.delayMillis(), ...)); // scheduled future
publish(task);                                      // SSE "PENDING"
```

**Dispatch** (dispatcher thread, after the delay):

```java
task.markQueued();
publish(task);
inboundQueue.put(new QueuedTask(task, seq.incrementAndGet(), now));
```

**Consume** (each `scheduler-consumer-N` thread):

```java
QueuedTask item = inboundQueue.poll(500, MILLISECONDS); // block w/ timeout
if (item.task().isCancelRequested()) continue;           // already CANCELLED
jobWorkers.submit(() -> execute(task));                  // hand to the pool
```

**Execute** (worker pool thread):

```java
task.startAttempt(workerName);        // RUNNING, attempt++, event log
dispatcher.schedule(timeoutGuard, task.timeoutMillis());   // TIMED_OUT guard
try {
    String result = jobFactory.execute(task, this::snapshot);
    if (task.status() == RUNNING) { task.markCompleted(result); metrics.taskCompleted(); }
} catch (JobCancelledException c) { task.markCancelled(...); }
  catch (JobFailureException f)   { retryOrFail(task, f.getMessage()); }
  catch (Throwable t)             { retryOrFail(task, ...); }
publish(task);                                          // SSE "COMPLETED"/...
```

That design has a nice property: the **consumer threads never block on the job**,
so the priority queue keeps turning even while all workers are busy.

## 5. Failure handling & retries

- Jobs may throw on purpose for the demo; `failRate` on a request controls the
  probability (`ThreadLocalRandom.nextInt(100) < failRate`).
- `retryOrFail` uses **exponential back-off**: after attempt *k* fails it
  schedules a re-queue `2^(k-1)` seconds later (`1s, 2s, 4s, …`), up to
  `maxRetries` automatic attempts, then the task ends **FAILED**.
- Each failed attempt is recorded in the task's **event log** before re-queue.
- The dashboard can also **manually retry** any FAILED / TIMED_OUT task
  (`POST /api/tasks/{id}/retry`) — which re-inserts it with its original
  priority via `resetForManualRetry()`.

## 6. Cancellation semantics (cooperative, safe)

Java cannot safely kill a running thread, so cancellation is **cooperative**:

- PENDING → the saved `ScheduledFuture.cancel(false)` unschedules the fire.
- QUEUED → the cancel flag is set; the consumer thread skips it.
- RUNNING → the cancel flag is set; long-running jobs call
  `checkCancelled(task)` in their loop and throw `JobCancelledException`, which
  the executor catches and turns into CANCELLED.

The flag is `volatile boolean cancelRequested` — set inside  the
`synchronized markCancelled` transition so it is never torn.

---

## 7. Advanced-Java feature catalogue (with code pointers)

> This project is deliberately built so you can point at a *specific line* for
> each standard concurrency interview topic.

| # | Facility | Where | What it does here |
| --- | --- | --- | --- |
| 1 | `ExecutorService` / `Executors.newFixedThreadPool` | `TaskSchedulerService.start()` | The `jobWorkers` pool that executes jobs |
| 2 | `ScheduledExecutorService` / `schedule` | `submit()`, `execute()`, `retryOrFail()` | Delayed dispatch, timeout guards, back-off timers |
| 3 | `PriorityBlockingQueue` | `PriorityTaskQueue` + `QueuedTask.compareTo` | Lock-free priority scheduling |
| 4 | `ConcurrentHashMap` | `TaskSchedulerService.registry` | Thread-safe task registry, lock-free reads |
| 5 | `CopyOnWriteArrayList` | listeners, consumer threads | Concurrent iteration while mutating |
| 6 | `AtomicLong` / `AtomicInteger` | `SchedulerMetrics`, `sequence`, progress | Lock-free counters & monotonic FIFO sequence |
| 7 | `volatile` | `TaskRecord.status`, `cancelRequested` | Lock-free visibility of hot state |
| 8 | `synchronized` transition methods | `TaskRecord` | Atomic state transitions |
| 9 | `LinkedBlockingQueue` | `burstChannel` | Producer-consumer demo (Burst ×10) |
| 10 | `Stream` / `parallelStream()` | `TaskSchedulerService.stats()`, `DemoJobFactory.buildReport` | Functional aggregation, parallel CPU report |
| 11 | `LongSummaryStatistics` | `buildReport`, stats | One-pass avg/min/max/stddev |
| 12 | Records (immutable DTOs & views) | all DTOs | `equals/hashCode/toString` + JSON for free |
| 13 | Enum with fields & behavior | `TaskStatus.isTerminal()`, `TaskType` | Type-safe state machine |
| 14 | Switch *expression* | `DemoJobFactory.execute` | Concise dispatch by task type |
| 15 | **Graceful shutdown** | `stop()` (await + `shutdownNow` fallback) | No torn jobs on exit |
| 16 | Records + `@ConfigurationProperties` | `SchedulerProperties` | Type-safe config binding |
## 8. REST & realtime contract

**SSE stream** (`GET /api/events`) is a `text/event-stream`:

```
event: task
data: {"id":"...","name":"...","status":"RUNNING","progress":37,...}

event: ping
data: keep-alive
```

Each connection registers a `Consumer<TaskRecord>` listener; every transition
fans out to all of them on a `CopyOnWriteArrayList`. The listener builds an
immutable `TaskView` snapshot and `emitter.send(...)`s it. A 15 s `ping`
heartbeat keeps proxies from closing idle connections; `onCompletion/onTimeout/
onError` unsubscribe and complete the emitter.

**Stats** (`GET /api/stats`) are composed from `AtomicLong` counters plus values
computed with a `Stream` over a `TaskView` snapshot (avg & P95 latency from
completed tasks).

## 9. Design decisions & trade-offs

- **In-memory registry, no DB** — fine for a class project and for demonstrating
  concurrency in isolation; a real scheduler would persist + ack (see below).
- **Priority is only enforced at the queue**, not by interrupting running jobs
  (that would be unsafe). A CRITICAL job submitted after a long NORMAL one waits
  for it — the *next* free worker takes the CRITICAL job first.
- **Timeouts are advisory** — the underlying thread is not killed (no safe way in
  Java). Results after TIMED_OUT are discarded; the thread returns to the pool.
- **Unbounded priority queue** — matches `ScheduledExecutorService`; in
  production you'd bound it and apply back-pressure to submit.
- **One consumer per worker** — simple and adequate; a single consumer thread
  would also work.

## 10. Test plan (`mvn test`)

`TaskSchedulerIntegrationTest` boots the **real** Spring context and asserts:

| Test | Verifies |
| --- | --- |
| `submittedTaskRunsToCompletionAsynchronously` | full pipeline → COMPLETED, result present, stats bump |
| `highPriorityTaskExecutesBeforeEarlierSubmittedNormalTask` | with a busy worker and staggered delays (0/100/300 ms) the CRITICAL slot is dispatched first and finishes first |
| `retriesExhaustedTaskEndsFailed` | `maxRetries=1` + `failRate=100` → FAILED with `attempt==2` |
| `pendingTaskCanBeCancelledBeforeExecution` | cancel a PENDING task → CANCELLED |

`PriorityTaskQueueTest` is a **deterministic** (no-thread) unit test that proves
the scheduler's core ordering contract directly:

| Test | Verifies |
| --- | --- |
| `dequeuesHighestPriorityFirstRegardlessOfInsertOrder` | CRITICAL > HIGH > NORMAL > LOW |
| `respectsFifoOrderWithinTheSamePriority` | same rank keeps insertion order |
| `criticalPrecedesNormalEvenWhenInsertedLater` | priority outranks the FIFO sequence |

## 11. Future work

- Persistence (H2/Postgres) + durable, acknowledged delivery.
- Virtual threads (`Executors.newVirtualThreadPerTaskExecutor`) as the worker pool.
- WebSocket instead of SSE for bidirectional control.
- Distributed scheduling (leader election, Redis-backed queue).
- Prometheus metrics / micrometer gauges for real latency dashboards.
- A cron-like `schedule("0 0 * * *")` parser on top of the dispatcher.
timeout or cancel that fired mid-run is never overwritten by a late success.