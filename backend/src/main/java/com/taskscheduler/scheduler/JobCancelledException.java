package com.taskscheduler.scheduler;

/** Thrown by a running job when it observes a cooperative cancel request. */
public class JobCancelledException extends SchedulerException {

    public JobCancelledException(String message) {
        super(message);
    }
}