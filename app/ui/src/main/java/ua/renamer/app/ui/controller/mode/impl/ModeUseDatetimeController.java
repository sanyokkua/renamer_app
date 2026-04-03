package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.DateTimeParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.core.service.command.impl.preparation.DateTimeRenamePrepareInformationCommand;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.converter.DateFormatConverter;
import ua.renamer.app.ui.converter.DateTimeFormatConverter;
import ua.renamer.app.ui.converter.DateTimeSourceConverter;
import ua.renamer.app.ui.converter.TimeFormatConverter;
import ua.renamer.app.ui.widget.impl.ItemPositionWithReplacementRadioSelector;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeUseDatetimeController extends ModeBaseController
        implements ModeControllerV2Api<DateTimeParams> {

    private final DateFormatConverter dateFormatConverter;
    private final TimeFormatConverter timeFormatConverter;
    private final DateTimeFormatConverter dateTimeFormatConverter;
    private final DateTimeSourceConverter dateTimeSourceConverter;
    private final DateTimeOperations dateTimeOperations;

    @FXML
    private ItemPositionWithReplacementRadioSelector dateTimePositionInTheNameRadioSelector;
    @FXML
    private Label datetimeAndNameSeparatorLabel;
    @FXML
    private TextField dateTimeAndNameSeparatorTextField;
    @FXML
    private ChoiceBox<DateFormat> dateFormatChoiceBox;
    @FXML
    private ChoiceBox<TimeFormat> timeFormatChoiceBox;
    @FXML
    private ChoiceBox<DateTimeFormat> dateTimeFormatChoiceBox;
    @FXML
    private ChoiceBox<DateTimeSource> dateTimeSourceChoiceBox;
    @FXML
    private CheckBox useFallbackDateTimeCheckBox;
    @FXML
    private CheckBox useCustomDateTimeAsFallbackCheckBox;
    @FXML
    private GridPane dateTimePicker;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Spinner<Integer> hourSpinner;
    @FXML
    private Spinner<Integer> minuteSpinner;
    @FXML
    private Spinner<Integer> secondSpinner;
    @FXML
    private CheckBox useUppercaseForAmPmCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configItemPositionSelector();
        configDateTimeAndNameSeparatorTextField();
        configDateFormatChoiceBox();
        configTimeFormatChoiceBox();
        configDateTimeFormatChoiceBox();
        configDateTimeSourceChoiceBox();
        configUseFallbackDateTimeCheckBox();
        configUseCustomFallbackDateTimeCheckBox();
        configDateTimePickerWidgets();
        configUseAmPmInUppercaseCheckBox();
        updateDisplayedItems();
    }

    private void configItemPositionSelector() {
        log.info("configItemPositionSelector()");
        dateTimePositionInTheNameRadioSelector.addValueSelectedHandler(this::handlePositionChanged);
    }

    private void configDateTimeAndNameSeparatorTextField() {
        log.info("configDateTimeAndNameSeparatorTextField()");
        dateTimeAndNameSeparatorTextField.textProperty()
                .addListener((observable, oldValue, newValue) -> this.handleDateTimeAndNameSeparatorChanged(newValue));
    }

    private void configDateFormatChoiceBox() {
        log.info("configDateFormatChoiceBox()");
        dateFormatChoiceBox.getItems().addAll(DateFormat.values());
        dateFormatChoiceBox.setValue(DateFormat.DO_NOT_USE_DATE);
        dateFormatChoiceBox.setConverter(dateFormatConverter);
        dateFormatChoiceBox.setOnAction(event -> handleDateFormatChanged());
    }

    private void configTimeFormatChoiceBox() {
        log.info("configTimeFormatChoiceBox()");
        timeFormatChoiceBox.getItems().addAll(TimeFormat.values());
        timeFormatChoiceBox.setValue(TimeFormat.DO_NOT_USE_TIME);
        timeFormatChoiceBox.setConverter(timeFormatConverter);
        timeFormatChoiceBox.setOnAction(event -> handleTimeFormatChanged());
    }

    private void configDateTimeFormatChoiceBox() {
        log.info("configDateTimeFormatChoiceBox()");
        dateTimeFormatChoiceBox.getItems().addAll(DateTimeFormat.values());
        dateTimeFormatChoiceBox.setValue(DateTimeFormat.DATE_TIME_TOGETHER);
        dateTimeFormatChoiceBox.setConverter(dateTimeFormatConverter);
        dateTimeFormatChoiceBox.setOnAction(event -> handleDateTimeFormatChanged());
    }

    private void configDateTimeSourceChoiceBox() {
        log.info("configDateTimeSourceChoiceBox()");
        dateTimeSourceChoiceBox.getItems().addAll(DateTimeSource.values());
        dateTimeSourceChoiceBox.setValue(DateTimeSource.FILE_CREATION_DATE);
        dateTimeSourceChoiceBox.setConverter(dateTimeSourceConverter);
        dateTimeSourceChoiceBox.setOnAction(event -> handleTimeSourceChanged());
    }

    private void configUseFallbackDateTimeCheckBox() {
        log.info("configUseFallbackDateTimeCheckBox()");
        BooleanProperty useFallbackDateTimeProperty = useFallbackDateTimeCheckBox.selectedProperty();
        useFallbackDateTimeProperty.addListener((observable, oldValue, newValue) -> this.handleUseFallbackDateTimeChanged(newValue));
    }

    private void configUseCustomFallbackDateTimeCheckBox() {
        log.info("configUseCustomFallbackDateTimeCheckBox()");
        BooleanProperty useCustomFallbackDateTimeProperty = useCustomDateTimeAsFallbackCheckBox.selectedProperty();
        useCustomFallbackDateTimeProperty.addListener((observable, oldValue, newValue) -> this.handleUseCustomFallbackDateTimeChanged(newValue));
    }

    private void configDateTimePickerWidgets() {
        log.info("configDateTimePickerWidgets()");
        var currentDateTime = LocalDateTime.now();

        datePicker.setValue(currentDateTime.toLocalDate());

        var hourValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, currentDateTime.getHour());
        var minuteValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, currentDateTime.getMinute());
        var secondValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, currentDateTime.getSecond());

        hourSpinner.setValueFactory(hourValueFactory);
        hourSpinner.setEditable(true);

        minuteSpinner.setValueFactory(minuteValueFactory);
        minuteSpinner.setEditable(true);

        secondSpinner.setValueFactory(secondValueFactory);
        secondSpinner.setEditable(true);

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> handleDateTimeChange());
        hourSpinner.valueProperty().addListener((observable, oldValue, newValue) -> handleDateTimeChange());
        minuteSpinner.valueProperty().addListener((observable, oldValue, newValue) -> handleDateTimeChange());
        secondSpinner.valueProperty().addListener((observable, oldValue, newValue) -> handleDateTimeChange());
    }

    private void configUseAmPmInUppercaseCheckBox() {
        log.info("configUseAmPmInUppercaseCheckBox()");
        BooleanProperty useAmPmUppercaseProperty = useUppercaseForAmPmCheckBox.selectedProperty();
        useAmPmUppercaseProperty.addListener((observable, oldValue, newValue) -> this.handleUseAmPmUppercaseChanged(newValue));
    }

    private void handlePositionChanged(ItemPositionWithReplacement itemPositionWithReplacement) {
        log.debug("itemPositionWithReplacement: {}", itemPositionWithReplacement);
        updateCommand();
    }

    private void handleDateTimeAndNameSeparatorChanged(String newValue) {
        log.debug("newValue: {}", newValue);
        updateCommand();
    }

    private void handleDateFormatChanged() {
        log.debug("dateFormatChanged");
        updateCommand();
    }

    private void handleTimeFormatChanged() {
        log.debug("timeFormatChanged");
        updateCommand();
    }

    private void handleDateTimeFormatChanged() {
        log.debug("dateTimeFormatChanged");
        updateCommand();
    }

    private void handleTimeSourceChanged() {
        log.debug("timeSourceChanged");
        updateCommand();
    }

    private void handleUseFallbackDateTimeChanged(Boolean newValue) {
        log.debug("useFallbackDateTimeChanged: {}", newValue);
        updateCommand();
    }

    private void handleUseCustomFallbackDateTimeChanged(Boolean newValue) {
        log.debug("useCustomFallbackDateTimeChanged: {}", newValue);
        updateCommand();
    }

    private void handleDateTimeChange() {
        log.debug("dateTimeChange");
        updateCommand();
    }

    private void handleUseAmPmUppercaseChanged(Boolean newValue) {
        log.debug("useAmPmUppercaseChanged: {}", newValue);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        updateDisplayedItems();

        var dateTimePositionInTheName = dateTimePositionInTheNameRadioSelector.getSelectedValue();
        var dateFormat = dateFormatChoiceBox.getSelectionModel().getSelectedItem();
        var timeFormat = timeFormatChoiceBox.getSelectionModel().getSelectedItem();
        var dateTimeFormat = dateTimeFormatChoiceBox.getSelectionModel().getSelectedItem();
        var dateTimeSource = dateTimeSourceChoiceBox.getSelectionModel().getSelectedItem();
        var useUppercaseForAmPm = useUppercaseForAmPmCheckBox.isSelected();
        var customDateTime = getCustomLocalDateTime();
        var dateTimeAndNameSeparator = dateTimeAndNameSeparatorTextField.getText();
        var useFallbackDateTime = useFallbackDateTimeCheckBox.isSelected();
        var useCustomDateTimeAsFallback = useCustomDateTimeAsFallbackCheckBox.isSelected();

        var cmd = DateTimeRenamePrepareInformationCommand.builder()
                .dateTimeOperations(dateTimeOperations)
                .dateTimePositionInTheName(dateTimePositionInTheName)
                .dateFormat(dateFormat)
                .timeFormat(timeFormat)
                .dateTimeFormat(dateTimeFormat)
                .dateTimeSource(dateTimeSource)
                .useUppercaseForAmPm(useUppercaseForAmPm)
                .customDateTime(customDateTime)
                .dateTimeAndNameSeparator(dateTimeAndNameSeparator)
                .useFallbackDateTime(useFallbackDateTime)
                .useCustomDateTimeAsFallback(useCustomDateTimeAsFallback)
                .build();
        log.debug("updateCommand {}", cmd);
        setCommand(cmd);
    }

    private void updateDisplayedItems() {
        log.debug("updateDisplayedItems");
        var position = dateTimePositionInTheNameRadioSelector.getSelectedValue();
        var dateTimeSource = dateTimeSourceChoiceBox.getSelectionModel().getSelectedItem();

        var isNotReplaceMode = position != ItemPositionWithReplacement.REPLACE;
        datetimeAndNameSeparatorLabel.setVisible(isNotReplaceMode);
        dateTimeAndNameSeparatorTextField.setVisible(isNotReplaceMode);

        switch (dateTimeSource) {
            case CURRENT_DATE:
                setVisibleDateTimeChooser(false);
                setVisibleUseFallbackCheckBox(false);
                setVisibleUseCustomFallbackCheckBox(false);
                break;
            case CUSTOM_DATE:
                setVisibleDateTimeChooser(true);
                setVisibleUseFallbackCheckBox(false);
                setVisibleUseCustomFallbackCheckBox(false);
                break;
            default:
                setVisibleDateTimeChooser(false);
                setVisibleUseFallbackCheckBox(true);

                // Show additional controls based on checkboxes
                boolean useFallback = useFallbackDateTimeCheckBox.isSelected();
                setVisibleUseCustomFallbackCheckBox(useFallback);

                boolean useCustomDateTime = useCustomDateTimeAsFallbackCheckBox.isSelected();
                setVisibleDateTimeChooser(useFallback && useCustomDateTime);
                break;
        }
    }

    private void setVisibleDateTimeChooser(boolean visible) {
        log.debug("setVisibleDateTimeChooser: {}", visible);
        dateTimePicker.setVisible(visible);
    }

    private void setVisibleUseFallbackCheckBox(boolean visible) {
        log.debug("setVisibleUseFallbackCheckBox: {}", visible);
        if (visible) {
            useFallbackDateTimeCheckBox.setDisable(false);
            useFallbackDateTimeCheckBox.setVisible(true);
        } else {
            useFallbackDateTimeCheckBox.setDisable(true);
            useFallbackDateTimeCheckBox.setVisible(false);
        }
    }

    private void setVisibleUseCustomFallbackCheckBox(boolean visible) {
        log.debug("setVisibleUseCustomFallbackCheckBox: {}", visible);
        if (visible) {
            useCustomDateTimeAsFallbackCheckBox.setDisable(false);
            useCustomDateTimeAsFallbackCheckBox.setVisible(true);
        } else {
            useCustomDateTimeAsFallbackCheckBox.setDisable(true);
            useCustomDateTimeAsFallbackCheckBox.setVisible(false);
        }
    }

    private LocalDateTime getCustomLocalDateTime() {
        LocalDate date = datePicker.getValue();

        if (Objects.isNull(date)) {
            log.warn("Date was null, current date will be used");
            date = LocalDate.now();
            datePicker.setValue(date);
        }

        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        int second = secondSpinner.getValue();

        LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(hour, minute, second));

        log.debug("getCustomLocalDateTime: {}", dateTime);
        return dateTime;
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.USE_DATETIME;
    }

    @Override
    public void bind(ModeApi<DateTimeParams> modeApi) {
        var params = modeApi.currentParameters();

        // ── Init: source (API → core enum) ──────────────────────────────────────
        if (params.source() != null) {
            var coreSource = ua.renamer.app.core.enums.DateTimeSource.valueOf(params.source().name());
            dateTimeSourceChoiceBox.setValue(coreSource);
        }

        // ── Init: dateFormat (API → core enum) ──────────────────────────────────
        if (params.dateFormat() != null) {
            var coreDateFormat = ua.renamer.app.core.enums.DateFormat.valueOf(params.dateFormat().name());
            dateFormatChoiceBox.setValue(coreDateFormat);
        }

        // ── Init: timeFormat (API → core enum) ──────────────────────────────────
        if (params.timeFormat() != null) {
            var coreTimeFormat = ua.renamer.app.core.enums.TimeFormat.valueOf(params.timeFormat().name());
            timeFormatChoiceBox.setValue(coreTimeFormat);
        }

        // ── Init: position (API → core enum, radio selector) ────────────────────
        if (params.position() != null) {
            var corePos = ua.renamer.app.core.enums.ItemPositionWithReplacement
                    .valueOf(params.position().name());
            dateTimePositionInTheNameRadioSelector.getButtons().stream()
                    .filter(btn -> btn.getValue() == corePos)
                    .findFirst()
                    .ifPresent(btn -> dateTimePositionInTheNameRadioSelector
                            .getToggleGroup().selectToggle(btn));
        }

        // ── Init: boolean flags ──────────────────────────────────────────────────
        useFallbackDateTimeCheckBox.setSelected(params.useFallbackDateTime());
        useCustomDateTimeAsFallbackCheckBox.setSelected(params.useCustomDateTimeAsFallback());
        useUppercaseForAmPmCheckBox.setSelected(params.useUppercaseForAmPm());

        // ── Init: customDateTime (null-safe) ────────────────────────────────────
        if (params.customDateTime() != null) {
            datePicker.setValue(params.customDateTime().toLocalDate());
            hourSpinner.getValueFactory().setValue(params.customDateTime().getHour());
            minuteSpinner.getValueFactory().setValue(params.customDateTime().getMinute());
            secondSpinner.getValueFactory().setValue(params.customDateTime().getSecond());
        }

        // ── Sync visibility after all inits ─────────────────────────────────────
        updateDisplayedItems();

        // ── Wire: source ─────────────────────────────────────────────────────────
        dateTimeSourceChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        var apiSource = ua.renamer.app.api.enums.DateTimeSource.valueOf(newVal.name());
                        log.debug("bind: source changed → {}", apiSource);
                        modeApi.updateParameters(p -> p.withSource(apiSource));
                        updateDisplayedItems();
                    }
                });

        // ── Wire: dateFormat (atomic with useDatePart) ───────────────────────────
        dateFormatChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        var apiDateFormat = ua.renamer.app.api.enums.DateFormat.valueOf(newVal.name());
                        boolean useDatePart = newVal != ua.renamer.app.core.enums.DateFormat.DO_NOT_USE_DATE;
                        log.debug("bind: dateFormat changed → {}, useDatePart={}", apiDateFormat, useDatePart);
                        modeApi.updateParameters(p -> p.withDateFormat(apiDateFormat).withUseDatePart(useDatePart));
                    }
                });

        // ── Wire: timeFormat (atomic with useTimePart) ───────────────────────────
        timeFormatChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        var apiTimeFormat = ua.renamer.app.api.enums.TimeFormat.valueOf(newVal.name());
                        boolean useTimePart = newVal != ua.renamer.app.core.enums.TimeFormat.DO_NOT_USE_TIME;
                        log.debug("bind: timeFormat changed → {}, useTimePart={}", apiTimeFormat, useTimePart);
                        modeApi.updateParameters(p -> p.withTimeFormat(apiTimeFormat).withUseTimePart(useTimePart));
                    }
                });

        // ── Wire: position ───────────────────────────────────────────────────────
        dateTimePositionInTheNameRadioSelector.addValueSelectedHandler(corePos -> {
            var apiPos = ua.renamer.app.api.enums.ItemPositionWithReplacement.valueOf(corePos.name());
            log.debug("bind: position changed → {}", apiPos);
            modeApi.updateParameters(p -> p.withPosition(apiPos));
            updateDisplayedItems();
        });

        // ── Wire: useFallbackDateTime ────────────────────────────────────────────
        useFallbackDateTimeCheckBox.selectedProperty()
                .addListener((obs, oldVal, newVal) -> {
                    log.debug("bind: useFallbackDateTime changed → {}", newVal);
                    modeApi.updateParameters(p -> p.withUseFallbackDateTime(newVal));
                    updateDisplayedItems();
                });

        // ── Wire: useCustomDateTimeAsFallback ────────────────────────────────────
        useCustomDateTimeAsFallbackCheckBox.selectedProperty()
                .addListener((obs, oldVal, newVal) -> {
                    log.debug("bind: useCustomDateTimeAsFallback changed → {}", newVal);
                    modeApi.updateParameters(p -> p.withUseCustomDateTimeAsFallback(newVal));
                    updateDisplayedItems();
                });

        // ── Wire: customDateTime pickers (all 4 map to a single field) ───────────
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            var dt = getCustomLocalDateTime();
            log.debug("bind: customDateTime (date) changed → {}", dt);
            modeApi.updateParameters(p -> p.withCustomDateTime(dt));
        });
        hourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                var dt = getCustomLocalDateTime();
                log.debug("bind: customDateTime (hour) changed → {}", dt);
                modeApi.updateParameters(p -> p.withCustomDateTime(dt));
            }
        });
        minuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                var dt = getCustomLocalDateTime();
                log.debug("bind: customDateTime (minute) changed → {}", dt);
                modeApi.updateParameters(p -> p.withCustomDateTime(dt));
            }
        });
        secondSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                var dt = getCustomLocalDateTime();
                log.debug("bind: customDateTime (second) changed → {}", dt);
                modeApi.updateParameters(p -> p.withCustomDateTime(dt));
            }
        });

        // ── Wire: useUppercaseForAmPm ────────────────────────────────────────────
        useUppercaseForAmPmCheckBox.selectedProperty()
                .addListener((obs, oldVal, newVal) -> {
                    log.debug("bind: useUppercaseForAmPm changed → {}", newVal);
                    modeApi.updateParameters(p -> p.withUseUppercaseForAmPm(newVal));
                });

        // NOTE: dateTimeFormatChoiceBox is NOT wired — no DateTimeParams field.
        // NOTE: dateTimeAndNameSeparatorTextField is NOT wired — no DateTimeParams field.
    }

}
