package com.taskscheduler.web;

import com.taskscheduler.model.StatsView;
import com.taskscheduler.scheduler.SchedulerProperties;
import com.taskscheduler.scheduler.TaskSchedulerService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Dashboard endpoints: live stats plus a real-time Server-Sent-Events stream.
 *
 * <p>Every task transition is pushed to the browser over plain HTTP text/event-stream.
 * The event name is {@code task} with a JSON {@link com.taskscheduler.model.TaskView}
 * payload; a {@code ping} heartbeat keeps idle connections from being closed.</p>
 */
@Controller
public class DashboardController {

    private static final ScheduledExecutorService HEARTBEAT_POOL =
            Executors.newScheduledThreadPool(1);

    private final TaskSchedulerService scheduler;
    private final SchedulerProperties props;

    public DashboardController(TaskSchedulerService scheduler, SchedulerProperties props) {
        this.scheduler = scheduler;
        this.props = props;
    }

    @GetMapping(value = "/api/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);   // never auto-timeout

        Runnable unsubscribe = scheduler.subscribe(task -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("task")
                        .data(com.taskscheduler.model.TaskView.from(task)));
            } catch (IOException e) {
                throw new IllegalStateException("SSE channel closed", e);
            }
        });

        Runnable close = () -> { unsubscribe.run(); emitter.complete(); };
        emitter.onCompletion(close);
        emitter.onTimeout(close);
        emitter.onError(t -> close.run());

        // Heartbeat so proxies/browsers keep the stream open.
        HEARTBEAT_POOL.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
            } catch (IOException e) {
                close.run();
            }
        }, props.heartbeatMs(), props.heartbeatMs() + 1, TimeUnit.MILLISECONDS);

        return emitter;
    }

    @GetMapping(value = "/api/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StatsView stats() {
        return scheduler.stats();
    }

    @GetMapping(value = "/api/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public java.util.Map<String, Object> health() {
        return java.util.Map.of(
                "status", "UP",
                "taskCount", scheduler.snapshot().size(),
                "time", System.currentTimeMillis());
    }

    @PreDestroy
    public void shutdownHeartbeatPool() {
        HEARTBEAT_POOL.shutdownNow();
    }
}