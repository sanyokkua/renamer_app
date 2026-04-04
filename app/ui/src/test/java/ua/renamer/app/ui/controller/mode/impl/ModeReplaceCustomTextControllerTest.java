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
import ua.renamer.app.api.enums.ItemPositionExtended;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ReplaceTextParams;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.ui.converter.ItemPositionExtendedConverter;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.widget.impl.ItemPositionExtendedRadioSelector;

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
 * Unit tests for {@link ModeReplaceCustomTextController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 * </ul>
 *
 * <p>{@code textToReplaceTextField}, {@code textToAddTextField}, and
 * {@code itemPositionRadioSelector} are {@code @FXML}-injected fields;
 * in tests they are injected via reflection (the package is unconditionally opened
 * in {@code module-info.java}).
 */
@ExtendWith(MockitoExtension.class)
class ModeReplaceCustomTextControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    @Mock
    private ModeApi<ReplaceTextParams> modeApi;

    private ModeReplaceCustomTextController controller;

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

    private static void injectTextToReplaceField(ModeReplaceCustomTextController target, TextField tf)
            throws Exception {
        Field f = ModeReplaceCustomTextController.class.getDeclaredField("textToReplaceTextField");
        f.setAccessible(true);
        f.set(target, tf);
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject / read @FXML fields
    // -----------------------------------------------------------------------

    private static void injectTextToAddField(ModeReplaceCustomTextController target, TextField tf)
            throws Exception {
        Field f = ModeReplaceCustomTextController.class.getDeclaredField("textToAddTextField");
        f.setAccessible(true);
        f.set(target, tf);
    }

    private static void injectRadioSelector(ModeReplaceCustomTextController target,
                                            ItemPositionExtendedRadioSelector sel) throws Exception {
        Field f = ModeReplaceCustomTextController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        f.set(target, sel);
    }

    private static TextField readTextToReplaceField(ModeReplaceCustomTextController target) throws Exception {
        Field f = ModeReplaceCustomTextController.class.getDeclaredField("textToReplaceTextField");
        f.setAccessible(true);
        return (TextField) f.get(target);
    }

    private static TextField readTextToAddField(ModeReplaceCustomTextController target) throws Exception {
        Field f = ModeReplaceCustomTextController.class.getDeclaredField("textToAddTextField");
        f.setAccessible(true);
        return (TextField) f.get(target);
    }

    private static ItemPositionExtendedRadioSelector readRadioSelector(ModeReplaceCustomTextController target)
            throws Exception {
        Field f = ModeReplaceCustomTextController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        return (ItemPositionExtendedRadioSelector) f.get(target);
    }

    private static TextField readTextToReplaceFieldUnchecked(ModeReplaceCustomTextController target) {
        try {
            return readTextToReplaceField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TextField readTextToAddFieldUnchecked(ModeReplaceCustomTextController target) {
        try {
            return readTextToAddField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemPositionExtendedRadioSelector readRadioSelectorUnchecked(
            ModeReplaceCustomTextController target) {
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
        var converter = new ItemPositionExtendedConverter(languageTextRetriever);
        controller = new ModeReplaceCustomTextController();
        injectTextToReplaceField(controller, new TextField());
        injectTextToAddField(controller, new TextField());
        injectRadioSelector(controller, new ItemPositionExtendedRadioSelector(converter, ""));
    }

    @FunctionalInterface
    private interface RunnableEx {
        void run() throws Exception;
    }

    @Nested
    class SupportedModeTests {

        @Test
        void supportedMode_returnsReplaceText() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.REPLACE_TEXT);
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
        void bind_initialisesTextToReplaceFieldFromCurrentParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("hello", "rep", ItemPositionExtended.EVERYWHERE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextToReplaceField(controller);
            assertThat(tf.getText()).isEqualTo("hello");
        }

        @Test
        void bind_initialisesTextToReplaceFieldEmpty_whenTextToReplaceIsNull() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams(null, "rep", ItemPositionExtended.EVERYWHERE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextToReplaceField(controller);
            assertThat(tf.getText()).isEqualTo("");
        }

        @Test
        void bind_initialisesReplacementTextFieldFromCurrentParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("txt", "world", ItemPositionExtended.EVERYWHERE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextToAddField(controller);
            assertThat(tf.getText()).isEqualTo("world");
        }

        @Test
        void bind_initialisesReplacementTextFieldEmpty_whenReplacementTextIsNull() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("txt", null, ItemPositionExtended.EVERYWHERE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField tf = readTextToAddField(controller);
            assertThat(tf.getText()).isEqualTo("");
        }

        @Test
        void bind_initialisesPositionToBegin() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("txt", "rep", ItemPositionExtended.BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ItemPositionExtendedRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue())
                    .isEqualTo(ua.renamer.app.api.enums.ItemPositionExtended.BEGIN);
        }

        @Test
        void bind_initialisesPositionToEverywhere() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("txt", "rep", ItemPositionExtended.EVERYWHERE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ItemPositionExtendedRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue())
                    .isEqualTo(ua.renamer.app.api.enums.ItemPositionExtended.EVERYWHERE);
        }

        @Test
        void bind_textToReplaceChange_callsUpdateParametersWithNewText() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("txt", "rep", ItemPositionExtended.EVERYWHERE));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — simulate user typing a new value
            runOnFxThreadAndWait(() -> readTextToReplaceFieldUnchecked(controller).setText("changed"));

            // Assert — capture the mutator and verify it applies withTextToReplace correctly
            ArgumentCaptor<ModeApi.ParamMutator<ReplaceTextParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ReplaceTextParams original = new ReplaceTextParams("txt", "rep", ItemPositionExtended.EVERYWHERE);
            ReplaceTextParams updated = captor.getValue().apply(original);
            assertThat(updated.textToReplace()).isEqualTo("changed");
        }

        @Test
        void bind_replacementTextChange_callsUpdateParametersWithNewReplacementText() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("txt", "rep", ItemPositionExtended.EVERYWHERE));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — simulate user typing a new replacement value
            runOnFxThreadAndWait(() -> readTextToAddFieldUnchecked(controller).setText("newrep"));

            // Assert — capture the mutator and verify it applies withReplacementText correctly
            ArgumentCaptor<ModeApi.ParamMutator<ReplaceTextParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ReplaceTextParams original = new ReplaceTextParams("txt", "rep", ItemPositionExtended.EVERYWHERE);
            ReplaceTextParams updated = captor.getValue().apply(original);
            assertThat(updated.replacementText()).isEqualTo("newrep");
        }

        @Test
        void bind_onValidationError_textToReplaceFieldStyleSetToRed() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("", "rep", ItemPositionExtended.EVERYWHERE));
            CompletableFuture<ValidationResult> errorFuture =
                    CompletableFuture.completedFuture(
                            ValidationResult.fieldError("field", "must not be null"));
            when(modeApi.updateParameters(any())).thenReturn(errorFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change text to trigger the listener
            runOnFxThreadAndWait(() -> readTextToReplaceFieldUnchecked(controller).setText("bad"));
            // Drain any runLater callbacks queued by the thenAccept in bind()
            drainFxQueue();

            // Assert
            TextField tf = readTextToReplaceField(controller);
            assertThat(tf.getStyle()).contains("-fx-border-color: red;");
        }

        @Test
        void bind_onValidationError_replacementTextFieldStyleSetToRed() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("txt", "", ItemPositionExtended.EVERYWHERE));
            CompletableFuture<ValidationResult> errorFuture =
                    CompletableFuture.completedFuture(
                            ValidationResult.fieldError("field", "must not be null"));
            when(modeApi.updateParameters(any())).thenReturn(errorFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change replacement text to trigger the listener
            runOnFxThreadAndWait(() -> readTextToAddFieldUnchecked(controller).setText("bad"));
            // Drain any runLater callbacks queued by the thenAccept in bind()
            drainFxQueue();

            // Assert
            TextField tf = readTextToAddField(controller);
            assertThat(tf.getStyle()).contains("-fx-border-color: red;");
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ReplaceTextParams("txt", "rep", ItemPositionExtended.EVERYWHERE));

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }
}
