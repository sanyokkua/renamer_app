package ua.renamer.app.backend.service;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.concurrent.*;

/**
 * Provides a dedicated state thread and a virtual thread pool for the backend pipeline.
 *
 * <p>All mutations to {@link ua.renamer.app.backend.session.RenameSession} MUST be submitted
 * via {@link #submitStateChange(Callable)} to guarantee serial execution and thread safety.
 * I/O-bound pipeline work (V2 {@code FileRenameOrchestrator} calls) is submitted via
 * {@link #submitWork(Callable)}, which runs on an unbounded virtual thread pool.
 *
 * <p>Call {@link #close()} on application shutdown to release both executors gracefully.
 * Both executors use daemon threads and will not prevent JVM exit.
 */
@Slf4j
@Singleton
public class BackendExecutor implements Closeable {

    private final ExecutorService stateThread = Executors.newSingleThreadExecutor(
            r -> {
                Thread t = new Thread(r, "backend-state");
                t.setDaemon(true);
                return t;
            });

    private final ExecutorService virtualPool = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Submits a state mutation to the single state thread.
     * Mutations submitted concurrently are serialized in submission order.
     *
     * @param <T>      the return type of the mutation
     * @param mutation the state mutation to execute; must not be null
     * @return a {@link CompletableFuture} that completes with the mutation result,
     * or completes exceptionally if the callable throws or the executor is shut down
     */
    public <T> CompletableFuture<T> submitStateChange(Callable<T> mutation) {
        log.debug("Submitting state change to backend-state thread");
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return mutation.call();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, stateThread);
        } catch (RejectedExecutionException e) {
            return CompletableFuture.failedFuture(new CompletionException(e));
        }
    }

    /**
     * Submits I/O-bound work to the virtual thread pool.
     * Each submitted callable runs on a new virtual thread.
     *
     * @param <T>  the return type of the work
     * @param work the work to execute; must not be null
     * @return a {@link CompletableFuture} that completes with the work result,
     * or completes exceptionally if the callable throws or the executor is shut down
     */
    public <T> CompletableFuture<T> submitWork(Callable<T> work) {
        log.debug("Submitting work to virtual thread pool");
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return work.call();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, virtualPool);
        } catch (RejectedExecutionException e) {
            return CompletableFuture.failedFuture(new CompletionException(e));
        }
    }

    /**
     * Initiates an orderly shutdown of both executors.
     * Previously submitted tasks continue to completion; no new tasks are accepted.
     */
    @Override
    public void close() {
        log.info("Shutting down BackendExecutor — state thread and virtual pool");
        stateThread.shutdown();
        virtualPool.shutdown();
    }
}
