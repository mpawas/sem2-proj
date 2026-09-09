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