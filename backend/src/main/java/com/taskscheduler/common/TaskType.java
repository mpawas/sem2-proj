package com.taskscheduler.common;

/**
 * The kinds of demo jobs the scheduler can execute.
 * Each type maps to a different execution profile:
 *
 * <ul>
 *   <li>{@link #DELAY}          - simulated I/O wait (sleep)</li>
 *   <li>{@link #COMPUTE}        - CPU bound prime counting job</li>
 *   <li>{@link #FETCH}          - simulated remote HTTP fetch</li>
 *   <li>{@link #BUILD_REPORT}   - aggregate report over all tasks (parallel streams)</li>
 * </ul>
 */
public enum TaskType {

    DELAY("Simulated I/O delay", "durationMs"),
    COMPUTE("CPU-bound prime computation", "limit"),
    FETCH("Simulated network fetch", "url"),
    BUILD_REPORT("Aggregate report built with parallel streams", null);

    private final String label;
    private final String primaryParam;

    TaskType(String label, String primaryParam) {
        this.label = label;
        this.primaryParam = primaryParam;
    }

    public String label() {
        return label;
    }

    public String primaryParam() {
        return primaryParam;
    }

    public static TaskType fromName(String name) {
        for (TaskType t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return DELAY;
    }
}