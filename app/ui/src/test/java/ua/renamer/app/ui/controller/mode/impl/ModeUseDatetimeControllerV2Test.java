package ua.renamer.app.ui.controller.mode.impl;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.DateTimeParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ValidationResult;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.ui.converter.*;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;
import ua.renamer.app.ui.widget.impl.ItemPositionWithReplacementRadioSelector;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ModeUseDatetimeController} — V2 {@code bind()} contract.
 *
 * <p>Test categories:
 * <ul>
 *   <li>Pure (no-FX) tests for {@code supportedMode()}</li>
 *   <li>FX tests for {@code bind()} init — control state after bind(params)</li>
 *   <li>FX tests for {@code bind()} wire — user interaction propagates to modeApi</li>
 *   <li>FX tests for {@code bind()} visibility — source/position drive panel visibility</li>
 *   <li>No-throw contract — every public method must never propagate exceptions</li>
 * </ul>
 *
 * <p>All {@code @FXML} fields are injected via reflection; the package
 * {@code ua.renamer.app.ui.controller.mode.impl} is unconditionally opened
 * in {@code module-info.java}.
 */
@ExtendWith(MockitoExtension.class)
class ModeUseDatetimeControllerV2Test {

    private static final long FX_TIMEOUT_MS = 5_000;

    @Mock
    private LanguageTextRetrieverApi languageTextRetriever;

    private ModeUseDatetimeController controller;

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

    @SuppressWarnings("unchecked")
    private static <T> T readFieldUnchecked(ModeUseDatetimeController target, String fieldName) {
        try {
            Field f = ModeUseDatetimeController.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // Reflection helpers — inject and read @FXML fields
    // -----------------------------------------------------------------------

    private static ChoiceBox<ua.renamer.app.core.enums.DateTimeSource> readSourceBoxUnchecked(
            ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "dateTimeSourceChoiceBox");
    }

    private static ChoiceBox<ua.renamer.app.core.enums.DateFormat> readDateFormatBoxUnchecked(
            ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "dateFormatChoiceBox");
    }

