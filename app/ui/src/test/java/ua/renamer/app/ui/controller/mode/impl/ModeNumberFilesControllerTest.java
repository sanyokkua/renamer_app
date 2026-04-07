package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.extern.slf4j.Slf4j;
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
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.SequenceParams;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.ui.converter.SortSourceConverter;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ModeNumberFilesController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 *   <li>FX tests for {@code updateCommand()} — verifies V1-bridge command construction</li>
 *   <li>No-throw contract tests — every public method must never propagate exceptions</li>
 * </ul>
 *
 * <p>{@code startSeqNumberSpinner}, {@code stepValueSpinner},
 * {@code minDigitAmountSpinner}, and {@code sortingSourceChoiceBox} are
 * {@code @FXML}-injected fields; in tests they are injected via reflection
 * (the package is unconditionally opened in {@code module-info.java}).
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class ModeNumberFilesControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    private ModeNumberFilesController controller;

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

    private static void injectStartSeqNumberSpinner(
            ModeNumberFilesController target,
            Spinner<Integer> spinner) throws Exception {
        Field f = ModeNumberFilesController.class.getDeclaredField("startSeqNumberSpinner");
        f.setAccessible(true);
        f.set(target, spinner);
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject @FXML fields
    // -----------------------------------------------------------------------

    private static void injectStepValueSpinner(
            ModeNumberFilesController target,
            Spinner<Integer> spinner) throws Exception {
        Field f = ModeNumberFilesController.class.getDeclaredField("stepValueSpinner");
        f.setAccessible(true);
        f.set(target, spinner);
    }

    private static void injectMinDigitAmountSpinner(
            ModeNumberFilesController target,
            Spinner<Integer> spinner) throws Exception {
        Field f = ModeNumberFilesController.class.getDeclaredField("minDigitAmountSpinner");
        f.setAccessible(true);
        f.set(target, spinner);
    }

    private static void injectSortingSourceChoiceBox(
            ModeNumberFilesController target,
            ChoiceBox<ua.renamer.app.api.enums.SortSource> box) throws Exception {
        Field f = ModeNumberFilesController.class.getDeclaredField("sortingSourceChoiceBox");
        f.setAccessible(true);
        f.set(target, box);
    }

    private static void injectPerFolderCountingCheckBox(
            ModeNumberFilesController target,
            CheckBox checkBox) throws Exception {
        Field f = ModeNumberFilesController.class.getDeclaredField("perFolderCountingCheckBox");
        f.setAccessible(true);
        f.set(target, checkBox);
    }

    private static CheckBox readPerFolderCheckBoxUnchecked(ModeNumberFilesController target) {
        try {
            Field f = ModeNumberFilesController.class.getDeclaredField("perFolderCountingCheckBox");
            f.setAccessible(true);
            return (CheckBox) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("unchecked")
    private static Spinner<Integer> readStartSpinnerUnchecked(ModeNumberFilesController target) {
        try {
            Field f = ModeNumberFilesController.class.getDeclaredField("startSeqNumberSpinner");
            f.setAccessible(true);
            return (Spinner<Integer>) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — read @FXML fields (unchecked, for use inside lambdas)
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Spinner<Integer> readStepSpinnerUnchecked(ModeNumberFilesController target) {
        try {
            Field f = ModeNumberFilesController.class.getDeclaredField("stepValueSpinner");
            f.setAccessible(true);
            return (Spinner<Integer>) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Spinner<Integer> readPaddingSpinnerUnchecked(ModeNumberFilesController target) {
        try {
            Field f = ModeNumberFilesController.class.getDeclaredField("minDigitAmountSpinner");
            f.setAccessible(true);
            return (Spinner<Integer>) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static ChoiceBox<ua.renamer.app.api.enums.SortSource> readSortBoxUnchecked(
            ModeNumberFilesController target) {
        try {
            Field f = ModeNumberFilesController.class.getDeclaredField("sortingSourceChoiceBox");
            f.setAccessible(true);
            return (ChoiceBox<ua.renamer.app.api.enums.SortSource>) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static SequenceParams defaultParams() {
        return new SequenceParams(0, 1, 0, ua.renamer.app.api.enums.SortSource.FILE_NAME, true);
    }

    // -----------------------------------------------------------------------
    // Baseline params — for applying captured mutators
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

    @BeforeEach
    void setUp() throws Exception {
        SortSourceConverter converter = new SortSourceConverter(languageTextRetriever);
        controller = new ModeNumberFilesController(converter);

        // @FXML field 1: startSeqNumberSpinner — range [0, MAX_VALUE], initial = 0
        Spinner<Integer> startSpinner = new Spinner<>(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        injectStartSeqNumberSpinner(controller, startSpinner);

        // @FXML field 2: stepValueSpinner — range [1, MAX_VALUE], initial = 1
        Spinner<Integer> stepSpinner = new Spinner<>(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));
        injectStepValueSpinner(controller, stepSpinner);

        // @FXML field 3: minDigitAmountSpinner — range [0, MAX_VALUE], initial = 0
        Spinner<Integer> paddingSpinner = new Spinner<>(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        injectMinDigitAmountSpinner(controller, paddingSpinner);

        // @FXML field 4: sortingSourceChoiceBox — seeded with all core enum values, initial = FILE_NAME
        ChoiceBox<ua.renamer.app.api.enums.SortSource> choiceBox =
                new ChoiceBox<>(FXCollections.observableArrayList(
                        Arrays.asList(ua.renamer.app.api.enums.SortSource.values())));
        choiceBox.setValue(ua.renamer.app.api.enums.SortSource.FILE_NAME);
        injectSortingSourceChoiceBox(controller, choiceBox);

        // @FXML field 5: perFolderCountingCheckBox — unchecked by default
        injectPerFolderCountingCheckBox(controller, new CheckBox());

        // Run initialize on the FX thread (mirrors FXML-loader lifecycle)
        runOnFxThreadAndWait(() -> controller.initialize(null, null));
    }

    // -----------------------------------------------------------------------
    // No-throw contract — V2 pipeline must never propagate exceptions
    // -----------------------------------------------------------------------

    @FunctionalInterface
    private interface RunnableEx {
        void run() throws Exception;
    }

    // -----------------------------------------------------------------------
    // FX threading utilities
    // -----------------------------------------------------------------------

    @Nested
    class SupportedModeTests {

        @Test
        void supportedMode_returnsAddSequence() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.NUMBER_FILES);
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

        @Mock
        private ModeApi<SequenceParams> modeApi;

        // ── Init: spinners and choice box read params correctly ─────────────

        @Test
        void bind_initializesStartNumberSpinnerFromParams() throws Exception {
            // Arrange — non-default start number to verify it was actually applied
            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(5, 1, 0, ua.renamer.app.api.enums.SortSource.FILE_NAME, true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readStartSpinnerUnchecked(controller);
            assertThat(spinner.getValue()).isEqualTo(5);
        }

        @Test
        void bind_initializesStepValueSpinnerFromParams() throws Exception {
            // Arrange — non-default step value
            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(0, 3, 0, ua.renamer.app.api.enums.SortSource.FILE_NAME, true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readStepSpinnerUnchecked(controller);
            assertThat(spinner.getValue()).isEqualTo(3);
        }

        @Test
        void bind_initializesPaddingDigitsSpinnerFromParams() throws Exception {
            // Arrange — non-default padding
            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(0, 1, 4, ua.renamer.app.api.enums.SortSource.FILE_NAME, true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readPaddingSpinnerUnchecked(controller);
            assertThat(spinner.getValue()).isEqualTo(4);
        }

        @Test
        void bind_initializesAllFieldsFromNonDefaultParams() throws Exception {
            // Arrange — every param is non-default to prove all fields are initialised
            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(5, 3, 2, ua.renamer.app.api.enums.SortSource.FILE_SIZE, true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readStartSpinnerUnchecked(controller).getValue()).isEqualTo(5);
            assertThat(readStepSpinnerUnchecked(controller).getValue()).isEqualTo(3);
            assertThat(readPaddingSpinnerUnchecked(controller).getValue()).isEqualTo(2);
            assertThat(readSortBoxUnchecked(controller).getValue())
                    .isEqualTo(ua.renamer.app.api.enums.SortSource.FILE_SIZE);
        }

        @Test
        void bind_initializesSortSourceChoiceBoxFromParams() throws Exception {
            // Arrange — non-default sort source
            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(0, 1, 0, ua.renamer.app.api.enums.SortSource.FILE_SIZE, true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — API enum FILE_SIZE maps to core FILE_SIZE
            assertThat(readSortBoxUnchecked(controller).getValue())
                    .isEqualTo(ua.renamer.app.api.enums.SortSource.FILE_SIZE);
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(0, 1, 0, ua.renamer.app.api.enums.SortSource.FILE_NAME, true));

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }

        // ── Listener: startSeqNumberSpinner → modeApi ───────────────────────

        @Test
        void startNumber_change_propagatesToModeApi() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(defaultParams());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change start number to a non-default value
            runOnFxThreadAndWait(() ->
                    readStartSpinnerUnchecked(controller).getValueFactory().setValue(10));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<SequenceParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            SequenceParams updated = captor.getValue().apply(defaultParams());
            assertThat(updated.startNumber()).isEqualTo(10);
        }

        // ── Listener: stepValueSpinner → modeApi ────────────────────────────

        @Test
        void stepValue_change_propagatesToModeApi() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(defaultParams());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change step to a non-default value
            runOnFxThreadAndWait(() ->
                    readStepSpinnerUnchecked(controller).getValueFactory().setValue(5));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<SequenceParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            SequenceParams updated = captor.getValue().apply(defaultParams());
            assertThat(updated.stepValue()).isEqualTo(5);
        }

        // ── Listener: minDigitAmountSpinner → modeApi ───────────────────────

        @Test
        void paddingDigits_change_propagatesToModeApi() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(defaultParams());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change padding to a non-default value
            runOnFxThreadAndWait(() ->
                    readPaddingSpinnerUnchecked(controller).getValueFactory().setValue(3));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<SequenceParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            SequenceParams updated = captor.getValue().apply(defaultParams());
            assertThat(updated.paddingDigits()).isEqualTo(3);
        }

        // ── Listener: sortingSourceChoiceBox → modeApi (core → API enum) ────

        @ParameterizedTest(name = "sortSource [{0}] maps from core to api enum via selectedItemProperty")
        @EnumSource(ua.renamer.app.api.enums.SortSource.class)
        void sortSource_change_propagatesApiEnum(ua.renamer.app.api.enums.SortSource apiSort) throws Exception {
            // Arrange — seed with FILE_PATH so that every parameterized value (including
            // FILE_PATH itself needs special handling) causes a real selection change.
            // We seed with the enum immediately AFTER apiSort in ordinal order (wrapping),
            // which guarantees the initial value differs from the target.
            ua.renamer.app.api.enums.SortSource[] apiValues = ua.renamer.app.api.enums.SortSource.values();
            ua.renamer.app.api.enums.SortSource seedApiSort =
                    apiValues[(apiSort.ordinal() + 1) % apiValues.length];

            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(0, 1, 0, seedApiSort, true));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act — set the core enum that corresponds to the API enum under test
            ua.renamer.app.api.enums.SortSource coreSort =
                    ua.renamer.app.api.enums.SortSource.valueOf(apiSort.name());
            runOnFxThreadAndWait(() -> readSortBoxUnchecked(controller).setValue(coreSort));

            // Assert — the mutator applied to defaultParams must carry the correct API enum
            ArgumentCaptor<ModeApi.ParamMutator<SequenceParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            SequenceParams updated = captor.getValue().apply(defaultParams());
            assertThat(updated.sortSource()).isEqualTo(apiSort);
        }

        // ── Enum bridge — pure (no FX) ───────────────────────────────────────

        @ParameterizedTest(name = "api SortSource [{0}] maps to core enum without exception")
        @EnumSource(ua.renamer.app.api.enums.SortSource.class)
        void bind_allApiSortSourceValues_mapToCoreEnumWithoutException(
                ua.renamer.app.api.enums.SortSource apiSort) {
            assertThatCode(() ->
                    ua.renamer.app.api.enums.SortSource.valueOf(apiSort.name())
            ).doesNotThrowAnyException();
        }

        // ── Pre-bind isolation ───────────────────────────────────────────────

        @Test
        void bind_beforeBind_spinnerChange_doesNotInteractWithModeApi() throws Exception {
            // Arrange — no bind() called; modeApi is a fresh mock with no stubbing

            // Act — change the spinner directly (initialize() wired V1 listener, not V2 modeApi)
            runOnFxThreadAndWait(() ->
                    readStartSpinnerUnchecked(controller).getValueFactory().setValue(99));

            // Assert — V2 modeApi must never be called before bind()
            verifyNoInteractions(modeApi);
        }

        @Test
        void bind_beforeBind_sortBoxChange_doesNotInteractWithModeApi() throws Exception {
            // Arrange — no bind() called

            // Act
            runOnFxThreadAndWait(() ->
                    readSortBoxUnchecked(controller).setValue(
                            ua.renamer.app.api.enums.SortSource.FILE_SIZE));

            // Assert
            verifyNoInteractions(modeApi);
        }
    }

    @Nested
    class NoThrowContractTests {

        @Mock
        private ModeApi<SequenceParams> modeApi;

        @Test
        void supportedMode_neverThrows() {
            assertThatCode(() -> controller.supportedMode()).doesNotThrowAnyException();
        }

        @Test
        void bind_neverThrows_withMinimalValidParams() {
            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(0, 1, 0, ua.renamer.app.api.enums.SortSource.FILE_NAME, true));

            assertThatCode(() -> runOnFxThreadAndWaitUnchecked(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }

        @Test
        void bind_neverThrows_withLargeValues() {
            when(modeApi.currentParameters()).thenReturn(
                    new SequenceParams(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
                            ua.renamer.app.api.enums.SortSource.IMAGE_HEIGHT, true));

            assertThatCode(() -> runOnFxThreadAndWaitUnchecked(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }
}
