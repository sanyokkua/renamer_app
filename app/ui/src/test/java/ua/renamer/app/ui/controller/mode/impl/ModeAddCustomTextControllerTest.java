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
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.AddTextParams;
import ua.renamer.app.api.session.ModeApi;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ModeAddCustomTextController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 *   <li>FX tests for {@code updateCommand()} — verifies V1-bridge position mapping</li>
 * </ul>
 *
 * <p>{@code textField} and {@code itemPositionRadioSelector} are {@code @FXML}-injected fields;
 * in tests they are injected via reflection (the package is unconditionally opened
 * in {@code module-info.java}).
 */
@ExtendWith(MockitoExtension.class)
class ModeAddCustomTextControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    @Mock
    private ModeApi<AddTextParams> modeApi;

    private ItemPositionConverter itemPositionConverter;
    private ModeAddCustomTextController controller;

    // -----------------------------------------------------------------------
    // Toolkit bootstrap — shared across all nested classes
    // -----------------------------------------------------------------------

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            // Toolkit already running in this JVM (e.g. from a prior test class).
            latch.countDown();
        }
        assertThat(latch.await(FX_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                .as("JavaFX toolkit must start within timeout").isTrue();
    }

    @BeforeEach
    void setUp() throws Exception {
        itemPositionConverter = new ItemPositionConverter(languageTextRetriever);
        controller = new ModeAddCustomTextController();
        injectTextField(controller, new TextField());
        injectRadioSelector(controller, new ItemPositionRadioSelector("", itemPositionConverter));
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject / read @FXML fields
    // -----------------------------------------------------------------------

    private static void injectTextField(ModeAddCustomTextController target, TextField tf) throws Exception {
        Field f = ModeAddCustomTextController.class.getDeclaredField("textField");
        f.setAccessible(true);
        f.set(target, tf);
    }

    private static void injectRadioSelector(ModeAddCustomTextController target, ItemPositionRadioSelector sel)
            throws Exception {
        Field f = ModeAddCustomTextController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        f.set(target, sel);
    }

    private static TextField readTextField(ModeAddCustomTextController target) throws Exception {
        Field f = ModeAddCustomTextController.class.getDeclaredField("textField");
        f.setAccessible(true);
        return (TextField) f.get(target);
    }

    private static ItemPositionRadioSelector readRadioSelector(ModeAddCustomTextController target) throws Exception {
        Field f = ModeAddCustomTextController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        return (ItemPositionRadioSelector) f.get(target);
    }

    private static TextField readTextFieldUnchecked(ModeAddCustomTextController target) {
        try {
            return readTextField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemPositionRadioSelector readRadioSelectorUnchecked(ModeAddCustomTextController target) {
        try {
            return readRadioSelector(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // Pure (no-FX) tests
    // -----------------------------------------------------------------------

    @Nested
    class SupportedModeTests {

        @Test
        void supportedMode_returnsAddText() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.ADD_TEXT);
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

    // -----------------------------------------------------------------------
    // bind() — FX thread required for JavaFX control interaction
    // -----------------------------------------------------------------------

    @Nested
    class BindTests {

        @Test
        void bind_initialisesTextFieldFromCurrentParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams("hello", ItemPosition.BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getText()).isEqualTo("hello");
        }

        @Test
        void bind_initialisesTextFieldEmpty_whenTextToAddIsNull() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams(null, ItemPosition.BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getText()).isEqualTo("");
        }

        @Test
        void bind_initialisesPositionToBegin() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams("text", ItemPosition.BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ItemPositionRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue()).isEqualTo(ua.renamer.app.core.enums.ItemPosition.BEGIN);
        }

        @Test
        void bind_initialisesPositionToEnd() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams("text", ItemPosition.END));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ItemPositionRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue()).isEqualTo(ua.renamer.app.core.enums.ItemPosition.END);
        }

        @Test
        void bind_textFieldChange_callsUpdateParametersWithNewText() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams("initial", ItemPosition.BEGIN));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — simulate user typing a new value
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("changed"));

            // Assert — capture the mutator and verify it applies withTextToAdd correctly
            ArgumentCaptor<ModeApi.ParamMutator<AddTextParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            AddTextParams original = new AddTextParams("initial", ItemPosition.BEGIN);
            AddTextParams updated = captor.getValue().apply(original);
            assertThat(updated.textToAdd()).isEqualTo("changed");
        }

        @Test
        void bind_positionChange_callsUpdateParametersWithNewPosition() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams("text", ItemPosition.BEGIN));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — select the END radio button programmatically
            runOnFxThreadAndWait(() -> {
                ItemPositionRadioSelector selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == ua.renamer.app.core.enums.ItemPosition.END)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert — capture the mutator and verify it applies withPosition(api.END) correctly
            ArgumentCaptor<ModeApi.ParamMutator<AddTextParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            AddTextParams original = new AddTextParams("text", ItemPosition.BEGIN);
            AddTextParams updated = captor.getValue().apply(original);
            assertThat(updated.position()).isEqualTo(ItemPosition.END);
        }

        @Test
        void bind_onValidationError_textFieldStyleSetToRed() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams("", ItemPosition.BEGIN));
            CompletableFuture<ValidationResult> errorFuture =
                    CompletableFuture.completedFuture(
                            ValidationResult.fieldError("textToAdd", "must not be null"));
            when(modeApi.updateParameters(any())).thenReturn(errorFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change text to trigger the listener
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("bad"));
            // Drain any runLater callbacks queued by the thenAccept in bind()
            drainFxQueue();

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getStyle()).contains("-fx-border-color: red;");
        }

        @Test
        void bind_onValidationSuccess_textFieldStyleCleared() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams("", ItemPosition.BEGIN));
            CompletableFuture<ValidationResult> validFuture =
                    CompletableFuture.completedFuture(ValidationResult.valid());
            when(modeApi.updateParameters(any())).thenReturn(validFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — trigger listener and drain queue
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("good"));
            drainFxQueue();

            // Assert
            TextField tf = readTextField(controller);
            assertThat(tf.getStyle()).isEqualTo("");
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new AddTextParams("hello", ItemPosition.BEGIN));

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }

    // -----------------------------------------------------------------------
    // FX threading utilities
    // -----------------------------------------------------------------------

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

    /**
     * Submits a no-op task to the FX queue and waits for it — this ensures all
     * {@code Platform.runLater()} calls queued before this point have executed.
     */
    private static void drainFxQueue() throws Exception {
        runOnFxThreadAndWait(() -> {
            /* intentional no-op — drains previously queued runLater callbacks */
        });
    }

    @FunctionalInterface
    private interface RunnableEx {
        void run() throws Exception;
    }
}
