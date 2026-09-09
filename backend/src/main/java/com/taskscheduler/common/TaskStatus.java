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