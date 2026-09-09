package com.taskscheduler.scheduler;

import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskType;
import com.taskscheduler.model.TaskRecord;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deterministic (no threads, no timing) tests of the priority-queue semantics
 * that {@link QueuedTask#compareTo} + {@link PriorityBlockingQueue} provide:
 * highest rank first, FIFO within a rank.
 */
class PriorityTaskQueueTest {

    private static long seq = 0;

    @Test
    void dequeuesHighestPriorityFirstRegardlessOfInsertOrder() throws InterruptedException {
        PriorityTaskQueue queue = new PriorityTaskQueue(16);
        // Inserted deliberately out of priority order.
        queue.put(new QueuedTask(task("low", TaskPriority.LOW), nextSeq(), now()));
        queue.put(new QueuedTask(task("critical", TaskPriority.CRITICAL), nextSeq(), now()));
        queue.put(new QueuedTask(task("normal", TaskPriority.NORMAL), nextSeq(), now()));
        queue.put(new QueuedTask(task("high", TaskPriority.HIGH), nextSeq(), now()));

        assertThat(queue.take().task().priority()).isEqualTo(TaskPriority.CRITICAL);
        assertThat(queue.take().task().priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(queue.take().task().priority()).isEqualTo(TaskPriority.NORMAL);
        assertThat(queue.take().task().priority()).isEqualTo(TaskPriority.LOW);

        assertThat(queue.depth()).isZero();
    }

    @Test
    void respectsFifoOrderWithinTheSamePriority() throws InterruptedException {
        PriorityTaskQueue queue = new PriorityTaskQueue(16);
        queue.put(new QueuedTask(task("first", TaskPriority.NORMAL), nextSeq(), now()));
        queue.put(new QueuedTask(task("second", TaskPriority.NORMAL), nextSeq(), now()));

        assertThat(queue.take().task().name()).isEqualTo("first");
        assertThat(queue.take().task().name()).isEqualTo("second");
    }

    @Test
    void criticalPrecedesNormalEvenWhenInsertedLater() throws InterruptedException {
        PriorityTaskQueue queue = new PriorityTaskQueue(16);
        // normal inserted BEFORE critical (lower sequence number)
        TaskRecord normal = task("normal-first", TaskPriority.NORMAL);
        TaskRecord critical = task("critical-second", TaskPriority.CRITICAL);
        queue.put(new QueuedTask(normal, nextSeq(), now()));
        queue.put(new QueuedTask(critical, nextSeq(), now()));

        assertThat(queue.take().task()).isSameAs(critical);   // higher rank wins
        assertThat(queue.take().task()).isSameAs(normal);
    }

    private static TaskRecord task(String name, TaskPriority priority) {
        return new TaskRecord("id-" + name, name, TaskType.DELAY, priority,
                0, 60_000, 0, 0, Map.of());
    }

    private static long nextSeq() {
        return ++seq;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}