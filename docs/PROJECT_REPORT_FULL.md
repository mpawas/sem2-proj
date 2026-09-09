# PROJECT REPORT

## Asynchronous Task Scheduler

### An Advanced Java Concurrency Application with a React Real-Time Dashboard

---

| | |
|---|---|
| **Submitted by** | *[Full Name]* |
| **Roll Number** | *[Roll No.]* |
| **Class / Semester** | *Second Year, Semester [4]* |
| **Paper** | *Advanced Java Programming* |
| **College** | *[Name of College / Institute]* |
| **University** | *[Name of University]* |
| **Supervisor / Guide** | *[Name of Faculty Guide]* |
| **Academic Year** | *[2025 – 2026]* |

---

*"The user is the interface. The scheduler is the backbone."*

---

## Chapter 2 — Declaration and Certification

I, *[Full Name]*, Roll Number *[Roll No.]*, a student of Second Year, hereby declare that the project work entitled **"Asynchronous Task Scheduler — An Advanced Java Concurrency Application with a React Real-Time Dashboard"** submitted as part of the *Advanced Java Programming* paper is my original work.

The project has been developed by me under the supervision and guidance of *[Name of Faculty Guide]*. All the source code, design documents, test cases and this report have been prepared by me personally. Wherever the work of others has been referred to, it has been duly acknowledged and listed in the References chapter of this report.

I further declare that this work has not been submitted, in whole or in part, to any other examination, competition or publication prior to this submission, and that the contents of this report are true to the best of my knowledge.

**Signature:** ______________________ &nbsp;&nbsp;&nbsp; **Date:** __________________

---

## Chapter 3 — Acknowledgements

I would like to express my sincere gratitude to the following people and communities without whose support this project would not have been possible:

1. **Faculty Guide**, *[Name of Faculty Guide]*, for constant guidance, valuable feedback on the design of the scheduler core, and for encouraging me to explore the Java Concurrency API beyond the syllabus.

2. **Department of Computer Science**, *[College Name]*, for providing the laboratory infrastructure and reference materials.

3. **Oracle and the Java Community** — for producing the excellent documentation of the `java.util.concurrent` package, which served as the primary reference while building the scheduler.

4. **The Spring Boot team** — for simplifying REST API and Server-Sent Events development with sensible defaults.

5. **The React and Vite community** — for building a fast, modern frontend toolchain that made the real-time dashboard pleasant to build.

6. **My parents and friends** — for their patience and encouragement throughout the project.

---

## Chapter 4 — Abstract

The **Asynchronous Task Scheduler** is a full-stack, real-time application that demonstrates the practical use of advanced Java concurrency constructs in a realistic, web-facing system. Built entirely on the JDK's `java.util.concurrent` toolkit — with no external job-scheduling library — the backend accepts tasks over a REST API, places them in a thread-safe, priority-ordered queue, executes them asynchronously on a bounded worker pool, retries transient failures with exponential back-off, enforces per-task execution timeouts, allows cooperative cancellation, and streams every state transition to a modern React dashboard over Server-Sent Events.

The project's stated aim is twofold: (1) to build a working asynchronous task scheduler that exhibits priority scheduling, delayed execution, failure tolerance and real-time observability, and (2) to serve as a teaching artifact that maps every concurrency interview topic — executors, blocking queues, concurrent collections, atomic counters, volatile state, synchronized transitions, producer–consumer patterns, parallel streams and graceful shutdown — to a specific, runnable piece of code.

The backend is a Spring Boot 3.2 application written in Java 17, packaged with Maven. The frontend is a React 18 application built with Vite 5, styled with plain CSS and consuming the backend through a Vite proxy so that development is seamless. The system is covered by seven automated tests (four integration and three deterministic unit tests) and has been verified with live end-to-end smoke testing against a running deployment.

The outcome is a fast, observable, thread-safe scheduler whose every design decision is explained and whose every advanced Java feature is documented with a precise code reference. The project also identifies a concrete set of future extensions — persistence, virtual threads, WebSocket control, distributed scheduling and Prometheus metrics — and gives an honest account of what is in scope and what is deliberately out of scope for a second-year project.

---

## Chapter 5 — Table of Contents

1. Title Page
2. Declaration and Certification
3. Acknowledgements
4. Abstract
5. Table of Contents
6. List of Figures and Tables
7. Chapter 1 — Introduction
8. Chapter 2 — Literature Survey and Background
9. Chapter 3 — Existing System and Proposed System
10. Chapter 4 — Technology Stack and Justification
11. Chapter 5 — Advanced Java Concepts Employed
12. Chapter 6 — Problem Analysis and Requirements Specification
13. Chapter 7 — System Design
14. Chapter 8 — Implementation: Backend
15. Chapter 9 — Implementation: Frontend
16. Chapter 10 — API Contract and Configuration
17. Chapter 11 — Testing Strategy and Results
18. Chapter 12 — Results, Observations and Demo Scenarios
19. Chapter 13 — Performance Analysis
20. Chapter 14 — Security Considerations
21. Chapter 15 — Limitations of the Project
22. Chapter 16 — Future Scope and Enhancements
23. Chapter 17 — Conclusion
24. Chapter 18 — References
25. Chapter 25 — Viva-Voce Q&A (Parts 1–2)
26. Chapter 26 — Development Log and Timeline
27. Chapter 27 — Glossary of Concurrency Terms
25. Appendix A — Complete Backend Source Code
26. Appendix B — Complete Frontend Source Code
27. Appendix C — Sample API Requests and Responses
28. Appendix D — Screenshots and Operational Notes

---

## Chapter 6 — List of Figures and Tables

| Figure / Table | Description | Location |
|---|---|---|
| Figure 1 | High-level architecture diagram | Chapter 7 |
| Figure 2 | Task life-cycle state machine | Chapter 7 |
| Figure 3 | Threading model — submit to completion | Chapter 7 |
| Figure 4 | Priority queue ordering | Chapter 7 |
| Table 1 | Technology stack summary | Chapter 4 |
| Table 2 | Advanced Java feature catalogue | Chapter 5 |
| Table 3 | Requirements matrix | Chapter 6 |
| Table 4 | REST API contract | Chapter 10 |
| Table 5 | Test suite summary | Chapter 11 |
| Table 6 | Demo scenario observations | Chapter 12 |
| Table 7 | Configuration properties | Chapter 10 |

---

## Chapter 7 — Introduction

### 7.1 Background

Modern software systems — web applications, microservices, batch processors, notification engines — routinely need to perform work that should not block the user's immediate request. Examples include sending an email, generating a PDF report, waiting for a timer to expire, calling an unreliable external API, or recomputing a cache. Doing such work inline would make the user wait, tie up a server thread, and turn a fast request into a slow one.

A **task scheduler** is the component that absorbs this work: it accepts tasks, orders them, executes them in the background, and reports on their progress. In production systems this is often handled by large, specialised frameworks (Quartz, XXL-JOB, Celery, Temporal, and many others). However, for an educational project — particularly one in an Advanced Java course — it is far more instructive to build a small, honest scheduler from the JDK's own concurrency primitives and to understand exactly what each primitive does.

### 7.2 Motivation

The motivation for this project comes from two directions. The **pedagogical motivation** is that the Advanced Java syllabus introduces `ExecutorService`, `ScheduledExecutorService`, `ConcurrentHashMap`, `AtomicInteger`, `volatile`, `synchronized`, blocking queues and parallel streams in isolation. Students frequently struggle to see how these pieces fit together into a real, correct, thread-safe system. This project was conceived as a single, coherent artifact that ties them all together, with explicit code references so that each concept can be located in the source.

The **practical motivation** is that even a small application benefits from asynchronous execution and observability. A scheduler that accepts work, runs it in the background, retries on failure, times out runaway jobs, and shows the user what is happening in real time is genuinely useful — and the patterns it uses are exactly the patterns one would use in a larger system.

### 7.3 Objectives of the Project

1. To design and implement an **asynchronous task scheduler** in Java, using only the standard `java.util.concurrent` API, with no third-party job-scheduling library.
2. To support **priority scheduling** so that urgent tasks can jump ahead of less urgent ones, while preserving fairness (first-in-first-out) among tasks of equal priority.
3. To support **delayed execution**, where a task is not started immediately but after a configurable delay.
4. To provide **failure tolerance** through automatic retries with exponential back-off, and through per-task execution timeouts.
5. To allow **cooperative cancellation** of tasks that are pending, queued, or running.
6. To make the system's behaviour **observable in real time** by streaming every state transition to a browser-based dashboard using Server-Sent Events.
7. To expose **aggregate statistics** — counts by state, average and P95 latency, queue depth — through a REST endpoint.
8. To build a complete **React-based dashboard** that lets a user submit tasks, watch them run, cancel them, retry failed tasks, and inspect their full event history.
9. To keep every component **thread-safe** and to back the implementation with **automated tests** that genuinely exercise the concurrency.

### 7.4 Scope of the Project

**In scope:** a single-node, in-memory asynchronous task scheduler; priority ordering with FIFO fairness within a priority level; delayed dispatch of tasks; execution timeouts and automatic retries with exponential back-off; cooperative cancellation; four demo job types (`DELAY`, `COMPUTE`, `FETCH`, `BUILD_REPORT`); a producer–consumer burst demonstration; real-time monitoring via SSE and a React dashboard; a REST API with a consistent error contract; automated tests and full documentation.

**Out of scope (for this version):** durable persistence across restarts; distributed scheduling across multiple nodes; authentication and authorisation; cron-like recurring schedules; cluster-wide metrics and dashboards. These out-of-scope items are discussed as future work in Chapter 22.

### 7.5 Organisation of the Report

The remainder of this report is organised as follows. **Chapter 8** surveys the relevant literature and the background concurrency patterns that the project builds on. **Chapter 9** contrasts the existing (manual, blocking, hard-to-observe) approach with the proposed scheduler. **Chapter 10** lists the technology choices and justifies them. **Chapter 11** catalogues every advanced Java concept used, with pointers to the exact code. **Chapter 12** presents the problem analysis and a formal requirements specification. **Chapter 13** describes the system design — architecture, threading model, state machine, scheduling algorithm, and failure model. **Chapters 14 and 15** describe the implementation of the backend and frontend respectively, with full code listings in the appendices. **Chapter 16** documents the REST API contract and the configuration. **Chapter 17** presents the testing strategy and results. **Chapter 18** gives the results, observations and demo scenarios. **Chapter 19** analyses performance. **Chapter 20** covers security. **Chapter 21** discusses limitations. **Chapter 22** outlines future scope and enhancements. **Chapter 23** concludes. **Chapter 24** lists references. **Appendices A–D** contain the complete source code, sample requests and responses, and operational notes.
---

## Chapter 8 — Literature Survey and Background

### 8.1 The Java Concurrency API (java.util.concurrent)

Since Java 5, the platform has shipped the `java.util.concurrent` (JUC) package designed largely by Doug Lea. It replaced raw `Thread` plus `synchronized`-only programming with higher-level, composable abstractions. The package is documented in *Java Concurrency in Practice* (Goetz et al., 2006), still the definitive text, and in the official Oracle tutorials.

The abstractions relevant to this project are:

**Executors.** `ExecutorService` decouples task submission from thread management. `ScheduledExecutorService` adds delayed and periodic execution backed by a `DelayedWorkQueue`. This project uses a scheduled executor for three timing duties: delayed dispatch, retry back-off timers, and execution-timeout guards. The alternative — `java.util.Timer` — is single-threaded and fragile; the alternative of `Thread.sleep` inside workers wastes worker threads. The scheduled executor is strictly superior for all three duties.

**Concurrent collections.** `ConcurrentHashMap` provides lock-striped, thread-safe maps with atomic operations such as `computeIfAbsent` and `putIfAbsent`. `CopyOnWriteArrayList` is optimised for read-heavy, write-rare lists. `BlockingQueue` implementations (`PriorityBlockingQueue`, `LinkedBlockingQueue`, `ArrayBlockingQueue`, `SynchronousQueue`, `DelayQueue`) provide blocking `put`/`take` semantics that make producer–consumer designs clean. This project uses `ConcurrentHashMap` for the task registry, `CopyOnWriteArrayList` for SSE listeners, `PriorityBlockingQueue` for the dispatch queue, and `LinkedBlockingQueue` in the burst demo.

**Atomics and volatility.** `AtomicLong`/`AtomicInteger` give lock-free counters via compare-and-set. `volatile` guarantees visibility of a write across threads without mutual exclusion. This project uses `AtomicLong` for sequence numbers, task IDs, and every metric counter, and `volatile` for the hot task status field.

**Synchronisation.** `synchronized` methods/blocks provide mutual exclusion with monitor semantics. `CountDownLatch`, `CyclicBarrier`, `Semaphore`, `Phaser` and `Lock`/`Condition` provide coordination. This project uses `synchronized` state transitions in `TaskRecord`, and `CountDownLatch` plus `TimeUnit.await` patterns in the integration tests.

**Parallel streams.** The Stream API with `parallelStream()` parallelises bulk operations over the common `ForkJoinPool`. This project uses it in the `BUILD_REPORT` job and for P95 latency computation.

### 8.2 The Producer–Consumer Pattern

### 8.3 Thread Pools and the Thread-Pool Pattern

Creating a thread per task is unbounded and expensive (default 1 MB stack each, costly context switches). A fixed pool reuses N threads over M tasks. `Executors.newFixedThreadPool` pairs N threads with an unbounded `LinkedBlockingQueue`; `newCachedThreadPool` grows and shrinks; `newScheduledThreadPool` adds a `DelayedWorkQueue` for timers. This project uses three pools with separated responsibilities so a fault in one dimension cannot stall another: (1) a `ScheduledExecutorService` dispatcher for delayed dispatch, retry timers and timeout guards; (2) a fixed worker pool for job execution; (3) one consumer thread per worker that blocks on `queue.take()` and hands envelopes to the pool. A slow job therefore never stalls timing machinery, and a busy timer loop never starves a running job.

### 8.4 Priority Scheduling

Priority scheduling orders tasks by urgency rather than arrival. OS schedulers favour interactive processes; this scheduler favours CRITICAL over HIGH over NORMAL over LOW. The implementation is a `PriorityBlockingQueue<QueuedTask>` ordered by a `Comparable` that compares priority rank first and a monotonic sequence number second (FIFO fairness within a level). `PriorityBlockingQueue` is unbounded (no `put`-blocking, `take` blocks when empty) and its iterator is weakly consistent, so draining it requires `poll()`. Alternatives considered: a `DelayQueue` for delays (rejected — delays are one-shot timers, not queue residence), and separate per-priority queues (rejected — a single comparable queue is simpler and starvation is bounded by the small worker count).

### 8.5 Failure Handling: Retries with Exponential Back-Off

Transient failures (network blips, busy files) deserve retries; permanent failures deserve a fast verdict. Exponential back-off waits 1 s, then 2 s, then 4 s (base × 2^attempt), capping attempts at `maxRetries`. This avoids thundering-herd retries while giving flaky operations room to recover. The scheduler computes the delay, records the attempt count on the task, re-queues after the delay via the dispatcher, and marks the task RETRYING in between. When attempts are exhausted it transitions to FAILED with the last error.

### 8.6 Timeouts and Cancellation

A runaway job must not hold a worker forever. Each execution arms a one-shot timeout guard on the dispatcher (`schedule(timeoutMillis)`); whichever fires first — job completion or guard — wins, and the loser is ignored via a `Future`/`AtomicBoolean` handshake. Cancellation is cooperative: a `volatile boolean cancelled` flag that `DELAY` (sleep loop), `COMPUTE` (sieve loop), `FETCH` (retry loop) and `BUILD_REPORT` (chunk loop) poll at safe points. Tasks in PENDING have their timer future cancelled; tasks in QUEUED are removed from the queue; RUNNING tasks set the flag and the job unwinds to CANCELLED. Forced `Thread.stop`/`interrupt`-only kills are deliberately avoided — they corrupt state.

### 8.7 Server-Sent Events and Real-Time Dashboards

SSE is a text protocol over a long-lived HTTP response: the server writes `event:` / `data:` frames, the browser consumes them via `EventSource` with automatic reconnection. It is ideal for one-way server→client notification (task transitions) and far simpler than WebSockets when the client never pushes over the same channel. Spring's `SseEmitter` (one per subscriber, infinite timeout, `CopyOnWriteArrayList` registry, dead-emitter eviction on send failure) feeds the React `EventSource` client, which merges snapshots into state. Polling was rejected (wasteful, laggy); WebSockets were rejected (bidirectional machinery for a unidirectional need).

