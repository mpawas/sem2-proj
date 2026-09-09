package com.taskscheduler.model;

import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskStatus;
import com.taskscheduler.common.TaskType;

import java.util.List;
import java.util.Map;

/**
 * Immutable API snapshot of a {@link TaskRecord} - safe to serialize at any time.
 * Building it takes a coherent, copy-on-read view of the mutable state.
 */
public record TaskView(
        String id,
        String name,
        TaskType type,
        TaskPriority priority,
        int delayMillis,
        int timeoutMillis,
        int maxRetries,
        int failRate,
        Map<String, Object> params,
        TaskStatus status,
        int progress,
        int attempt,
        String workerId,
        String result,
        String error,
        long submittedAtMillis,
        long startedAtMillis,
        long finishedAtMillis,
        long elapsedMillis,
        List<TaskRecord.EventEntry> eventLog) {

    /** Static factory - keeps the controller free of record internals. */
    public static TaskView from(TaskRecord t) {
        return new TaskView(
                t.id(),
                t.name(),
                t.type(),
                t.priority(),
                t.delayMillis(),
                t.timeoutMillis(),
                t.maxRetries(),
                t.failRate(),
                t.params(),
                t.status(),
                t.progress(),
                t.attempt(),
                t.workerId(),
                t.result(),
                t.error(),
                t.submittedAtMillis(),
                t.startedAtMillis(),
                t.finishedAtMillis(),
                t.elapsedMillis(),
                t.eventLog());
    }
}