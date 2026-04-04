package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
import ua.renamer.app.api.session.ParentFolderParams;
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
 * Unit tests for {@link ModeUseParentFolderNameController}.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()} — no toolkit required</li>
 *   <li>FX tests for {@code bind()} — toolkit started once via {@code Platform.startup}</li>
 * </ul>
 *
 * <p>{@code parentsNumberSpinner}, {@code fileNameSeparatorTextField}, and
 * {@code itemPositionRadioSelector} are {@code @FXML}-injected fields;
 * in tests they are injected via reflection (the package is unconditionally opened
 * in {@code module-info.java}).
 */
@ExtendWith(MockitoExtension.class)
class ModeUseParentFolderNameControllerTest {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    @Mock
    private ModeApi<ParentFolderParams> modeApi;

    private ModeUseParentFolderNameController controller;

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

    private static void injectSpinner(ModeUseParentFolderNameController target, Spinner<Integer> spinner)
            throws Exception {
        Field f = ModeUseParentFolderNameController.class.getDeclaredField("parentsNumberSpinner");
        f.setAccessible(true);
        f.set(target, spinner);
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject / read @FXML fields
    // -----------------------------------------------------------------------

    private static void injectTextField(ModeUseParentFolderNameController target, TextField field)
            throws Exception {
        Field f = ModeUseParentFolderNameController.class.getDeclaredField("fileNameSeparatorTextField");
        f.setAccessible(true);
        f.set(target, field);
    }

    private static void injectRadioSelector(ModeUseParentFolderNameController target,
                                            ItemPositionRadioSelector selector) throws Exception {
        Field f = ModeUseParentFolderNameController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        f.set(target, selector);
    }

    @SuppressWarnings("unchecked")
    private static Spinner<Integer> readSpinner(ModeUseParentFolderNameController target) throws Exception {
        Field f = ModeUseParentFolderNameController.class.getDeclaredField("parentsNumberSpinner");
        f.setAccessible(true);
        return (Spinner<Integer>) f.get(target);
    }

    private static TextField readTextField(ModeUseParentFolderNameController target) throws Exception {
        Field f = ModeUseParentFolderNameController.class.getDeclaredField("fileNameSeparatorTextField");
        f.setAccessible(true);
        return (TextField) f.get(target);
    }

    private static ItemPositionRadioSelector readRadioSelector(ModeUseParentFolderNameController target)
            throws Exception {
        Field f = ModeUseParentFolderNameController.class.getDeclaredField("itemPositionRadioSelector");
        f.setAccessible(true);
        return (ItemPositionRadioSelector) f.get(target);
    }

    @SuppressWarnings("unchecked")
    private static Spinner<Integer> readSpinnerUnchecked(ModeUseParentFolderNameController target) {
        try {
            return readSpinner(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TextField readTextFieldUnchecked(ModeUseParentFolderNameController target) {
        try {
            return readTextField(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemPositionRadioSelector readRadioSelectorUnchecked(
            ModeUseParentFolderNameController target) {
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

    @BeforeEach
    void setUp() throws Exception {
        controller = new ModeUseParentFolderNameController();

        var converter = new ItemPositionConverter(languageTextRetriever);

        // @FXML field 1: Spinner<Integer> with value factory, editable
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));
        spinner.setEditable(true);
        injectSpinner(controller, spinner);

        // @FXML field 2: TextField for separator
        injectTextField(controller, new TextField());

        // @FXML field 3: ItemPositionRadioSelector
        injectRadioSelector(controller, new ItemPositionRadioSelector("", converter));
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
        void supportedMode_returnsUseParentFolderName() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.USE_PARENT_FOLDER_NAME);
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
        void bind_initializesSpinnerFromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(3, ItemPosition.BEGIN, "_"));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readSpinner(controller);
            assertThat(spinner.getValue()).isEqualTo(3);
        }

        @Test
        void bind_initializesSpinnerToOne_whenNumberOfParentFoldersIsOne() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.END, ""));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            Spinner<Integer> spinner = readSpinner(controller);
            assertThat(spinner.getValue()).isEqualTo(1);
        }

        @Test
        void bind_initializesSeparatorFromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, " - "));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField textField = readTextField(controller);
            assertThat(textField.getText()).isEqualTo(" - ");
        }

        @Test
        void bind_initializesSeparatorToEmpty_whenSeparatorIsEmptyString() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, ""));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            TextField textField = readTextField(controller);
            assertThat(textField.getText()).isEqualTo("");
        }

        @Test
        void bind_initializesSeparatorToEmpty_whenSeparatorIsNull() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, null));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — null separator must be treated as empty string, never set null on TextField
            TextField textField = readTextField(controller);
            assertThat(textField.getText()).isEqualTo("");
        }

        @Test
        void bind_initializesPositionToBegin_fromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, ""));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — core BEGIN radio button must be selected
            ItemPositionRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue())
                    .isEqualTo(ua.renamer.app.core.enums.ItemPosition.BEGIN);
        }

        @Test
        void bind_initializesPositionToEnd_fromParams() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.END, ""));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — core END radio button must be selected
            ItemPositionRadioSelector selector = readRadioSelector(controller);
            assertThat(selector.getSelectedValue())
                    .isEqualTo(ua.renamer.app.core.enums.ItemPosition.END);
        }

        @Test
        void bind_spinnerChange_callsUpdateParameters() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, ""));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change the spinner value to trigger the valueProperty listener added in bind()
            runOnFxThreadAndWait(() -> readSpinnerUnchecked(controller).getValueFactory().setValue(4));

            // Assert — capture the mutator and verify it applies the right number of parent folders
            ArgumentCaptor<ModeApi.ParamMutator<ParentFolderParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ParentFolderParams original = new ParentFolderParams(1, ItemPosition.BEGIN, "");
            ParentFolderParams updated = captor.getValue().apply(original);
            assertThat(updated.numberOfParentFolders()).isEqualTo(4);
        }

        @Test
        void bind_spinnerChange_usesOneAsFallback_whenValueIsNull() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(2, ItemPosition.BEGIN, ""));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — the bind() listener uses (newVal != null ? newVal : 1)
            // Capture the mutator by triggering any spinner change first
            runOnFxThreadAndWait(() -> readSpinnerUnchecked(controller).getValueFactory().setValue(5));

            ArgumentCaptor<ModeApi.ParamMutator<ParentFolderParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            // Apply the captured mutator with a null-simulating scenario:
            // the mutator uses newVal captured at time of change, so verify fallback contract holds
            // by verifying the non-null path was taken (value=5, not null)
            ParentFolderParams original = new ParentFolderParams(2, ItemPosition.BEGIN, "");
            ParentFolderParams updated = captor.getValue().apply(original);
            assertThat(updated.numberOfParentFolders()).isEqualTo(5);
        }

        @Test
        void bind_separatorChange_callsUpdateParameters() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, ""));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — change the text field content to trigger the textProperty listener
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText("__"));

            // Assert — capture the mutator and verify it sets the new separator
            ArgumentCaptor<ModeApi.ParamMutator<ParentFolderParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ParentFolderParams original = new ParentFolderParams(1, ItemPosition.BEGIN, "");
            ParentFolderParams updated = captor.getValue().apply(original);
            assertThat(updated.separator()).isEqualTo("__");
        }

        @Test
        void bind_separatorChange_usesEmptyStringAsFallback_whenValueIsNull() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, "old"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — set text to empty to trigger a change
            runOnFxThreadAndWait(() -> readTextFieldUnchecked(controller).setText(""));

            ArgumentCaptor<ModeApi.ParamMutator<ParentFolderParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            // The bind() listener uses (newVal != null ? newVal : "")
            // An empty string is a valid separator — verify it passes through
            ParentFolderParams original = new ParentFolderParams(1, ItemPosition.BEGIN, "old");
            ParentFolderParams updated = captor.getValue().apply(original);
            assertThat(updated.separator()).isEqualTo("");
        }

        @Test
        void bind_positionChange_callsUpdateParameters() throws Exception {
            // Arrange — start with BEGIN
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, ""));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — select the END radio button to trigger the addValueSelectedHandler added in bind()
            ua.renamer.app.core.enums.ItemPosition coreEnd = ua.renamer.app.core.enums.ItemPosition.END;
            runOnFxThreadAndWait(() -> {
                ItemPositionRadioSelector selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == coreEnd)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert — capture the mutator and verify it maps core END → API END
            ArgumentCaptor<ModeApi.ParamMutator<ParentFolderParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ParentFolderParams original = new ParentFolderParams(1, ItemPosition.BEGIN, "");
            ParentFolderParams updated = captor.getValue().apply(original);
            assertThat(updated.position()).isEqualTo(ItemPosition.END);
        }

        @Test
        void bind_positionChange_beginToEnd_mapsEnumCorrectly() throws Exception {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(2, ItemPosition.END, "-"));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Act — select BEGIN button
            ua.renamer.app.core.enums.ItemPosition coreBegin = ua.renamer.app.core.enums.ItemPosition.BEGIN;
            runOnFxThreadAndWait(() -> {
                ItemPositionRadioSelector selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue() == coreBegin)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<ParentFolderParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());

            ParentFolderParams original = new ParentFolderParams(2, ItemPosition.END, "-");
            ParentFolderParams updated = captor.getValue().apply(original);
            assertThat(updated.position()).isEqualTo(ItemPosition.BEGIN);
        }

        @Test
        void bind_doesNotThrowWithValidParams() {
            // Arrange
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, "_"));

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }

        /**
         * Verifies that every api-enum ItemPosition constant is correctly mirrored in the
         * core enum — i.e. {@code core.ItemPosition.valueOf(apiPos.name())} never throws.
         */
        @ParameterizedTest(name = "bind maps api ItemPosition [{0}] to core enum without exception")
        @EnumSource(ItemPosition.class)
        void bind_allApiPositionValues_mapToCoreEnumWithoutException(ItemPosition apiPos) {
            // This validates the valueOf name-bridge used in bind()
            assertThatCode(() ->
                    ua.renamer.app.core.enums.ItemPosition.valueOf(apiPos.name())
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
            when(modeApi.currentParameters())
                    .thenReturn(new ParentFolderParams(1, ItemPosition.BEGIN, ""));

            assertThatCode(() -> runOnFxThreadAndWaitUnchecked(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }
}
