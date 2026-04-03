package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.enums.TextCaseOptions;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ChangeCaseParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.ui.converter.TextCaseOptionsConverter;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

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
 * Unit tests for {@link ModeChangeCaseController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 *   <li>FX tests for {@code updateCommand()} — verifies V1-bridge enum mapping</li>
 * </ul>
 *
 * <p>{@code caseChoiceBox} and {@code capitalizeCheckBox} are {@code @FXML}-injected fields;
 * in tests they are injected via reflection (the package is unconditionally opened
 * in {@code module-info.java}).
 */
@ExtendWith(MockitoExtension.class)
class ModeChangeCaseControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    @Mock
    private ModeApi<ChangeCaseParams> modeApi;

    private TextCaseOptionsConverter converter;
    private ModeChangeCaseController controller;

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
        converter = new TextCaseOptionsConverter(languageTextRetriever);
        controller = new ModeChangeCaseController(converter);
        injectChoiceBox(controller, new ChoiceBox<>());
        injectCheckBox(controller, new CheckBox());
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject / read @FXML fields
    // -----------------------------------------------------------------------

    private static void injectChoiceBox(ModeChangeCaseController target, ChoiceBox<TextCaseOptions> box)
            throws Exception {
        Field field = ModeChangeCaseController.class.getDeclaredField("caseChoiceBox");
        field.setAccessible(true);
        field.set(target, box);
    }

    private static void injectCheckBox(ModeChangeCaseController target, CheckBox box)
            throws Exception {
        Field field = ModeChangeCaseController.class.getDeclaredField("capitalizeCheckBox");
        field.setAccessible(true);
        field.set(target, box);
    }

    @SuppressWarnings("unchecked")
    private static ChoiceBox<TextCaseOptions> readChoiceBox(ModeChangeCaseController target) throws Exception {
        Field field = ModeChangeCaseController.class.getDeclaredField("caseChoiceBox");
        field.setAccessible(true);
        return (ChoiceBox<TextCaseOptions>) field.get(target);
    }

    private static CheckBox readCheckBox(ModeChangeCaseController target) throws Exception {
        Field field = ModeChangeCaseController.class.getDeclaredField("capitalizeCheckBox");
        field.setAccessible(true);
        return (CheckBox) field.get(target);
    }

    @SuppressWarnings("unchecked")
    private static ChoiceBox<TextCaseOptions> readChoiceBoxUnchecked(ModeChangeCaseController target) {
        try {
            return readChoiceBox(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static CheckBox readCheckBoxUnchecked(ModeChangeCaseController target) {
        try {
            return readCheckBox(target);
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
        void supportedMode_returnsChangeCase() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.CHANGE_CASE);
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
        void bind_initialisesChoiceBoxFromCurrentParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ChangeCaseParams(TextCaseOptions.SNAKE_CASE, false));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ChoiceBox<TextCaseOptions> box = readChoiceBox(controller);
            assertThat(box.getValue()).isEqualTo(TextCaseOptions.SNAKE_CASE);
        }

        @Test
        void bind_initialisesCheckBoxFromCurrentParams_whenCapitalizeIsTrue() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ChangeCaseParams(TextCaseOptions.UPPERCASE, true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            CheckBox cb = readCheckBox(controller);
            assertThat(cb.isSelected()).isTrue();
        }

        @Test
        void bind_initialisesCheckBoxFromCurrentParams_whenCapitalizeIsFalse() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ChangeCaseParams(TextCaseOptions.LOWERCASE, false));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            CheckBox cb = readCheckBox(controller);
            assertThat(cb.isSelected()).isFalse();
        }

        @Test
        void bind_choiceBoxChange_callsUpdateParametersWithNewCaseOption() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ChangeCaseParams(TextCaseOptions.CAMEL_CASE, false));
            CompletableFuture<ValidationResult> future =
                    CompletableFuture.completedFuture(ValidationResult.valid());
            when(modeApi.updateParameters(any())).thenReturn(future);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — simulate user picking a different case option
            runOnFxThreadAndWait(() -> readChoiceBoxUnchecked(controller).setValue(TextCaseOptions.TITLE_CASE));

            // Assert — capture the mutator and verify it applies the right option
            ArgumentCaptor<ModeApi.ParamMutator<ChangeCaseParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ChangeCaseParams original = new ChangeCaseParams(TextCaseOptions.CAMEL_CASE, false);
            ChangeCaseParams updated = captor.getValue().apply(original);
            assertThat(updated.caseOption()).isEqualTo(TextCaseOptions.TITLE_CASE);
        }

        @Test
        void bind_checkBoxToggle_callsUpdateParametersWithNewCapitalizeFlag() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ChangeCaseParams(TextCaseOptions.PASCAL_CASE, false));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — toggle the CheckBox
            runOnFxThreadAndWait(() -> readCheckBoxUnchecked(controller).setSelected(true));

            // Assert — capture the mutator and verify the capitalize flag
            ArgumentCaptor<ModeApi.ParamMutator<ChangeCaseParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ChangeCaseParams original = new ChangeCaseParams(TextCaseOptions.PASCAL_CASE, false);
            ChangeCaseParams updated = captor.getValue().apply(original);
            assertThat(updated.capitalizeFirstLetter()).isTrue();
        }

        @Test
        void bind_onValidationError_choiceBoxStyleSetToRed() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ChangeCaseParams(TextCaseOptions.CAMEL_CASE, false));
            CompletableFuture<ValidationResult> errorFuture =
                    CompletableFuture.completedFuture(
                            ValidationResult.fieldError("caseOption", "must not be null"));
            when(modeApi.updateParameters(any())).thenReturn(errorFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — fire an action on the ChoiceBox to trigger the listener
            runOnFxThreadAndWait(() -> readChoiceBoxUnchecked(controller).setValue(TextCaseOptions.KEBAB_CASE));
            // Drain any runLater callbacks queued by the thenAccept in bind()
            drainFxQueue();

            // Assert
            ChoiceBox<TextCaseOptions> box = readChoiceBox(controller);
            assertThat(box.getStyle()).contains("-fx-border-color: red;");
        }

        @Test
        void bind_onValidationSuccess_choiceBoxStyleCleared() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ChangeCaseParams(TextCaseOptions.CAMEL_CASE, false));
            CompletableFuture<ValidationResult> validFuture =
                    CompletableFuture.completedFuture(ValidationResult.valid());
            when(modeApi.updateParameters(any())).thenReturn(validFuture);

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — trigger the listener and drain the queue
            runOnFxThreadAndWait(() -> readChoiceBoxUnchecked(controller).setValue(TextCaseOptions.UPPERCASE));
            drainFxQueue();

            // Assert
            ChoiceBox<TextCaseOptions> box = readChoiceBox(controller);
            assertThat(box.getStyle()).isEqualTo("");
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ChangeCaseParams(TextCaseOptions.LOWERCASE, false));

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