### 8.8 Related Work

Quartz (database-backed, cron-rich), Spring `@Scheduled` (declarative periodic tasks), Celery/Temporal/XXL-JOB (distributed) all solve adjacent problems at far greater complexity. This project deliberately uses none of them: every timing, ordering, retry and timeout construct is hand-built from the JDK so each can be cited in an exam answer. The closest relatives are textbook scheduler implementations (Goetz Ch. 6–8) scaled up into a deployable full-stack system.
---

## Chapter 9 — Existing System and Proposed System

### 9.1 The Existing Approach (What Developers Do Without a Scheduler)

Without a scheduler, background work is typically handled in one of three ad-hoc ways, each with concrete drawbacks.

**Inline execution.** The request-handling thread performs the work directly before responding. The user waits for the full duration; a 30-second report blocks an HTTP worker for 30 seconds; under load the server exhausts its thread budget and stops accepting requests. There is no priority, no retry, no timeout, no cancellation, and no visibility.

**Manual threading.** The developer writes `new Thread(runnable).start()` per task. This is unbounded — a burst of 1,000 requests spawns 1,000 threads, each with a ~1 MB stack — and provides no lifecycle management, no ordering, no retry, no cancellation, and no observability. Uncaught exceptions kill threads silently. It is the canonical example of what *Java Concurrency in Practice* warns against.

**A single ad-hoc executor.** The developer creates one `ExecutorService` and submits everything to it. Better than raw threads, but still FIFO-only (no priority), immediate-only (no delay), fail-once (no retry), run-forever (no timeout), fire-and-forget (no cancellation), and opaque (no state query, no event stream). Each missing capability must then be reinvented per project.

In all three cases the system's internal state is invisible: which tasks are running, which failed, how long they took, and what is queued can only be discovered through scattered log lines.

### 9.2 The Proposed System

The proposed Asynchronous Task Scheduler replaces all three ad-hoc patterns with one coherent component:

1. **Asynchronous, non-blocking submission.** `POST /api/tasks` validates the request, registers the task, and returns `201 Created` with the ID immediately. All work happens off the request thread.
2. **Priority scheduling.** A `PriorityBlockingQueue` dispatches CRITICAL first, with FIFO fairness inside each level.
3. **Delayed execution.** `delayMillis` arms a one-shot dispatcher timer; the task waits in PENDING, then enters the queue.
4. **Failure tolerance.** `maxRetries` with exponential back-off plus a per-task `timeoutMillis` guard.
5. **Cooperative cancellation.** `POST /api/tasks/{id}/cancel` handles PENDING (timer cancelled), QUEUED (envelope removed) and RUNNING (flag set, job unwinds).
6. **Real-time observability.** Every transition fans out over SSE; the dashboard updates without refresh.
7. **Statistics.** `GET /api/stats` reports per-state counts, per-type counts, average/P95 latency, and queue depth.
8. **Complete dashboard.** Submit, filter, inspect (full event log modal), cancel, retry-failed, burst ×10, and clear-finished.
9. **Thread safety everywhere and tests that prove it.** Seven automated tests, including concurrent submission from multiple threads.
---

## Chapter 10 — Technology Stack and Justification

### 10.1 Stack Summary (Table 1)

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Language | Java (LTS) | 17 | Backend logic and all concurrency |
| Framework | Spring Boot | 3.2 | REST, SSE, DI, configuration binding |
| Build | Maven | 3.9 | Dependencies, build, test, packaging |
| Concurrency | `java.util.concurrent` | JDK | The entire scheduler core — no job library |
| UI language | JavaScript + JSX | ES2022 | Dashboard logic |
| UI framework | React | 18 | Components, hooks, live state |
| UI build | Vite | 5 | Dev server, HMR, proxy, production bundle |
| Transport | Server-Sent Events | HTTP/1.1 | Push transitions server → browser |
| Tests | JUnit 5, AssertJ, Awaitility, Spring Boot Test | — | Unit + integration tests |
| Styling | Plain CSS | — | Dark dashboard theme, no dependency |

### 10.2 Backend Justification

**Java 17** is the current widely-supported LTS in classrooms: records give immutable DTOs (`TaskView`, `StatsView`) with zero boilerplate; switch expressions give concise type dispatch in `DemoJobFactory`; text blocks keep tests readable. **Spring Boot 3.2** contributes the HTTP layer, `@ConfigurationProperties` binding (`SchedulerProperties`), `@RestControllerAdvice` error handling, and `SseEmitter` — removing plumbing so the report can focus on concurrency. **Maven** with the Spring Boot parent manages the full dependency tree reproducibly. The decisive choice is **`java.util.concurrent` only**: Quartz or `@Scheduled` would hide exactly the mechanisms this project must exhibit.

### 10.3 Frontend Justification

**React 18** hooks (`useState`, `useEffect`, `useMemo`) map naturally onto a live task list fed by an `EventSource`. **Vite 5** builds in milliseconds, hot-reloads dashboard edits instantly, and — critically — proxies `/api` to `localhost:8080` in development, eliminating CORS during development while `CorsConfig` covers any direct access. **Plain CSS** keeps the bundle dependency-free; the dashboard needs layout, badges and a modal, not a design system.

### 10.4 Why No Database

Persistence would add JPA, migrations, connection pools and transaction semantics — an entire second project — while teaching nothing about threads. The registry is a `ConcurrentHashMap` (correct under concurrency, O(1), zero setup), which keeps every interesting problem in the concurrency domain. File-backed snapshots and a JPA option are specified as future work (Chapter 22).
---

## Chapter 11 — Advanced Java Concepts Employed (Table 2)

This chapter is the heart of the "advanced Java" requirement: every concurrency and modern-Java feature in the project, what it does, and exactly where to find it.

| # | Concept | Where | Role |
|---|---|---|---|
| 1 | `ScheduledExecutorService` | `TaskSchedulerService` dispatcher | Delayed dispatch, retry timers, timeout guards |
| 2 | Fixed worker `ExecutorService` | `TaskSchedulerService` pool | Bounded parallel job execution |
| 3 | `PriorityBlockingQueue` | `PriorityTaskQueue` | Priority dispatch with FIFO tie-break |
| 4 | `Comparable` / `Comparator` | `QueuedTask.compareTo` | Priority rank, then sequence number |
| 5 | `ConcurrentHashMap` | Task registry | Thread-safe ID → record store |
| 6 | `CopyOnWriteArrayList` | SSE listeners | Read-heavy subscriber list |
| 7 | `LinkedBlockingQueue` | `burstTasks` | Producer–consumer fan-out demo |
| 8 | `AtomicLong` | IDs, sequences, metrics | Lock-free counters |
| 9 | `volatile` | `TaskRecord.status`, cancelled flag | Cross-thread visibility |
| 10 | `synchronized` | `TaskRecord` transitions | Atomic check-then-act state changes |
| 11 | `Future` + `ScheduledFuture` | Dispatch/cancel paths | Timer handles, completion handles |
| 12 | `CountDownLatch` / `TimeUnit` | Tests | Deterministic async assertions |
| 13 | `parallelStream` | `BUILD_REPORT`, P95 calc | Data-parallel aggregation |
| 14 | Records | `TaskView`, `StatsView` | Immutable DTOs |
| 15 | Switch expressions + `@JsonCreator` | `DemoJobFactory`, enums | Concise dispatch, lenient JSON enums |
| 16 | `@ConfigurationProperties` | `SchedulerProperties` | Type-safe `scheduler.*` config |
| 17 | `@RestControllerAdvice` | `GlobalExceptionHandler` | Uniform `{error, message, taskId}` errors |
| 18 | `SseEmitter` | `DashboardController` | Infinite-timeout SSE fan-out |
| 19 | Graceful shutdown | `@PreDestroy` | Ordered pool termination |
| 20 | Custom thread factories | Both pools | Named daemon threads for readable dumps |

**1–2. Executors.** The dispatcher is a `ScheduledExecutorService` sized `max(2, workers)`; the job pool is fixed at `scheduler.workers` (default 4). Separation means timing never waits for jobs and jobs never wait for timers. Both use custom factories (`scheduler-dispatch-N`, `scheduler-worker-N`, daemon) so thread dumps are self-explanatory.

**3–4. Priority queue.** `PriorityTaskQueue` wraps a `PriorityBlockingQueue<QueuedTask>`; `QueuedTask` carries `seq` from an `AtomicLong` and compares `(priorityRank, seq)`. `remove(UUID)` uses `removeIf`, `drain()` polls until null (never the weakly-consistent iterator). **5–7. Collections.** The registry's `putIfAbsent`/`computeIfPresent` give atomic registration; the listener list tolerates concurrent broadcast iteration with rare subscribe/unsubscribe writes; the burst `LinkedBlockingQueue` (capacity 64) demonstrates blocking `put`/`take`.

**8–10. Atomics, volatility, synchronisation.** IDs and metrics are `AtomicLong` (no locks, no lost updates). `status` is `volatile` so dashboard/stats threads see transitions instantly; multi-step transitions (`canTransition` check + set + event log + timestamp) are `synchronized` so check-then-act is atomic and the event log order matches the state order. The cancelled flag is `volatile boolean`, polled at safe points in every job loop.

**11–12. Futures and latches.** Every scheduled timer returns a `ScheduledFuture` stored on the record for PENDING cancellation; executions return `Future`s paired with the timeout guard. Tests use `Awaitility.await().atMost(...).until(...)` and `CountDownLatch` so async assertions are deterministic rather than `sleep`-based.

**13–16. Modern Java.** `parallelStream` aggregates report chunks and sorts latency samples for P95; records make `TaskView`/`StatsView` immutable one-liners; switch expressions dispatch job construction; `@JsonCreator` factory methods accept `"critical"`, `"CRITICAL"` or `"Critical"`; `@ConfigurationProperties(prefix="scheduler")` binds `workers`, `queueCapacity`, `defaultTimeoutMillis`, `retryBaseDelayMillis`, `historySize` with validation.

**17–20. Web and lifecycle.** `@RestControllerAdvice` maps `TaskNotFoundException`→404 and `SchedulerException`/validation→400 with a uniform body. `SseEmitter(Long.MAX_VALUE)` plus send-failure eviction keeps the fan-out leak-free. `@PreDestroy` shuts down consumer loops, then dispatcher, then workers, each with `awaitTermination` and `shutdownNow` fallback. Daemon threads guarantee the JVM can always exit.
---

## Chapter 12 — Problem Analysis and Requirements Specification

### 12.1 Problem Statement

Design and build a single-node asynchronous task scheduler: clients submit typed tasks with priority, delay, timeout and retry policy; the system executes them concurrently on a bounded pool in priority order; it survives transient failures via back-off retries, bounds runaway executions via timeouts, supports cancellation at every pre-terminal stage, and exposes live state plus aggregate statistics to a real-time dashboard.

### 12.2 Stakeholders

The **student developer** implements and documents; the **faculty evaluator** assesses correctness, concurrency depth, tests and report quality; the **end user** (demo audience) submits tasks and watches them run; **future maintainers** extend the system using this report as the design record.

### 12.3 Functional Requirements (Table 3)

| ID | Requirement | Priority | Verified by |
|---|---|---|---|
| FR-01 | Submit task (type, name, priority, delay, timeout, retries, params) → 201 + ID | Must | Test: submitAndComplete |
| FR-02 | Priority dispatch CRITICAL > HIGH > NORMAL > LOW, FIFO within level | Must | Test: priorityOrdering |
| FR-03 | Delayed dispatch after `delayMillis` | Must | Test: delayedDispatch |
| FR-04 | Retry with exponential back-off to `maxRetries`, then FAILED | Must | Test: retryThenFail |
| FR-05 | Timeout guard per execution → TIMEOUT | Must | Manual + code review |
| FR-06 | Cancel PENDING / QUEUED / RUNNING → CANCELLED | Must | Test: cancelPending |
| FR-07 | SSE stream of every transition | Must | Live smoke test |
| FR-08 | Stats endpoint (counts, avg/P95, queue depth) | Must | `GET /api/stats` |
| FR-09 | Burst ×10 producer–consumer demo | Should | Dashboard + code |
| FR-10 | Retry-failed endpoint (reset attempts, re-run) | Should | Dashboard button |
| FR-11 | Clear finished (bounded history eviction) | Should | `DELETE /api/tasks` |

### 12.4 Non-Functional Requirements

**NFR-01 Thread safety:** safe under concurrent REST submissions; no lost updates, no torn state. **NFR-02 Responsiveness:** submission returns in milliseconds regardless of job duration. **NFR-03 Observability:** every transition broadcast within milliseconds; full per-task event log retained. **NFR-04 Robustness:** worker exceptions never kill consumer loops; dead SSE emitters evicted; pools shut down gracefully. **NFR-05 Usability:** dashboard operable with no manual; empty states, live badge, error toasts. **NFR-06 Testability:** deterministic async tests with Awaitility, no `Thread.sleep` polling. **NFR-07 Portability:** runs on any JDK 17 + Maven + Node 18 machine with two commands per side.

### 12.5 Constraints and Assumptions

Single node; in-memory state (restart loses history); cooperative (not preemptive) cancellation; at-most-once execution per attempt (retries are new attempts); SSE is best-effort (a disconnected client misses frames, then resyncs via REST); default 4 workers and documented `scheduler.*` tuning.
---

## Chapter 13 — System Design

### 13.1 Architecture Overview (Figure 1)

```
 Browser (React 18 + Vite)                Spring Boot 3.2 (Java 17)
┌─────────────────────────┐              ┌──────────────────────────────────┐
│ TaskForm  TaskTable     │   REST       │ TaskController  DashboardCtrl    │
│ StatsBar  DetailModal   │◄────────────►│ GlobalExceptionHandler CorsConfig│
│ api.js EventSource      │  /api/*      └──────────────┬───────────────────┘
│                         │   SSE                       │ submit/query/cancel
└─────────────────────────┘              ┌──────────────▼───────────────────┐
                                         │ TaskSchedulerService  (core)     │
                                         │ dispatcher │ workers │ consumers │
                                         └──┬──────────┬─────────┬──────────┘
                                            │          │         │
                              PriorityTaskQueue   DemoJobFactory  SchedulerMetrics
                              TaskRecord registry  DELAY/COMPUTE/FETCH/REPORT
```

The browser never touches scheduler internals: every interaction crosses the REST/SSE boundary. `TaskController` owns task CRUD; `DashboardController` owns stats, health, SSE, burst and clear. Both delegate to `TaskSchedulerService`, which owns all threads.

### 13.2 Task Life-Cycle State Machine (Figure 2)

```
        submit                  delay fires            consumer takes
PENDING ───────► QUEUED ───────────────────► RUNNING ──────┬──► COMPLETED
   │                │                             │         ├──► FAILED (attempts exhausted)
   │                │                             │         ├──► TIMEOUT (guard fires)
   │                │                             │         └──► CANCELLED (flag observed)
   │                │                             ▼
   │                │                         RETRYING ──► QUEUED (back-off timer)
   │                └────────► CANCELLED ◄────┘
   └───────────────► CANCELLED
```

Terminal states are `COMPLETED`, `FAILED`, `TIMEOUT`, `CANCELLED`. Legal transitions are enforced by `TaskRecord.canTransition` inside `synchronized` methods, so an illegal jump (e.g. COMPLETED → RUNNING) is impossible even under races. Every transition appends a timestamped event line retained for the detail modal.

### 13.3 Threading Model (Figure 3)

Submit path: HTTP thread → validate → `registry.put` → `PENDING` → if `delayMillis > 0`, `dispatcher.schedule(fire, delay)` else `enqueue` immediately → return 201. Dispatch path: each of N consumer threads loops `queue.take()` (blocks when empty) → `RUNNING` → `workers.submit(job)` → arm `dispatcher.schedule(timeoutGuard)` → completion/timeout/cancel handshake → terminal or RETRYING (re-queue via `dispatcher.schedule(backoff)`). Broadcast path: after every transition, iterate the `CopyOnWriteArrayList` of emitters and send the JSON snapshot; evict failures. Shutdown path: `@PreDestroy` stops consumers, then dispatcher, then workers, with `awaitTermination` + `shutdownNow` fallback.

### 13.4 Scheduling Algorithm (Figure 4)

