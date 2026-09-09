package com.taskscheduler.scheduler;

/** Base runtime exception for scheduler failures (mapped to HTTP 500 by the advice). */
public class SchedulerException extends RuntimeException {

    public SchedulerException(String message) {
        super(message);
    }

    public SchedulerException(String message, Throwable cause) {
        super(message, cause);
    }
}