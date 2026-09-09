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