`QueuedTask.compareTo` implements `(rank(priority), seq)`: lower rank (CRITICAL=0 … LOW=3) dequeues first; equal ranks compare the `AtomicLong` sequence, giving FIFO fairness. `PriorityBlockingQueue.take()` always yields the current head, so a CRITICAL task submitted into a busy system runs as soon as any worker frees. Complexity is O(log n) enqueue/dequeue; memory is one small envelope per queued task. Starvation of LOW tasks under CRITICAL floods is theoretically possible and accepted (documented in Chapter 21); the default workload does not flood.

### 13.5 Failure Model

Failures are classified at the point of occurrence. **Job exceptions** (including simulated `failRate` dice rolls): attempt counter increments; if attempts remain, RETRYING + back-off timer, else FAILED with the last message. **Timeouts**: the guard wins the handshake, the worker future is cancelled, the task becomes TIMEOUT (no retry — a timeout signals a sizing problem, surfaced for the user to fix). **Cancellations**: timer/queue/flag per stage, always ending CANCELLED with an explanatory event. **Infrastructure faults** (dead SSE emitter, consumer-loop throwable): contained locally — evict, log, continue — so one bad subscriber or one poisoned task can never wedge the scheduler.

### 13.6 Package and Class Design

`common` holds the `TaskStatus`, `TaskPriority`, `TaskType` enums (lenient `@JsonCreator`, rank helpers). `model` holds `TaskRecord` (the synchronised state machine), `TaskSubmitRequest` (validated DTO), and `TaskView`/`StatsView` records. `scheduler` holds the service, queue, envelope, job factory, metrics, properties and the four exception types. `web` holds the two controllers plus the advice; `config` holds CORS. Dependencies point strictly inward: web → service → queue/record/factory; nothing points back outward.
---

## Chapter 14 — Implementation: Backend

### 14.1 Module Map

The backend (`backend/`, Maven, Spring Boot 3.2, Java 17) contains 19 main-source files plus 2 test files:

- `AsyncTaskSchedulerApp.java` — entry point, `@EnableConfigurationProperties`.
- `common/` — `TaskStatus`, `TaskPriority`, `TaskType` enums.
- `model/` — `TaskRecord`, `TaskSubmitRequest`, `TaskView`, `StatsView`.
- `scheduler/` — `TaskSchedulerService`, `PriorityTaskQueue`, `QueuedTask`, `DemoJobFactory`, `SchedulerMetrics`, `SchedulerProperties`, `SchedulerException`, `TaskNotFoundException`, `JobFailureException`, `JobCancelledException`.
- `web/` — `TaskController`, `DashboardController`, `GlobalExceptionHandler`.
- `config/` — `CorsConfig`. Plus `application.yml` and tests `TaskSchedulerIntegrationTest`, `PriorityTaskQueueTest`.

### 14.2 Submission and Validation

`TaskController.POST /api/tasks` accepts a `TaskSubmitRequest` (Bean Validation: known type, `timeoutMillis ≥ 100`, `maxRetries ≥ 0`, `delayMillis ≥ 0`, `failRate ∈ [0,100]`). Violations return 400 via the advice with field-level messages. The service generates a UUID, builds the `TaskRecord` (PENDING, attempt 0, creation timestamp), registers it with `putIfAbsent`, broadcasts the snapshot, and either enqueues immediately or arms the dispatcher timer. The response is `201 Created` with a `Location` header and the full `TaskView`. Unknown IDs anywhere return 404 with `{error, message, taskId}`.

### 14.3 The Four Demo Jobs

`DemoJobFactory.build(record)` switch-dispatches on type. **DELAY** sleeps in 50 ms slices to `durationMillis`, polling the cancelled flag — the canonical cancellable wait. **COMPUTE** runs a Sieve of Eratosthenes to `computeLimit` with periodic cancellation checks, returning prime counts — genuine CPU-bound work that scales with worker count. **FETCH** simulates an unreliable endpoint: up to `fetchAttempts` tries with 100 ms latency, each rolling `failRate`; a natural retry showcase. **BUILD_REPORT** splits the registry snapshot into chunks, aggregates with `parallelStream`, and computes per-state/per-type counts plus average latency — the parallel-streams exhibit. Every job also rolls the record-level `failRate` dice first, so any type can demonstrate the retry path, and every job checks cancellation before starting.

### 14.4 Dispatch, Retry, Timeout, Cancel, Burst

Dispatch is N consumer threads on `queue.take()`; each envelope becomes RUNNING, runs on the worker pool, and is supervised by a dispatcher timeout guard. Normal completion records latency into `SchedulerMetrics` and transitions COMPLETED with a result summary. Exceptions route to the retry engine: `backoff = retryBaseDelayMillis × 2^attempt`, RETRYING meanwhile, re-queue on timer fire, FAILED when exhausted. Timeout victory transitions TIMEOUT and cancels the worker future. Cancel handles all three stages (future.cancel / queue.remove / flag-set). Burst submits ten tasks through a `LinkedBlockingQueue` fed by one producer thread and drained inline — the producer–consumer demo. Retry-failed re-arms a FAILED/TIMEOUT/CANCELLED record at attempt 0; clear-finished evicts terminal records beyond `historySize`.

### 14.5 Metrics, Config, Errors, Shutdown

`SchedulerMetrics` keeps `AtomicLong` submitted/completed/failed/cancelled/timeout counters, a `LongAdder`-style latency sum, and a bounded latency deque for P95 (sorted copy, nearest-rank). `SchedulerProperties` binds `scheduler.workers=4`, `queueCapacity=1000`, `defaultTimeoutMillis=30000`, `retryBaseDelayMillis=1000`, `historySize=200`. `GlobalExceptionHandler` maps validation→400, `SchedulerException`→400, `TaskNotFoundException`→404, fallback→500, always shaped `{error, message, taskId?}`. `@PreDestroy` stops consumers, dispatcher, workers in order with `awaitTermination`/`shutdownNow`, and closes SSE emitters. (Full source: Appendix A.)
---

## Chapter 15 — Implementation: Frontend

### 15.1 Module Map

The frontend (`frontend/`, React 18, Vite 5) contains `index.html` (root div + `/src/main.jsx` script), `vite.config.js` (dev proxy `/api → localhost:8080`), `package.json` (react, react-dom, vite), and `src/`: `main.jsx` (App component + `createRoot` mount), `api.js` (fetch wrappers for every endpoint + SSE subscription), `status.js` (label/color maps), `styles.css` (dark theme, ~600 lines), and `components/` (`TaskForm`, `TaskTable`, `TaskDetailModal`, `StatsBar`, `StatusBadge`).

### 15.2 Components and Data Flow

`App` owns all state: `tasks`, `stats`, `connected` (SSE), `filter`, `selected` (modal), `error`. On mount it fetches tasks + stats once (REST resync) and opens the `EventSource`; every `event: task` frame upserts the matching record and refreshes stats, so the table animates PENDING → QUEUED → RUNNING → terminal without refresh. `TaskForm` submits typed tasks with priority/delay/timeout/retries/params and exposes Burst ×10 and Clear-finished; `TaskTable` filters by state/type/priority and offers per-row Inspect/Cancel/Retry; `TaskDetailModal` renders the full event log, params JSON, and result/error; `StatsBar` shows counts, avg/P95, queue depth and the live badge; `StatusBadge` color-codes all eight states. Failures surface as dismissible error toasts; empty states guide first use. (Full source: Appendix B.)
---

## Chapter 16 — API Contract and Configuration

### 16.1 REST Endpoints (Table 4)

| Method + Path | Purpose | Success | Errors |
|---|---|---|---|
| `POST /api/tasks` | Submit a task | 201 + `TaskView` (+ `Location`) | 400 validation |
| `GET /api/tasks` | List tasks (newest first) | 200 + array | — |
| `GET /api/tasks/{id}` | Get one task | 200 + `TaskView` | 404 unknown ID |
| `POST /api/tasks/{id}/cancel` | Cancel PENDING/QUEUED/RUNNING | 200 + updated view | 404; 400 if terminal |
| `POST /api/tasks/{id}/retry` | Re-run FAILED/TIMEOUT/CANCELLED | 200 + updated view | 404; 400 if active |
| `DELETE /api/tasks` | Clear finished (keeps active) | 200 + `{removed}` | — |
| `GET /api/stats` | Aggregate counters + latency | 200 + `StatsView` | — |
| `GET /api/health` | Liveness probe | 200 `{"status":"UP"}` | — |
| `POST /api/burst?count=10` | Producer–consumer demo | 200 + ID list | 400 bad count |
| `GET /api/events` | SSE stream (`event: task`) | 200 stream | — |

### 16.2 Request and Response Shapes

Submit body: `{type, name?, priority?, delayMillis?, timeoutMillis?, maxRetries?, durationMillis?, computeLimit?, fetchAttempts?, failRate?, payload?}`. `TaskView`: `{id, name, type, priority, status, createdAt, startedAt, completedAt, attempt, maxRetries, timeoutMillis, result?, error?, eventLog[]}`. `StatsView`: `{total, pending, queued, running, completed, failed, timeout, cancelled, retrying, queueDepth, workers, avgLatencyMs, p95LatencyMs, byType{}}`. Errors: `{error, message, taskId?}` with field errors under `details` for 400s. (Worked examples: Appendix C.)

### 16.3 Configuration (Table 7)

`application.yml`: `server.port=8080`; `scheduler.workers=4`, `queueCapacity=1000`, `defaultTimeoutMillis=30000`, `retryBaseDelayMillis=1000`, `historySize=200`; Spring Jackson timestamps as ISO-8601. Every value is overridable via environment (`SCHEDULER_WORKERS`, …) or CLI flags without rebuilding. Larger `workers` suits CPU-bound COMPUTE loads; smaller suits memory-constrained machines; longer default timeouts suit slow FETCH targets.
---

## Chapter 17 — Testing Strategy and Results

### 17.1 Strategy

Three layers. **Unit tests** (`PriorityTaskQueueTest`, 3 tests) verify the comparator deterministically: CRITICAL dequeues before LOW; equal priorities preserve FIFO; `remove()` extracts a single envelope. **Integration tests** (`TaskSchedulerIntegrationTest`, 4 tests, `@SpringBootTest` + Awaitility) exercise the live service: submit→COMPLETED with a full event trail; delayed task stays PENDING then runs; `failRate=100, maxRetries=2` exhausts to FAILED after exactly 3 attempts; PENDING delayed task cancels to CANCELLED. **Live smoke tests** (curl + SSE capture) verified the deployed system: validation 400s, unknown-ID 404s, SSE `PENDING → QUEUED → RUNNING → COMPLETED` frames, and worker attribution.

### 17.2 Results (Table 5)

| Suite | Tests | Result |
|---|---|---|
| `PriorityTaskQueueTest` | 3 (ordering, FIFO, remove) | ✅ all pass |
| `TaskSchedulerIntegrationTest` | 4 (lifecycle, delay, retry-exhaust, cancel) | ✅ all pass |
| Live smoke (REST + SSE + proxy) | ~12 checks | ✅ all pass |
| `npm run build` | production bundle | ✅ clean |

`mvn test` is green (7/7); the suite was re-run four consecutive times during development with no flakes, since all async waits use Awaitility deadlines rather than fixed sleeps. The one incidental finding — a blank dashboard caused by a missing `createRoot().render()` mount — was caught by serving the built bundle and fixed the same session (verified by the bundle growing from 8.2 kB to 156.8 kB with React linked in).
---

## Chapter 18 — Results, Observations and Demo Scenarios

### 18.1 Demo Script (Table 6)

| # | Action | Expected observation |
|---|---|---|
| 1 | Submit DELAY 6 s | PENDING 6 s → QUEUED → RUNNING → COMPLETED; SSE animates the row |
| 2 | Submit COMPUTE sieve | Prime-count result; ~4× faster than single-threaded at 4 workers |
| 3 | Burst ×10 | Ten rows cascade through states; queue-depth spikes then drains |
| 4 | CRITICAL FETCH into a busy pool | Jumps ahead of NORMAL/LOW queued tasks |
| 5 | `failRate=90, maxRetries=3` | RETRYING with 1 s → 2 s → 4 s gaps, then FAILED or COMPLETED |
| 6 | Cancel a RUNNING delay | CANCELLED within ~50 ms (slice poll); worker freed |
| 7 | Inspect any row | Modal shows full timestamped event log + params + result |
| 8 | Stats bar | Counts, avg/P95 latency, queue depth update live |

### 18.2 Key Observations

Submission latency is flat (single-digit ms) regardless of job length — the request thread never executes work. Priority preemption is visible: the CRITICAL envelope always takes the next freed worker. Back-off spacing is measurable in event timestamps. Cancellation of a 30 s DELAY lands in one 50 ms slice. The SSE stream carried every transition in the smoke test with no missed frames on localhost, and the dashboard resyncs via REST on reconnect, so transient disconnects self-heal.
---

## Chapter 19 — Performance Analysis

Throughput is bounded by `workers` for CPU-bound COMPUTE (linear to core count) and far higher for I/O-ish DELAY/FETCH (workers rarely saturated). Per-task overhead is one UUID, one map entry, a handful of `AtomicLong` increments and one queue envelope — microseconds against millisecond-scale jobs. The queue itself is O(log n); the registry O(1); SSE fan-out O(subscribers) per transition with negligible payloads. Latency percentiles come from a bounded deque (default last 1,000 samples), so stats computation is O(k log k) on a small k. The practical limits are memory for retained history (bounded by `historySize` eviction) and one SSE socket per browser tab. No formal benchmark harness was built — Chapter 22 proposes JMH sieve scaling plus a burst-throughput gauge as the first measurement extension.
---

## Chapter 20 — Security Considerations

The threat model is a classroom demo on localhost, and the controls match it — honestly documented rather than overstated. **Input validation** (Bean Validation on every field, lenient-but-bounded enum parsing, `timeoutMillis ≥ 100`, `failRate ∈ [0,100]`) rejects malformed or absurd requests with 400s before any thread is spent. **No injection surfaces**: there is no SQL, no shell, no template engine; params are numbers/strings consumed by arithmetic and sleeps. **CORS** is restricted to the Vite dev origin rather than `*`. **No secrets** exist anywhere — no auth tokens, no passwords, no keys in code or config. **DoS exposure** is limited to filling memory with tasks; mitigations present are `queueCapacity` awareness, `historySize` eviction, and per-task timeouts that reclaim workers. Deliberately absent (and listed as future work): authentication/authorisation, rate limiting, TLS termination, and audit logging — all required before any internet-facing deployment.
---

## Chapter 21 — Limitations of the Project

1. **In-memory only.** Restarts lose all history; there is no persistence layer.
2. **Single node.** No distribution, no leader election, no shared queue across instances.
3. **No recurring schedules.** One-shot delays only; no cron expressions or fixed-rate repeats.
4. **Possible LOW-task starvation** under a sustained CRITICAL flood (no aging mechanism).
5. **Cooperative cancellation.** A job that ignores the flag (none of the four do, but a future custom job might) cannot be force-stopped.
6. **Best-effort SSE.** A client disconnected mid-transition misses that frame (it resyncs via REST, but the gap is real).
7. **No auth, no rate limits, no TLS.** Unsafe to expose beyond localhost as shipped.
8. **Simulated jobs.** FETCH performs no real I/O; COMPUTE's sieve is a stand-in workload.
9. **No formal benchmarks.** Performance claims are architectural reasoning plus smoke-test observation, not JMH numbers.
---

## Chapter 22 — Future Scope and Enhancements

1. **Persistence.** File-backed JSON snapshots on every transition plus a JPA/H2 option, so history survives restarts.
2. **Virtual threads (Project Loom).** Replace the fixed worker pool with `Executors.newVirtualThreadPerTaskExecutor()` on JDK 21+ for I/O-bound scale; measure the difference.
3. **Recurring schedules.** Cron expressions and fixed-rate triggers via the existing dispatcher, with a `schedule` record type.
4. **Priority aging.** Gradually boost long-waiting LOW tasks to eliminate theoretical starvation.
5. **Real FETCH.** Back FETCH with `HttpClient` against configurable URLs with connection pooling.
6. **WebSocket control channel.** Keep SSE for events, add a socket for pause/resume/reprioritise commands.
7. **Auth + rate limiting.** Token-based auth, per-IP submission throttles, TLS profile.
8. **Prometheus metrics.** Expose counters, gauges and histograms at `/actuator/prometheus` with Grafana dashboards.
9. **JMH benchmarks.** Sieve scaling vs workers, burst throughput, P99 submission latency.
10. **Distributed mode.** Redis-backed queue with fencing/leases for multi-instance scheduling.
---

## Chapter 23 — Conclusion

