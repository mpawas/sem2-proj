package com.taskscheduler.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.taskscheduler.common.TaskPriority;
import com.taskscheduler.common.TaskType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Immutable REST request DTO (a Java {@code record}).
 *
 * <p>Record components are implicitly {@code final}, so friendly defaults for
 * omitted fields are applied by the {@link #create} {@code @JsonCreator}
 * factory, which Jackson uses instead of the canonical constructor. The boxed
 * {@link Integer} component types let us distinguish "omitted" (null) from an
 * explicit value. Bean-validation annotations ({@code jakarta.validation}) are
 * evaluated by Spring before the controller method runs.</p>
 */
public record TaskSubmitRequest(
        @Size(max = 80, message = "name must be at most 80 characters")
        String name,

        TaskType type,

        TaskPriority priority,

        @Min(value = 0, message = "delayMillis must be >= 0")
        @Max(value = 86_400_000, message = "delayMillis must be <= 86400000")
        Integer delayMillis,

        @Min(value = 100, message = "timeoutMillis must be >= 100")
        @Max(value = 3_600_000, message = "timeoutMillis must be <= 3600000")
        Integer timeoutMillis,

        @Min(value = 0, message = "maxRetries must be >= 0")
        @Max(value = 10, message = "maxRetries must be <= 10")
        Integer maxRetries,

        @Min(value = 0, message = "failRate must be >= 0")
        @Max(value = 100, message = "failRate must be <= 100")
        Integer failRate,

        Map<String, Object> params) {

    /** Jackson factory: applies defaults for anything the client omitted. */
    @JsonCreator
    public static TaskSubmitRequest create(
            @JsonProperty("name") String name,
            @JsonProperty("type") TaskType type,
            @JsonProperty("priority") TaskPriority priority,
            @JsonProperty("delayMillis") Integer delayMillis,
            @JsonProperty("timeoutMillis") Integer timeoutMillis,
            @JsonProperty("maxRetries") Integer maxRetries,
            @JsonProperty("failRate") Integer failRate,
            @JsonProperty("params") Map<String, Object> params) {
        return new TaskSubmitRequest(
                name == null || name.isBlank() ? "Task" : name.trim(),
                type == null ? TaskType.DELAY : type,
                priority == null ? TaskPriority.NORMAL : priority,
                delayMillis == null ? 0 : delayMillis,
                timeoutMillis == null ? 60_000 : timeoutMillis,
                maxRetries == null ? 0 : maxRetries,
                failRate == null ? 0 : failRate,
                params == null ? Map.of() : Map.copyOf(params));
    }
}