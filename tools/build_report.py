#!/usr/bin/env python3
"""
Generate the complete 50+ page project report:
  docs/PROJECT_REPORT_FULL.md

Run:  python3 tools/build_report.py
"""

from pathlib import Path
import os

ROOT = Path("/Users/mishrapawas/Documents/class/2nd Year project")
OUT = ROOT / "docs" / "PROJECT_REPORT_FULL.md"

def write_report():
    with open(OUT, "w", encoding="utf-8") as f:
        # === CHAPTER 1: TITLE PAGE ===
        f.write("""# PROJECT REPORT

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

""")
    
    print(f"Written part 1 to {OUT}")

if __name__ == "__main__":
    write_report()