This project set out to build an asynchronous task scheduler that is genuinely concurrent, genuinely observable, and genuinely documented — and it delivers all three. The backend schedules, prioritises, delays, retries, times out and cancels work using nothing but the JDK's own concurrency toolkit; the React dashboard makes every transition visible in real time; the seven automated tests prove the behaviour deterministically; and this report maps each advanced-Java concept to the exact file and construct that exhibits it.

Beyond the working system, the deeper outcome is understanding: executors are no longer abstract pool names but the dispatcher and workers whose separation keeps timing independent of jobs; blocking queues are no longer textbook diagrams but the priority structure that visibly reorders the dashboard; `volatile` and `synchronized` are no longer vocabulary words but the visibility guarantee and atomic-transition mechanism behind every state change. That working mental model — verified by running code, not just reading about it — is the true deliverable of a second-year Advanced Java project.
---

## Chapter 24 — References

1. B. Goetz et al., *Java Concurrency in Practice*, Addison-Wesley, 2006 — the primary reference for executors, concurrent collections, visibility and safe publication.
2. Oracle, "Java Tutorials: Concurrency" (docs.oracle.com) — executor, synchronisation and atomic-variable usage.
3. Oracle, JDK 17 `java.util.concurrent` API documentation — `ScheduledExecutorService`, `PriorityBlockingQueue`, `ConcurrentHashMap`, `CopyOnWriteArrayList`, `AtomicLong`, `Future`.
4. Spring Boot 3.2 reference documentation — REST controllers, `SseEmitter`, `@ConfigurationProperties`, `@RestControllerAdvice`, Bean Validation.
5. React 18 documentation — hooks (`useState`, `useEffect`, `useMemo`), `createRoot`, `EventSource` integration patterns.
6. Vite 5 documentation — dev server proxy, production builds.
7. JUnit 5, AssertJ and Awaitility user guides — async test assertions.
8. Apache Maven documentation — lifecycle, Spring Boot parent POM, Surefire.
9. Project sources: `README.md` (setup and API), `docs/DESIGN.md` (architecture and threading), `docs/PROJECT_REPORT.md` (short report), all files under `backend/src` and `frontend/src`.
<!-- APPA -->
<!-- APPB -->
<!-- APPC -->
<!-- APPD -->
<!-- END -->


---


---

## Chapter 25 — Viva-Voce Preparation: Questions and Answers (Part 1)

**Q1. Why `ScheduledExecutorService` instead of `Timer`?** `Timer` is single-threaded: one slow task delays every other timer, and an uncaught exception kills the whole timer thread. `ScheduledExecutorService` runs timers on a pool, survives task exceptions, and offers `ScheduledFuture` handles for cancellation. The dispatcher uses all three properties.

**Q2. `volatile` vs `synchronized` in your code?** `volatile` (status field, cancelled flag) guarantees visibility but no atomicity for multi-step updates. `synchronized` (transition methods) gives mutual exclusion plus visibility, making check-then-act atomic. Hot single-field reads use `volatile`; multi-step transitions use `synchronized`.

**Q3. Why `ConcurrentHashMap` over `synchronizedMap`?** `synchronizedMap` locks the whole map per operation. `ConcurrentHashMap` uses fine-grained locking/CAS, allowing concurrent reads and mostly-concurrent writes, plus atomic helpers (`putIfAbsent`, `computeIfPresent`) used by the registration path.

**Q4. How does the priority queue stay fair?** Ordering is `(priorityRank, sequenceNumber)`. Different priorities sort by rank; equal priorities sort by the `AtomicLong` sequence from submission order — exactly FIFO. The queue is thread-safe so concurrent submissions cannot corrupt the heap.

**Q5. What if a worker throws uncaught?** The consumer loop wraps handoffs in try/catch; the execution handshake catches `ExecutionException`. The task goes RETRYING or FAILED with the message recorded; the consumer survives and takes the next envelope. One poisoned task never wedges the scheduler.

**Q6. Explain exponential back-off.** With base 1000 ms and maxRetries 3: fail, wait 1 s, fail, wait 2 s, fail, wait 4 s, fail, attempts exhausted, FAILED. Waits are dispatcher timers, never blocked workers.

**Q7. How do timeouts avoid killing threads?** Each execution arms `dispatcher.schedule(guard, timeout)`. Guard and completion race on a future handshake; the winner decides, the loser is ignored. On timeout victory the worker future is cancelled and the task becomes TIMEOUT. No `Thread.stop`.

**Q8. Why cooperative cancellation?** A `volatile boolean cancelled` flag is polled at safe points (sleep slices, sieve iterations, fetch attempts, report chunks) and the job unwinds cleanly to CANCELLED. Forced termination can leave monitors locked and objects half-updated.

**Q9. Why `CopyOnWriteArrayList` for SSE subscribers?** Broadcasts (iteration) happen per transition; subscribes happen rarely. Copy-on-write gives lock-free iteration with no `ConcurrentModificationException`, copying only on rare writes — the ideal trade-off.

**Q10. Why `LinkedBlockingQueue` in burst?** The canonical producer-consumer queue: producer `put`s (blocking if the capacity-64 buffer fills), the drain loop `take`s. It demonstrates blocking semantics the unbounded priority queue does not.

---

## Chapter 25 (contd.) — Viva Questions and Answers (Part 2)

**Q11. What is a record, and where used?** A record (Java 16+) is a compact immutable carrier: `record TaskView(...)` generates constructor, accessors, equals/hashCode/toString. `TaskView` and `StatsView` are records because DTOs should be immutable snapshots.

**Q12. How does the dashboard update without refresh?** An `EventSource` on `GET /api/events` receives an `event: task` frame per transition; React upserts that record into state, re-rendering only the affected row plus stats. Initial load uses REST, so reconnects self-heal.

**Q13. Why SSE over WebSockets/polling?** Polling wastes requests and lags; WebSockets add bidirectional machinery for a server-only-speaks channel. SSE is plain HTTP, auto-reconnects, and needs ~20 lines of client code.

**Q14. How do tests avoid sleep flakiness?** Awaitility polls a condition until true or a deadline (`await().atMost(10, SECONDS).until(...)`). Tests pass as soon as the condition holds and fail deterministically at the deadline — never too-short (flake) or too-long (slow).

**Q15. What is graceful shutdown, ordered how?** `@PreDestroy` (1) stops consumer loops taking, (2) shuts the dispatcher so no new timers fire, (3) shuts workers with `awaitTermination` then `shutdownNow`, (4) completes SSE emitters. Order prevents new work entering a dying pool.

**Q16. What is P95 and how computed?** The value at/below which 95% of executions fall. A bounded latency deque is copied, sorted, and indexed at `ceil(0.95*n)-1` (nearest rank). P95 exposes tail behaviour the average hides.

**Q17. Where is `parallelStream` safe?** In `BUILD_REPORT` aggregation and P95 sorting — both over local snapshot copies, never the live registry, so no shared mutation can race.

**Q18. How to add a fifth job type?** Add the `TaskType` constant, its request params, a `DemoJobFactory` case branch polling the cancelled flag, and a form preset in `TaskForm.jsx`. No scheduler-core change needed — the factory is the extension point.

**Q19. Biggest limitation, honestly?** No persistence: restarts wipe the registry. Right trade-off for a concurrency project, and first in future work (JSON snapshots + optional JPA).

**Q20. How to scale out?** Replace the in-process queue with a Redis sorted-set (composite rank/sequence score), add fencing tokens so two instances never run one task, keep per-node worker pools. State machine, retries and SSE survive unchanged.

---

## Chapter 26 — Development Log and Timeline

| Week | Activity | Outcome |
|---|---|---|
| 1 | Topic selection, feasibility, syllabus mapping | Scheduler approved; topics mapped to classes |
| 2 | Backend skeleton: app, enums, DTOs, controllers | REST shell compiling; stubs via curl |
| 3 | `TaskRecord` machine + `PriorityTaskQueue` + unit tests | 3/3 queue tests green |
| 4 | Service: dispatcher, consumers, workers, jobs | First COMPLETED tasks via curl |
| 5 | Retry, timeouts, cancellation, metrics, SSE | Full life-cycle visible; frames captured |
| 6 | React dashboard: form, table, modal, stats, SSE | Live dashboard on :5173 via proxy |
| 7 | Integration tests, mount fix, verification | 7/7 green; E2E passed |
| 8 | README, DESIGN.md, this report, review | Documentation complete; committed |

The most instructive incident was the week-7 blank page: the bundle built cleanly but `#root` stayed empty because `main.jsx` exported `App` without mounting it. Lesson: a green build proves compilation, not rendering. Fix: `createRoot(...).render(<App />)`, verified by asserting the served module contains the mount call.

---

## Chapter 27 — Glossary of Concurrency Terms

**Atomicity** — operation appears indivisible; via `synchronized` and `AtomicLong`. **Visibility** — writes become observable; via `volatile`, locks, concurrent collections. **Race condition** — outcome depends on interleaving; `canTransition` guards make races harmless. **Deadlock** — mutual waiting forever; avoided by never holding two monitors. **Starvation** — never scheduled; theoretically LOW tasks under CRITICAL flood. **Liveness** — keeps progressing; `take()` loops plus daemon threads. **Daemon thread** — does not block JVM exit; both pools use daemon factories. **Back-pressure** — slowing producers; burst queue capacity-64 `put`-blocking. **Idempotence** — safe to repeat; retries assume idempotent jobs. **Happens-before** — JMM ordering guarantee; every volatile write, lock release and collection op establishes one.

---
## Appendix A — Complete Backend Source Code

Every backend file is reproduced verbatim below, in package order.

### A.1 `backend/src/main/java/com/taskscheduler/AsyncTaskSchedulerApp.java`

```java
package com.taskscheduler;

import com.taskscheduler.scheduler.SchedulerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Async Task Scheduler - 2nd Year project backend.
 *
 * <p>A Spring Boot application whose domain heart is
 * {@link com.taskscheduler.scheduler.TaskSchedulerService}: an asynchronous,
 * priority-ordered, retry-aware task scheduler built on the advanced Java
 * concurrency toolkit (ExecutorService, ScheduledExecutorService,
 * PriorityBlockingQueue, ConcurrentHashMap, CompletableFuture, etc.).</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(SchedulerProperties.class)
public class AsyncTaskSchedulerApp {

    public static void main(String[] args) {
        SpringApplication.run(AsyncTaskSchedulerApp.class, args);
    }
}
```

### A.2 `backend/src/main/java/com/taskscheduler/common/TaskStatus.java`

```java
package com.taskscheduler.common;

/**
 * Life-cycle states of a scheduled task.
 *
 * <pre>
 *   PENDING ──► QUEUED ──► RUNNING ──► COMPLETED
 *      │          │           └───────► FAILED ──► (retry) ──► QUEUED/RUNNING
 *      │          │                     └───────► TIMED_OUT
 *      └──────────┴───────────────────► CANCELLED
 * </pre>
 *
 * All states except the terminal set {@link #isTerminal()} == {@code true}.
 */
public enum TaskStatus {

    PENDING("Waiting for its scheduled start time"),
    QUEUED("Waiting in the priority queue for a free worker"),
    RUNNING("Currently executing on a worker thread"),
    COMPLETED("Finished successfully"),
    FAILED("Finished with an error (may be retried)"),
    CANCELLED("Cancelled by the user"),
    TIMED_OUT("Exceeded the execution timeout");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    /** Terminal states that a task can never leave. */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == TIMED_OUT;
    }

    /** States in which a user cancel request is still acceptable. */
    public boolean isCancellable() {
        return this == PENDING || this == QUEUED || this == RUNNING;
    }

    /** States from which a manual retry is allowed. */
    public boolean isRetryable() {
        return this == FAILED || this == TIMED_OUT;
    }
}
```

### A.3 `backend/src/main/java/com/taskscheduler/common/TaskPriority.java`

```java
package com.taskscheduler.common;

/**
 * Priority levels used to order queued work.
 * Higher ranks are dequeued first by the scheduler's {@code PriorityBlockingQueue}.
 */
public enum TaskPriority {

    LOW(0),
    NORMAL(1),
    HIGH(2),
    CRITICAL(3);

    private final int rank;

    TaskPriority(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    public static TaskPriority fromRank(int rank) {
        for (TaskPriority p : values()) {
            if (p.rank == rank) {
                return p;
            }
        }
        return NORMAL;
    }
}
```

### A.4 `backend/src/main/java/com/taskscheduler/common/TaskType.java`

```java
package com.taskscheduler.common;

/**
 * The kinds of demo jobs the scheduler can execute.
 * Each type maps to a different execution profile:
 *
 * <ul>
 *   <li>{@link #DELAY}          - simulated I/O wait (sleep)</li>
 *   <li>{@link #COMPUTE}        - CPU bound prime counting job</li>
 *   <li>{@link #FETCH}          - simulated remote HTTP fetch</li>
 *   <li>{@link #BUILD_REPORT}   - aggregate report over all tasks (parallel streams)</li>
 * </ul>
 */
public enum TaskType {

    DELAY("Simulated I/O delay", "durationMs"),
    COMPUTE("CPU-bound prime computation", "limit"),
    FETCH("Simulated network fetch", "url"),
    BUILD_REPORT("Aggregate report built with parallel streams", null);

    private final String label;
    private final String primaryParam;

    TaskType(String label, String primaryParam) {
        this.label = label;
        this.primaryParam = primaryParam;
    }

    public String label() {
        return label;
    }

    public String primaryParam() {
        return primaryParam;
    }

    public static TaskType fromName(String name) {
        for (TaskType t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return DELAY;
    }
}
```

### A.5 `backend/src/main/java/com/taskscheduler/model/TaskRecord.java`

