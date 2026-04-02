package ua.renamer.app.backend.session;

import ua.renamer.app.api.session.TaskHandle;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Package-private implementation of {@link TaskHandle} that wraps a {@link CompletableFuture}.
 *
 * <p>Full progress listener fan-out will be added in TASK-3.5.
 * This stub provides the handle contract required by {@link RenameSessionService#execute()}.
 *
 * @param <T> the type of the task's result value
 */
class TaskHandleImpl<T> implements TaskHandle<T> {

    private final String id = UUID.randomUUID().toString();
    private final CompletableFuture<T> future;
    private volatile boolean cancelRequested = false;

    /**
     * Creates a new {@code TaskHandleImpl} wrapping the given future.
     *
     * @param future the future representing the async task result; must not be null
     */
    TaskHandleImpl(CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public String taskId() {
        return id;
    }

    @Override
    public CompletableFuture<T> result() {
        return future;
    }

    @Override
    public void requestCancellation() {
        cancelRequested = true;
    }

    @Override
    public boolean isCancellationRequested() {
        return cancelRequested;
    }

    @Override
    public void addProgressListener(TaskHandle.ProgressListener listener) {
        // Full implementation in TASK-3.5
    }

    @Override
    public void removeProgressListener(TaskHandle.ProgressListener listener) {
        // Full implementation in TASK-3.5
    }

    /**
     * Notifies registered progress listeners of task progress.
     * Fan-out to registered listeners will be added in TASK-3.5.
     *
     * @param done  units of work completed
     * @param total total units of work expected
     * @param msg   human-readable description of the current step; may be null
     */
    void notifyProgress(double done, double total, String msg) {
        // Fan-out to registered listeners — TASK-3.5
    }
}
