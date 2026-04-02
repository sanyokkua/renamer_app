package ua.renamer.app.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link BackendExecutor}.
 *
 * <p>Contract under test:
 * <ul>
 *   <li>{@code submitStateChange} serialises work on the single "backend-state" thread.</li>
 *   <li>{@code submitWork} runs each task on a virtual thread.</li>
 *   <li>Both methods wrap callable exceptions in {@code CompletionException} internally, but
 *       {@code CompletableFuture.get()} unwraps one {@code CompletionException} layer, so
 *       callers see: {@code ExecutionException} → original cause.</li>
 *   <li>Post-{@link BackendExecutor#close()} submissions complete exceptionally with
 *       {@code ExecutionException} → {@link RejectedExecutionException} (the
 *       {@code CompletionException} wrapper is again unwrapped by {@code get()}).</li>
 * </ul>
 */
class BackendExecutorTest {

    private BackendExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new BackendExecutor();
    }

    @AfterEach
    void tearDown() {
        executor.close();
    }

    // =========================================================================
    // State-thread tests
    // =========================================================================

    @Nested
    class StateThreadTests {

        @Test
        void givenMutation_whenSubmitStateChange_thenRunsOnStateThread() throws Exception {
            // Arrange — capture the name of the thread that executes the callable
            CompletableFuture<String> future = executor.submitStateChange(
                    () -> Thread.currentThread().getName()
            );

            // Act
            String threadName = future.get(5, TimeUnit.SECONDS);

            // Assert
            assertThat(threadName).isEqualTo("backend-state");
        }

        @Test
        void givenMultipleMutations_whenSubmitStateChangeConcurrently_thenAllExecuteOnStateThread()
                throws Exception {
            // Arrange
            List<String> capturedNames = new CopyOnWriteArrayList<>();
            int taskCount = 20;

            // Act — submit 20 tasks from the test thread; they are queued on the single state thread
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < taskCount; i++) {
                CompletableFuture<Void> f = executor.submitStateChange(() -> {
                    capturedNames.add(Thread.currentThread().getName());
                    return null;
                }).thenApply(ignored -> null);
                futures.add(f);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(5, TimeUnit.SECONDS);

            // Assert
            assertThat(capturedNames)
                    .hasSize(taskCount)
                    .containsOnly("backend-state");
        }
    }

    // =========================================================================
    // Virtual-pool tests
    // =========================================================================

    @Nested
    class VirtualPoolTests {

        @Test
        void givenWork_whenSubmitWork_thenRunsOnVirtualThread() throws Exception {
            // Arrange — capture whether the executing thread is virtual
            CompletableFuture<Boolean> future = executor.submitWork(
                    () -> Thread.currentThread().isVirtual()
            );

            // Act
            boolean isVirtual = future.get(5, TimeUnit.SECONDS);

            // Assert
            assertThat(isVirtual).isTrue();
        }
    }

    // =========================================================================
    // Exception-propagation tests
    // =========================================================================

    @Nested
    class ExceptionPropagationTests {

        @Test
        void givenThrowingCallable_whenSubmitStateChange_thenFutureCompletesExceptionally() {
            // Arrange
            RuntimeException cause = new RuntimeException("state-mutation failure");
            CompletableFuture<Object> future = executor.submitStateChange(() -> {
                throw cause;
            });

            // Act + Assert
            // The JDK's CompletableFuture.get() calls reportGet() → wrapInExecutionException(),
            // which unwraps one layer of CompletionException before re-wrapping in ExecutionException.
            // Actual chain: ExecutionException → RuntimeException (original cause).
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(RuntimeException.class)
                    .extracting(Throwable::getCause)       // the original RuntimeException
                    .isSameAs(cause);
        }

        @Test
        void givenThrowingCallable_whenSubmitWork_thenFutureCompletesExceptionally() {
            // Arrange
            RuntimeException cause = new RuntimeException("work failure");
            CompletableFuture<Object> future = executor.submitWork(() -> {
                throw cause;
            });

            // Act + Assert
            // Same JDK unwrapping: ExecutionException → RuntimeException (original cause).
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(RuntimeException.class)
                    .extracting(Throwable::getCause)       // the original RuntimeException
                    .isSameAs(cause);
        }
    }

    // =========================================================================
    // Close / rejection tests
    // =========================================================================

    @Nested
    class CloseTests {

        @Test
        void givenClosedExecutor_whenSubmitStateChange_thenFutureCompletesExceptionally() {
            // Arrange — use a local instance so @AfterEach closing the main executor
            // does not interfere with this test's state
            BackendExecutor local = new BackendExecutor();
            local.close();

            // Act
            CompletableFuture<Object> future = local.submitStateChange(() -> "should not run");

            // Assert: BackendExecutor wraps REE in CompletionException for failedFuture.
            // future.get() calls wrapInExecutionException() which unwraps the CompletionException,
            // so the actual chain is: ExecutionException → RejectedExecutionException.
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(RejectedExecutionException.class);
        }

        @Test
        void givenClosedExecutor_whenSubmitWork_thenFutureCompletesExceptionally() {
            // Arrange — same isolation pattern
            BackendExecutor local = new BackendExecutor();
            local.close();

            // Act
            CompletableFuture<Object> future = local.submitWork(() -> "should not run");

            // Assert: same JDK unwrapping behaviour as submitStateChange.
            // Actual chain: ExecutionException → RejectedExecutionException.
            assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(RejectedExecutionException.class);
        }
    }
}
