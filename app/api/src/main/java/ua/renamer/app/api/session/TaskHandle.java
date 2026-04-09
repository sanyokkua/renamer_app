package ua.renamer.app.api.session;

import java.util.concurrent.CompletableFuture;

/**
 * A handle to an asynchronous task, providing access to the result future,
 * cancellation control, and progress notifications.
 *
 * <p>Instances are returned by {@link SessionApi#execute()} and must not
 * carry any {@code javafx.*} types.
 *
 * @param <T> the type of the task's result value
 */
public interface TaskHandle<T> {

    /**
     * Returns the unique identifier for this task. Never null.
     */
    String taskId();

    /**
     * Returns a future that completes with the task result, completes exceptionally
     * on unrecoverable error, or is cancelled. Never null.
     */
    CompletableFuture<T> result();

    /**
     * Signals that the task should stop at the next safe cancellation point.
     * Cooperative — task may not stop immediately.
     */
    void requestCancellation();

    /**
     * Returns {@code true} if {@link #requestCancellation()} has been called,
     * regardless of whether the task has actually stopped yet.
     */
    boolean isCancellationRequested();

    /**
     * Registers a listener for progress updates. Duplicate registrations are ignored.
     */
    void addProgressListener(ProgressListener listener);

    /**
     * Removes a previously registered progress listener. No-op if not registered.
     */
    void removeProgressListener(ProgressListener listener);

    /**
     * Callback for receiving task progress notifications.
     */
    @FunctionalInterface
    interface ProgressListener {
        /**
         * Called by the task to report progress.
         *
         * @param workDone  units completed; {@code -1} if indeterminate
         * @param totalWork total units expected; {@code -1} if indeterminate
         * @param message   human-readable description of current step; may be null
         */
        void onProgress(double workDone, double totalWork, String message);
    }
}
