package com.taskscheduler.scheduler;

/** Raised when a task id does not exist - mapped to HTTP 404 by the exception advice. */
public class TaskNotFoundException extends RuntimeException {

    private final String taskId;

    public TaskNotFoundException(String taskId) {
        super("Task not found: " + taskId);
        this.taskId = taskId;
    }

    public String taskId() {
        return taskId;
    }
}