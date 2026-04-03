package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.RemoveTextParams;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.core.service.command.impl.preparation.RemoveTextPrepareInformationCommand;
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
 * Unit tests for {@link ModeRemoveCustomTextController}.
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
@ExtendWith(MockitoExtension.class)
class ModeRemoveCustomTextControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    @Mock
    private ModeApi<RemoveTextParams> modeApi;

    private ItemPositionConverter itemPositionConverter;
    private ModeRemoveCustomTextController controller;

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
        controller = new ModeRemoveCustomTextController();
        injectTextField(controller, new TextField());
        injectRadioSelector(controller, new ItemPositionRadioSelector("", itemPositionConverter));
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject / read @FXML fields
    // -----------------------------------------------------------------------

    private static void injectTextField(ModeRemoveCustomTextController target, TextField tf) throws Exception {
        Field f = ModeRemoveCustomTextController.class.getDeclaredField("removeTextField");
        f.setAccessible(true);
        f.set(target, tf);
    }

    private static void injectRadioSelector(ModeRemoveCustomTextController target, ItemPositionRadioSelector sel)
            throws Exception {
        Field f = ModeRemoveCustomTextController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        f.set(target, sel);
    }

    private static TextField readTextField(ModeRemoveCustomTextController target) throws Exception {
        Field f = ModeRemoveCustomTextController.class.getDeclaredField("removeTextField");
        f.setAccessible(true);
        return (TextField) f.get(target);
    }

    private static ItemPositionRadioSelector readRadioSelector(ModeRemoveCustomTextController target) throws Exception {
        Field f = ModeRemoveCustomTextController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        return (ItemPositionRadioSelector) f.get(target);
    }

    private static TextField readTextFieldUnchecked(ModeRemoveCustomTextController target) {
        try {
            return readTextField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemPositionRadioSelector readRadioSelectorUnchecked(ModeRemoveCustomTextController target) {
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

    // -----------------------------------------------------------------------
    // bind() — FX thread required for JavaFX control interaction
    // -----------------------------------------------------------------------

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
            assertThat(tf.getText()).isEqualTo("");
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
            assertThat(selector.getSelectedValue()).isEqualTo(ua.renamer.app.core.enums.ItemPosition.BEGIN);
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
            assertThat(selector.getSelectedValue()).isEqualTo(ua.renamer.app.core.enums.ItemPosition.END);
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
                        .filter(btn -> btn.getValue() == ua.renamer.app.core.enums.ItemPosition.END)
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
        void bind_onValidationError_textFieldStyleSetToRed() throws Exception {
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
            assertThat(tf.getStyle()).contains("-fx-border-color: red;");
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
            assertThat(tf.getStyle()).isEqualTo("");
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

    // -----------------------------------------------------------------------
    // updateCommand() — V1 bridge: widget state → RemoveTextPrepareInformationCommand
    // -----------------------------------------------------------------------

    @Nested
    class UpdateCommandTests {

        @Test
        void updateCommand_doesNotThrow() {
            // Arrange — set a non-empty value so updateCommand() reads sensible data
            runOnFxThreadAndWaitUnchecked(() -> readTextFieldUnchecked(controller).setText("test"));

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.updateCommand()))
                    .doesNotThrowAnyException();
        }

        @Test
        void updateCommand_setsCommandWithCorrectText() throws Exception {
            // Arrange
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("world"));

            // Act
            runOnFxThreadAndWait(() -> controller.updateCommand());

            // Assert
            var command = controller.getCommand();
            assertThat(command).isInstanceOf(RemoveTextPrepareInformationCommand.class);
            RemoveTextPrepareInformationCommand cmd = (RemoveTextPrepareInformationCommand) command;
            assertThat(cmd.getText()).isEqualTo("world");
        }

        @Test
        void updateCommand_setsCommandWithBeginPosition() throws Exception {
            // Arrange — widget selects BEGIN by default (first button selected in RadioSelector)
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("prefix"));

            // Act
            runOnFxThreadAndWait(() -> controller.updateCommand());

            // Assert
            RemoveTextPrepareInformationCommand cmd =
                    (RemoveTextPrepareInformationCommand) controller.getCommand();
            assertThat(cmd.getPosition()).isEqualTo(ua.renamer.app.core.enums.ItemPosition.BEGIN);
        }

        @Test
        void updateCommand_setsCommandWithEndPosition() throws Exception {
            // Arrange — programmatically select the END button
            runOnFxThreadAndWait(() -> {
                ItemPositionRadioSelector selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == ua.renamer.app.core.enums.ItemPosition.END)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
                readTextFieldUnchecked(controller).setText("suffix");
            });

            // Act
            runOnFxThreadAndWait(() -> controller.updateCommand());

            // Assert
            RemoveTextPrepareInformationCommand cmd =
                    (RemoveTextPrepareInformationCommand) controller.getCommand();
            assertThat(cmd.getPosition()).isEqualTo(ua.renamer.app.core.enums.ItemPosition.END);
        }

        @ParameterizedTest(name = "updateCommand produces non-null REMOVE_TEXT command for core position [{0}]")
        @EnumSource(ua.renamer.app.core.enums.ItemPosition.class)
        void updateCommand_allCorePositions_produceNonNullCommandOfCorrectType(
                ua.renamer.app.core.enums.ItemPosition corePosition) throws Exception {
            // Arrange — select the target position button
            runOnFxThreadAndWait(() -> {
                ItemPositionRadioSelector selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == corePosition)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Act
            runOnFxThreadAndWait(() -> controller.updateCommand());

            // Assert
            assertThat(controller.getCommand())
                    .isNotNull()
                    .isInstanceOf(RemoveTextPrepareInformationCommand.class);
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