    private static ChoiceBox<ua.renamer.app.core.enums.TimeFormat> readTimeFormatBoxUnchecked(
            ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "timeFormatChoiceBox");
    }

    private static ItemPositionWithReplacementRadioSelector readRadioSelectorUnchecked(
            ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "dateTimePositionInTheNameRadioSelector");
    }

    private static CheckBox readUseFallbackCheckBoxUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "useFallbackDateTimeCheckBox");
    }

    private static CheckBox readUseCustomFallbackCheckBoxUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "useCustomDateTimeAsFallbackCheckBox");
    }

    private static CheckBox readUseUppercaseCheckBoxUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "useUppercaseForAmPmCheckBox");
    }

    private static DatePicker readDatePickerUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "datePicker");
    }

    private static Spinner<Integer> readHourSpinnerUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "hourSpinner");
    }

    private static Spinner<Integer> readMinuteSpinnerUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "minuteSpinner");
    }

    private static Spinner<Integer> readSecondSpinnerUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "secondSpinner");
    }

    private static GridPane readDateTimePickerPanelUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "dateTimePicker");
    }

    private static Label readSeparatorLabelUnchecked(ModeUseDatetimeController target) {
        return readFieldUnchecked(target, "datetimeAndNameSeparatorLabel");
    }

    private static DateTimeParams baseline() {
        return new DateTimeParams(
                ua.renamer.app.api.enums.DateTimeSource.FILE_CREATION_DATE,
                ua.renamer.app.api.enums.DateFormat.DO_NOT_USE_DATE,
                ua.renamer.app.api.enums.TimeFormat.DO_NOT_USE_TIME,
                ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                false, false, false, false, false, null, false,
                ua.renamer.app.api.enums.DateTimeFormat.DATE_TIME_TOGETHER, "");
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
    // Baseline params — for applying captured mutators
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
    // Pure (no-FX) tests
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() throws Exception {
        var dateFormatConverter = new DateFormatConverter(languageTextRetriever);
        var timeFormatConverter = new TimeFormatConverter(languageTextRetriever);
        var dateTimeFormatConverter = new DateTimeFormatConverter(languageTextRetriever);
        var dateTimeSourceConverter = new DateTimeSourceConverter(languageTextRetriever);
        var dateTimeOperations = new DateTimeOperations();

        controller = new ModeUseDatetimeController(
                dateFormatConverter,
                timeFormatConverter,
                dateTimeFormatConverter,
                dateTimeSourceConverter,
                dateTimeOperations);

        // @FXML field: dateTimePositionInTheNameRadioSelector
        var posConverter = new ItemPositionWithReplacementConverter(languageTextRetriever);
        injectField("dateTimePositionInTheNameRadioSelector",
                new ItemPositionWithReplacementRadioSelector("", posConverter));

        // @FXML field: datetimeAndNameSeparatorLabel
        injectField("datetimeAndNameSeparatorLabel", new Label());

        // @FXML field: dateTimeAndNameSeparatorTextField
        injectField("dateTimeAndNameSeparatorTextField", new TextField());

        // @FXML field: dateFormatChoiceBox
        injectField("dateFormatChoiceBox", new ChoiceBox<ua.renamer.app.core.enums.DateFormat>());

        // @FXML field: timeFormatChoiceBox
        injectField("timeFormatChoiceBox", new ChoiceBox<ua.renamer.app.core.enums.TimeFormat>());

        // @FXML field: dateTimeFormatChoiceBox
        injectField("dateTimeFormatChoiceBox", new ChoiceBox<ua.renamer.app.core.enums.DateTimeFormat>());

        // @FXML field: dateTimeSourceChoiceBox
        injectField("dateTimeSourceChoiceBox", new ChoiceBox<ua.renamer.app.core.enums.DateTimeSource>());

        // @FXML field: useFallbackDateTimeCheckBox
        injectField("useFallbackDateTimeCheckBox", new CheckBox());

        // @FXML field: useCustomDateTimeAsFallbackCheckBox
        injectField("useCustomDateTimeAsFallbackCheckBox", new CheckBox());

        // @FXML field: dateTimePicker (GridPane container)
        injectField("dateTimePicker", new GridPane());

        // @FXML field: datePicker
        injectField("datePicker", new DatePicker());

        // @FXML field: hourSpinner — range [0, 23], initial = 0
        injectField("hourSpinner",
                new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0)));

        // @FXML field: minuteSpinner — range [0, 59], initial = 0
        injectField("minuteSpinner",
                new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0)));

        // @FXML field: secondSpinner — range [0, 59], initial = 0
        injectField("secondSpinner",
                new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0)));

        // @FXML field: useUppercaseForAmPmCheckBox
        injectField("useUppercaseForAmPmCheckBox", new CheckBox());

        // Run initialize() on the FX thread (mirrors FXML-loader lifecycle)
        runOnFxThreadAndWait(() -> controller.initialize(null, null));
    }

    // -----------------------------------------------------------------------
    // bind() — init: controls reflect params values after bind()
    // -----------------------------------------------------------------------

    private void injectField(String fieldName, Object value) throws Exception {
        Field f = ModeUseDatetimeController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(controller, value);
    }

    // -----------------------------------------------------------------------
    // bind() — wire: user interaction propagates to modeApi via mutator
    // -----------------------------------------------------------------------

    @FunctionalInterface
    private interface RunnableEx {
        void run() throws Exception;
    }

    // -----------------------------------------------------------------------
    // bind() — visibility: source and position drive panel/label visibility
    // -----------------------------------------------------------------------

    @Nested
    class SupportedModeTests {

        @Test
        void supportedMode_returnsUseDatetime() {
            TransformationMode result = controller.supportedMode();

            assertThat(result).isEqualTo(TransformationMode.USE_DATETIME);
        }

        @Test
        void supportedMode_isNotNull() {
            assertThat(controller.supportedMode()).isNotNull();
        }

        @Test
        void supportedMode_doesNotThrow() {
            assertThatCode(() -> controller.supportedMode()).doesNotThrowAnyException();
        }
    }

    // -----------------------------------------------------------------------
    // No-throw contract — V2 pipeline must never propagate exceptions
    // -----------------------------------------------------------------------

    @Nested
    class BindInitTests {

        @Mock
        private ModeApi<DateTimeParams> modeApi;

        @Test
        void bind_initializesSourceChoiceBox() throws Exception {
            // Arrange — non-default source
            when(modeApi.currentParameters()).thenReturn(baseline().withSource(
                    ua.renamer.app.api.enums.DateTimeSource.FILE_MODIFICATION_DATE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readSourceBoxUnchecked(controller).getValue())
                    .isEqualTo(ua.renamer.app.core.enums.DateTimeSource.FILE_MODIFICATION_DATE);
        }

        @Test
        void bind_initializesDateFormatChoiceBox() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline().withDateFormat(
                    ua.renamer.app.api.enums.DateFormat.YYYY_MM_DD_DASHED));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readDateFormatBoxUnchecked(controller).getValue())
                    .isEqualTo(ua.renamer.app.core.enums.DateFormat.YYYY_MM_DD_DASHED);
        }

        @Test
        void bind_initializesTimeFormatChoiceBox() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline().withTimeFormat(
                    ua.renamer.app.api.enums.TimeFormat.HH_MM_SS_24_DASHED));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readTimeFormatBoxUnchecked(controller).getValue())
                    .isEqualTo(ua.renamer.app.core.enums.TimeFormat.HH_MM_SS_24_DASHED);
        }

        @Test
        void bind_initializesPositionToEnd() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline().withPosition(
                    ua.renamer.app.api.enums.ItemPositionWithReplacement.END));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — radio selector should have END selected
            var selector = readRadioSelectorUnchecked(controller);
            assertThat(selector.getSelectedValue())
                    .isEqualTo(ua.renamer.app.core.enums.ItemPositionWithReplacement.END);
        }

        @Test
        void bind_initializesPositionToReplace() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline().withPosition(
                    ua.renamer.app.api.enums.ItemPositionWithReplacement.REPLACE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            var selector = readRadioSelectorUnchecked(controller);
            assertThat(selector.getSelectedValue())
                    .isEqualTo(ua.renamer.app.core.enums.ItemPositionWithReplacement.REPLACE);
        }

        @Test
        void bind_initializesUseFallbackDateTimeToTrue() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline().withUseFallbackDateTime(true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readUseFallbackCheckBoxUnchecked(controller).isSelected()).isTrue();
        }

        @Test
        void bind_initializesUseFallbackDateTimeToFalse() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline().withUseFallbackDateTime(false));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readUseFallbackCheckBoxUnchecked(controller).isSelected()).isFalse();
        }

        @Test
        void bind_initializesUseCustomDateTimeAsFallbackToTrue() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withUseCustomDateTimeAsFallback(true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readUseCustomFallbackCheckBoxUnchecked(controller).isSelected()).isTrue();
        }

        @Test
        void bind_initializesUseCustomDateTimeAsFallbackToFalse() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withUseCustomDateTimeAsFallback(false));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readUseCustomFallbackCheckBoxUnchecked(controller).isSelected()).isFalse();
        }

        @Test
        void bind_initializesUseUppercaseForAmPmToTrue() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withUseUppercaseForAmPm(true));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readUseUppercaseCheckBoxUnchecked(controller).isSelected()).isTrue();
        }

        @Test
        void bind_initializesUseUppercaseForAmPmToFalse() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withUseUppercaseForAmPm(false));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readUseUppercaseCheckBoxUnchecked(controller).isSelected()).isFalse();
        }

        @Test
        void bind_initializesDatePickerFromCustomDateTime() throws Exception {
            // Arrange
            LocalDateTime customDt = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
            when(modeApi.currentParameters()).thenReturn(baseline().withCustomDateTime(customDt));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readDatePickerUnchecked(controller).getValue())
                    .isEqualTo(LocalDate.of(2024, 6, 15));
        }

        @Test
        void bind_initializesHourSpinnerFromCustomDateTime() throws Exception {
            // Arrange
            LocalDateTime customDt = LocalDateTime.of(2024, 6, 15, 14, 30, 45);
            when(modeApi.currentParameters()).thenReturn(baseline().withCustomDateTime(customDt));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readHourSpinnerUnchecked(controller).getValue()).isEqualTo(14);
        }

        @Test
        void bind_initializesMinuteSpinnerFromCustomDateTime() throws Exception {
            // Arrange
            LocalDateTime customDt = LocalDateTime.of(2024, 6, 15, 10, 47, 45);
            when(modeApi.currentParameters()).thenReturn(baseline().withCustomDateTime(customDt));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readMinuteSpinnerUnchecked(controller).getValue()).isEqualTo(47);
        }

        @Test
        void bind_initializesSecondSpinnerFromCustomDateTime() throws Exception {
            // Arrange
            LocalDateTime customDt = LocalDateTime.of(2024, 6, 15, 10, 30, 58);
            when(modeApi.currentParameters()).thenReturn(baseline().withCustomDateTime(customDt));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readSecondSpinnerUnchecked(controller).getValue()).isEqualTo(58);
        }

        @Test
        void bind_doesNotOverrideDatePicker_whenCustomDateTimeIsNull() throws Exception {
            // Arrange — record the initial date before bind; null customDateTime must leave it unchanged
            LocalDate initialDate = LocalDate.of(2020, 1, 1);
            runOnFxThreadAndWait(() -> readDatePickerUnchecked(controller).setValue(initialDate));
            when(modeApi.currentParameters()).thenReturn(baseline()); // customDateTime == null

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — datePicker must not have been cleared or replaced with null
            assertThat(readDatePickerUnchecked(controller).getValue()).isNotNull();
        }

        @Test
        void bind_doesNotThrow() {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());

            // Act + Assert
            assertThatCode(() -> runOnFxThreadAndWait(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }
    }

    // -----------------------------------------------------------------------
    // FX threading utilities
    // -----------------------------------------------------------------------

    @Nested
    class BindWireTests {

        @Mock
        private ModeApi<DateTimeParams> modeApi;

        @Test
        void source_change_propagatesSource() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readSourceBoxUnchecked(controller)
                    .setValue(ua.renamer.app.core.enums.DateTimeSource.FILE_MODIFICATION_DATE));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.source())
                    .isEqualTo(ua.renamer.app.api.enums.DateTimeSource.FILE_MODIFICATION_DATE);
        }

        @Test
        void dateFormat_change_toNonDefault_setsUseDatePartTrue() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readDateFormatBoxUnchecked(controller)
                    .setValue(ua.renamer.app.core.enums.DateFormat.YYYY_MM_DD_DASHED));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.dateFormat())
                    .isEqualTo(ua.renamer.app.api.enums.DateFormat.YYYY_MM_DD_DASHED);
            assertThat(updated.useDatePart()).isTrue();
        }

        @Test
        void dateFormat_change_toDoNotUseDate_setsUseDatePartFalse() throws Exception {
            // Arrange — start with a non-default date format so the "change" is detectable
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withDateFormat(ua.renamer.app.api.enums.DateFormat.YYYY_MM_DD_DASHED)
                            .withUseDatePart(true));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readDateFormatBoxUnchecked(controller)
                    .setValue(ua.renamer.app.core.enums.DateFormat.DO_NOT_USE_DATE));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.dateFormat())
                    .isEqualTo(ua.renamer.app.api.enums.DateFormat.DO_NOT_USE_DATE);
            assertThat(updated.useDatePart()).isFalse();
        }

        @Test
        void timeFormat_change_toNonDefault_setsUseTimePartTrue() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readTimeFormatBoxUnchecked(controller)
                    .setValue(ua.renamer.app.core.enums.TimeFormat.HH_MM_SS_24_DASHED));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.timeFormat())
                    .isEqualTo(ua.renamer.app.api.enums.TimeFormat.HH_MM_SS_24_DASHED);
            assertThat(updated.useTimePart()).isTrue();
        }

        @Test
        void timeFormat_change_toDoNotUseTime_setsUseTimePartFalse() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withTimeFormat(ua.renamer.app.api.enums.TimeFormat.HH_MM_SS_24_DASHED)
                            .withUseTimePart(true));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readTimeFormatBoxUnchecked(controller)
                    .setValue(ua.renamer.app.core.enums.TimeFormat.DO_NOT_USE_TIME));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.timeFormat())
                    .isEqualTo(ua.renamer.app.api.enums.TimeFormat.DO_NOT_USE_TIME);
            assertThat(updated.useTimePart()).isFalse();
        }

        @Test
        void position_change_toEnd_propagates() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act — select END via the radio selector toggle group
            runOnFxThreadAndWait(() -> {
                var selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue()
                                == ua.renamer.app.core.enums.ItemPositionWithReplacement.END)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.position())
                    .isEqualTo(ua.renamer.app.api.enums.ItemPositionWithReplacement.END);
        }

        @Test
        void position_change_toReplace_propagates() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> {
                var selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue()
                                == ua.renamer.app.core.enums.ItemPositionWithReplacement.REPLACE)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.position())
                    .isEqualTo(ua.renamer.app.api.enums.ItemPositionWithReplacement.REPLACE);
        }

        @Test
        void useFallbackDateTime_change_toTrue_propagates() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readUseFallbackCheckBoxUnchecked(controller).setSelected(true));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.useFallbackDateTime()).isTrue();
        }

        @Test
        void useFallbackDateTime_change_toFalse_propagates() throws Exception {
            // Arrange — start with true so toggling to false triggers a real change event
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withUseFallbackDateTime(true));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readUseFallbackCheckBoxUnchecked(controller).setSelected(false));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline().withUseFallbackDateTime(true));
            assertThat(updated.useFallbackDateTime()).isFalse();
        }

        @Test
        void useCustomDateTimeAsFallback_change_toTrue_propagates() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() ->
                    readUseCustomFallbackCheckBoxUnchecked(controller).setSelected(true));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.useCustomDateTimeAsFallback()).isTrue();
        }

        @Test
        void useCustomDateTimeAsFallback_change_toFalse_propagates() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withUseCustomDateTimeAsFallback(true));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() ->
                    readUseCustomFallbackCheckBoxUnchecked(controller).setSelected(false));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(
                    baseline().withUseCustomDateTimeAsFallback(true));
            assertThat(updated.useCustomDateTimeAsFallback()).isFalse();
        }

        @Test
        void datePicker_change_propagatesCustomDateTime() throws Exception {
            // Arrange
            LocalDateTime initialDt = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
            when(modeApi.currentParameters()).thenReturn(baseline().withCustomDateTime(initialDt));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            LocalDate newDate = LocalDate.of(2025, 3, 20);
            runOnFxThreadAndWait(() -> readDatePickerUnchecked(controller).setValue(newDate));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline().withCustomDateTime(initialDt));
            assertThat(updated.customDateTime()).isNotNull();
            assertThat(updated.customDateTime().toLocalDate()).isEqualTo(newDate);
        }

        @Test
        void hourSpinner_change_propagatesCustomDateTime() throws Exception {
            // Arrange
            LocalDateTime initialDt = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
            when(modeApi.currentParameters()).thenReturn(baseline().withCustomDateTime(initialDt));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() ->
                    readHourSpinnerUnchecked(controller).getValueFactory().setValue(18));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline().withCustomDateTime(initialDt));
            assertThat(updated.customDateTime()).isNotNull();
            assertThat(updated.customDateTime().getHour()).isEqualTo(18);
        }

        @Test
        void minuteSpinner_change_propagatesCustomDateTime() throws Exception {
            // Arrange
            LocalDateTime initialDt = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
            when(modeApi.currentParameters()).thenReturn(baseline().withCustomDateTime(initialDt));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() ->
                    readMinuteSpinnerUnchecked(controller).getValueFactory().setValue(42));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline().withCustomDateTime(initialDt));
            assertThat(updated.customDateTime()).isNotNull();
            assertThat(updated.customDateTime().getMinute()).isEqualTo(42);
        }

        @Test
        void secondSpinner_change_propagatesCustomDateTime() throws Exception {
            // Arrange
            LocalDateTime initialDt = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
            when(modeApi.currentParameters()).thenReturn(baseline().withCustomDateTime(initialDt));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() ->
                    readSecondSpinnerUnchecked(controller).getValueFactory().setValue(55));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline().withCustomDateTime(initialDt));
            assertThat(updated.customDateTime()).isNotNull();
            assertThat(updated.customDateTime().getSecond()).isEqualTo(55);
        }

        @Test
        void useUppercaseForAmPm_change_toTrue_propagates() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() ->
                    readUseUppercaseCheckBoxUnchecked(controller).setSelected(true));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.useUppercaseForAmPm()).isTrue();
        }

        @Test
        void useUppercaseForAmPm_change_toFalse_propagates() throws Exception {
            // Arrange — start selected so toggle to false is a real change
            when(modeApi.currentParameters()).thenReturn(
                    baseline().withUseUppercaseForAmPm(true));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() ->
                    readUseUppercaseCheckBoxUnchecked(controller).setSelected(false));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(
                    baseline().withUseUppercaseForAmPm(true));
            assertThat(updated.useUppercaseForAmPm()).isFalse();
        }

        @Test
        void dateTimeFormat_change_propagates() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act — change dateTimeFormatChoiceBox
            ChoiceBox<ua.renamer.app.core.enums.DateTimeFormat> dateTimeFormatBox =
                    readFieldUnchecked(controller, "dateTimeFormatChoiceBox");
            runOnFxThreadAndWait(() -> dateTimeFormatBox.setValue(
                    ua.renamer.app.core.enums.DateTimeFormat.DATE_TIME_DASHED));

            // Assert — updateParameters must have been called
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.dateTimeFormat())
                    .isEqualTo(ua.renamer.app.api.enums.DateTimeFormat.DATE_TIME_DASHED);
        }

        @Test
        void separator_change_propagates() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline());
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act — change separator text field
            TextField separatorField = readFieldUnchecked(controller, "dateTimeAndNameSeparatorTextField");
            runOnFxThreadAndWait(() -> separatorField.setText("_-_"));

            // Assert
            ArgumentCaptor<ModeApi.ParamMutator<DateTimeParams>> captor =
                    ArgumentCaptor.forClass(ModeApi.ParamMutator.class);
            verify(modeApi, atLeastOnce()).updateParameters(captor.capture());
            DateTimeParams updated = captor.getValue().apply(baseline());
            assertThat(updated.separator()).isEqualTo("_-_");
        }
    }

    @Nested
    class BindVisibilityTests {

        @Mock
        private ModeApi<DateTimeParams> modeApi;

        @Test
        void bind_withSourceCurrentDate_hidesDateTimePicker() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline()
                    .withSource(ua.renamer.app.api.enums.DateTimeSource.CURRENT_DATE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert — CURRENT_DATE never needs a custom date picker
            assertThat(readDateTimePickerPanelUnchecked(controller).isVisible()).isFalse();
        }

        @Test
        void bind_withSourceCustomDate_showsDateTimePicker() throws Exception {
            // Arrange
            when(modeApi.currentParameters()).thenReturn(baseline()
                    .withSource(ua.renamer.app.api.enums.DateTimeSource.CUSTOM_DATE));

            // Act
            runOnFxThreadAndWait(() -> controller.bind(modeApi));

            // Assert
            assertThat(readDateTimePickerPanelUnchecked(controller).isVisible()).isTrue();
        }

        @Test
        void source_changeToCustomDate_showsDateTimePicker() throws Exception {
            // Arrange — start with a non-CUSTOM_DATE source
            when(modeApi.currentParameters()).thenReturn(baseline()
                    .withSource(ua.renamer.app.api.enums.DateTimeSource.FILE_CREATION_DATE));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readSourceBoxUnchecked(controller)
                    .setValue(ua.renamer.app.core.enums.DateTimeSource.CUSTOM_DATE));

            // Assert
            assertThat(readDateTimePickerPanelUnchecked(controller).isVisible()).isTrue();
        }

        @Test
        void source_changeToCurrentDate_hidesDateTimePicker() throws Exception {
            // Arrange — start with CUSTOM_DATE so panel is visible
            when(modeApi.currentParameters()).thenReturn(baseline()
                    .withSource(ua.renamer.app.api.enums.DateTimeSource.CUSTOM_DATE));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> readSourceBoxUnchecked(controller)
                    .setValue(ua.renamer.app.core.enums.DateTimeSource.CURRENT_DATE));

            // Assert
            assertThat(readDateTimePickerPanelUnchecked(controller).isVisible()).isFalse();
        }

        @Test
        void position_changeToReplace_hidesSeparatorLabel() throws Exception {
            // Arrange — start at BEGIN (separator label should be visible)
            when(modeApi.currentParameters()).thenReturn(baseline()
                    .withPosition(ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN));
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));
            runOnFxThreadAndWait(() -> controller.bind(modeApi));
            clearInvocations(modeApi);
            when(modeApi.updateParameters(any()))
                    .thenReturn(CompletableFuture.completedFuture(ValidationResult.valid()));

            // Act
            runOnFxThreadAndWait(() -> {
                var selector = readRadioSelectorUnchecked(controller);
                selector.getButtons().stream()
                        .filter(btn -> btn.getValue()
                                == ua.renamer.app.core.enums.ItemPositionWithReplacement.REPLACE)
                        .findFirst()
                        .ifPresent(btn -> selector.getToggleGroup().selectToggle(btn));
            });

            // Assert — separator label is hidden when REPLACE is selected
            assertThat(readSeparatorLabelUnchecked(controller).isVisible()).isFalse();
        }
    }

    @Nested
    class NoThrowContractTests {

        @Mock
        private ModeApi<DateTimeParams> modeApi;

        @Test
        void supportedMode_neverThrows() {
            assertThatCode(() -> controller.supportedMode()).doesNotThrowAnyException();
        }

        @Test
        void bind_neverThrows() {
            when(modeApi.currentParameters()).thenReturn(baseline());

            assertThatCode(() -> runOnFxThreadAndWaitUnchecked(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }

        @Test
        void bind_neverThrows_withAllNullableFieldsAsNull() {
            // dateFormat and timeFormat are nullable — pass null to verify null-safe guards
            when(modeApi.currentParameters()).thenReturn(new DateTimeParams(
                    ua.renamer.app.api.enums.DateTimeSource.FILE_CREATION_DATE,
                    null, // dateFormat
                    null, // timeFormat
                    ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                    false, false, false, false, false,
                    null, // customDateTime
                    false,
                    null, // dateTimeFormat
                    null  // separator
            ));

            assertThatCode(() -> runOnFxThreadAndWaitUnchecked(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }

        @Test
        void bind_neverThrows_withNullPosition() {
            // position is nullable — verify guard
            when(modeApi.currentParameters()).thenReturn(new DateTimeParams(
                    ua.renamer.app.api.enums.DateTimeSource.FILE_CREATION_DATE,
                    ua.renamer.app.api.enums.DateFormat.DO_NOT_USE_DATE,
                    ua.renamer.app.api.enums.TimeFormat.DO_NOT_USE_TIME,
                    null, // position
                    false, false, false, false, false, null, false,
                    ua.renamer.app.api.enums.DateTimeFormat.DATE_TIME_TOGETHER, ""));

            assertThatCode(() -> runOnFxThreadAndWaitUnchecked(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }

        @Test
        void bind_neverThrows_withNullSource() {
            // source is nullable — verify guard
            when(modeApi.currentParameters()).thenReturn(new DateTimeParams(
                    null, // source
                    ua.renamer.app.api.enums.DateFormat.DO_NOT_USE_DATE,
                    ua.renamer.app.api.enums.TimeFormat.DO_NOT_USE_TIME,
                    ua.renamer.app.api.enums.ItemPositionWithReplacement.BEGIN,
                    false, false, false, false, false, null, false,
                    ua.renamer.app.api.enums.DateTimeFormat.DATE_TIME_TOGETHER, ""));

            assertThatCode(() -> runOnFxThreadAndWaitUnchecked(() -> controller.bind(modeApi)))
                    .doesNotThrowAnyException();
        }

    }
}
