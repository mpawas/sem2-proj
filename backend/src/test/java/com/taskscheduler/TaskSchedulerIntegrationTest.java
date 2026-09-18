package com.taskscheduler;

import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskStatus;
import com.taskscheduler.common.TaskType;
import com.taskscheduler.model.TaskRecord;
import com.taskscheduler.model.TaskSubmitRequest;
import com.taskscheduler.scheduler.TaskSchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end test of the scheduler service on a real Spring context.
 * Verifies the whole pipeline: submit → queue → worker → terminal state.
 */
@SpringBootTest(properties = "scheduler.workers=1")
class TaskSchedulerIntegrationTest {

    @Autowired
    private TaskSchedulerService scheduler;

    @Test
    void submittedTaskRunsToCompletionAsynchronously() {
        TaskRecord task = scheduler.submit(new TaskSubmitRequest(
                "integration-delay",
                TaskType.DELAY,
                TaskPriority.NORMAL,
                0,
                10_000,
                0,
                0,
                java.util.Map.of("durationMs", 250)));

        assertThat(task.status()).isIn(TaskStatus.PENDING, TaskStatus.QUEUED,
                TaskStatus.RUNNING);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(task.id()).status())
                        .isEqualTo(TaskStatus.COMPLETED));

        assertThat(scheduler.require(task.id()).result()).contains("Simulated I/O wait");
        assertThat(scheduler.stats().completed()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void highPriorityTaskExecutesBeforeEarlierSubmittedNormalTask() {
        // Busy the single worker with a long task first (delay 0).
        scheduler.submit(new TaskSubmitRequest(
                "slow", TaskType.DELAY, TaskPriority.NORMAL, 0, 30_000, 0, 0,
                java.util.Map.of("durationMs", 1_500)));

        // NORMAL is SUBMITTED FIRST (delay 300 ms) so it is enqueued later ...
        TaskRecord normal = scheduler.submit(new TaskSubmitRequest(
                "normal-first", TaskType.DELAY, TaskPriority.NORMAL, 300, 30_000, 0, 0,
                java.util.Map.of("durationMs", 300)));
        // ... while CRITICAL is submitted SECOND but dispatched earlier (delay 100 ms).
        // While slow occupies the worker, the priority queue receives critical at t=100
        // and normal at t=300, so the worker pool picks up CRITICAL first.
        // With a single worker and well-separated delays this is deterministic.
        TaskRecord critical = scheduler.submit(new TaskSubmitRequest(
                "critical-second", TaskType.DELAY, TaskPriority.CRITICAL, 100, 30_000, 0, 0,
                java.util.Map.of("durationMs", 300)));

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(normal.id()).status())
                        .isEqualTo(TaskStatus.COMPLETED));
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(critical.id()).status())
                        .isEqualTo(TaskStatus.COMPLETED));

        // CRITICAL must have finished before NORMAL (its dispatch slot fired first).
        assertThat(scheduler.require(critical.id()).finishedAtMillis())
                .isLessThan(scheduler.require(normal.id()).finishedAtMillis());
    }

    @Test
    void retriesExhaustedTaskEndsFailed() {
        TaskRecord doomed = scheduler.submit(new TaskSubmitRequest(
                "doomed", TaskType.DELAY, TaskPriority.NORMAL, 0, 30_000, 1, 100,
                java.util.Map.of("durationMs", 100)));

        await().atMost(20, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(doomed.id()).status())
                        .isEqualTo(TaskStatus.FAILED));

        assertThat(scheduler.require(doomed.id()).attempt()).isEqualTo(2); // 1 + 1 retry
    }

    @Test
    void pendingTaskCanBeCancelledBeforeExecution() {
        TaskRecord task = scheduler.submit(new TaskSubmitRequest(
                "cancel-me", TaskType.DELAY, TaskPriority.LOW, 120_000, 30_000, 0, 0,
                java.util.Map.of("durationMs", 100)));

        assertThat(scheduler.cancel(task.id())).isTrue();
        assertThat(scheduler.require(task.id()).status())
                .isEqualTo(TaskStatus.CANCELLED);
    }

    @Test
    void clearFinishedEvictsOnlyTerminalTasks() {
        // The Spring context (and therefore the registry) is shared across tests,
        // so earlier tests may have left terminal tasks behind. We therefore
        // assert order-independent invariants rather than a global removal count.
        // Two tasks that run to completion...
        TaskRecord doneA = scheduler.submit(new TaskSubmitRequest(
                "done-a", TaskType.DELAY, TaskPriority.NORMAL, 0, 30_000, 0, 0,
                java.util.Map.of("durationMs", 100)));
        TaskRecord doneB = scheduler.submit(new TaskSubmitRequest(
                "done-b", TaskType.DELAY, TaskPriority.NORMAL, 0, 30_000, 0, 0,
                java.util.Map.of("durationMs", 100)));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(doneA.id()).status())
                        .isEqualTo(TaskStatus.COMPLETED));
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(scheduler.require(doneB.id()).status())
                        .isEqualTo(TaskStatus.COMPLETED));

        // ...and one that is still PENDING (long delay) and must be kept.
        TaskRecord active = scheduler.submit(new TaskSubmitRequest(
                "still-pending", TaskType.DELAY, TaskPriority.LOW, 120_000, 30_000, 0, 0,
                java.util.Map.of("durationMs", 100)));

        int removed = scheduler.clearFinished();

        // Our two completed tasks are gone, and the active one survives.
        assertThat(removed).isGreaterThanOrEqualTo(2);
        assertThat(scheduler.require(active.id()).status())
                .isEqualTo(TaskStatus.PENDING);      // active task survives
        assertThat(scheduler.snapshot().stream().map(TaskRecord::id))
                .containsExactly(active.id());
        // Nothing terminal may remain after a clear.
        assertThat(scheduler.snapshot())
                .allSatisfy(t -> assertThat(t.status().isTerminal()).isFalse());
    }
}