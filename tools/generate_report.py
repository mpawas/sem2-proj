#!/usr/bin/env python3
"""
Generator for the complete 50+ page project report:
  docs/PROJECT_REPORT_FULL.md

Run:  python3 tools/generate_report.py
The report is assembled from narrative chapters below PLUS every project source
file, embedded verbatim, so code listings always match the repo.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPORT = []   # list of markdown chunks


def embed(title, relpath, lang="java"):
    """Read a project file and emit a numbered code listing section."""
    text = (ROOT / relpath).read_text(encoding="utf-8")
    return (
        f"\n#### Listing: `{relpath}`\n\n"
        f"**File:** `{relpath}`\n\n"
        f"```{lang}\n{text.rstrip()}\n```\n"
    )


# =====================================================================
# CHAPTER 1 - TITLE PAGE
# =====================================================================
REPORT.append(r"""
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

*“The user is the interface. The scheduler is the backbone.”*

---
""")

# =====================================================================
# CHAPTER 2 - DECLARATION
# =====================================================================
REPORT.append("""
## Chapter 2 — Declaration and Certification

I, *[Full Name]*, Roll Number *[Roll No.]*, a student of Second Year, hereby declare
that the project work entitled **“Asynchronous Task Scheduler — An Advanced Java
Concurrency Application with a React Real-Time Dashboard”** submitted as part of the
*Advanced Java Programming* paper is my original work.

The project has been developed by me under the supervision and guidance of
*[Name of Faculty Guide]*. All the source code, design documents, test cases and this
report have been prepared by me personally. Wherever the work of others has been
referred to, it has been duly acknowledged and listed in the References chapter of
this report.

I further declare that this work has not been submitted, in whole or in part, to any
other examination, competition or publication prior to this submission, and that the
contents of this report are true to the best of my knowledge.

**Signature:** ______________________ &nbsp;&nbsp;&nbsp; **Date:** __________________

---
""")

# =====================================================================
# CHAPTER 3 - ACKNOWLEDGEMENT
# =====================================================================
REPORT.append("""
## Chapter 3 — Acknowledgements

I would like to express my sincere gratitude to the following people and communities
without whose support this project would not have been possible:

1. **Faculty Guide**, *[Name of Faculty Guide]*, for constant guidance, valuable
   feedback on the design of the scheduler core, and for encouraging me to explore the
   Java Concurrency API beyond the syllabus.
2. **Department of Computer Science**, *[College Name]*, for providing the laboratory
   infrastructure and reference books that supported the development of this project.
3. **The Java / Spring Boot open-source community**, for the excellent documentation of
   `java.util.concurrent`, Spring Framework and Spring Boot, which form the technical
   foundation of this work.
4. **The React and Vite communities**, for tooling that made the real-time dashboard
   straightforward to build and package.
5. My **classmates**, for their useful discussions on thread-safety, blocking queues
   and the design of the task state machine.

---
""")

# =====================================================================
# CHAPTER 4 - TABLE OF CONTENTS
# =====================================================================
REPORT.append("""
## Chapter 4 — Table of Contents

| Chapter | Title |
|---:|---|
| 1 | Title Page |
| 2 | Declaration and Certification |
| 3 | Acknowledgements |
| 4 | Table of Contents, List of Figures, Tables and Abbreviations |
| 5 | Abstract |
| 6 | Introduction |
| 7 | Literature Survey and Related Work |
| 8 | Technology Selection and Rationale |
| 9 | Advanced Java Concepts Used |
| 10 | Requirements Analysis and Specifications |
| 11 | System Design |
| 12 | Implementation — Common and Model Layers |
| 13 | Implementation — The Scheduler Core |
| 14 | Implementation — Web Layer, REST API and SSE |
| 15 | Implementation — React Frontend |
| 16 | Configuration, Build and Deployment |
| 17 | REST API Reference and Error Contract |
| 18 | Testing and Test Results |
| 19 | Results, Observations and Demo Scenarios |
| 20 | Performance Analysis |
| 21 | Security Considerations |
| 22 | Limitations of the Work |
| 23 | Future Enhancements |
| 24 | Conclusion |
| 25 | Bibliography and References |
| Appendix A | Complete Source Code Listing |
| Appendix B | Project File Inventory |
| Appendix C | How to Run the Project |

