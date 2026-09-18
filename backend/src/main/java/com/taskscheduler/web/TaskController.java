package com.taskscheduler.web;

import com.taskscheduler.model.TaskSubmitRequest;
import com.taskscheduler.model.TaskView;
import com.taskscheduler.scheduler.TaskSchedulerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST API for tasks.
 *
 * <ul>
 *   <li>{@code POST   /api/tasks}            - submit</li>
 *   <li>{@code GET    /api/tasks}            - list (newest first)</li>
 *   <li>{@code GET    /api/tasks/{id}}       - one task</li>
 *   <li>{@code DELETE /api/tasks/{id}}       - cancel</li>
 *   <li>{@code POST   /api/tasks/{id}/retry} - requeue a failed task</li>
 *   <li>{@code POST   /api/tasks/burst}      - submit N synthetic tasks</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskSchedulerService scheduler;

    public TaskController(TaskSchedulerService scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping
    public ResponseEntity<TaskView> submit(@Valid @RequestBody TaskSubmitRequest request) {
        TaskView created = TaskView.from(scheduler.submit(request));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/api/tasks/" + created.id()))
                .body(created);
    }

    @GetMapping
    public List<TaskView> list(@RequestParam(required = false) String status) {
        return scheduler.snapshot().stream()
                .filter(t -> status == null || status.isBlank()
                        || t.status().name().equalsIgnoreCase(status))
                .map(TaskView::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TaskView get(@PathVariable String id) {
        return TaskView.from(scheduler.require(id));
    }

    @DeleteMapping
    public ResponseEntity<java.util.Map<String, Object>> clearFinished() {
        int removed = scheduler.clearFinished();
        return ResponseEntity.ok().body(java.util.Map.of(
                "removed", removed,
                "message", removed + " finished task(s) cleared"));
    }

    @DeleteMapping("/{id}")
    public TaskView cancel(@PathVariable String id) {
        if (!scheduler.cancel(id)) {
            throw new IllegalStateException(
                    "Task '" + id + "' is already in a terminal state and cannot be cancelled");
        }
        return TaskView.from(scheduler.require(id));
    }

    @PostMapping("/{id}/retry")
    public TaskView retry(@PathVariable String id) {
        return TaskView.from(scheduler.manualRetry(id));
    }

    @PostMapping("/burst")
    public ResponseEntity<java.util.Map<String, Object>> burst(
            @RequestParam(defaultValue = "10") int count) {
        int accepted = scheduler.submitBurst(count);
        return ResponseEntity.accepted().body(java.util.Map.of(
                "accepted", accepted,
                "message", accepted + " tasks accepted for submission"));
    }
}