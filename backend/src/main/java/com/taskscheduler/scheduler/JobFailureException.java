package com.taskscheduler.scheduler;

/** Thrown by a job when it decides to fail (used for retry/failure handling). */
public class JobFailureException extends SchedulerException {

    public JobFailureException(String message) {
        super(message);
    }

    public JobFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}