---
""")
### 4.1 List of Figures

| Figure | Title |
|---:|---|
| 6.1 | The asynchronous scheduler in the real world |
| 7.1 | Synchronous request-response vs. asynchronous task queues |
| 9.1 | The Java thread lifecycle |
| 9.2 | Thread pool anatomy (ThreadPoolExecutor) |
| 11.1 | High-level system architecture |
| 11.2 | Module decomposition diagram |
| 11.3 | Class diagram of the scheduler package |
| 11.4 | The task state machine |
| 11.5 | Threading model of the scheduler |
| 11.6 | Sequence diagram: submitting and running a delayed task |
| 15.1 | Frontend component tree and data flow |
| 18.1 | Integration test execution summary (Maven Surefire) |

### 4.2 List of Tables

| Table | Title |
|---:|---|
| 7.1 | Synchronous vs. asynchronous execution |
| 8.1 | Technology stack and rationale |
| 9.1 | Concurrency constructs used in this project |
| 10.1 | Functional requirements |
| 10.2 | Non-functional requirements |
| 10.3 | Use-case catalogue |
| 11.1 | Thread pools in the scheduler |
| 11.2 | Design decisions and trade-offs |
| 16.1 | Configuration keys |
| 17.1 | REST API surface |
| 17.2 | HTTP response codes |
| 18.1 | Unit test catalogue |
| 19.1 | Demo scenarios and observations |

### 4.3 List of Abbreviations

| Abbreviation | Expansion |
|---:|---|
| API | Application Programming Interface |
| CPU | Central Processing Unit |
| DTO | Data Transfer Object |
| FIFO | First In, First Out |
| HTTP | HyperText Transfer Protocol |
| JDK | Java Development Kit |
| JVM | Java Virtual Machine |
| REST | REpresentational State Transfer |
| SSE | Server-Sent Events |
| SRS | Software Requirements Specification |
| UI | User Interface |
| UUID | Universally Unique Identifier |

---
""")
# =====================================================================
# CHAPTER 5 - ABSTRACT
# =====================================================================
REPORT.append("""
## Chapter 5 — Abstract

An *asynchronous task scheduler* is a software component that accepts units of work
(called tasks) and arranges for them to be executed **outside the calling thread**,
typically with a priority, a start delay, retry capabilities and timeout protection.
Schedulers are ubiquitous — every banking app, e-commerce platform, notification
service and batch pipeline depends on one.

This project designs and implements a complete asynchronous task scheduler as a
full-stack application:

- **A Java 17 / Spring Boot 3.2 backend** whose scheduler core is built *entirely*
  from the JDK's `java.util.concurrent` toolkit — no third-party job-scheduling
  library. The core uses a `ScheduledExecutorService` for delayed dispatch, a
  `PriorityBlockingQueue` for priority-ordered waiting, a fixed worker pool for
  execution, a `ConcurrentHashMap` registry, `AtomicLong` counters and `volatile` +
  `synchronized` state transitions. Failed tasks are retried automatically with
  exponential back-off; slow tasks are cut off by scheduled timeout guards; tasks can
  be cancelled cooperatively at any stage.
- **A React 18 dashboard** that shows the entire system live through a Server-Sent
  Events (SSE) stream: every state transition of every task is pushed to the browser
  and rendered immediately, with statistics, filtering, a task audit log, cancel and
  retry actions.
- **A test suite** of seven tests (four Spring integration tests and three
  deterministic unit tests) that verifies asynchronous completion, priority ordering,
  retry exhaustion and cancellation.

The report documents the background, requirements, design, implementation, testing,
results and conclusions of the work, and additionally contains a complete listing of
the project source code in Appendix A.

**Keywords:** concurrency, scheduler, thread pool, blocking queue, priority queue,
executor, race condition, thread safety, REST, Server-Sent Events, React.

---
""")
### 6.3 Problem Statement