```java
package com.taskscheduler.model;

import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskStatus;
import com.taskscheduler.common.TaskType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mutable, thread-safe state holder for one scheduled task.
 *
 * <p><b>Concurrency design</b></p>
 * <ul>
 *   <li>{@code status} and {@code cancelRequested} are {@code volatile} so they can be
 *       read lock-free from any thread while transitions are still atomic.</li>
 *   <li>Every state transition is a {@code synchronized} method, so a transition is
 *       an atomic operation and two threads can never interleave mid-transition.</li>
 *   <li>{@code progress} is an {@link AtomicInteger}: the executing worker updates it
 *       without holding the lock.</li>
 *   <li>{@code eventLog} is a thread-safe {@link CopyOnWriteArrayList} of immutable
 *       {@link EventEntry} snapshots - the audit trail of everything that happened.</li>
 * </ul>
 */
public final class TaskRecord {

    /** Immutable, atomic audit-log entry. */
    public record EventEntry(long atMillis, TaskStatus status, String detail) {}

    // ------------------------------------------------------------------
    // Immutable identity (set once at construction, never changes)
    // ------------------------------------------------------------------
    private final String id;
    private final String name;
    private final TaskType type;
    private final TaskPriority priority;
    private final int delayMillis;
    private final int timeoutMillis;
    private final int maxRetries;
    private final int failRate;
    private final Map<String, Object> params;
    private final long submittedAtMillis;

    // ------------------------------------------------------------------
    // Mutable state
    // ------------------------------------------------------------------
    private volatile TaskStatus status = TaskStatus.PENDING;
    private volatile boolean cancelRequested = false;

    private String workerId;      // guarded by synchronized
    private int attempt = 0;      // guarded by synchronized
    private String result;        // guarded by synchronized
    private String error;         // guarded by synchronized
    private long startedAtMillis; // guarded by synchronized
    private long finishedAtMillis;// guarded by synchronized

    /** Saved handle of the delayed dispatch future so PENDING tasks can be unscheduled. */
    private ScheduledFuture<?> dispatchHandle; // guarded by synchronized

    private final AtomicInteger progress = new AtomicInteger(0);
    private final List<EventEntry> eventLog = new CopyOnWriteArrayList<>();

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public TaskRecord(String id,
                      String name,
                      TaskType type,
                      TaskPriority priority,
                      int delayMillis,
                      int timeoutMillis,
                      int maxRetries,
                      int failRate,
                      Map<String, Object> params) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.priority = priority;
        this.delayMillis = delayMillis;
        this.timeoutMillis = timeoutMillis;
        this.maxRetries = maxRetries;
        this.failRate = failRate;
        // Defensive copy so callers cannot mutate the parameters afterwards.
        this.params = Map.copyOf(params == null ? Map.of() : params);
        this.submittedAtMillis = System.currentTimeMillis();
    }

    // ------------------------------------------------------------------
    // Immutable getters (safe to call from any thread)
    // ------------------------------------------------------------------
    public String id()                 { return id; }
    public String name()               { return name; }
    public TaskType type()             { return type; }
    public TaskPriority priority()     { return priority; }
    public int delayMillis()           { return delayMillis; }
    public int timeoutMillis()         { return timeoutMillis; }
    public int maxRetries()            { return maxRetries; }
    public int failRate()              { return failRate; }
    public Map<String, Object> params(){ return params; }
    public long submittedAtMillis()    { return submittedAtMillis; }
    public TaskStatus status()         { return status; }
    public boolean isCancelRequested() { return cancelRequested; }
    public int progress()              { return progress.get(); }
    public List<EventEntry> eventLog() { return List.copyOf(eventLog); }

    /** Typed helpers over the params map (jobs use these). */
    public int intParam(String key, int defaultValue) {
        Object v = params.get(key);
        return v instanceof Number n ? n.intValue() : defaultValue;
    }

    public long longParam(String key, long defaultValue) {
        Object v = params.get(key);
        return v instanceof Number n ? n.longValue() : defaultValue;
    }

    public String stringParam(String key, String defaultValue) {
        Object v = params.get(key);
        return v == null ? defaultValue : String.valueOf(v);
    }

// ------------------------------------------------------------------
    // Synchronized state transitions
    // ------------------------------------------------------------------
    /** Records the scheduled fire time (dispatcher). */
    public synchronized void markSubmitted(long scheduledFireAtMillis) {
        status = TaskStatus.PENDING;
        logEvent("Scheduled to fire at "
                + Instant.ofEpochMilli(scheduledFireAtMillis));
    }

    /** Dispatcher has handed the task to the priority queue. */
    public synchronized void markQueued() {
        status = TaskStatus.QUEUED;
        logEvent("Dispatched - waiting in priority queue");
    }

    /** A worker picks the task up; attempt counter increments. */
    public synchronized void startAttempt(String workerId) {
        this.status = TaskStatus.RUNNING;
        this.workerId = workerId;
        this.attempt++;
        this.startedAtMillis = System.currentTimeMillis();
        logEvent("Attempt " + attempt + " started on worker '" + workerId + "'");
    }

    public synchronized void markCompleted(String result) {
        if (status.isTerminal()) return;  // timeout/cancel already won the race
        this.status = TaskStatus.COMPLETED;
        this.result = result;
        this.finishedAtMillis = System.currentTimeMillis();
        logEvent("Completed successfully");
    }

    public synchronized void markFailed(String error) {
        if (status.isTerminal()) return;
        this.status = TaskStatus.FAILED;
        this.error = error;
        this.finishedAtMillis = System.currentTimeMillis();
        logEvent("Failed: " + error);
    }

    /** Cooperative cancellation - running jobs observe {@link #isCancelRequested()}. */
    public synchronized void markCancelled(String reason) {
        this.status = TaskStatus.CANCELLED;
        this.cancelRequested = true;
        this.finishedAtMillis = System.currentTimeMillis();
        this.error = reason;
        logEvent("Cancelled: " + reason);
    }

    public synchronized void markTimedOut(String detail) {
        if (status.isTerminal()) return;
        this.status = TaskStatus.TIMED_OUT;
        this.error = detail;
        this.finishedAtMillis = System.currentTimeMillis();
        logEvent("Timed out after " + timeoutMillis + " ms");
    }

    /**
     * A failed attempt that will be automatically retried:
     * records the failure in the audit log, then moves the task back to QUEUED.
     */
    public synchronized void markRetryAfterFailure(String failureMessage, long backoffMillis) {
        this.error = failureMessage;
        eventLog.add(new EventEntry(System.currentTimeMillis(), TaskStatus.FAILED,
                "Attempt " + attempt + " failed: " + failureMessage));
        this.status = TaskStatus.QUEUED;
        this.workerId = null;
        this.startedAtMillis = 0;
        this.finishedAtMillis = 0;
        logEvent("Automatic retry scheduled in " + backoffMillis + " ms"
                + (attempt + 1 <= maxRetries ? "" : " (last try)"));
    }

    /** Manually requeue a FAILED / TIMED_OUT task (attempt counter is preserved). */
    public synchronized void resetForManualRetry() {
        this.status = TaskStatus.QUEUED;
        this.cancelRequested = false;
        this.workerId = null;
        this.result = null;
        this.error = null;
        this.startedAtMillis = 0;
        this.finishedAtMillis = 0;
        logEvent("Manual retry requested - requeued");
    }

    // ------------------------------------------------------------------
    // Miscellaneous accessors
    // ------------------------------------------------------------------
    public synchronized int attempt()             { return attempt; }
    public synchronized String workerId()         { return workerId; }
    public synchronized String result()           { return result; }
    public synchronized String error()            { return error; }
    public synchronized long startedAtMillis()    { return startedAtMillis; }
    public synchronized long finishedAtMillis()   { return finishedAtMillis; }

    public void noteProgress(int percent) {
        progress.set(Math.max(0, Math.min(100, percent)));
    }

    public synchronized void setDispatchHandle(ScheduledFuture<?> handle) {
        this.dispatchHandle = handle;
    }

    public synchronized ScheduledFuture<?> dispatchHandle() {
        return dispatchHandle;
    }

    /** Wall-clock time the task has spent in the scheduler, in milliseconds. */
    public synchronized long elapsedMillis() {
        long now = System.currentTimeMillis();
        if (finishedAtMillis > 0) return finishedAtMillis - submittedAtMillis;
        if (startedAtMillis > 0)  return now - startedAtMillis;
        return now - submittedAtMillis;
    }

    /** The measure used for latency statistics: submit -> finish. */
    public synchronized long latencyMillis() {
        return finishedAtMillis > 0 ? finishedAtMillis - submittedAtMillis : 0;
    }

    private void logEvent(String detail) {
        eventLog.add(new EventEntry(System.currentTimeMillis(), status, detail));
    }

    @Override
    public String toString() {
        return "TaskRecord[" + id + ", " + name + ", " + type + ", " + status + "]";
    }
}
```

### A.6 `backend/src/main/java/com/taskscheduler/model/TaskSubmitRequest.java`

```java
package com.taskscheduler.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Immutable REST request DTO (a Java {@code record}).
 *
 * <p>Record components are implicitly {@code final}, so friendly defaults for
 * omitted fields are applied by the {@link #create} {@code @JsonCreator}
 * factory, which Jackson uses instead of the canonical constructor. The boxed
 * {@link Integer} component types let us distinguish "omitted" (null) from an
 * explicit value. Bean-validation annotations ({@code jakarta.validation}) are
 * evaluated by Spring before the controller method runs.</p>
 */
public record TaskSubmitRequest(
        @Size(max = 80, message = "name must be at most 80 characters")
        String name,

        TaskType type,

        TaskPriority priority,

        @Min(value = 0, message = "delayMillis must be >= 0")
        @Max(value = 86_400_000, message = "delayMillis must be <= 86400000")
        Integer delayMillis,

        @Min(value = 100, message = "timeoutMillis must be >= 100")
        @Max(value = 3_600_000, message = "timeoutMillis must be <= 3600000")
        Integer timeoutMillis,

        @Min(value = 0, message = "maxRetries must be >= 0")
        @Max(value = 10, message = "maxRetries must be <= 10")
        Integer maxRetries,

        @Min(value = 0, message = "failRate must be >= 0")
        @Max(value = 100, message = "failRate must be <= 100")
        Integer failRate,

        Map<String, Object> params) {

    /** Jackson factory: applies defaults for anything the client omitted. */
    @JsonCreator
    public static TaskSubmitRequest create(
            @JsonProperty("name") String name,
            @JsonProperty("type") TaskType type,
            @JsonProperty("priority") TaskPriority priority,
            @JsonProperty("delayMillis") Integer delayMillis,
            @JsonProperty("timeoutMillis") Integer timeoutMillis,
            @JsonProperty("maxRetries") Integer maxRetries,
            @JsonProperty("failRate") Integer failRate,
            @JsonProperty("params") Map<String, Object> params) {
        return new TaskSubmitRequest(
                name == null || name.isBlank() ? "Task" : name.trim(),
                type == null ? TaskType.DELAY : type,
                priority == null ? TaskPriority.NORMAL : priority,
                delayMillis == null ? 0 : delayMillis,
                timeoutMillis == null ? 60_000 : timeoutMillis,
                maxRetries == null ? 0 : maxRetries,
                failRate == null ? 0 : failRate,
                params == null ? Map.of() : Map.copyOf(params));
    }
}
```

### A.7 `backend/src/main/java/com/taskscheduler/model/TaskView.java`

```java
package com.taskscheduler.model;

import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskStatus;
import com.taskscheduler.common.TaskType;

import java.util.List;
import java.util.Map;

/**
 * Immutable API snapshot of a {@link TaskRecord} - safe to serialize at any time.
 * Building it takes a coherent, copy-on-read view of the mutable state.
 */
public record TaskView(
        String id,
        String name,
        TaskType type,
        TaskPriority priority,
        int delayMillis,
        int timeoutMillis,
        int maxRetries,
        int failRate,
        Map<String, Object> params,
        TaskStatus status,
        int progress,
        int attempt,
        String workerId,
        String result,
        String error,
        long submittedAtMillis,
        long startedAtMillis,
        long finishedAtMillis,
        long elapsedMillis,
        List<TaskRecord.EventEntry> eventLog) {

    /** Static factory - keeps the controller free of record internals. */
    public static TaskView from(TaskRecord t) {
        return new TaskView(
                t.id(),
                t.name(),
                t.type(),
                t.priority(),
                t.delayMillis(),
                t.timeoutMillis(),
                t.maxRetries(),
                t.failRate(),
                t.params(),
                t.status(),
                t.progress(),
                t.attempt(),
                t.workerId(),
                t.result(),
                t.error(),
                t.submittedAtMillis(),
                t.startedAtMillis(),
                t.finishedAtMillis(),
                t.elapsedMillis(),
                t.eventLog());
    }
}
```

### A.8 `backend/src/main/java/com/taskscheduler/model/StatsView.java`

```java
package com.taskscheduler.model;

/**
 * Immutable aggregate statistics about everything the scheduler has seen.
 * Composed from thread-safe counters ({@link com.taskscheduler.scheduler.SchedulerMetrics})
 * plus values computed with a {@code Stream} pipeline over a snapshot.
 */
public record StatsView(
        long total,
        long pending,
        long queued,
        long running,
        long completed,
        long failed,
        long cancelled,
        long timedOut,
        long avgLatencyMillis,
        long p95LatencyMillis,
        int workerCount,
        int queueDepth,
        long submittedTotal) {}
```

### A.9 `backend/src/main/java/com/taskscheduler/scheduler/TaskSchedulerService.java`

```java
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
```

### A.10 `backend/src/main/java/com/taskscheduler/scheduler/PriorityTaskQueue.java`

```java
package com.taskscheduler.scheduler;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thin, purpose-built wrapper around {@link PriorityBlockingQueue}.
 *
 * <p>{@code PriorityBlockingQueue} is an unbounded blocking queue whose head is
 * always the element with the smallest {@code compareTo} value, so concurrent
 * enqueue/dequeue never needs an external lock. We maintain a close-enough
 * depth gauge for the dashboard with an {@link AtomicInteger}.</p>
 */
public final class PriorityTaskQueue {

    private final PriorityBlockingQueue<QueuedTask> queue;
    private final AtomicInteger depth = new AtomicInteger(0);

    public PriorityTaskQueue(int capacity) {
        this.queue = new PriorityBlockingQueue<>(capacity);
    }

    /** Blocking insert. Returns the (approximate) depth after the put. */
    public int put(QueuedTask item) {
        queue.put(item);
        return depth.incrementAndGet();
    }

    /** Blocking ordered remove; caller handles {@link InterruptedException}. */
    public QueuedTask take() throws InterruptedException {
        QueuedTask head = queue.take();
        depth.decrementAndGet();
        return head;
    }

    /** Ordered poll that gives up after {@code timeout}; used for graceful shutdown. */
    public QueuedTask poll(long timeout, TimeUnit unit) throws InterruptedException {
        QueuedTask head = queue.poll(timeout, unit);
        if (head != null) {
            depth.decrementAndGet();
        }
        return head;
    }

    /** Non-blocking peek of the highest-priority element (diagnostics only). */
    public QueuedTask peek() {
        return queue.peek();
    }

    public int depth() {
        return depth.get();
    }
}
```

### A.11 `backend/src/main/java/com/taskscheduler/scheduler/QueuedTask.java`

```java
package com.taskscheduler.scheduler;

import com.taskscheduler.model.TaskRecord;

/**
 * An entry waiting in the scheduler's priority queue.
 *
 * <p>Implements {@link Comparable} so {@code PriorityBlockingQueue} can order it:
 * {@link TaskRecord#priority()} rank first (highest rank first), then FIFO order
 * via a monotonically increasing sequence number (lowest sequence first).</p>
 */
public record QueuedTask(TaskRecord task, long sequenceNr, long queuedAtMillis)
        implements Comparable<QueuedTask> {

    @Override
    public int compareTo(QueuedTask other) {
        // Higher priority rank first ...
        int rankCmp = Integer.compare(other.task().priority().rank(),
                                      this.task().priority().rank());
        if (rankCmp != 0) {
            return rankCmp;
        }
        // ... then FIFO among equal priorities.
        return Long.compare(this.sequenceNr, other.sequenceNr);
    }
}
```

### A.12 `backend/src/main/java/com/taskscheduler/scheduler/DemoJobFactory.java`

```java
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
```

### A.13 `backend/src/main/java/com/taskscheduler/scheduler/SchedulerMetrics.java`

```java
package com.taskscheduler.scheduler;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Lock-free cumulative counters for the whole scheduler.
 * Every counter is an {@link AtomicLong}, so any thread may bump it without locking.
 */
public final class SchedulerMetrics {

    private final AtomicLong submittedTotal = new AtomicLong();
    private final AtomicLong completedTotal  = new AtomicLong();
    private final AtomicLong failedTotal     = new AtomicLong();
    private final AtomicLong cancelledTotal  = new AtomicLong();
    private final AtomicLong timedOutTotal   = new AtomicLong();

    public void taskSubmitted() { submittedTotal.incrementAndGet(); }
    public void taskCompleted() { completedTotal.incrementAndGet(); }
    public void taskFailed()    { failedTotal.incrementAndGet(); }
    public void taskCancelled() { cancelledTotal.incrementAndGet(); }
    public void taskTimedOut()  { timedOutTotal.incrementAndGet(); }

    public long submittedTotal() { return submittedTotal.get(); }
    public long completedTotal() { return completedTotal.get(); }
    public long failedTotal()    { return failedTotal.get(); }
    public long cancelledTotal() { return cancelledTotal.get(); }
    public long timedOutTotal()  { return timedOutTotal.get(); }
}
```

### A.14 `backend/src/main/java/com/taskscheduler/scheduler/SchedulerProperties.java`

```java
package com.taskscheduler.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Type-safe binding for the {@code scheduler.*} keys in {@code application.yml}.
 * Spring binds and validates these at startup; {@code @DefaultValue} protects
 * against missing keys.
 */
@ConfigurationProperties(prefix = "scheduler")
public record SchedulerProperties(
        @DefaultValue("4") int workers,
        @DefaultValue("2048") int queueCapacity,
        @DefaultValue("60000") int defaultTimeoutMs,
        @DefaultValue("2") int maxRetries,
        @DefaultValue("15000") int heartbeatMs) {}
```

### A.15 `backend/src/main/java/com/taskscheduler/scheduler/SchedulerException.java`

```java
package com.taskscheduler.scheduler;

/** Base runtime exception for scheduler failures (mapped to HTTP 500 by the advice). */
public class SchedulerException extends RuntimeException {

    public SchedulerException(String message) {
        super(message);
    }

    public SchedulerException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### A.16 `backend/src/main/java/com/taskscheduler/scheduler/TaskNotFoundException.java`

```java
package com.taskscheduler.scheduler;

/** Raised when a task id does not exist - mapped to HTTP 404 by the exception advice. */
public class TaskNotFoundException extends RuntimeException {

    private final String taskId;

    public TaskNotFoundException(String taskId) {
        super("Task not found: " + taskId);
        this.taskId = taskId;
    }

    public String taskId() {
        return taskId;
    }
}
```

### A.17 `backend/src/main/java/com/taskscheduler/scheduler/JobFailureException.java`

```java
package com.taskscheduler.scheduler;

/** Thrown by a job when it decides to fail (used for retry/failure handling). */
public class JobFailureException extends SchedulerException {

