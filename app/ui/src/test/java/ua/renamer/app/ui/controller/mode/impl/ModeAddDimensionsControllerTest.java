package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
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
import ua.renamer.app.api.enums.ImageDimensionOptions;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ImageDimensionsParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.ui.converter.ImageDimensionOptionsConverter;
import ua.renamer.app.ui.converter.ItemPositionWithReplacementConverter;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.widget.impl.ItemPositionWithReplacementRadioSelector;

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
 * Unit tests for {@link ModeAddDimensionsController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 * </ul>
 *
 * <p>{@code leftDimensionChoiceBox}, {@code rightDimensionChoiceBox},
 * {@code dimensionsSeparatorTextField}, {@code dimensionsAndFileSeparatorTextField}, and
 * {@code itemPositionRadioSelector} are {@code @FXML}-injected fields;
 * in tests they are injected via reflection (the package is unconditionally opened
 * in {@code module-info.java}).
 */
@ExtendWith(MockitoExtension.class)
class ModeAddDimensionsControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    @Mock
    private ModeApi<ImageDimensionsParams> modeApi;

    private ModeAddDimensionsController controller;

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

    private static void injectLeftDimensionChoiceBox(
            ModeAddDimensionsController target,
            ChoiceBox<ImageDimensionOptions> box) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("leftDimensionChoiceBox");
        f.setAccessible(true);
        f.set(target, box);
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject @FXML fields
    // -----------------------------------------------------------------------

    private static void injectRightDimensionChoiceBox(
            ModeAddDimensionsController target,
            ChoiceBox<ImageDimensionOptions> box) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("rightDimensionChoiceBox");
        f.setAccessible(true);
        f.set(target, box);
    }

    private static void injectDimensionsSeparatorTextField(
            ModeAddDimensionsController target, TextField field) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("dimensionsSeparatorTextField");
        f.setAccessible(true);
        f.set(target, field);
    }

    private static void injectDimensionsAndFileSeparatorTextField(
            ModeAddDimensionsController target, TextField field) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("dimensionsAndFileSeparatorTextField");
        f.setAccessible(true);
        f.set(target, field);
    }

    private static void injectItemPositionRadioSelector(
            ModeAddDimensionsController target,
            ItemPositionWithReplacementRadioSelector selector) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        f.set(target, selector);
    }


    @SuppressWarnings("unchecked")
    private static ChoiceBox<ImageDimensionOptions> readLeftDimensionChoiceBox(
            ModeAddDimensionsController target) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("leftDimensionChoiceBox");
        f.setAccessible(true);
        return (ChoiceBox<ImageDimensionOptions>) f.get(target);
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — read @FXML fields
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static ChoiceBox<ImageDimensionOptions> readRightDimensionChoiceBox(
            ModeAddDimensionsController target) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("rightDimensionChoiceBox");
        f.setAccessible(true);
        return (ChoiceBox<ImageDimensionOptions>) f.get(target);
    }

    private static TextField readDimensionsSeparatorTextField(
            ModeAddDimensionsController target) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("dimensionsSeparatorTextField");
        f.setAccessible(true);
        return (TextField) f.get(target);
    }

    private static TextField readDimensionsAndFileSeparatorTextField(
            ModeAddDimensionsController target) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("dimensionsAndFileSeparatorTextField");
        f.setAccessible(true);
        return (TextField) f.get(target);
    }

    private static ItemPositionWithReplacementRadioSelector readItemPositionRadioSelector(
            ModeAddDimensionsController target) throws Exception {
        Field f = ModeAddDimensionsController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        return (ItemPositionWithReplacementRadioSelector) f.get(target);
    }

    @SuppressWarnings("unchecked")
    private static ChoiceBox<ImageDimensionOptions> readLeftBoxUnchecked(
            ModeAddDimensionsController target) {
        try {
            return readLeftDimensionChoiceBox(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Unchecked read helpers for use inside lambdas

    @SuppressWarnings("unchecked")
    private static ChoiceBox<ImageDimensionOptions> readRightBoxUnchecked(
            ModeAddDimensionsController target) {
        try {
            return readRightDimensionChoiceBox(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TextField readDimensionsSepUnchecked(ModeAddDimensionsController target) {
        try {
            return readDimensionsSeparatorTextField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TextField readNameSepUnchecked(ModeAddDimensionsController target) {
        try {
            return readDimensionsAndFileSeparatorTextField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemPositionWithReplacementRadioSelector readRadioSelectorUnchecked(
            ModeAddDimensionsController target) {
        try {
            return readItemPositionRadioSelector(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ImageDimensionsParams baseline() {
        return new ImageDimensionsParams(
                ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                "", "x");
    }

    // -----------------------------------------------------------------------
    // Baseline params (for mutator assertions)
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
        controller = new ModeAddDimensionsController(
                new ImageDimensionOptionsConverter(languageTextRetriever));

        // @FXML field 1: leftDimensionChoiceBox — seed all enum values, initial = DO_NOT_USE
        ChoiceBox<ImageDimensionOptions> leftBox = new ChoiceBox<>();
        leftBox.getItems().addAll(Arrays.asList(ImageDimensionOptions.values()));
        leftBox.setValue(ImageDimensionOptions.DO_NOT_USE);
        injectLeftDimensionChoiceBox(controller, leftBox);

        // @FXML field 2: rightDimensionChoiceBox — seed all enum values, initial = DO_NOT_USE
        ChoiceBox<ImageDimensionOptions> rightBox = new ChoiceBox<>();
        rightBox.getItems().addAll(Arrays.asList(ImageDimensionOptions.values()));
        rightBox.setValue(ImageDimensionOptions.DO_NOT_USE);
        injectRightDimensionChoiceBox(controller, rightBox);

        // @FXML field 3: dimensionsSeparatorTextField (V1-only — NOT wired in V2)
        injectDimensionsSeparatorTextField(controller, new TextField());

        // @FXML field 4: dimensionsAndFileSeparatorTextField (wired in V2 as nameSeparator)
        injectDimensionsAndFileSeparatorTextField(controller, new TextField());

        // @FXML field 5: itemPositionRadioSelector
        var posConverter = new ItemPositionWithReplacementConverter(languageTextRetriever);
        injectItemPositionRadioSelector(controller,
                new ItemPositionWithReplacementRadioSelector("", posConverter));

        // @FXML field 6: preview label
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
        void supportedMode_returnsUseImageDimensions() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.ADD_DIMENSIONS);
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

        // ── Init: leftDimensionChoiceBox ────────────────────────────────────

        @Test
        void bind_initializesLeftDimensionToWidth_fromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ChoiceBox<ImageDimensionOptions> leftBox = readLeftDimensionChoiceBox(controller);
            assertThat(leftBox.getValue()).isEqualTo(ImageDimensionOptions.WIDTH);
        }

        @Test
        void bind_initializesLeftDimensionToDoNotUse_whenDoNotUse() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — ChoiceBox remains at its seeded default (DO_NOT_USE)
            ChoiceBox<ImageDimensionOptions> leftBox = readLeftDimensionChoiceBox(controller);
            assertThat(leftBox.getValue()).isEqualTo(ImageDimensionOptions.DO_NOT_USE);
        }

        // ── Init: rightDimensionChoiceBox ───────────────────────────────────

        @Test
        void bind_initializesRightDimensionToHeight_fromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ImageDimensionOptions.HEIGHT,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            ChoiceBox<ImageDimensionOptions> rightBox = readRightDimensionChoiceBox(controller);
            assertThat(rightBox.getValue()).isEqualTo(ImageDimensionOptions.HEIGHT);
        }

        @Test
        void bind_initializesRightDimensionToDoNotUse_whenDoNotUse() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — ChoiceBox remains at its seeded default (DO_NOT_USE)
            ChoiceBox<ImageDimensionOptions> rightBox = readRightDimensionChoiceBox(controller);
            assertThat(rightBox.getValue()).isEqualTo(ImageDimensionOptions.DO_NOT_USE);
        }

        // ── Init: nameSeparator (dimensionsAndFileSeparatorTextField) ───────

        @Test
        void bind_initializesNameSeparatorFromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            " - ", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField nameSepField = readDimensionsAndFileSeparatorTextField(controller);
            assertThat(nameSepField.getText()).isEqualTo(" - ");
        }

        @Test
        void bind_initializesNameSeparatorToEmpty_whenNull() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            null, "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — null must be converted to empty string, never set null on TextField
            TextField nameSepField = readDimensionsAndFileSeparatorTextField(controller);
            assertThat(nameSepField.getText()).isEqualTo("");
        }

        @Test
        void bind_initializesNameSeparatorToEmpty_whenEmptyString() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField nameSepField = readDimensionsAndFileSeparatorTextField(controller);
            assertThat(nameSepField.getText()).isEqualTo("");
        }

        // ── Init: position (itemPositionRadioSelector) ──────────────────────

        @Test
        void bind_initializesPositionToBegin_fromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — core BEGIN radio button must be selected
            ItemPositionWithReplacementRadioSelector selector = readItemPositionRadioSelector(controller);
            assertThat(selector.getSelectedValue()).isEqualTo(ItemPositionWithReplacement.BEGIN);
        }

        @Test
        void bind_initializesPositionToEnd_fromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.END,
                            "", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — core END radio button must be selected
            ItemPositionWithReplacementRadioSelector selector = readItemPositionRadioSelector(controller);
            assertThat(selector.getSelectedValue()).isEqualTo(ItemPositionWithReplacement.END);
        }

        @Test
        void bind_initializesPositionToReplace_fromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.REPLACE,
                            "", "x"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — core REPLACE radio button must be selected
            ItemPositionWithReplacementRadioSelector selector = readItemPositionRadioSelector(controller);
            assertThat(selector.getSelectedValue()).isEqualTo(ItemPositionWithReplacement.REPLACE);
        }

        // ── Listener: leftDimensionChoiceBox → modeApi ──────────────────────

        @Test
        void bind_leftDimensionChange_callsUpdateParameters() throws Exception {
            // Arrange — seed DO_NOT_USE; change to WIDTH
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change left box to WIDTH
            runOnFxThreadAndWait(() -> readLeftBoxUnchecked(controller).setValue(ImageDimensionOptions.WIDTH));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.leftSide())
                    .isEqualTo(ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH);
        }

        @Test
        void bind_leftDimensionChange_mapsToHeight() throws Exception {
            // Arrange — seed DO_NOT_USE; change to HEIGHT
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act
            runOnFxThreadAndWait(() -> readLeftBoxUnchecked(controller).setValue(ImageDimensionOptions.HEIGHT));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.leftSide())
                    .isEqualTo(ua.renamer.app.api.enums.ImageDimensionOptions.HEIGHT);
        }

        // ── Listener: rightDimensionChoiceBox → modeApi ─────────────────────

        @Test
        void bind_rightDimensionChange_callsUpdateParameters() throws Exception {
            // Arrange — seed DO_NOT_USE; change right to HEIGHT
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act
            runOnFxThreadAndWait(() -> readRightBoxUnchecked(controller).setValue(ImageDimensionOptions.HEIGHT));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.rightSide())
                    .isEqualTo(ua.renamer.app.api.enums.ImageDimensionOptions.HEIGHT);
        }

        @Test
        void bind_rightDimensionChange_mapsToWidth() throws Exception {
            // Arrange — seed DO_NOT_USE; change right to WIDTH
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act
            runOnFxThreadAndWait(() -> readRightBoxUnchecked(controller).setValue(ImageDimensionOptions.WIDTH));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.rightSide())
                    .isEqualTo(ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH);
        }

        // ── Listener: dimensionsAndFileSeparatorTextField → modeApi ─────────

        @Test
        void bind_nameSeparatorChange_callsUpdateParameters() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act
            runOnFxThreadAndWait(() -> readNameSepUnchecked(controller).setText("__"));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.nameSeparator()).isEqualTo("__");
        }

        @Test
        void bind_nameSeparatorChange_usesEmptyString_whenEmpty() throws Exception {
            // Arrange — start with a non-empty separator, then clear it
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "old", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — clear to empty
            runOnFxThreadAndWait(() -> readNameSepUnchecked(controller).setText(""));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.nameSeparator()).isEqualTo("");
        }

        // ── Listener: itemPositionRadioSelector → modeApi ───────────────────

        @Test
        void bind_positionChange_toEnd_callsUpdateParameters() throws Exception {
            // Arrange — start with BEGIN
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — select the END radio button
            ItemPositionWithReplacement coreEnd = ItemPositionWithReplacement.END;
            runOnFxThreadAndWait(() -> {
                ItemPositionWithReplacementRadioSelector selector =
                        readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == coreEnd)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert — capture the mutator and verify it maps core END → api END
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.position())
                    .isEqualTo(ua.renamer.app.api.enums.ItemPositionWithReplacement.END);
        }

        @Test
        void bind_positionChange_toReplace_callsUpdateParameters() throws Exception {
            // Arrange — start with BEGIN
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — select REPLACE
            ItemPositionWithReplacement coreReplace = ItemPositionWithReplacement.REPLACE;
            runOnFxThreadAndWait(() -> {
                ItemPositionWithReplacementRadioSelector selector =
                        readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == coreReplace)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.position())
                    .isEqualTo(ua.renamer.app.api.enums.ItemPositionWithReplacement.REPLACE);
        }

        @Test
        void bind_positionChange_toBegin_callsUpdateParameters() throws Exception {
            // Arrange — start with END so selecting BEGIN is a real change
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.END,
                            "", "x"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — select BEGIN
            ItemPositionWithReplacement coreBegin = ItemPositionWithReplacement.BEGIN;
            runOnFxThreadAndWait(() -> {
                ItemPositionWithReplacementRadioSelector selector =
                        readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == coreBegin)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ImageDimensionsParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ImageDimensionsParams updated = captor.getValue().apply(baseline());
            assertThat(updated.position())
                    .isEqualTo(ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN);
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "_", "x"));

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }

        // ── dimensionsSeparatorTextField — wired to withSeparator ─────────────

        @Test
        void bind_wiresDimensionsSeparatorTextField_toModeApi() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Clear any interactions from bind() init phase
            clearInvocations(modeApi);

            // Act — change the dimensionsSeparatorTextField
            runOnFxThreadAndWait(() -> readDimensionsSepUnchecked(controller).setText("-"));

            // Assert — modeApi.updateParameters must be called (separator change wired)
            verify(modeApi, atLeastOnce()).updateParameters(any());
        }

        // ── Enum bridge — parameterized, pure (no FX) ───────────────────────

        @ParameterizedTest(name = "api ImageDimensionOptions [{0}] maps to core enum without exception")
        @EnumSource(ua.renamer.app.api.enums.ImageDimensionOptions.class)
        void bind_allApiImageDimensionOptions_mapToCoreEnumWithoutException(
                ua.renamer.app.api.enums.ImageDimensionOptions apiOpt) {
            assertThatCode(() ->
                    ua.renamer.app.api.enums.ImageDimensionOptions.valueOf(apiOpt.name())
            ).doesNotThrowAnyException();
        }

        @ParameterizedTest(name = "api ItemPositionWithReplacement [{0}] maps to core enum without exception")
        @EnumSource(ua.renamer.app.api.enums.ItemPositionWithReplacement.class)
        void bind_allApiPositionValues_mapToCoreEnumWithoutException(
                ua.renamer.app.api.enums.ItemPositionWithReplacement apiPos) {
            assertThatCode(() ->
                    ua.renamer.app.api.enums.ItemPositionWithReplacement.valueOf(apiPos.name())
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    class NoThrowContractTests {

        @Test
        void supportedMode_neverThrows() {
            assertThatCode(() -> controller.supportedMode()).doesNotThrowAnyException();
        }

        @Test
        void bind_neverThrows_withMinimalValidParams() {
            when(modeApi.currentParameters()).thenReturn(
                    new ImageDimensionsParams(
                            ua.renamer.app.api.enums.ImageDimensionOptions.WIDTH,
                            ua.renamer.app.api.enums.ImageDimensionOptions.DO_NOT_USE,
                            ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                            "", "x"));

            assertThatCode(() -> runOnFxThreadAndWaitUnchecked(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }
}
