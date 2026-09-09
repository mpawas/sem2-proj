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