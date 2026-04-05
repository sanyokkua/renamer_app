package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ExtensionChangeParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ValidationResult;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ModeChangeExtensionController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 * </ul>
 *
 * <p>{@code extensionTextField} is an {@code @FXML}-injected field; in tests it is
 * set via reflection (the package is unconditionally opened in {@code module-info.java}).
 */
@ExtendWith(MockitoExtension.class)
class ModeChangeExtensionControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private ModeApi<ExtensionChangeParams> modeApi;

    private ModeChangeExtensionController controller;

    // -----------------------------------------------------------------------
    // Toolkit bootstrap — shared across all nested classes
    // -----------------------------------------------------------------------

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            // Toolkit already running in this JVM (e.g. prior test class).
            latch.countDown();
        }
        assertThat(latch.await(FX_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                .as("JavaFX toolkit must start within timeout").isTrue();
    }

    private static void injectTextField(ModeChangeExtensionController target, TextField tf)
            throws Exception {
        Field field = ModeChangeExtensionController.class.getDeclaredField("extensionTextField");
        field.setAccessible(true);
        field.set(target, tf);
    }


    // -----------------------------------------------------------------------
    // Helper: inject @FXML TextField via reflection
    // -----------------------------------------------------------------------

    private static TextField readTextField(ModeChangeExtensionController target) throws Exception {
        Field field = ModeChangeExtensionController.class.getDeclaredField("extensionTextField");
        field.setAccessible(true);
        return (TextField) field.get(target);
    }

    /**
     * Runs the given task on the FX Application Thread and blocks until it completes
     * or the timeout elapses.
     */
    private static void runOnFxThreadAndWait(RunnableEx task) throws Exception {
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                thrown.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertThat(latch.await(FX_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                .as("FX task must complete within timeout").isTrue();

        if (thrown.get() != null) {
            throw new RuntimeException("Exception on FX thread", thrown.get());
        }
    }

    // -----------------------------------------------------------------------
    // Pure (no-FX) tests
    // -----------------------------------------------------------------------

    /**
     * Submits a no-op task to the FX queue and waits for it — this ensures all
     * {@code Platform.runLater()} calls queued before this point have executed.
     */
    private static void drainFxQueue() throws Exception {
        runOnFxThreadAndWait(() -> {
            /* intentional no-op — just drains the queue */
        });
    }

    // -----------------------------------------------------------------------
    // bind() — FX thread required for TextField interaction
    // -----------------------------------------------------------------------

    /**
     * Reads the text field, rethrowing checked exceptions as unchecked.
     */
    private static TextField readTextFieldUnchecked(ModeChangeExtensionController target) {
        try {
            return readTextField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // FX threading utilities
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() throws Exception {
        controller = new ModeChangeExtensionController();
        injectTextField(controller, new TextField());
    }

    @FunctionalInterface
    private interface RunnableEx {
        void run() throws Exception;
    }

    @Nested
    class SupportedModeTests {

        @Test
        void supportedMode_returnsChangeExtension() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.CHANGE_EXTENSION);
        }

        @Test
        void supportedMode_doesNotReturnNull() {
            assertThat(controller.supportedMode()).isNotNull();
        }

        @Test
        void supportedMode_doesNotThrow() {
            assertThatCode(() -> controller.supportedMode()).doesNotThrowAnyException();
        }
    }

    @Nested
    class BindTests {

        @Test
        void bind_initializesTextFieldFromCurrentParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(new ExtensionChangeParams("mp4"));

            // Act — bind() reads currentParameters() and calls setText(); no Platform.runLater here
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getText()).isEqualTo("mp4");
        }

        @Test
        void bind_withEmptyExtension_initializesTextFieldToEmpty() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(new ExtensionChangeParams(""));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getText()).isEqualTo("");
        }

        @Test
        void bind_onTextChange_callsUpdateParametersWithNewValue() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(new ExtensionChangeParams("txt"));
            CompletableFuture<ValidationResult> future =
                    CompletableFuture.completedFuture(ValidationResult.valid());
            when(modeApi.updateParameters(any())).thenReturn(future);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — simulate the user typing a new extension
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("jpg"));

            // Assert — updateParameters was called; capture the mutator and verify it produces the right params
            ArgumentCaptor<ModeApi.ParamMutator<ExtensionChangeParams>> mutatorCaptor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi).updateParameters(mutatorCaptor.capture());

            ExtensionChangeParams updated = mutatorCaptor.getValue().apply(new ExtensionChangeParams("old"));
            assertThat(updated.newExtension()).isEqualTo("jpg");
        }

        @Test
        void bind_onValidResult_clearsTextFieldStyle() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(new ExtensionChangeParams("png"));
            CompletableFuture<ValidationResult> validFuture =
                    CompletableFuture.completedFuture(ValidationResult.valid());
            when(modeApi.updateParameters(any())).thenReturn(validFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — trigger listener and allow Platform.runLater inside thenAccept to execute
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("webp"));
            // Drain the runLater queued by the thenAccept callback
            drainFxQueue();

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getStyle()).isEqualTo("");
        }

        @Test
        void bind_onErrorResult_addsValidationErrorClassToTextField() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(new ExtensionChangeParams("pdf"));
            CompletableFuture<ValidationResult> errorFuture =
                    CompletableFuture.completedFuture(
                            ValidationResult.fieldError("newExtension", "must not be blank"));
            when(modeApi.updateParameters(any())).thenReturn(errorFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — trigger listener with a blank value
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText(""));
            // Drain the runLater queued by the thenAccept callback
            drainFxQueue();

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getStyleClass()).contains("validation-error");
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(new ExtensionChangeParams("mkv"));

            // Act + Assert — no exception must escape bind()
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }
}
