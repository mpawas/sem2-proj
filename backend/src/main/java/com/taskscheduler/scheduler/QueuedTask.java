package com.taskscheduler.scheduler;

import com.taskscheduler.model.TaskRecord;

/**
 * An entry waiting in the scheduler's priority queue.
 *
 * <p>Implements {@link Comparable} so {@code PriorityBlockingQueue} can order it:
 * {@link TaskRecord#priority()} rank first (highest rank first), then FIFO order
 * via a monotonically increasing sequence number (lowest sequence first).</p>
 */
public record QueuedTask(TaskRecord task, long sequenceNr, long queuedAtMillis)
        implements Comparable<QueuedTask> {

    @Override
    public int compareTo(QueuedTask other) {
        // Higher priority rank first ...
        int rankCmp = Integer.compare(other.task().priority().rank(),
                                      this.task().priority().rank());
        if (rankCmp != 0) {
            return rankCmp;
        }
        // ... then FIFO among equal priorities.
        return Long.compare(this.sequenceNr, other.sequenceNr);
    }
}