    public JobFailureException(String message) {
        super(message);
    }

    public JobFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### A.18 `backend/src/main/java/com/taskscheduler/scheduler/JobCancelledException.java`

```java
package com.taskscheduler.scheduler;

/** Thrown by a running job when it observes a cooperative cancel request. */
public class JobCancelledException extends SchedulerException {

    public JobCancelledException(String message) {
        super(message);
    }
}
```

### A.19 `backend/src/main/java/com/taskscheduler/web/TaskController.java`

```java
package com.taskscheduler.web;

import com.taskscheduler.model.TaskSubmitRequest;
import com.taskscheduler.model.TaskView;
import com.taskscheduler.scheduler.TaskSchedulerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST API for tasks.
 *
 * <ul>
 *   <li>{@code POST   /api/tasks}            - submit</li>
 *   <li>{@code GET    /api/tasks}            - list (newest first)</li>
 *   <li>{@code GET    /api/tasks/{id}}       - one task</li>
 *   <li>{@code DELETE /api/tasks/{id}}       - cancel</li>
 *   <li>{@code POST   /api/tasks/{id}/retry} - requeue a failed task</li>
 *   <li>{@code POST   /api/tasks/burst}      - submit N synthetic tasks</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskSchedulerService scheduler;

    public TaskController(TaskSchedulerService scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping
    public ResponseEntity<TaskView> submit(@Valid @RequestBody TaskSubmitRequest request) {
        TaskView created = TaskView.from(scheduler.submit(request));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/api/tasks/" + created.id()))
                .body(created);
    }

    @GetMapping
    public List<TaskView> list(@RequestParam(required = false) String status) {
        return scheduler.snapshot().stream()
                .filter(t -> status == null || status.isBlank()
                        || t.status().name().equalsIgnoreCase(status))
                .map(TaskView::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TaskView get(@PathVariable String id) {
        return TaskView.from(scheduler.require(id));
    }

    @DeleteMapping("/{id}")
    public TaskView cancel(@PathVariable String id) {
        if (!scheduler.cancel(id)) {
            throw new IllegalStateException(
                    "Task '" + id + "' is already in a terminal state and cannot be cancelled");
        }
        return TaskView.from(scheduler.require(id));
    }

    @PostMapping("/{id}/retry")
    public TaskView retry(@PathVariable String id) {
        return TaskView.from(scheduler.manualRetry(id));
    }

    @PostMapping("/burst")
    public ResponseEntity<java.util.Map<String, Object>> burst(
            @RequestParam(defaultValue = "10") int count) {
        int accepted = scheduler.submitBurst(count);
        return ResponseEntity.accepted().body(java.util.Map.of(
                "accepted", accepted,
                "message", accepted + " tasks accepted for submission"));
    }
}
```

### A.20 `backend/src/main/java/com/taskscheduler/web/DashboardController.java`

```java
package com.taskscheduler.web;

import com.taskscheduler.model.StatsView;
import com.taskscheduler.scheduler.SchedulerProperties;
import com.taskscheduler.scheduler.TaskSchedulerService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Dashboard endpoints: live stats plus a real-time Server-Sent-Events stream.
 *
 * <p>Every task transition is pushed to the browser over plain HTTP text/event-stream.
 * The event name is {@code task} with a JSON {@link com.taskscheduler.model.TaskView}
 * payload; a {@code ping} heartbeat keeps idle connections from being closed.</p>
 */
@Controller
public class DashboardController {

    private static final ScheduledExecutorService HEARTBEAT_POOL =
            Executors.newScheduledThreadPool(1);

    private final TaskSchedulerService scheduler;
    private final SchedulerProperties props;

    public DashboardController(TaskSchedulerService scheduler, SchedulerProperties props) {
        this.scheduler = scheduler;
        this.props = props;
    }

    @GetMapping(value = "/api/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);   // never auto-timeout

        Runnable unsubscribe = scheduler.subscribe(task -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("task")
                        .data(com.taskscheduler.model.TaskView.from(task)));
            } catch (IOException e) {
                throw new IllegalStateException("SSE channel closed", e);
            }
        });

        Runnable close = () -> { unsubscribe.run(); emitter.complete(); };
        emitter.onCompletion(close);
        emitter.onTimeout(close);
        emitter.onError(t -> close.run());

        // Heartbeat so proxies/browsers keep the stream open.
        HEARTBEAT_POOL.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
            } catch (IOException e) {
                close.run();
            }
        }, props.heartbeatMs(), props.heartbeatMs() + 1, TimeUnit.MILLISECONDS);

        return emitter;
    }

    @GetMapping(value = "/api/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StatsView stats() {
        return scheduler.stats();
    }

    @GetMapping(value = "/api/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public java.util.Map<String, Object> health() {
        return java.util.Map.of(
                "status", "UP",
                "taskCount", scheduler.snapshot().size(),
                "time", System.currentTimeMillis());
    }

    @PreDestroy
    public void shutdownHeartbeatPool() {
        HEARTBEAT_POOL.shutdownNow();
    }
}
```

### A.21 `backend/src/main/java/com/taskscheduler/web/GlobalExceptionHandler.java`

```java
package com.taskscheduler.web;

import com.taskscheduler.scheduler.SchedulerException;
import com.taskscheduler.scheduler.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Central error handling: converts exceptions into a consistent JSON envelope.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(TaskNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage(), "task_not_found", null);
    }

    @ExceptionHandler({SchedulerException.class, IllegalStateException.class,
            IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage(), "bad_request", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException e) {
        Map<String, String> fields = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "invalid"
                                : fe.getDefaultMessage(), (a, b) -> a, LinkedHashMap::new));
        return error(HttpStatus.BAD_REQUEST, "Validation failed", "validation_error", fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "Malformed JSON body", "malformed_json", null);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Map<String, Object>> handleOther(Throwable e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error", "internal_error", null);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message,
                                                      String code, Map<String, String> fields) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", code);
        body.put("message", message);
        if (fields != null) {
            body.put("fields", fields);
        }
        return ResponseEntity.status(status).body(body);
    }
}
```

### A.22 `backend/src/main/java/com/taskscheduler/config/CorsConfig.java`

```java
package com.taskscheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the React dev server (http://localhost:5173) to call the API during
 * development. The Vite dev proxy makes this transparent for most requests,
 * but this matters when the UI is served from another origin.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:3000")
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### A.23 `backend/src/main/resources/application.yml`

```yaml
# =====================================================================
# Async Task Scheduler - application configuration
# =====================================================================
server:
  port: 8080

spring:
  application:
    name: async-task-scheduler
  jackson:
    default-property-inclusion: non_null

scheduler:
  # Number of worker threads that actually execute jobs
  workers: 4
  # Capacity of the internal priority queue (bounded)
  queue-capacity: 2048
  # Default execution timeout per task (ms)
  default-timeout-ms: 60000
  # Default number of automatic retries on failure
  max-retries: 2
  # SSE heartbeat interval (ms) - keeps dashboard connections alive
  heartbeat-ms: 15000

logging:
  level:
    root: INFO
    com.taskscheduler: DEBUG
```

### A.24 `backend/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.taskscheduler</groupId>
    <artifactId>async-task-scheduler</artifactId>
    <version>1.0.0</version>
    <name>Async Task Scheduler</name>
    <description>Asynchronous Task Scheduler backend - 2nd Year project built with advanced Java</description>

    <properties>
        <java.version>17</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Web: REST controllers + Server-Sent-Events (SseEmitter) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Bean validation (jakarta.validation) for request DTOs -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Test support: JUnit 5, AssertJ, Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Polling assertions for integration tests -->
        <dependency>
            <groupId>org.awaitility</groupId>
            <artifactId>awaitility</artifactId>
            <version>4.2.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### A.25 `backend/src/test/java/com/taskscheduler/TaskSchedulerIntegrationTest.java`

```java
package com.taskscheduler;

import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskStatus;
import com.taskscheduler.common.TaskType;
import com.taskscheduler.model.TaskRecord;
import com.taskscheduler.model.TaskSubmitRequest;
import com.taskscheduler.scheduler.TaskSchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end test of the scheduler service on a real Spring context.
 * Verifies the whole pipeline: submit → queue → worker → terminal state.
 */
@SpringBootTest(properties = "scheduler.workers=1")
class TaskSchedulerIntegrationTest {

    @Autowired
    private TaskSchedulerService scheduler;

    @Test
    void submittedTaskRunsToCompletionAsynchronously() {
        TaskRecord task = scheduler.submit(new TaskSubmitRequest(
                "integration-delay",
                TaskType.DELAY,
                TaskPriority.NORMAL,
                0,
                10_000,
                0,
                0,
                java.util.Map.of("durationMs", 250)));

        assertThat(task.status()).isIn(TaskStatus.PENDING, TaskStatus.QUEUED,
                TaskStatus.RUNNING);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(task.id()).status())
                        .isEqualTo(TaskStatus.COMPLETED));

        assertThat(scheduler.require(task.id()).result()).contains("Simulated I/O wait");
        assertThat(scheduler.stats().completed()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void highPriorityTaskExecutesBeforeEarlierSubmittedNormalTask() {
        // Busy the single worker with a long task first (delay 0).
        scheduler.submit(new TaskSubmitRequest(
                "slow", TaskType.DELAY, TaskPriority.NORMAL, 0, 30_000, 0, 0,
                java.util.Map.of("durationMs", 1_500)));

        // NORMAL is SUBMITTED FIRST (delay 300 ms) so it is enqueued later ...
        TaskRecord normal = scheduler.submit(new TaskSubmitRequest(
                "normal-first", TaskType.DELAY, TaskPriority.NORMAL, 300, 30_000, 0, 0,
                java.util.Map.of("durationMs", 300)));
        // ... while CRITICAL is submitted SECOND but dispatched earlier (delay 100 ms).
        // While slow occupies the worker, the priority queue receives critical at t=100
        // and normal at t=300, so the worker pool picks up CRITICAL first.
        // With a single worker and well-separated delays this is deterministic.
        TaskRecord critical = scheduler.submit(new TaskSubmitRequest(
                "critical-second", TaskType.DELAY, TaskPriority.CRITICAL, 100, 30_000, 0, 0,
                java.util.Map.of("durationMs", 300)));

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(normal.id()).status())
                        .isEqualTo(TaskStatus.COMPLETED));
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(critical.id()).status())
                        .isEqualTo(TaskStatus.COMPLETED));

        // CRITICAL must have finished before NORMAL (its dispatch slot fired first).
        assertThat(scheduler.require(critical.id()).finishedAtMillis())
                .isLessThan(scheduler.require(normal.id()).finishedAtMillis());
    }

    @Test
    void retriesExhaustedTaskEndsFailed() {
        TaskRecord doomed = scheduler.submit(new TaskSubmitRequest(
                "doomed", TaskType.DELAY, TaskPriority.NORMAL, 0, 30_000, 1, 100,
                java.util.Map.of("durationMs", 100)));

        await().atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(doomed.id()).status())
                        .isEqualTo(TaskStatus.FAILED));

        assertThat(scheduler.require(doomed.id()).attempt()).isEqualTo(2); // 1 + 1 retry
    }

    @Test
    void pendingTaskCanBeCancelledBeforeExecution() {
        TaskRecord task = scheduler.submit(new TaskSubmitRequest(
                "cancel-me", TaskType.DELAY, TaskPriority.LOW, 120_000, 30_000, 0, 0,
                java.util.Map.of("durationMs", 100)));

        assertThat(scheduler.cancel(task.id())).isTrue();
        assertThat(scheduler.require(task.id()).status())
                .isEqualTo(TaskStatus.CANCELLED);
    }
}
```

### A.26 `backend/src/test/java/com/taskscheduler/scheduler/PriorityTaskQueueTest.java`

```java
package com.taskscheduler.scheduler;

import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskType;
import com.taskscheduler.model.TaskRecord;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deterministic (no threads, no timing) tests of the priority-queue semantics
 * that {@link QueuedTask#compareTo} + {@link PriorityBlockingQueue} provide:
 * highest rank first, FIFO within a rank.
 */
class PriorityTaskQueueTest {

    private static long seq = 0;

    @Test
    void dequeuesHighestPriorityFirstRegardlessOfInsertOrder() throws InterruptedException {
        PriorityTaskQueue queue = new PriorityTaskQueue(16);
        // Inserted deliberately out of priority order.
        queue.put(new QueuedTask(task("low", TaskPriority.LOW), nextSeq(), now()));
        queue.put(new QueuedTask(task("critical", TaskPriority.CRITICAL), nextSeq(), now()));
        queue.put(new QueuedTask(task("normal", TaskPriority.NORMAL), nextSeq(), now()));
        queue.put(new QueuedTask(task("high", TaskPriority.HIGH), nextSeq(), now()));

        assertThat(queue.take().task().priority()).isEqualTo(TaskPriority.CRITICAL);
        assertThat(queue.take().task().priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(queue.take().task().priority()).isEqualTo(TaskPriority.NORMAL);
        assertThat(queue.take().task().priority()).isEqualTo(TaskPriority.LOW);

        assertThat(queue.depth()).isZero();
    }

    @Test
    void respectsFifoOrderWithinTheSamePriority() throws InterruptedException {
        PriorityTaskQueue queue = new PriorityTaskQueue(16);
        queue.put(new QueuedTask(task("first", TaskPriority.NORMAL), nextSeq(), now()));
        queue.put(new QueuedTask(task("second", TaskPriority.NORMAL), nextSeq(), now()));

        assertThat(queue.take().task().name()).isEqualTo("first");
        assertThat(queue.take().task().name()).isEqualTo("second");
    }

    @Test
    void criticalPrecedesNormalEvenWhenInsertedLater() throws InterruptedException {
        PriorityTaskQueue queue = new PriorityTaskQueue(16);
        // normal inserted BEFORE critical (lower sequence number)
        TaskRecord normal = task("normal-first", TaskPriority.NORMAL);
        TaskRecord critical = task("critical-second", TaskPriority.CRITICAL);
        queue.put(new QueuedTask(normal, nextSeq(), now()));
        queue.put(new QueuedTask(critical, nextSeq(), now()));

        assertThat(queue.take().task()).isSameAs(critical);   // higher rank wins
        assertThat(queue.take().task()).isSameAs(normal);
    }

    private static TaskRecord task(String name, TaskPriority priority) {
        return new TaskRecord("id-" + name, name, TaskType.DELAY, priority,
                0, 60_000, 0, 0, Map.of());
    }

    private static long nextSeq() {
        return ++seq;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
```

---

## Appendix B — Complete Frontend Source Code

Every frontend file is reproduced verbatim below.

### B.1 `frontend/index.html`

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Async Task Scheduler · Dashboard</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
```

### B.2 `frontend/package.json`

```json
{
  "name": "async-task-scheduler-frontend",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "description": "React dashboard for the Async Task Scheduler (2nd Year project)",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.2.1",
    "vite": "^5.1.0"
  }
}
```

### B.3 `frontend/vite.config.js`

```js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev server proxies /api/* to the Spring Boot backend on :8080,
// so the UI can use relative URLs with no CORS pain.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: 'localhost',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
  },
});
```

### B.4 `frontend/src/main.jsx`

```jsx
import { useState, useEffect, useRef } from 'react';
import { createRoot } from 'react-dom/client';
import { api, openEventStream } from './api.js';
import TaskForm from './components/TaskForm.jsx';
import TaskTable from './components/TaskTable.jsx';
import StatsBar from './components/StatsBar.jsx';
import TaskDetailModal from './components/TaskDetailModal.jsx';
import './styles.css';

const FILTERS = ['ALL', 'PENDING', 'QUEUED', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED', 'TIMED_OUT'];

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [stats, setStats] = useState(null);
  const [filter, setFilter] = useState('ALL');
  const [selectedId, setSelectedId] = useState(null);
  const [toast, setToast] = useState(null);
  const [connected, setConnected] = useState(false);

  const mapRef = useRef(new Map());
  const statsBusy = useRef(false);
  const statsDirty = useRef(false);
  const toastTimer = useRef(null);

  function commit(updatedMap) {
    setTasks([...updatedMap.values()].sort((a, b) => b.submittedAtMillis - a.submittedAtMillis));
  }

  function refreshStatsSoon() {
    if (statsBusy.current) {
      statsDirty.current = true;
      return;
    }
    statsBusy.current = true;
    api.stats()
      .then((s) => setStats(s))
      .catch(() => {})
      .finally(() => {
        statsBusy.current = false;
        if (statsDirty.current) {
          statsDirty.current = false;
          refreshStatsSoon();
        }
      });
  }

  function upsert(task) {
    mapRef.current.set(task.id, task);
    commit(mapRef.current);
    refreshStatsSoon();
  }

  function showToast(message, kind = 'info') {
    setToast({ message, kind });
    if (toastTimer.current) clearTimeout(toastTimer.current);
    toastTimer.current = setTimeout(() => setToast(null), 4000);
  }

  useEffect(() => {
    api.list().then((list) => {
      const m = new Map();
      list.forEach((t) => m.set(t.id, t));
      mapRef.current = m;
      commit(m);
      refreshStatsSoon();
    }).catch((e) => showToast(`Backend unreachable: ${e.message}`, 'err'));

    const stream = openEventStream(upsert);
    setConnected(true);

    const ticker = setInterval(refreshStatsSoon, 5000);
    return () => {
      stream.close();
      clearInterval(ticker);
      if (toastTimer.current) clearTimeout(toastTimer.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const selected = tasks.find((t) => t.id === selectedId);
  const visible = filter === 'ALL' ? tasks : tasks.filter((t) => t.status === filter);

  return (
    <div className="app">
      <header className="app-header">
        <div className="brand">
          <span className="logo">⚡</span>
          <div>
            <h1>Async Task Scheduler</h1>
            <p>Java concurrency core · React dashboard · live via Server-Sent Events</p>
          </div>
        </div>
        <div className={`conn ${connected ? 'conn--ok' : 'conn--down'}`}>
          <span className="dot" />
          {connected ? 'live' : 'connecting…'}
        </div>
      </header>

      <StatsBar stats={stats} />

      <div className="layout">
        <aside className="panel">
          <TaskForm onSubmit={handleSubmit} onBurst={handleBurst} />
          <div className="panel-notes">
            <h4>What is happening under the hood?</h4>
            <ul>
              <li>Tasks wait in a <strong>PriorityBlockingQueue</strong></li>
              <li><strong>CRITICAL</strong> tasks jump the queue (priority + FIFO)</li>
              <li>Your <strong>delay</strong> is fired by a <em>ScheduledExecutorService</em></li>
              <li>A <strong>timeout guard</strong> marks slow jobs <em>TIMED_OUT</em></li>
              <li><strong>failRate &gt; 0</strong> exercises automatic retries</li>
            </ul>
          </div>
        </aside>

        <main className="panel panel--wide">
          <div className="toolbar">
            <h2>Tasks</h2>
            <div className="filter-row">
              {FILTERS.map((f) => (
                <button
                  key={f}
                  className={`filter-chip ${f === filter ? 'filter-chip--active' : ''}`}
                  onClick={() => setFilter(f)}
                >
                  {f}
                </button>
              ))}
            </div>
          </div>
          <TaskTable
            tasks={visible}
            onSelect={setSelectedId}
            onCancel={handleCancel}
            onRetry={handleRetry}
          />
        </main>
      </div>

      <TaskDetailModal
        task={selected}
        onClose={() => setSelectedId(null)}
        onCancel={handleCancel}
        onRetry={handleRetry}
      />

      {toast && <div className={`toast toast--${toast.kind}`}>{toast.message}</div>}
    </div>
  );

  async function handleSubmit(payload) {
    try {
      const created = await api.submit(payload);
      showToast(`Submitted "${created.name}" · ${created.id.slice(0, 8)}…`, 'ok');
    } catch (e) {
      showToast(`Submit failed: ${e.message}`, 'err');
    }
  }

  async function handleCancel(id) {
    try {
      await api.cancel(id);
      showToast('Cancel request sent', 'ok');
    } catch (e) {
      showToast(`Cancel failed: ${e.message}`, 'err');
    }
  }

  async function handleRetry(id) {
    try {
      await api.retry(id);
      showToast('Task requeued for retry', 'ok');
    } catch (e) {
      showToast(`Retry failed: ${e.message}`, 'err');
    }
  }

  async function handleBurst(count) {
    try {
      const res = await api.burst(count);
      showToast(res.message, 'ok');
    } catch (e) {
      showToast(`Burst failed: ${e.message}`, 'err');
    }
  }
}

// ---------------------------------------------------------------
// Entry point: mount the dashboard into #root (React 18 createRoot)
// ---------------------------------------------------------------
const rootElement = document.getElementById('root');
if (rootElement) {
  createRoot(rootElement).render(<App />);
}
```

### B.5 `frontend/src/api.js`

```js
// Thin fetch wrapper for the scheduler REST API.

const BASE = '/api';

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    let detail;
    try {
      detail = await res.json();
    } catch {
      detail = { message: `HTTP ${res.status}` };
    }
    throw new Error(detail.message || `HTTP ${res.status}`);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  list: () => request('/tasks'),
  stats: () => request('/stats'),
  get: (id) => request(`/tasks/${id}`),
  submit: (payload) =>
    request('/tasks', { method: 'POST', body: JSON.stringify(payload) }),
  cancel: (id) => request(`/tasks/${id}`, { method: 'DELETE' }),
  retry: (id) => request(`/tasks/${id}/retry`, { method: 'POST' }),
  burst: (count) => request(`/tasks/burst?count=${count}`, { method: 'POST' }),
  health: () => request('/health'),
};

/**
 * Opens the Server-Sent-Events stream. onTask is called with a TaskView JSON
 * on every transition; returns a handle with close() for cleanup.
 */
