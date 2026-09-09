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
25. Appendix A — Code Availability and Key Snippets
26. Appendix B — Screenshots and Operational Notes

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

`SchedulerMetrics` keeps `AtomicLong` submitted/completed/failed/cancelled/timeout counters, a `LongAdder`-style latency sum, and a bounded latency deque for P95 (sorted copy, nearest-rank). `SchedulerProperties` binds `scheduler.workers=4`, `queueCapacity=1000`, `defaultTimeoutMillis=30000`, `retryBaseDelayMillis=1000`, `historySize=200`. `GlobalExceptionHandler` maps validation→400, `SchedulerException`→400, `TaskNotFoundException`→404, fallback→500, always shaped `{error, message, taskId?}`. `@PreDestroy` stops consumers, dispatcher, workers in order with `awaitTermination`/`shutdownNow`, and closes SSE emitters. (Repository link and key snippets: see Appendix A.)
---

## Chapter 15 — Implementation: Frontend

### 15.1 Module Map

The frontend (`frontend/`, React 18, Vite 5) contains `index.html` (root div + `/src/main.jsx` script), `vite.config.js` (dev proxy `/api → localhost:8080`), `package.json` (react, react-dom, vite), and `src/`: `main.jsx` (App component + `createRoot` mount), `api.js` (fetch wrappers for every endpoint + SSE subscription), `status.js` (label/color maps), `styles.css` (dark theme, ~600 lines), and `components/` (`TaskForm`, `TaskTable`, `TaskDetailModal`, `StatsBar`, `StatusBadge`).

### 15.2 Components and Data Flow

`App` owns all state: `tasks`, `stats`, `connected` (SSE), `filter`, `selected` (modal), `error`. On mount it fetches tasks + stats once (REST resync) and opens the `EventSource`; every `event: task` frame upserts the matching record and refreshes stats, so the table animates PENDING → QUEUED → RUNNING → terminal without refresh. `TaskForm` submits typed tasks with priority/delay/timeout/retries/params and exposes Burst ×10 and Clear-finished; `TaskTable` filters by state/type/priority and offers per-row Inspect/Cancel/Retry; `TaskDetailModal` renders the full event log, params JSON, and result/error; `StatsBar` shows counts, avg/P95, queue depth and the live badge; `StatusBadge` color-codes all eight states. Failures surface as dismissible error toasts; empty states guide first use. (Repository link and key snippets: see Appendix A.)
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

---
## Appendix A — Code Availability and Key Snippets

> This appendix is the single authoritative pointer to the project's source
> code. The full listing is **not** pasted into this report; instead the
> repository below contains every file, and the three short snippets that
> follow illustrate the core scheduling algorithm.

### A.1 Repository link

- **Git repository:** `[paste your GitHub / GitLab URL here — e.g. https://github.com/<user>/async-task-scheduler]`
- **Archived copy (Zenodo DOI):** `[archive the repository on Zenodo and paste the DOI here — e.g. 10.5281/zenodo.XXXXXXX]`
- **Commit / tag evaluated:** `[e.g. v1.0]`
- **Licence:** `[e.g. MIT]`

*How to run from the repository (summary):* start the backend with
`cd backend && mvn spring-boot:run` (port 8080), then the frontend with
`cd frontend && npm install && npm run dev` (http://localhost:5173, `/api`
proxied to the backend). Full steps, sample requests and troubleshooting are
in `README.md` and `docs/DESIGN.md` in the same repository.

### A.2 Folder structure

```
async-task-scheduler/
├── README.md                        # setup, API reference, demo scenarios
├── docs/
│   ├── DESIGN.md                    # architecture + threading model
│   ├── PROJECT_REPORT.md            # short submission report
│   └── PROJECT_REPORT_FULL.md       # this report
├── backend/                         # Java 17 + Spring Boot 3.2 (Maven)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/taskscheduler/
│       │   ├── AsyncTaskSchedulerApp.java
│       │   ├── common/              # TaskStatus, TaskPriority, TaskType
│       │   ├── model/               # TaskRecord, TaskSubmitRequest, views
│       │   ├── scheduler/           # CORE: TaskSchedulerService,
│       │   │                        #   PriorityTaskQueue, QueuedTask,
│       │   │                        #   DemoJobFactory, Metrics, Properties
│       │   ├── web/                 # TaskController, DashboardController
│       │   └── config/              # CorsConfig
│       ├── main/resources/application.yml
│       └── test/java/com/taskscheduler/
│           ├── TaskSchedulerIntegrationTest.java
│           └── scheduler/PriorityTaskQueueTest.java
└── frontend/                        # React 18 + Vite 5
    ├── index.html  package.json  vite.config.js
    └── src/
        ├── main.jsx  api.js  status.js  styles.css
        └── components/              # TaskForm, TaskTable, TaskDetailModal,
                                     # StatsBar, StatusBadge
```

