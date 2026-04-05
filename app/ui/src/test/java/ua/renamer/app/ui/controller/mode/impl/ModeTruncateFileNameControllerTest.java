package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.TruncateParams;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.ui.converter.TruncateOptionsConverter;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.widget.impl.ItemPositionTruncateRadioSelector;

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
 * Unit tests for {@link ModeTruncateFileNameController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 * </ul>
 *
 * <p>{@code amountOfSymbolsSpinner}, {@code itemPositionRadioSelector}, and
 * {@code amountOfSymbolsLabel} are {@code @FXML}-injected fields;
 * in tests they are injected via reflection (the package is unconditionally opened
 * in {@code module-info.java}).
 */
@ExtendWith(MockitoExtension.class)
class ModeTruncateFileNameControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    @Mock
    private ModeApi<TruncateParams> modeApi;

    private ModeTruncateFileNameController controller;

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

    private static void injectSpinner(ModeTruncateFileNameController target, Spinner<Integer> spinner)
            throws Exception {
        Field f = ModeTruncateFileNameController.class.getDeclaredField("amountOfSymbolsSpinner");
        f.setAccessible(true);
        f.set(target, spinner);
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject / read @FXML fields
    // -----------------------------------------------------------------------

    private static void injectRadioSelector(ModeTruncateFileNameController target,
                                            ItemPositionTruncateRadioSelector selector) throws Exception {
        Field f = ModeTruncateFileNameController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        f.set(target, selector);
    }

    private static void injectLabel(ModeTruncateFileNameController target, Label label) throws Exception {
        Field f = ModeTruncateFileNameController.class.getDeclaredField("amountOfSymbolsLabel");
        f.setAccessible(true);
        f.set(target, label);
    }


    @SuppressWarnings("unchecked")
    private static Spinner<Integer> readSpinner(ModeTruncateFileNameController target) throws Exception {
        Field f = ModeTruncateFileNameController.class.getDeclaredField("amountOfSymbolsSpinner");
        f.setAccessible(true);
        return (Spinner<Integer>) f.get(target);
    }

    private static ItemPositionTruncateRadioSelector readRadioSelector(ModeTruncateFileNameController target)
            throws Exception {
        Field f = ModeTruncateFileNameController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        return (ItemPositionTruncateRadioSelector) f.get(target);
    }

    private static Label readLabel(ModeTruncateFileNameController target) throws Exception {
        Field f = ModeTruncateFileNameController.class.getDeclaredField("amountOfSymbolsLabel");
        f.setAccessible(true);
        return (Label) f.get(target);
    }

    @SuppressWarnings("unchecked")
    private static Spinner<Integer> readSpinnerUnchecked(ModeTruncateFileNameController target) {
        try {
            return readSpinner(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemPositionTruncateRadioSelector readRadioSelectorUnchecked(
            ModeTruncateFileNameController target) {
        try {
            return readRadioSelector(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Label readLabelUnchecked(ModeTruncateFileNameController target) {
        try {
            return readLabel(target);
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
    @SuppressWarnings("unused")
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
        controller = new ModeTruncateFileNameController();

        var converter = new TruncateOptionsConverter(languageTextRetriever);

        // @FXML field 1: Spinner<Integer> with value factory, editable
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        spinner.setEditable(true);
        injectSpinner(controller, spinner);

        // @FXML field 2: ItemPositionTruncateRadioSelector
        injectRadioSelector(controller, new ItemPositionTruncateRadioSelector("", converter));

        // @FXML field 3: Label
        injectLabel(controller, new Label());
    }

    @FunctionalInterface
    private interface RunnableEx {
        void run() throws Exception;
    }

    @Nested
    class SupportedModeTests {

        @Test
        void supportedMode_returnsTruncateFileName() {
            // Act
            TransformationMode result = controller.supportedMode();

            // Assert
            assertThat(result).isEqualTo(TransformationMode.TRUNCATE_FILE_NAME);
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
        void bind_initialisesSpinnerFromCurrentParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(5, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readSpinner(controller);
            assertThat(spinner.getValue()).isEqualTo(5);
        }

        @Test
        void bind_initialisesSpinnerToZero_whenNumberOfSymbolsIsZero() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readSpinner(controller);
            assertThat(spinner.getValue()).isEqualTo(0);
        }

        @Test
        void bind_initialisesRadioSelector_toRemoveFromBegin() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ItemPositionTruncateRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue())
                    .isEqualTo(ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN);
        }

        @Test
        void bind_initialisesRadioSelector_toRemoveFromEnd() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_FROM_END));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ItemPositionTruncateRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue())
                    .isEqualTo(ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_FROM_END);
        }

        @Test
        void bind_hidesSpinnerAndLabel_whenTruncateEmptySymbolsSelected() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.TRUNCATE_EMPTY_SYMBOLS));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readSpinner(controller);
            Label label = readLabel(controller);
            assertThat(spinner.isVisible()).isFalse();
            assertThat(label.isVisible()).isFalse();
        }

        @Test
        void bind_showsSpinnerAndLabel_whenRemoveSymbolsInBeginSelected() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readSpinner(controller);
            Label label = readLabel(controller);
            assertThat(spinner.isVisible()).isTrue();
            assertThat(label.isVisible()).isTrue();
        }

        @Test
        void bind_showsSpinnerAndLabel_whenRemoveSymbolsFromEndSelected() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_FROM_END));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readSpinner(controller);
            Label label = readLabel(controller);
            assertThat(spinner.isVisible()).isTrue();
            assertThat(label.isVisible()).isTrue();
        }

        @Test
        void bind_spinnerChange_callsUpdateParametersWithNewValue() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change spinner value to trigger the valueProperty listener added in bind()
            runOnFxThreadAndWait(() -> readSpinnerUnchecked(controller).getValueFactory().setValue(7));

            // Assert — capture the mutator and verify it sets numberOfSymbols correctly
            ArgumentCaptor<ModeApi.ParamMutator<TruncateParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            TruncateParams original = new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN);
            TruncateParams updated = captor.getValue().apply(original);
            assertThat(updated.numberOfSymbols()).isEqualTo(7);
        }

        @Test
        void bind_positionChange_callsUpdateParametersWithNewOption() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — select REMOVE_SYMBOLS_FROM_END button to trigger the addValueSelectedHandler in bind()
            ua.renamer.app.api.enums.TruncateOptions coreTarget =
                    ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_FROM_END;
            runOnFxThreadAndWait(() -> {
                ItemPositionTruncateRadioSelector selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == coreTarget)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert — capture the mutator and verify it sets the correct API enum
            ArgumentCaptor<ModeApi.ParamMutator<TruncateParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            TruncateParams original = new TruncateParams(0, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN);
            TruncateParams updated = captor.getValue().apply(original);
            assertThat(updated.truncateOption())
                    .isEqualTo(ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_FROM_END);
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new TruncateParams(3, ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN));

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }
}