> **Design and implement an asynchronous task scheduler with the following
> capabilities:**
> 1. Accept task submissions over a REST API, where each task carries a name, a type,
>    a priority, an optional start delay, a timeout and a retry policy.
> 2. Execute tasks asynchronously on a bounded worker pool, so that the caller's
>    thread is never blocked by the execution.
> 3. Order waiting tasks by priority (CRITICAL before HIGH before NORMAL before LOW)
>    and, within the same priority, by submission order (FIFO).
> 4. Schedule tasks with a start delay using Java's timer machinery, without wasting
>    a thread per task.
> 5. Protect against hangs: any task still running after its timeout is marked
>    `TIMED_OUT`.
> 6. Protect against transient failures: a failing task is retried automatically with
>    exponential back-off up to a per-task retry budget, after which it is marked
>    `FAILED`.
> 7. Allow tasks to be cancelled cooperatively while they are pending, queued, or
>    running.
> 8. Stream every state transition to a browser-based dashboard in real time using
>    Server-Sent Events, and expose aggregate statistics (counts by state, average and
>    P95 latency, queue depth).
> 9. Be thread-safe in every component, and be covered by automated tests that
>    actually exercise the concurrency.

### 6.4 Objectives

The objectives of the project are precisely aligned with the problem statement:

| # | Objective | How it is achieved |
|---:|---|
| O1 | Demonstrate advanced Java concurrency | `ExecutorService`, `ScheduledExecutorService`, `PriorityBlockingQueue`, `ConcurrentHashMap`, atomics |
| O2 | Non-blocking, asynchronous execution | Submit returns immediately with a task id; the worker pool does the work |
| O3 | Priority + fairness | `PriorityBlockingQueue` with a custom `Comparable` |
| O4 | Delayed start | Dispatcher schedules a future Runnable per task |
| O5 | Failure tolerance | Exponential back-off retries + scheduled timeout guards |
| O6 | Cooperative cancellation | A `volatile` flag that jobs observe in their loops |
| O7 | Real-time visibility | SSE stream from the backend to a React dashboard |
| O8 | Quality | Seven automated tests + end-to-end smoke testing |

### 6.5 Scope and Limitations

**In scope.** Single-node, in-memory asynchronous task scheduling; priority
ordering; delayed dispatch; timeouts; automatic and manual retries; cancellation;
real-time monitoring; REST API; a complete reactive dashboard; tests and
documentation.

**Out of scope (initially).** Durable persistence across restarts, distributed
scheduling, authentication/authorization, cron-like recurring schedules and
cluster-wide metrics. These are discussed as future work in Chapter 23.

### 6.6 Methodology

The project was carried out in an iterative, five-phase methodology:

1. **Analysis** — understanding the scheduler problem, reading about
   `java.util.concurrent` and studying patterns such as the producer-consumer
   pattern and the thread pool pattern (Chapter 7).
2. **Design** — drawing the architecture, module and class views, the state machine
   and the threading model (Chapter 11).
3. **Implementation** — writing the backend, then the frontend, then polishing the
   API contract and configuration (Chapters 12-16).
4. **Testing** — unit and integration tests followed by a live smoke test against the
   running system (Chapter 18).
5. **Evaluation and documentation** — analysing results, performance and security,
   and writing this report (Chapters 19-25).

### 6.7 Organisation of the Report

The remainder of the report is organised as follows. Chapter 7 surveys the relevant
literature and background patterns. Chapter 8 justifies the technology choices.
Chapter 9 explains the advanced Java concepts that the project uses. Chapter 10
presents the requirements specification. Chapter 11 describes the system design.
Chapters 12-15 describe the implementation of every layer, with full code listings.
Chapters 16-17 cover configuration and the REST API. Chapter 18 presents the testing
strategy and results. Chapters 19-23 evaluate the work, its limitations and possible
extensions. Chapters 24-25 conclude the report, and the appendices contain the
complete source code and operational instructions.

---
""")