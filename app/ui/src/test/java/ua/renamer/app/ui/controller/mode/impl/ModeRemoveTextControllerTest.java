package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.RemoveTextParams;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.ui.converter.ItemPositionConverter;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ModeRemoveTextController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 *   <li>FX tests for {@code updateCommand()} — verifies V1-bridge position mapping</li>
 * </ul>
 *
 * <p>{@code removeTextField} and {@code itemPositionRadioSelector} are {@code @FXML}-injected fields;
 * in tests they are injected via reflection (the package is unconditionally opened
 * in {@code module-info.java}).
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class ModeRemoveTextControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    @Mock
    private ModeApi<RemoveTextParams> modeApi;

    private ItemPositionConverter itemPositionConverter;
    private ModeRemoveTextController controller;

    // -----------------------------------------------------------------------
    // Toolkit bootstrap — shared across all nested classes
    // -----------------------------------------------------------------------

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            log.debug("JavaFX toolkit already running, continuing. Exception: {}", e.getMessage());
            latch.countDown();
        }
        assertThat(latch.await(FX_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                .as("JavaFX toolkit must start within timeout").isTrue();
    }

    private static void injectTextField(ModeRemoveTextController target, TextField tf) throws Exception {
        Field f = ModeRemoveTextController.class.getDeclaredField("removeTextField");
        f.setAccessible(true);
        f.set(target, tf);
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject / read @FXML fields
    // -----------------------------------------------------------------------

    private static void injectRadioSelector(ModeRemoveTextController target, ItemPositionRadioSelector sel)
            throws Exception {
        Field f = ModeRemoveTextController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        f.set(target, sel);
    }


    private static TextField readTextField(ModeRemoveTextController target) throws Exception {
        Field f = ModeRemoveTextController.class.getDeclaredField("removeTextField");
        f.setAccessible(true);
        return (TextField) f.get(target);
    }

    private static ItemPositionRadioSelector readRadioSelector(ModeRemoveTextController target) throws Exception {
        Field f = ModeRemoveTextController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        return (ItemPositionRadioSelector) f.get(target);
    }

    private static TextField readTextFieldUnchecked(ModeRemoveTextController target) {
        try {
            return readTextField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemPositionRadioSelector readRadioSelectorUnchecked(ModeRemoveTextController target) {
        try {
            return readRadioSelector(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the given task on the FX Application Thread and blocks the calling
     * thread until the task completes or the timeout elapses.
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
     * Convenience overload that rethrows as {@link RuntimeException}.
     * Use only when the calling site cannot propagate a checked exception.
     */
    private static void runOnFxThreadAndWaitUnchecked(RunnableEx task) {
        try {
            runOnFxThreadAndWait(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // bind() — FX thread required for JavaFX control interaction
    // -----------------------------------------------------------------------

    /**
     * Submits a no-op task to the FX queue and waits for it — this ensures all
     * {@code Platform.runLater()} calls queued before this point have executed.
     */
    private static void drainFxQueue() throws Exception {
        runOnFxThreadAndWait(() -> {
            /* intentional no-op — drains previously queued runLater callbacks */
        });
    }

    // -----------------------------------------------------------------------
    // FX threading utilities
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() throws Exception {
        itemPositionConverter = new ItemPositionConverter(languageTextRetriever);
        controller = new ModeRemoveTextController();
        injectTextField(controller, new TextField());
        injectRadioSelector(controller, new ItemPositionRadioSelector("", itemPositionConverter));
    }

    @FunctionalInterface
    private interface RunnableEx {
        void run() throws Exception;
    }

    @Nested
    class SupportedModeTests {

        @Test
        void supportedMode_returnsRemoveText() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.REMOVE_TEXT);
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
        void bind_initialisesTextFieldFromCurrentParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams("hello", ItemPosition.BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getText()).isEqualTo("hello");
        }

        @Test
        void bind_initialisesTextFieldEmpty_whenTextToRemoveIsNull() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams(null, ItemPosition.BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getText()).isEmpty();
        }

        @Test
        void bind_initialisesPositionToBegin() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams("text", ItemPosition.BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ItemPositionRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue()).isEqualTo(ua.renamer.app.api.enums.ItemPosition.BEGIN);
        }

        @Test
        void bind_initialisesPositionToEnd() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams("text", ItemPosition.END));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ItemPositionRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue()).isEqualTo(ua.renamer.app.api.enums.ItemPosition.END);
        }

        @Test
        void bind_textFieldChange_callsUpdateParametersWithNewText() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams("initial", ItemPosition.BEGIN));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — simulate user typing a new value
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("changed"));

            // Assert — capture the mutator and verify it applies withTextToRemove correctly
            ArgumentCaptor<ModeApi.ParamMutator<RemoveTextParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            RemoveTextParams original = new RemoveTextParams("initial", ItemPosition.BEGIN);
            RemoveTextParams updated = captor.getValue().apply(original);
            assertThat(updated.textToRemove()).isEqualTo("changed");
        }

        @Test
        void bind_positionChange_callsUpdateParametersWithNewPosition() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams("text", ItemPosition.BEGIN));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — select the END radio button programmatically
            runOnFxThreadAndWait(() -> {
                ItemPositionRadioSelector selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == ua.renamer.app.api.enums.ItemPosition.END)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert — capture the mutator and verify it applies withPosition(api.END) correctly
            ArgumentCaptor<ModeApi.ParamMutator<RemoveTextParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            RemoveTextParams original = new RemoveTextParams("text", ItemPosition.BEGIN);
            RemoveTextParams updated = captor.getValue().apply(original);
            assertThat(updated.position()).isEqualTo(ItemPosition.END);
        }

        @Test
        void bind_onValidationError_textFieldValidationErrorClassAdded() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams("", ItemPosition.BEGIN));
            CompletableFuture<ValidationResult> errorFuture =
                    CompletableFuture.completedFuture(
                            ValidationResult.fieldError("textToRemove", "must not be null"));
            when(modeApi.updateParameters(any())).thenReturn(errorFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change text to trigger the listener
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("bad"));
            // Drain any runLater callbacks queued by the thenAccept in bind()
            drainFxQueue();

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getStyleClass()).contains("validation-error");
        }

        @Test
        void bind_onValidationSuccess_textFieldStyleCleared() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams("", ItemPosition.BEGIN));
            CompletableFuture<ValidationResult> validFuture =
                    CompletableFuture.completedFuture(ValidationResult.valid());
            when(modeApi.updateParameters(any())).thenReturn(validFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — trigger listener and drain queue
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("good"));
            drainFxQueue();

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getStyle()).isEmpty();
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new RemoveTextParams("hello", ItemPosition.BEGIN));

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }
}