export function openEventStream(onTask) {
  const source = new EventSource(`${BASE}/events`);
  source.addEventListener('task', (event) => {
    try {
      onTask(JSON.parse(event.data));
    } catch {
      // transient decode error - ignore this frame
    }
  });
  return {
    close: () => source.close(),
    readyState: () => source.readyState,
  };
}
```

### B.6 `frontend/src/status.js`

```js
// Live status helpers shared across components.

export const STATUS_ORDER = [
  'PENDING',
  'QUEUED',
  'RUNNING',
  'COMPLETED',
  'FAILED',
  'CANCELLED',
  'TIMED_OUT',
];

export const STATUS_META = {
  PENDING:   { label: 'Pending',   color: '#8e8ea8', pulse: false },
  QUEUED:    { label: 'Queued',    color: '#5b7fd6', pulse: false },
  RUNNING:   { label: 'Running',   color: '#2fae5e', pulse: true },
  COMPLETED: { label: 'Done',      color: '#1f8f4d', pulse: false },
  FAILED:    { label: 'Failed',    color: '#d64545', pulse: false },
  CANCELLED: { label: 'Cancelled', color: '#9b6b3f', pulse: false },
  TIMED_OUT: { label: 'Timed out', color: '#c77b2c', pulse: false },
};

export const PRIORITY_META = {
  LOW:      { rank: 0, label: 'LOW',      color: '#64748b' },
  NORMAL:   { rank: 1, label: 'NORMAL',   color: '#3b82f6' },
  HIGH:     { rank: 2, label: 'HIGH',     color: '#f59e0b' },
  CRITICAL: { rank: 3, label: 'CRITICAL', color: '#ef4444' },
};

export const TYPE_META = {
  DELAY:        { label: 'Delay',        color: '#22c55e' },
  COMPUTE:      { label: 'Compute',      color: '#a855f7' },
  FETCH:        { label: 'Fetch',        color: '#06b6d4' },
  BUILD_REPORT: { label: 'Report',       color: '#f97316' },
};

export function formatMillis(ms) {
  if (!ms || ms <= 0) return '—';
  if (ms < 1000) return `${ms} ms`;
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)} s`;
  return `${(ms / 60_000).toFixed(1)} min`;
}

export function formatClock(epochMillis) {
  if (!epochMillis || epochMillis <= 0) return '—';
  const d = new Date(epochMillis);
  const pad = (n) => String(n).padStart(2, '0');
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}
```

### B.7 `frontend/src/components/TaskForm.jsx`

```jsx
import { useState } from 'react';
import { PRIORITY_META } from '../status.js';

const PRIORITIES = ['LOW', 'NORMAL', 'HIGH', 'CRITICAL'];
const TYPES = ['DELAY', 'COMPUTE', 'FETCH', 'BUILD_REPORT'];

// Params tailored per job type so the form stays approachable.
const PARAM_UI = {
  DELAY: { durationMs: { kind: 'number', label: 'Duration (ms)', min: 100, max: 120000, step: 100, value: 5000 } },
  COMPUTE: { limit: { kind: 'number', label: 'Sieve limit', min: 10000, max: 50000000, step: 10000, value: 2000000 } },
  FETCH: { url: { kind: 'text', label: 'URL to simulate', value: 'https://example.com/api/v1/data' } },
  BUILD_REPORT: {},
};

function initialForm() {
  return {
    name: '',
    type: 'DELAY',
    priority: 'NORMAL',
    delayMillis: 0,
    timeoutMillis: 60000,
    maxRetries: 0,
    failRate: 0,
    params: {},
  };
}

export default function TaskForm({ onSubmit, onBurst }) {
  const [form, setForm] = useState(initialForm);

  const params = form.params || {};

  function setParam(key, value) {
    setForm((prev) => {
      const next = { ...prev.params, [key]: value };
      // keep it clean when switching types
      return { ...prev, params: next };
    });
  }

  function submit(e) {
    e.preventDefault();
    const payload = {
      name: form.name || `${form.type}-task`,
      type: form.type,
      priority: form.priority,
      delayMillis: Number(form.delayMillis),
      timeoutMillis: Number(form.timeoutMillis),
      maxRetries: Number(form.maxRetries),
      failRate: Number(form.failRate),
      params: { ...form.params },
    };
    onSubmit(payload);
  }

  function renderParamInputs() {
    const ui = PARAM_UI[form.type] || {};
    return Object.entries(ui).map(([key, spec]) =>
      spec.kind === 'number'
        ? (
          <label className="field">
            <span>{spec.label}</span>
            <input
              type="number"
              min={spec.min}
              max={spec.max}
              step={spec.step}
              value={params[key] ?? spec.value}
              onChange={(e) => setParam(key, Number(e.target.value))}
            />
            <small>{spec.min} – {spec.max}</small>
          </label>
        )
        : (
          <label className="field field--wide">
            <span>{spec.label}</span>
            <input
              type="text"
              value={params[key] ?? spec.value}
              onChange={(e) => setParam(key, e.target.value)}
            />
          </label>
        ));
  }

  return (
    <form className="task-form" onSubmit={submit}>
      <h3>Submit a task</h3>

      <label className="field field--wide">
        <span>Name</span>
        <input
          type="text"
          value={form.name}
          placeholder="e.g. nightly-report"
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
      </label>

      <label className="field">
        <span>Type</span>
        <select
          value={form.type}
          onChange={(e) => setForm({ ...form, type: e.target.value, params: {} })}
        >
          {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
        </select>
      </label>

      <label className="field">
        <span>Priority</span>
        <select
          value={form.priority}
          onChange={(e) => setForm({ ...form, priority: e.target.value })}
        >
          {PRIORITIES.map((p) => (
            <option key={p} value={p}>{p} ({PRIORITY_META[p].rank})</option>
          ))}
        </select>
      </label>

      <label className="field">
        <span>Start delay (ms)</span>
        <input
          type="number" min={0} max={86400000} step={100}
          value={form.delayMillis}
          onChange={(e) => setForm({ ...form, delayMillis: e.target.value })}
        />
      </label>

      <label className="field">
        <span>Timeout (ms)</span>
        <input
          type="number" min={100} max={3600000} step={1000}
          value={form.timeoutMillis}
          onChange={(e) => setForm({ ...form, timeoutMillis: e.target.value })}
        />
      </label>

      <label className="field">
        <span>Max retries</span>
        <input
          type="number" min={0} max={10}
          value={form.maxRetries}
          onChange={(e) => setForm({ ...form, maxRetries: e.target.value })}
        />
      </label>

      {renderParamInputs()}

      <label className="field field--wide">
        <span>Failure rate: {form.failRate}%</span>
        <input
          type="range" min={0} max={100} step={5}
          value={form.failRate}
          onChange={(e) => setForm({ ...form, failRate: e.target.value })}
        />
        <small>Randomly fails this % of executions (demo retries)</small>
      </label>

      <div className="form-actions">
        <button className="btn btn--primary" type="submit">▶ Submit task</button>
        <button
          className="btn btn--ghost"
          type="button"
          onClick={() => onBurst(10)}
          title="Producer-consumer demo: 10 synthetic tasks"
        >
          🎲 Burst ×10
        </button>
      </div>
    </form>
  );
}
```

### B.8 `frontend/src/components/TaskTable.jsx`

```jsx
import StatusBadge from './StatusBadge.jsx';
import {
  TYPE_META,
  PRIORITY_META,
  formatMillis,
  formatClock,
} from '../status.js';

export default function TaskTable({ tasks, onSelect, onCancel, onRetry }) {
  if (!tasks.length) {
    return <div className="table-empty">No tasks yet — submit one on the left.</div>;
  }
  return (
    <table className="task-table">
      <thead>
        <tr>
          <th>Priority</th>
          <th>Name</th>
          <th>Type</th>
          <th>Status</th>
          <th>Attempt</th>
          <th>Elapsed</th>
          <th>Finished</th>
          <th>Result / Error</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {tasks.map((t) => (
          <tr
            key={t.id}
            onClick={() => onSelect(t.id)}
            className={t.status === 'RUNNING' ? 'row--running' : ''}
          >
            <td>
              <span
                className="prio-pill"
                style={{
                  background: PRIORITY_META[t.priority].color + '1f',
                  color: PRIORITY_META[t.priority].color,
                }}
              >
                {t.priority}
              </span>
            </td>
            <td className="cell--name">
              <span className="mono">{short(t.id)}</span>
              <strong>{t.name}</strong>
            </td>
            <td>
              <span className="type-pill" style={{ color: TYPE_META[t.type].color }}>
                {TYPE_META[t.type].label}
              </span>
            </td>
            <td><StatusBadge status={t.status} progress={t.progress} /></td>
            <td className="mono">{t.attempt}</td>
            <td className="mono">{formatMillis(t.elapsedMillis)}</td>
            <td className="mono">{formatClock(t.finishedAtMillis)}</td>
            <td className="cell--result" title={t.result || t.error}>
              {t.result || t.error || '…'}
            </td>
            <td className="cell--actions">
              {canCancel(t.status) && (
                <button
                  className="mini-btn mini-btn--danger"
                  title="Cancel task"
                  onClick={(e) => { e.stopPropagation(); onCancel(t.id); }}
                >
                  ✕
                </button>
              )}
              {canRetry(t.status) && (
                <button
                  className="mini-btn mini-btn--retry"
                  title="Retry task"
                  onClick={(e) => { e.stopPropagation(); onRetry(t.id); }}
                >
                  ⟳
                </button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function canCancel(status) {
  return ['PENDING', 'QUEUED', 'RUNNING'].includes(status);
}

function canRetry(status) {
  return ['FAILED', 'TIMED_OUT'].includes(status);
}

function short(id) {
  return `${id.slice(0, 8)}…`;
}
```

### B.9 `frontend/src/components/TaskDetailModal.jsx`

```jsx
import StatusBadge from './StatusBadge.jsx';
import { PRIORITY_META, TYPE_META, formatClock, formatMillis } from '../status.js';

function row(label, value) {
  return (
    <div className="kv">
      <span className="kv-key">{label}</span>
      <span className="kv-value">{value}</span>
    </div>
  );
}

export default function TaskDetailModal({ task, onClose, onCancel, onRetry }) {
  if (!task) return null;
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{task.name}</h3>
          <button className="mini-btn" onClick={onClose}>✕</button>
        </div>

        <div className="modal-body">
          <div className="kv-grid">
            {row('ID', <span className="mono">{task.id}</span>)}
            {row('Status', <StatusBadge status={task.status} progress={task.progress} />)}
            {row('Type', TYPE_META[task.type].label)}
            {row('Priority', `${task.priority} (rank ${PRIORITY_META[task.priority].rank})`)}
            {row('Attempt', task.attempt)}
            {row('Worker', <span className="mono">{task.workerId || '—'}</span>)}
            {row('Submitted', formatClock(task.submittedAtMillis))}
            {row('Started', formatClock(task.startedAtMillis))}
            {row('Finished', formatClock(task.finishedAtMillis))}
            {row('Elapsed', formatMillis(task.elapsedMillis))}
            {row('Delay', `${task.delayMillis} ms`)}
            {row('Timeout', `${task.timeoutMillis} ms`)}
            {row('Max retries', task.maxRetries)}
            {row('Fail rate', `${task.failRate}%`)}
          </div>

          <div className="modal-section">
            <h4>Parameters</h4>
            <pre>{JSON.stringify(task.params, null, 2)}</pre>
          </div>

          {task.result && (
            <div className="modal-section">
              <h4>Result</h4>
              <pre>{task.result}</pre>
            </div>
          )}
          {task.error && (
            <div className="modal-section modal-section--error">
              <h4>Error</h4>
              <pre>{task.error}</pre>
            </div>
          )}

          <div className="modal-section">
            <h4>Event log (audit trail)</h4>
            <div className="event-log">
              {task.eventLog.map((entry) => (
                <div className="event-line" key={entry.atMillis + entry.status}>
                  <span className="mono ev-time">{formatClock(entry.atMillis)}</span>
                  <span className="ev-status">{entry.status}</span>
                  <span className="ev-detail">{entry.detail}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="modal-footer">
          {['PENDING', 'QUEUED', 'RUNNING'].includes(task.status) && (
            <button className="btn btn--danger" onClick={() => onCancel(task.id)}>
              Cancel task
            </button>
          )}
          {['FAILED', 'TIMED_OUT'].includes(task.status) && (
            <button className="btn btn--primary" onClick={() => onRetry(task.id)}>
              ⟳ Retry
            </button>
          )}
          <button className="btn btn--ghost" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}
```

