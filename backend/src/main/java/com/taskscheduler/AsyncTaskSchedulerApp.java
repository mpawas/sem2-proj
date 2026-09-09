package com.taskscheduler;

import com.taskscheduler.scheduler.SchedulerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Async Task Scheduler - 2nd Year project backend.
 *
 * <p>A Spring Boot application whose domain heart is
 * {@link com.taskscheduler.scheduler.TaskSchedulerService}: an asynchronous,
 * priority-ordered, retry-aware task scheduler built on the advanced Java
 * concurrency toolkit (ExecutorService, ScheduledExecutorService,
 * PriorityBlockingQueue, ConcurrentHashMap, CompletableFuture, etc.).</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(SchedulerProperties.class)
public class AsyncTaskSchedulerApp {

    public static void main(String[] args) {
        SpringApplication.run(AsyncTaskSchedulerApp.class, args);
    }
}