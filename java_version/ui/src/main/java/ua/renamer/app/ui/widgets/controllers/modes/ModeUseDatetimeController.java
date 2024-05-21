package ua.renamer.app.ui.widgets.controllers.modes;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.commands.preparation.DateTimeRenamePrepareInformationCommand;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.ui.abstracts.ModeBaseController;
import ua.renamer.app.ui.converters.DateFormatConverter;
import ua.renamer.app.ui.converters.DateTimeFormatConverter;
import ua.renamer.app.ui.converters.DateTimeSourceConverter;
import ua.renamer.app.ui.converters.TimeFormatConverter;
import ua.renamer.app.ui.widgets.view.ItemPositionWithReplacementRadioSelector;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

@Slf4j
public class ModeUseDatetimeController extends ModeBaseController {

    @FXML
    private ItemPositionWithReplacementRadioSelector itemPositionRadioSelector;
    @FXML
    private TextField datetimeAndNameSeparatorTextField;
    @FXML
    private ChoiceBox<DateFormat> dateFormatChoiceBox;
    @FXML
    private ChoiceBox<TimeFormat> timeFormatChoiceBox;
    @FXML
    private ChoiceBox<DateTimeFormat> dateTimeFormatChoiceBox;
    @FXML
    private ChoiceBox<DateTimeSource> timeSourceChoiceBox;
    @FXML
    private CheckBox useFallbackDateTimeCheckBox;
    @FXML
    private CheckBox useCustomFallbackDateTimeCheckBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Spinner<Integer> hourSpinner;
    @FXML
    private Spinner<Integer> minuteSpinner;
    @FXML
    private Spinner<Integer> secondSpinner;
    @FXML
    private CheckBox useAmPmInUppercaseCheckBox;

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
    }

    private void configItemPositionSelector() {
        log.info("configItemPositionSelector()");
        itemPositionRadioSelector.addValueSelectedHandler(this::handlePositionChanged);
    }

    private void configDateTimeAndNameSeparatorTextField() {
        log.info("configDateTimeAndNameSeparatorTextField()");
        datetimeAndNameSeparatorTextField.textProperty()
                                         .addListener((observable, oldValue, newValue) -> this.handleDateTimeAndNameSeparatorChanged(
                                                 newValue));
    }

    private void configDateFormatChoiceBox() {
        log.info("configDateFormatChoiceBox()");
        dateFormatChoiceBox.getItems().addAll(DateFormat.values());
        dateFormatChoiceBox.setValue(DateFormat.DO_NOT_USE_DATE);
        dateFormatChoiceBox.setConverter(new DateFormatConverter());
        dateFormatChoiceBox.setOnAction(event -> handleDateFormatChanged());
    }

    private void configTimeFormatChoiceBox() {
        log.info("configTimeFormatChoiceBox()");
        timeFormatChoiceBox.getItems().addAll(TimeFormat.values());
        timeFormatChoiceBox.setValue(TimeFormat.DO_NOT_USE_TIME);
        timeFormatChoiceBox.setConverter(new TimeFormatConverter());
        timeFormatChoiceBox.setOnAction(event -> handleTimeFormatChanged());
    }

    private void configDateTimeFormatChoiceBox() {
        log.info("configDateTimeFormatChoiceBox()");
        dateTimeFormatChoiceBox.getItems().addAll(DateTimeFormat.values());
        dateTimeFormatChoiceBox.setValue(DateTimeFormat.DATE_TIME_TOGETHER);
        dateTimeFormatChoiceBox.setConverter(new DateTimeFormatConverter());
        dateTimeFormatChoiceBox.setOnAction(event -> handleDateTimeFormatChanged());
    }

    private void configDateTimeSourceChoiceBox() {
        log.info("configDateTimeSourceChoiceBox()");
        timeSourceChoiceBox.getItems().addAll(DateTimeSource.values());
        timeSourceChoiceBox.setValue(DateTimeSource.FILE_CREATION_DATE);
        timeSourceChoiceBox.setConverter(new DateTimeSourceConverter());
        timeSourceChoiceBox.setOnAction(event -> handleTimeSourceChanged());
    }

    private void configUseFallbackDateTimeCheckBox() {
        log.info("configUseFallbackDateTimeCheckBox()");
        BooleanProperty useFallbackDateTimeProperty = useFallbackDateTimeCheckBox.selectedProperty();
        useFallbackDateTimeProperty.addListener((observable, oldValue, newValue) -> this.handleUseFallbackDateTimeChanged(
                newValue));
    }

    private void configUseCustomFallbackDateTimeCheckBox() {
        log.info("configUseCustomFallbackDateTimeCheckBox()");
        BooleanProperty useCustomFallbackDateTimeProperty = useCustomFallbackDateTimeCheckBox.selectedProperty();
        useCustomFallbackDateTimeProperty.addListener((observable, oldValue, newValue) -> this.handleUseCustomFallbackDateTimeChanged(
                newValue));
    }

    private void configDateTimePickerWidgets() {
        log.info("configDateTimePickerWidgets()");

        SpinnerValueFactory<Integer> hourValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
        SpinnerValueFactory<Integer> minuteValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        SpinnerValueFactory<Integer> secondValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);

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
        BooleanProperty useAmPmUppercaseProperty = useAmPmInUppercaseCheckBox.selectedProperty();
        useAmPmUppercaseProperty.addListener((observable, oldValue, newValue) -> this.handleUseAmPmUppercaseChanged(
                newValue));
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
        LocalDate date = datePicker.getValue();
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        int second = secondSpinner.getValue();

        if (date != null) {
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(hour, minute, second));
            log.debug("dateTime: {}", dateTime);
//             Add your handling code here TODO:
        }

        var position = itemPositionRadioSelector.getSelectedValue();
        var dateFormat = dateFormatChoiceBox.getSelectionModel().getSelectedItem();
        var timeFormat = timeFormatChoiceBox.getSelectionModel().getSelectedItem();
        var dateTimeFormat = dateTimeFormatChoiceBox.getSelectionModel().getSelectedItem();
        var dateTimeSource = timeSourceChoiceBox.getSelectionModel().getSelectedItem();
        var useUppercase = useAmPmInUppercaseCheckBox.isSelected();
//        var customDatetime =
        var separatorForNameAndDatetime = datetimeAndNameSeparatorTextField.getText();
        var useFallbackDates = useFallbackDateTimeCheckBox.isSelected();
        var useFallbackDateTimestamp = useCustomFallbackDateTimeCheckBox.isSelected();

        var cmd = DateTimeRenamePrepareInformationCommand.builder()
                                                         .position(position)
                                                         .dateFormat(dateFormat)
                                                         .timeFormat(timeFormat)
                                                         .dateTimeFormat(dateTimeFormat)
                                                         .dateTimeSource(dateTimeSource)
                                                         .useUppercase(useUppercase)
//                .customDatetime(customDatetime)
                                                         .separatorForNameAndDatetime(separatorForNameAndDatetime)
                                                         .useFallbackDates(useFallbackDates)
//                .useFallbackDateTimestamp(useFallbackDateTimestamp)
                                                         .build();

        log.debug("updateCommand {}", cmd);
        setCommand(cmd);
    }

}
