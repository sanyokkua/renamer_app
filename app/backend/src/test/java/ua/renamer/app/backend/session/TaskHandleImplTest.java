package ua.renamer.app.backend.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ua.renamer.app.api.session.TaskHandle;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link TaskHandleImpl}.
 *
 * <p>No Mockito or threading infrastructure needed — all behaviour is exercised
 * directly against a {@link CompletableFuture} and the handle's listener list.
 */
class TaskHandleImplTest {

    private CompletableFuture<String> future;
    private TaskHandleImpl<String> handle;

    @BeforeEach
    void setUp() {
        future = new CompletableFuture<>();
        handle = new TaskHandleImpl<>(future);
    }

    // =========================================================================
    // taskId
    // =========================================================================

    @Nested
    class TaskIdTests {

        @Test
        void givenHandle_whenTaskId_thenNonNullAndUnique() {
            TaskHandleImpl<String> other = new TaskHandleImpl<>(new CompletableFuture<>());

            assertThat(handle.taskId()).isNotNull();
            assertThat(handle.taskId()).isNotEqualTo(other.taskId());
        }
    }

    // =========================================================================
    // Cancellation
    // =========================================================================

    @Nested
    class CancellationTests {

        @Test
        void givenNoCancellation_whenIsCancellationRequested_thenFalse() {
            assertThat(handle.isCancellationRequested()).isFalse();
        }

        @Test
        void givenTaskHandleCancel_whenRequested_thenCancellationFlagSet() {
            handle.requestCancellation();

            assertThat(handle.isCancellationRequested()).isTrue();
            assertThat(handle.result().isCancelled()).isTrue();
        }

        @Test
        void givenCancellationRequested_whenResult_thenFutureIsCancelled() {
            handle.requestCancellation();

            assertThat(future.isCancelled()).isTrue();
        }
    }

    // =========================================================================
    // Progress listeners
    // =========================================================================

    @Nested
    class ProgressListenerTests {

        @Test
        void givenListener_whenNotifyProgress_thenListenerReceivesValues() {
            double[] received = new double[2];
            handle.addProgressListener((w, t, m) -> {
                received[0] = w;
                received[1] = t;
            });

            handle.notifyProgress(3.0, 10.0, "step 1");

            assertThat(received[0]).isEqualTo(3.0);
            assertThat(received[1]).isEqualTo(10.0);
        }

        @Test
        void givenListenerAddedTwice_whenNotifyProgress_thenCalledOnce() {
            AtomicInteger counter = new AtomicInteger();
            TaskHandle.ProgressListener listener = (w, t, m) -> counter.incrementAndGet();

            handle.addProgressListener(listener);
            handle.addProgressListener(listener); // duplicate — must be ignored

            handle.notifyProgress(1.0, 1.0, null);

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        void givenListenerRemoved_whenNotifyProgress_thenNotCalled() {
            AtomicInteger counter = new AtomicInteger();
            TaskHandle.ProgressListener listener = (w, t, m) -> counter.incrementAndGet();

            handle.addProgressListener(listener);
            handle.removeProgressListener(listener);

            handle.notifyProgress(1.0, 1.0, null);

            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        void givenNoListeners_whenNotifyProgress_thenNoException() {
            assertThatCode(() -> handle.notifyProgress(0.0, 1.0, null))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // result()
    // =========================================================================

    @Nested
    class ResultTests {

        @Test
        void givenHandle_whenResult_thenReturnsSameFuture() {
            assertThat(handle.result()).isSameAs(future);
        }

        @Test
        void givenFutureCompleted_whenResult_thenCompletedValueAccessible() throws Exception {
            future.complete("done");

            assertThat(handle.result().get()).isEqualTo("done");
        }
    }
}
