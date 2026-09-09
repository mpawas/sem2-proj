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