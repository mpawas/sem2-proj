package com.taskscheduler.scheduler;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thin, purpose-built wrapper around {@link PriorityBlockingQueue}.
 *
 * <p>{@code PriorityBlockingQueue} is an unbounded blocking queue whose head is
 * always the element with the smallest {@code compareTo} value, so concurrent
 * enqueue/dequeue never needs an external lock. We maintain a close-enough
 * depth gauge for the dashboard with an {@link AtomicInteger}.</p>
 */
public final class PriorityTaskQueue {

    private final PriorityBlockingQueue<QueuedTask> queue;
    private final AtomicInteger depth = new AtomicInteger(0);

    public PriorityTaskQueue(int capacity) {
        this.queue = new PriorityBlockingQueue<>(capacity);
    }

    /** Blocking insert. Returns the (approximate) depth after the put. */
    public int put(QueuedTask item) {
        queue.put(item);
        return depth.incrementAndGet();
    }

    /** Blocking ordered remove; caller handles {@link InterruptedException}. */
    public QueuedTask take() throws InterruptedException {
        QueuedTask head = queue.take();
        depth.decrementAndGet();
        return head;
    }

    /** Ordered poll that gives up after {@code timeout}; used for graceful shutdown. */
    public QueuedTask poll(long timeout, TimeUnit unit) throws InterruptedException {
        QueuedTask head = queue.poll(timeout, unit);
        if (head != null) {
            depth.decrementAndGet();
        }
        return head;
    }

    /** Non-blocking peek of the highest-priority element (diagnostics only). */
    public QueuedTask peek() {
        return queue.peek();
    }

    public int depth() {
        return depth.get();
    }
}