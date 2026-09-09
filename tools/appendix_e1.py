#!/usr/bin/env python3
"""Insert Chapter 25 (viva Q&A part 1) before Appendix A."""
from pathlib import Path
ROOT = Path("/Users/mishrapawas/Documents/class/2nd Year project")
OUT = ROOT / "docs" / "PROJECT_REPORT_FULL.md"
text = OUT.read_text(encoding="utf-8")
marker = "## Appendix A"
assert marker in text
extra = """
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
"""
text = text.replace(marker, extra + marker)
OUT.write_text(text, encoding="utf-8")
print("Ch25a inserted; lines:", len(text.splitlines()))
