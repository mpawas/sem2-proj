# Project Report — Asynchronous Task Scheduler

> **2nd Year Project** — Advanced Java + React

---

## 1. Title page

| | |
| --- | --- |
| **Project title** | Asynchronous Task Scheduler |
| **Course** | Advanced Java Programming (2nd Year) |
| **Submitted by** | *[Your Name]* |
| **College / Roll No.** | *[College name] / [Roll No.]* |
| **Academic year** | *[2025 – 2026]* |

---

## 2. Abstract

The **Async Task Scheduler** is a full-stack application that schedules and
executes tasks asynchronously in the background. A **Java / Spring Boot**
backend accepts tasks over a REST API, orders them by priority in a
thread-safe priority queue, executes them on a bounded worker pool, retries
failures with exponential back-off, enforces execution timeouts, and streams
every state transition to a **React** dashboard over Server-Sent Events. The
project demonstrates the practical application of the advanced Java concurrency
toolkit: executors, blocking queues, concurrent collections, atomic counters,
volatile state and cooperative cancellation.

---

## 3. Introduction

Modern applications often need to run work that should not block the user's
request: sending e-mails, generating reports, waiting on a timer, or calling
external APIs. This project builds a small but complete *task scheduler* that
does exactly that, while making the internals visible and controllable through
a live web dashboard. It is written entirely with the JDK's own concurrency
API — no third-party job-scheduling library.

### Objectives

1. Understand and apply the Java Concurrency API in a real system.
2. Build a priority-aware, delayed, asynchronous task pipeline.
3. Make system behaviour observable in real time (SSE + React).
4. Add failure tolerance: automatic retries, timeouts, cooperative cancellation.

---

## 4. Technology stack

| Layer | Technology | Purpose |
| --- | --- | --- |
| Backend | Java 17 + Spring Boot 3.2 + Maven | REST API, SSE, DI, config binding |
| Concurrency | `java.util.concurrent` | The scheduler core |
| Frontend | React 18 + Vite 5 (JSX) | Live monitoring dashboard |
| Realtime | Server-Sent Events | Push task transitions to the browser |
| Testing | JUnit 5, AssertJ, Awaitility | Integration + deterministic unit tests |

> No external job scheduler (Quartz, xxl-job, …) is used anywhere — every
> scheduling construct is hand-built from the JDK's concurrency toolkit.

---

## 5. System design (summary)

```
Rest / burst ──► ConcurrentHashMap registry
                     │  ScheduledExecutorService (delayed dispatch)
                     ▼
              PriorityBlockingQueue  (CRITICAL > HIGH > NORMAL > LOW, FIFO tie-break)
                     │  consumer threads
                     ▼
              fixed worker pool  ──►  execute(job) + timeout guard
                     │
                     ▼
              publish(TaskRecord) ──► SSE listeners ──► React dashboard
```

Task life-cycle: `PENDING → QUEUED → RUNNING → COMPLETED` (or `FAILED` /
`TIMED_OUT` / `CANCELLED`), all transitions recorded in an immutable event log.

> Full design, threading model and an advanced-Java feature catalogue with code
> references: see [`DESIGN.md`](DESIGN.md).

---

## 6. Key features implemented

- Asynchronous, delayed execution (caller never blocks)
- Priority scheduling with FIFO fairness within a level
- Automatic retries with exponential back-off (1 s, 2 s, 4 s, …)
- Per-task execution timeouts via scheduled guards
- Cooperative cancellation of pending, queued and running tasks
- Four demo job types: `DELAY`, `COMPUTE` (prime sieve),
  `FETCH` (simulated call), `BUILD_REPORT` (parallel streams)
- Producer–consumer *burst* demo through a `LinkedBlockingQueue`
- Live dashboard: stats, filters, progress, audit trail, retry/cancel actions
- Consistent REST error contract + DTO validation

---

## 7. How to run

```bash
# Backend  → http://localhost:8080
cd backend && mvn spring-boot:run

# Frontend → http://localhost:5173
cd frontend && npm install && npm run dev
```

Open the dashboard and try: a **Delay** task (`durationMs: 6000`), a **Compute**
task (`limit: 20000000`), the **Burst ×10** button, and the **CRITICAL**
priority to watch it jump the queue — everything updates live.

---

## 8. Demo scenarios & observations

Add screenshots here (dashboard, SSE stream, terminal).

| Scenario | What to click/run | Expected observation |
| --- | --- | --- |
| Delayed run | Submit `DELAY`, duration 6000 ms | Runs ~6 s later; progress bar |
| Priority | 2× `DELAY` (NORMAL) then `FETCH` (CRITICAL) while workers busy | CRITICAL starts first |
| Retry | *Failure rate* = 90 %, *max retries* = 3 | Task fails, retries with back-off, then succeeds |
| Timeout | `DELAY` duration 3000 ms, timeout 1000 ms | Becomes `TIMED_OUT` after ~1 s |
| Cancel | Submit slow task, press ✕ while PENDING/RUNNING | Becomes `CANCELLED` quickly |
| Burst | Click **Burst ×10** | Tasks stream in; queue depth rises |
## 9. Testing

`cd backend && mvn test` — 7 tests, all green (verified over multiple runs):

| Suite | Tests | What it proves |
| --- | --- | --- |
| `TaskSchedulerIntegrationTest` | 4 | End-to-end pipeline, priority dispatch order, retry exhaustion, cancellation |
| `PriorityTaskQueueTest` | 3 | Deterministic priority + FIFO ordering of the queue |

Also performed a live smoke test: submit → SSE frames (`PENDING → QUEUED →
COMPLETED`), validation error contract (400), unknown task (404), cancel and
retry flows.

---

## 10. Advanced Java concepts demonstrated

| Concept | Where |
| --- | --- |
| `ExecutorService` / `ScheduledExecutorService` | worker pool, dispatcher, timeout guards |
| `PriorityBlockingQueue` + `Comparable` | priority scheduling |
| `ConcurrentHashMap` / `CopyOnWriteArrayList` | thread-safe registry & listener lists |
| `AtomicLong` / `AtomicInteger` / `volatile` | lock-free counters & state visibility |
| `synchronized` state transitions | atomic status machine |
| `LinkedBlockingQueue` | producer–consumer burst demo |
| Records, switch expressions, streams, parallel streams | modern functional Java |

---

## 11. Conclusion & future work

The project successfully demonstrates an asynchronous task scheduler whose
concurrency machinery is entirely hand-built on `java.util.concurrent`, wrapped
in a clean REST + SSE + React interface. The result is fast, observable and
testable.

**Future work:** persistence and durable delivery, virtual threads as the pool
executor, cron-like schedules, distributed scheduling, and Prometheus metrics.

---

## 12. References

- Oracle, *Java Concurrency* tutorial (java.util.concurrent)
- Oracle, *The Java Tutorials — Synchronization*, *Locks*, *Atomic Variables*
- Spring Framework Reference — `SseEmitter`, `@ConfigurationProperties`
- React / Vite documentation

---

*End of report.*
| API errors | `POST /api/tasks` with `timeoutMillis: 5` | HTTP 400 + JSON error contract |