### A.3 Snippet 1 — enqueue(): guarded transition into the priority queue

```java
private void enqueue(TaskRecord task) {
    task.markQueued();                        // PENDING → QUEUED, guarded transition
    QueuedTask entry = new QueuedTask(         // sequence # = FIFO tie-break
            task, sequence.incrementAndGet(), System.currentTimeMillis());
    if (!inboundQueue.offer(entry)) {         // bounded queue: never blocks submitter
        task.markFailed("Scheduler queue is full ...");
        publish(task);
        return;
    }
    publish(task);                            // pushes QUEUED frame to dashboard (SSE)
}
```

*Why it matters:* this is the single funnel through which **every** dispatch —
fresh submits, delay-timer firings, retries and burst items — enters the
`PriorityBlockingQueue`. The monotonic sequence gives FIFO fairness inside one
priority level; the non-blocking `offer` plus full-queue failure keeps one burst
from stalling a submitter thread; the `publish` call is what makes the QUEUED
row appear live on the dashboard.

### A.4 Snippet 2 — retryOrFail(): exponential back-off, worker stays free

```java
private void retryOrFail(TaskRecord task, String message) {
    if (task.attempt() > task.maxRetries()) {  // attempts exhausted → terminal
        task.markFailed(message);               // FAILED; dashboard shows Retry
        publish(task);
        return;
    }
    long backoff = (long) (1_000L              // 1s, 2s, 4s, … wait grows
            * Math.pow(2, task.attempt() - 1));
    task.markRetryAfterFailure(message, backoff); // FAILED → RETRYING + note
    publish(task);
    dispatcher.schedule(() -> {                 // timer, NOT a job worker, waits
        if (task.status() == TaskStatus.RETRYING) {
            task.markRetryReady();              // RETRYING → PENDING, re-enter
            enqueue(task);                      // same priority + params, new attempt
        }
    }, backoff, TimeUnit.MILLISECONDS);
}
```

*Why it matters:* this is the failure-tolerance core. The wait happens on the
shared `ScheduledExecutorService`, so retries never reduce execution capacity;
the 1 s → 2 s → 4 s growth gives a flapping downstream service breathing room;
and the `RETRYING`-status guard means a user cancellation during the wait is
honoured instead of being silently resurrected by the timer.

### A.5 Snippet 3 — QueuedTask.compareTo(): priority first, FIFO second

```java
public int compareTo(QueuedTask other) {
    // Higher priority rank first (CRITICAL=3 … LOW=0) …
    int rankCmp = Integer.compare(other.task().priority().rank(),
                                  this.task().priority().rank());
    if (rankCmp != 0) {
        return rankCmp;
    }
    // … then FIFO among equal priorities.
    return Long.compare(this.sequenceNr, other.sequenceNr);
}
```

*Why it matters:* these eleven lines **are** the scheduling policy. The
`PriorityBlockingQueue` dequeues the least element first, so the reversed rank
comparison puts CRITICAL ahead of LOW, while the sequence tie-break stops equal
priorities from overtaking each other — the property the
`PriorityTaskQueueTest` unit test pins down deterministically.

---

## Appendix B — Screenshots and Operational Notes

### B.1 How to run (development)

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

Open `http://localhost:5173`. The Vite proxy forwards `/api/*` to
`http://localhost:8080`, so no CORS setup is needed in development.

### B.2 Suggested screenshots for the printed report

1. Dashboard on load (empty state + live badge).
2. Dashboard mid-burst (mixed PENDING/QUEUED/RUNNING/COMPLETED rows).
3. Detail modal showing a full event log.
4. Stats bar after a retry demonstration (RETRYING then FAILED/COMPLETED).
5. Terminal showing `mvn test` green (7/7) and `npm run build` clean.

### B.3 Troubleshooting
*End of report.*
