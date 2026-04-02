package ua.renamer.app.backend.session;

import ua.renamer.app.api.session.TaskHandle;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Package-private implementation of {@link TaskHandle} that wraps a {@link CompletableFuture}.
 *
 * <p>Cancellation sets a cooperative flag and calls {@link CompletableFuture#cancel(boolean) cancel(false)}
 * on the underlying future — no interrupt is sent so that mid-rename virtual threads are not corrupted.
 * Progress listeners are stored in a {@link CopyOnWriteArrayList}; {@code addIfAbsent} silently drops
 * duplicate registrations as required by the interface contract.
 *
 * @param <T> the type of the task's result value
 */
class TaskHandleImpl<T> implements TaskHandle<T> {

    private final String id = UUID.randomUUID().toString();
    private final CompletableFuture<T> future;
    private final CopyOnWriteArrayList<TaskHandle.ProgressListener> listeners =
            new CopyOnWriteArrayList<>();
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
        future.cancel(false);
    }

    @Override
    public boolean isCancellationRequested() {
        return cancelRequested;
    }

    @Override
    public void addProgressListener(TaskHandle.ProgressListener listener) {
        listeners.addIfAbsent(listener);
    }

    @Override
    public void removeProgressListener(TaskHandle.ProgressListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered progress listeners of task progress.
     *
     * <p>Called from the virtual-thread work pool inside {@link RenameSessionService#execute()}.
     *
     * @param done  units of work completed; {@code -1} if indeterminate
     * @param total total units of work expected; {@code -1} if indeterminate
     * @param msg   human-readable description of the current step; may be null
     */
    void notifyProgress(double done, double total, String msg) {
        listeners.forEach(l -> l.onProgress(done, total, msg));
    }
}
