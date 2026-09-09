package com.taskscheduler.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Type-safe binding for the {@code scheduler.*} keys in {@code application.yml}.
 * Spring binds and validates these at startup; {@code @DefaultValue} protects
 * against missing keys.
 */
@ConfigurationProperties(prefix = "scheduler")
public record SchedulerProperties(
        @DefaultValue("4") int workers,
        @DefaultValue("2048") int queueCapacity,
        @DefaultValue("60000") int defaultTimeoutMs,
        @DefaultValue("2") int maxRetries,
        @DefaultValue("15000") int heartbeatMs) {}