#!/usr/bin/env python3
"""Insert Chapter 25 part 2 + Chapters 26-27 before Appendix A."""
from pathlib import Path
ROOT = Path("/Users/mishrapawas/Documents/class/2nd Year project")
OUT = ROOT / "docs" / "PROJECT_REPORT_FULL.md"
text = OUT.read_text(encoding="utf-8")
marker = "## Appendix A"
assert marker in text
extra = """
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
"""
text = text.replace(marker, extra + marker)
OUT.write_text(text, encoding="utf-8")
print("Ch25b-27 inserted; lines:", len(text.splitlines()))