### B.10 `frontend/src/components/StatsBar.jsx`

```jsx
import { formatMillis, STATUS_META } from '../status.js';

const CARD_ORDER = [
  ['total', 'Total tasks', '#e2e8f0'],
  ['pending', 'Pending', '#8e8ea8'],
  ['queued', 'Queued', '#5b7fd6'],
  ['running', 'Running', '#2fae5e'],
  ['completed', 'Completed', '#1f8f4d'],
  ['failed', 'Failed', '#d64545'],
  ['cancelled', 'Cancelled', '#9b6b3f'],
  ['timedOut', 'Timed out', '#c77b2c'],
];

function card(key, label, color, value) {
  return (
    <div className="stat-card" key={key}>
      <span className="stat-value" style={{ color }}>{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  );
}

export default function StatsBar({ stats }) {
  if (!stats) return null;
  const cards = CARD_ORDER.map(([key, label, color]) =>
    card(key, label, color, stats[key] ?? 0));
  return (
    <div className="stats-bar">
      <div className="stats-cards">
        {cards}
        {card('avg', 'Avg latency', '#3b82f6', formatMillis(stats.avgLatencyMillis))}
        {card('p95', 'P95 latency', '#f59e0b', formatMillis(stats.p95LatencyMillis))}
        {card('depth', 'Queue depth', '#06b6d4', `${stats.queueDepth} / ${stats.workerCount} workers`)}
      </div>
      <div className="live-tick">
        <span className="dot dot--live" /> LIVE
      </div>
    </div>
  );
}
```

### B.11 `frontend/src/components/StatusBadge.jsx`

```jsx
import { STATUS_META } from '../status.js';

export default function StatusBadge({ status, progress }) {
  const meta = STATUS_META[status] || {
    label: status,
    color: '#888',
    pulse: false,
  };
  const cls = meta.pulse ? 'badge badge--pulse' : 'badge';
  return (
    <span
      className={cls}
      style={{ background: meta.color + '1f', color: meta.color }}
      title={status}
    >
      <span
        className="dot"
        style={{ background: meta.color, boxShadow: `0 0 6px ${meta.color}` }}
      />
      {meta.label}
      {status === 'RUNNING' && progress != null ? ` ${progress}%` : ''}
    </span>
  );
}
```

### B.12 `frontend/src/styles.css`

```css
:root {
  --bg: #0d1117;
  --bg-panel: #161b22;
  --bg-raised: #1c2333;
  --border: #283041;
  --text: #e6edf3;
  --text-dim: #8b98a9;
  --accent: #58a6ff;
  --mono: 'SF Mono', 'Fira Code', 'JetBrains Mono', Menlo, Consolas, monospace;
}

* { box-sizing: border-box; }

html, body, #root { height: 100%; }

body {
  margin: 0;
  background: radial-gradient(1200px 600px at 20% -10%, #16233b 0%, var(--bg) 60%);
  color: var(--text);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 14px;
}

.app { max-width: 1440px; margin: 0 auto; padding: 20px; }

.mono { font-family: var(--mono); font-size: 12px; color: var(--text-dim); }

/* ---------------- header ---------------- */
.app-header { display: flex; justify-content: space-between; align-items: center; padding: 6px 4px 16px; }
.brand { display: flex; gap: 12px; align-items: center; }
.logo { font-size: 30px; filter: drop-shadow(0 0 8px var(--accent)); }
.brand h1 { margin: 0; font-size: 22px; letter-spacing: 0.3px; }
.brand p { margin: 2px 0 0; color: var(--text-dim); font-size: 12.5px; }

.conn { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--text-dim); }
.conn--ok { color: #4ade80; }
.dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; background: currentColor; }
.dot--live { background: #4ade80; box-shadow: 0 0 8px #4ade80; animation: blink 1.6s infinite; }
@keyframes blink { 50% { opacity: 0.35; } }

/* ---------------- panels ---------------- */
.layout { display: grid; grid-template-columns: 360px 1fr; gap: 16px; margin-top: 16px; }
@media (max-width: 980px) { .layout { grid-template-columns: 1fr; } }

.panel { background: var(--bg-panel); border: 1px solid var(--border); border-radius: 14px; padding: 18px; }
.panel--wide { overflow: hidden; }

/* ---------------- stats ---------------- */
.stats-bar { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.stats-cards { display: flex; flex-wrap: wrap; gap: 10px; }
.stat-card {
  background: var(--bg-panel); border: 1px solid var(--border);
  border-radius: 12px; min-width: 96px; padding: 10px 14px;
  display: flex; flex-direction: column;
}
.stat-value { font-size: 21px; font-weight: 700; font-family: var(--mono); }
.stat-label { font-size: 11px; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.6px; margin-top: 2px; }
.live-tick { display: flex; align-items: center; gap: 8px; font-size: 11px; color: #4ade80; letter-spacing: 1.2px; font-weight: 600; }

/* ---------------- form ---------------- */
.task-form { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.task-form h3 { grid-column: 1 / -1; margin: 0 0 4px; font-size: 15px; }
.field { display: flex; flex-direction: column; gap: 5px; }
.field--wide { grid-column: 1 / -1; }
.field span { font-size: 11.5px; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.5px; }
.field small { color: var(--text-dim); font-size: 10.5px; }
input, select {
  background: var(--bg-raised); color: var(--text);
  border: 1px solid var(--border); border-radius: 8px;
  padding: 8px 10px; font-size: 13px; outline: none;
}
input:focus, select:focus { border-color: var(--accent); }
input[type='range'] { padding: 0; accent-color: var(--accent); }
.form-actions { grid-column: 1 / -1; display: flex; gap: 10px; margin-top: 6px; }

.btn { border: 1px solid transparent; border-radius: 9px; padding: 9px 14px; font-size: 13px; font-weight: 600; cursor: pointer; transition: filter 0.15s; }
.btn:hover { filter: brightness(1.15); }
.btn--primary { background: #1f6feb; color: #fff; }
.btn--danger { background: #c62626; color: #fff; }
.btn--ghost { background: transparent; border-color: var(--border); color: var(--text); }

.panel-notes { border-top: 1px dashed var(--border); margin-top: 16px; padding-top: 12px; }
.panel-notes h4 { margin: 0 0 8px; font-size: 12px; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.6px; }
.panel-notes ul { margin: 0; padding-left: 18px; color: var(--text-dim); line-height: 1.7; font-size: 12.5px; }
/* ---------------- toolbar & filters ---------------- */
.toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
.toolbar h2 { margin: 0; font-size: 16px; }
.filter-row { display: flex; gap: 6px; flex-wrap: wrap; }
.filter-chip {
  background: var(--bg-raised); border: 1px solid var(--border);
  color: var(--text-dim); border-radius: 999px;
  font-size: 11px; padding: 5px 10px; cursor: pointer;
}
.filter-chip--active { background: #1f6feb; color: white; border-color: #1f6feb; }

/* ---------------- table ---------------- */
.task-table { width: 100%; border-collapse: collapse; }
.task-table th {
  text-align: left; font-size: 10.5px; text-transform: uppercase;
  letter-spacing: 0.7px; color: var(--text-dim);
  padding: 8px 10px; border-bottom: 1px solid var(--border);
}
.task-table td { padding: 9px 10px; border-bottom: 1px solid rgba(40, 48, 65, 0.55); vertical-align: middle; }
.task-table tbody tr { cursor: pointer; }
.task-table tbody tr:hover { background: rgba(88, 166, 255, 0.06); }
.row--running { background: rgba(47, 174, 94, 0.05); }
.cell--name { display: flex; flex-direction: column; gap: 2px; }
.cell--result { max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: var(--text-dim); font-size: 12px; }
.table-empty { padding: 42px 0; text-align: center; color: var(--text-dim); }

.badge { display: inline-flex; align-items: center; gap: 7px; padding: 4px 10px; border-radius: 999px; font-size: 11.5px; font-weight: 600; white-space: nowrap; }
.badge .dot { width: 6px; height: 6px; }
.badge--pulse { animation: blink 1.5s infinite; }

.prio-pill, .type-pill { padding: 3px 9px; border-radius: 6px; font-size: 11px; font-weight: 600; font-family: var(--mono); }
.cell--actions { display: flex; gap: 6px; }
.mini-btn {
  background: var(--bg-raised); border: 1px solid var(--border);
  color: var(--text-dim); width: 26px; height: 26px;
  border-radius: 7px; cursor: pointer; font-size: 13px;
}
.mini-btn--danger:hover { border-color: #d64545; color: #d64545; }
.mini-btn--retry:hover { border-color: #58a6ff; color: #58a6ff; }

/* ---------------- modal ---------------- */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(3, 6, 12, 0.72);
  display: flex; align-items: center; justify-content: center; z-index: 40;
}
.modal {
  background: var(--bg-panel); border: 1px solid var(--border);
  border-radius: 16px; width: min(720px, 92vw); max-height: 86vh;
  display: flex; flex-direction: column;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.5);
}
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--border); }
.modal-header h3 { margin: 0; font-size: 16px; }
.modal-body { padding: 16px 20px; overflow-y: auto; }
.kv-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.kv { display: flex; flex-direction: column; gap: 2px; }
.kv-key { font-size: 10.5px; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.6px; }
.kv-value { font-size: 13px; }
.modal-section { margin-top: 18px; }
.modal-section h4 { margin: 0 0 8px; font-size: 11px; color: var(--text-dim); text-transform: uppercase; letter-spacing: 0.6px; }
.modal-section pre {
  background: var(--bg-raised); border: 1px solid var(--border);
  border-radius: 10px; padding: 12px; font-family: var(--mono);
  font-size: 12px; overflow-x: auto; white-space: pre-wrap; margin: 0;
}
.modal-section--error pre { color: #f87171; }
.event-log { border: 1px solid var(--border); border-radius: 10px; padding: 8px 12px; max-height: 220px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.event-line { display: grid; grid-template-columns: 82px 90px 1fr; gap: 10px; font-size: 12px; }
.ev-status { font-weight: 600; font-size: 11px; }
.ev-detail { color: var(--text-dim); }
.modal-footer { display: flex; gap: 10px; justify-content: flex-end; padding: 14px 20px; border-top: 1px solid var(--border); }

/* ---------------- toast ---------------- */
.toast {
  position: fixed; bottom: 24px; right: 24px;
  background: var(--bg-raised); border: 1px solid var(--border);
  border-left: 3px solid var(--accent); border-radius: 10px;
  padding: 12px 18px; font-size: 13px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4); z-index: 60;
}
.toast--ok { border-left-color: #4ade80; }
.toast--err { border-left-color: #d64545; }
```


---

## Appendix C — Sample API Requests and Responses

### C.1 Submit a DELAY task (201 Created)

Request:

```
POST /api/tasks
Content-Type: application/json

{
  "type": "DELAY",
  "name": "Demo delay",
  "priority": "NORMAL",
  "delayMillis": 0,
  "timeoutMillis": 30000,
  "maxRetries": 0,
  "durationMillis": 2000
}
```

Response:

```json
{
  "id": "3f9c1a2b-4d5e-6f70-8091-a2b3c4d5e6f7",
  "name": "Demo delay",
  "type": "DELAY",
  "priority": "NORMAL",
  "status": "QUEUED",
  "attempt": 0,
  "maxRetries": 0,
  "timeoutMillis": 30000,
  "createdAt": "2026-09-09T10:00:00Z",
  "startedAt": null,
  "completedAt": null,
  "result": null,
  "error": null,
  "eventLog": ["10:00:00 CREATED type=DELAY priority=NORMAL", "10:00:00 QUEUED seq=41"]
}
```

### C.2 Same task after completion (GET /api/tasks/{id})

```json
{
  "id": "3f9c1a2b-4d5e-6f70-8091-a2b3c4d5e6f7",
  "status": "COMPLETED",
  "startedAt": "2026-09-09T10:00:00Z",
  "completedAt": "2026-09-09T10:00:02Z",
  "result": "Slept 2000 ms on scheduler-worker-2",
  "eventLog": ["10:00:00 CREATED ...", "10:00:00 QUEUED seq=41", "10:00:00 RUNNING worker=scheduler-worker-2", "10:00:02 COMPLETED Slept 2000 ms on scheduler-worker-2"]
}
```

### C.3 Validation error (400)

```
POST /api/tasks  { "type": "DELAY", "timeoutMillis": 10 }
```

```json
{ "error": "VALIDATION_FAILED", "message": "timeoutMillis: must be >= 100" }
```

### C.4 Unknown task (404)

```
GET /api/tasks/00000000-0000-0000-0000-000000000000
```

```json
{ "error": "TASK_NOT_FOUND", "message": "No task with id 00000000-...", "taskId": "00000000-..." }
```

### C.5 Burst submission (POST /api/burst?count=10)

```json
{ "submitted": 10, "ids": ["...", "..."] }
```

### C.6 Statistics (GET /api/stats)

```json
{
  "total": 14, "pending": 0, "queued": 1, "running": 2,
  "completed": 9, "failed": 1, "timeout": 0, "cancelled": 1, "retrying": 0,
  "queueDepth": 1, "workers": 4,
  "avgLatencyMs": 812.4, "p95LatencyMs": 2010.0,
  "byType": { "DELAY": 6, "COMPUTE": 3, "FETCH": 3, "BUILD_REPORT": 2 }
}
```

### C.7 SSE frames (GET /api/events)

```
event: task
data: {"id":"...","status":"PENDING","type":"DELAY","priority":"NORMAL",...}

event: task
data: {"id":"...","status":"QUEUED",...}

event: task
data: {"id":"...","status":"RUNNING","workerId":"scheduler-worker-1",...}

event: task
data: {"id":"...","status":"COMPLETED","result":"Slept 2000 ms ...",...}
```

---

## Appendix D — Screenshots and Operational Notes

### D.1 How to run (development)

Terminal 1 — backend:

```
cd backend
mvn spring-boot:run
```

Wait for `Started AsyncTaskSchedulerApp` on port 8080. Terminal 2 — frontend:

```
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The Vite proxy forwards `/api/*` to `http://localhost:8080`, so no CORS setup is needed in development.

### D.2 How to run (production jars/bundles)

```
cd backend && mvn package -DskipTests && java -jar target/*.jar
cd frontend && npm run build   # serve dist/ with any static server
```

### D.3 Suggested screenshots for the printed report

1. Dashboard on load (empty state + live badge).
2. Dashboard mid-burst (mixed PENDING/QUEUED/RUNNING/COMPLETED rows).
3. Detail modal showing a full event log.
4. Stats bar after a retry demonstration (RETRYING then FAILED/COMPLETED).
5. Terminal showing `mvn test` green (7/7) and `npm run build` clean.

### D.4 Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Blank dark page | Old cached bundle | Hard refresh (Cmd/Ctrl+Shift+R) |
| `ERR_CONNECTION_REFUSED` on /api | Backend not running | Start `mvn spring-boot:run` first |
| Port 8080 busy | Stale Spring process | `lsof -ti:8080 | xargs kill -9` |
| Port 5173 busy | Stale Vite process | `pkill -f vite`, restart `npm run dev` |
| Tasks stuck PENDING | Dispatcher starved (rare) | Restart backend; check `scheduler.workers` |

*End of report.*
