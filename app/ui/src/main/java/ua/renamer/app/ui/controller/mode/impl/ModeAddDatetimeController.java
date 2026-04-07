package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.*;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.DateTimeParams;
import ua.renamer.app.api.session.ModeApi;
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

/**
 * Controller for the Add Date & Time transformation mode.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeAddDatetimeController
        implements ModeControllerV2Api<DateTimeParams>, Initializable {

    private final DateFormatConverter dateFormatConverter;
    private final TimeFormatConverter timeFormatConverter;
    private final DateTimeFormatConverter dateTimeFormatConverter;
    private final DateTimeSourceConverter dateTimeSourceConverter;

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
    private VBox dateTimePicker;
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


    private ChangeListener<DateTimeSource> sourceListener;
    private ChangeListener<DateFormat> dateFormatListener;
    private ChangeListener<TimeFormat> timeFormatListener;
    private ChangeListener<Boolean> useFallbackListener;
    private ChangeListener<Boolean> useCustomFallbackListener;
    private ChangeListener<LocalDate> datePickerListener;
    private ChangeListener<Integer> hourListener;
    private ChangeListener<Integer> minuteListener;
    private ChangeListener<Integer> secondListener;
    private ChangeListener<Boolean> useUppercaseListener;
    private ChangeListener<DateTimeFormat> dateTimeFormatListener;
    private ChangeListener<String> separatorListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configDateFormatChoiceBox();
        configTimeFormatChoiceBox();
        configDateTimeFormatChoiceBox();
        configDateTimeSourceChoiceBox();
        configDateTimePickerWidgets();
        updateDisplayedItems();
    }

    private void configDateFormatChoiceBox() {
        log.info("configDateFormatChoiceBox()");
        dateFormatChoiceBox.getItems().addAll(DateFormat.values());
        dateFormatChoiceBox.setValue(DateFormat.DO_NOT_USE_DATE);
        dateFormatChoiceBox.setConverter(dateFormatConverter);
    }

    private void configTimeFormatChoiceBox() {
        log.info("configTimeFormatChoiceBox()");
        timeFormatChoiceBox.getItems().addAll(TimeFormat.values());
        timeFormatChoiceBox.setValue(TimeFormat.DO_NOT_USE_TIME);
        timeFormatChoiceBox.setConverter(timeFormatConverter);
    }

    private void configDateTimeFormatChoiceBox() {
        log.info("configDateTimeFormatChoiceBox()");
        dateTimeFormatChoiceBox.getItems().addAll(DateTimeFormat.values());
        dateTimeFormatChoiceBox.setValue(DateTimeFormat.DATE_TIME_TOGETHER);
        dateTimeFormatChoiceBox.setConverter(dateTimeFormatConverter);
    }

    private void configDateTimeSourceChoiceBox() {
        log.info("configDateTimeSourceChoiceBox()");
        dateTimeSourceChoiceBox.getItems().addAll(DateTimeSource.values());
        dateTimeSourceChoiceBox.setValue(DateTimeSource.FILE_CREATION_DATE);
        dateTimeSourceChoiceBox.setConverter(dateTimeSourceConverter);
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
        return TransformationMode.ADD_DATETIME;
    }

    @Override
    public void bind(ModeApi<DateTimeParams> modeApi) {
        var params = modeApi.currentParameters();

        // ── Remove old listeners ──────────────────────────────────────────────
        if (sourceListener != null) {
            dateTimeSourceChoiceBox.getSelectionModel().selectedItemProperty().removeListener(sourceListener);
        }
        if (dateFormatListener != null) {
            dateFormatChoiceBox.getSelectionModel().selectedItemProperty().removeListener(dateFormatListener);
        }
        if (timeFormatListener != null) {
            timeFormatChoiceBox.getSelectionModel().selectedItemProperty().removeListener(timeFormatListener);
        }
        if (useFallbackListener != null) {
            useFallbackDateTimeCheckBox.selectedProperty().removeListener(useFallbackListener);
        }
        if (useCustomFallbackListener != null) {
            useCustomDateTimeAsFallbackCheckBox.selectedProperty().removeListener(useCustomFallbackListener);
        }
        if (datePickerListener != null) {
            datePicker.valueProperty().removeListener(datePickerListener);
        }
        if (hourListener != null) {
            hourSpinner.valueProperty().removeListener(hourListener);
        }
        if (minuteListener != null) {
            minuteSpinner.valueProperty().removeListener(minuteListener);
        }
        if (secondListener != null) {
            secondSpinner.valueProperty().removeListener(secondListener);
        }
        if (useUppercaseListener != null) {
            useUppercaseForAmPmCheckBox.selectedProperty().removeListener(useUppercaseListener);
        }
        if (dateTimeFormatListener != null) {
            dateTimeFormatChoiceBox.getSelectionModel().selectedItemProperty().removeListener(dateTimeFormatListener);
        }
        if (separatorListener != null) {
            dateTimeAndNameSeparatorTextField.textProperty().removeListener(separatorListener);
        }

        // ── Init: source ────────────────────────────────────────────────────────
        if (params.source() != null) {
            dateTimeSourceChoiceBox.setValue(params.source());
        }

        // ── Init: dateFormat ─────────────────────────────────────────────────────
        if (params.dateFormat() != null) {
            dateFormatChoiceBox.setValue(params.dateFormat());
        }

        // ── Init: timeFormat ─────────────────────────────────────────────────────
        if (params.timeFormat() != null) {
            timeFormatChoiceBox.setValue(params.timeFormat());
        }

        // ── Init: position ───────────────────────────────────────────────────────
        if (params.position() != null) {
            dateTimePositionInTheNameRadioSelector.getButtons().stream()
                    .filter(btn -> btn.getValue() == params.position())
                    .findFirst()
                    .ifPresent(btn -> dateTimePositionInTheNameRadioSelector
                            .getToggleGroup().selectToggle(btn));
        }

        // ── Init: boolean flags ──────────────────────────────────────────────────
        useFallbackDateTimeCheckBox.setSelected(params.useFallbackDateTime());
        useCustomDateTimeAsFallbackCheckBox.setSelected(params.useCustomDateTimeAsFallback());
        useUppercaseForAmPmCheckBox.setSelected(params.useUppercaseForAmPm());

        // ── Init: dateTimeFormat ─────────────────────────────────────────────────
        if (params.dateTimeFormat() != null) {
            dateTimeFormatChoiceBox.setValue(params.dateTimeFormat());
        }

        // ── Init: separator ──────────────────────────────────────────────────────
        if (params.separator() != null) {
            dateTimeAndNameSeparatorTextField.setText(params.separator());
        }

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
        sourceListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                log.debug("bind: source changed → {}", newVal);
                modeApi.updateParameters(p -> p.withSource(newVal));
                updateDisplayedItems();
            }
        };
        dateTimeSourceChoiceBox.getSelectionModel().selectedItemProperty().addListener(sourceListener);

        // ── Wire: dateFormat (atomic with useDatePart) ───────────────────────────
        dateFormatListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean useDatePart = newVal != DateFormat.DO_NOT_USE_DATE;
                log.debug("bind: dateFormat changed → {}, useDatePart={}", newVal, useDatePart);
                modeApi.updateParameters(p -> p.withDateFormat(newVal).withUseDatePart(useDatePart));
            }
        };
        dateFormatChoiceBox.getSelectionModel().selectedItemProperty().addListener(dateFormatListener);

        // ── Wire: timeFormat (atomic with useTimePart) ───────────────────────────
        timeFormatListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean useTimePart = newVal != TimeFormat.DO_NOT_USE_TIME;
                log.debug("bind: timeFormat changed → {}, useTimePart={}", newVal, useTimePart);
                modeApi.updateParameters(p -> p.withTimeFormat(newVal).withUseTimePart(useTimePart));
            }
        };
        timeFormatChoiceBox.getSelectionModel().selectedItemProperty().addListener(timeFormatListener);

        // ── Wire: position ───────────────────────────────────────────────────────
        dateTimePositionInTheNameRadioSelector.setValueSelectedHandler(pos -> {
            log.debug("bind: position changed → {}", pos);
            modeApi.updateParameters(p -> p.withPosition(pos));
            updateDisplayedItems();
        });

        // ── Wire: useFallbackDateTime ────────────────────────────────────────────
        useFallbackListener = (obs, oldVal, newVal) -> {
            log.debug("bind: useFallbackDateTime changed → {}", newVal);
            modeApi.updateParameters(p -> p.withUseFallbackDateTime(newVal));
            updateDisplayedItems();
        };
        useFallbackDateTimeCheckBox.selectedProperty().addListener(useFallbackListener);

        // ── Wire: useCustomDateTimeAsFallback ────────────────────────────────────
        useCustomFallbackListener = (obs, oldVal, newVal) -> {
            log.debug("bind: useCustomDateTimeAsFallback changed → {}", newVal);
            modeApi.updateParameters(p -> p.withUseCustomDateTimeAsFallback(newVal));
            updateDisplayedItems();
        };
        useCustomDateTimeAsFallbackCheckBox.selectedProperty().addListener(useCustomFallbackListener);

        // ── Wire: customDateTime pickers (all 4 map to a single field) ───────────
        datePickerListener = (obs, oldVal, newVal) -> {
            var dt = getCustomLocalDateTime();
            log.debug("bind: customDateTime (date) changed → {}", dt);
            modeApi.updateParameters(p -> p.withCustomDateTime(dt));
        };
        datePicker.valueProperty().addListener(datePickerListener);

        hourListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                var dt = getCustomLocalDateTime();
                log.debug("bind: customDateTime (hour) changed → {}", dt);
                modeApi.updateParameters(p -> p.withCustomDateTime(dt));
            }
        };
        hourSpinner.valueProperty().addListener(hourListener);

        minuteListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                var dt = getCustomLocalDateTime();
                log.debug("bind: customDateTime (minute) changed → {}", dt);
                modeApi.updateParameters(p -> p.withCustomDateTime(dt));
            }
        };
        minuteSpinner.valueProperty().addListener(minuteListener);

        secondListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                var dt = getCustomLocalDateTime();
                log.debug("bind: customDateTime (second) changed → {}", dt);
                modeApi.updateParameters(p -> p.withCustomDateTime(dt));
            }
        };
        secondSpinner.valueProperty().addListener(secondListener);

        // ── Wire: useUppercaseForAmPm ────────────────────────────────────────────
        useUppercaseListener = (obs, oldVal, newVal) -> {
            log.debug("bind: useUppercaseForAmPm changed → {}", newVal);
            modeApi.updateParameters(p -> p.withUseUppercaseForAmPm(newVal));
        };
        useUppercaseForAmPmCheckBox.selectedProperty().addListener(useUppercaseListener);

        // ── Wire: dateTimeFormat ─────────────────────────────────────────────────
        dateTimeFormatListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                log.debug("bind: dateTimeFormat changed → {}", newVal);
                modeApi.updateParameters(p -> p.withDateTimeFormat(newVal));
            }
        };
        dateTimeFormatChoiceBox.getSelectionModel().selectedItemProperty().addListener(dateTimeFormatListener);

        // ── Wire: separator ──────────────────────────────────────────────────────
        separatorListener = (obs, oldVal, newVal) -> {
            log.debug("bind: separator changed → '{}'", newVal);
            modeApi.updateParameters(p -> p.withSeparator(newVal != null ? newVal : ""));
        };
        dateTimeAndNameSeparatorTextField.textProperty().addListener(separatorListener);
    }

}
