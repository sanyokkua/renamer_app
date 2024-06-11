package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.core.service.command.impl.preparation.DateTimeRenamePrepareInformationCommand;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
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
public class ModeUseDatetimeController extends ModeBaseController {

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

}
