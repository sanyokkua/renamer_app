package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.SortSource;
import ua.renamer.app.core.service.command.impl.preparation.SequencePrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.converter.SortSourceConverter;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeAddSequenceController extends ModeBaseController {

    private final SortSourceConverter converter;

    @FXML
    private Spinner<Integer> startSeqNumberSpinner;
    @FXML
    private Spinner<Integer> stepValueSpinner;
    @FXML
    private Spinner<Integer> minDigitAmountSpinner;
    @FXML
    private ChoiceBox<SortSource> sortingSourceChoiceBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configStartSeqNumberSpinner();
        configStepValueSpinner();
        configMinDigitAmountSpinner();
        configSortingSourceChoiceBox();
    }

    private void configStartSeqNumberSpinner() {
        log.info("configStartSeqNumberSpinner()");
        SpinnerValueFactory<Integer> startSeqFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,
                                                                                                          Integer.MAX_VALUE,
                                                                                                          0
        );
        startSeqNumberSpinner.setValueFactory(startSeqFactory);
        startSeqNumberSpinner.setEditable(true);
        startSeqNumberSpinner.valueProperty()
                             .addListener((observable, oldValue, newValue) -> handleStartSequenceNumberChanged(newValue));
    }

    private void configStepValueSpinner() {
        log.info("configStepValueSpinner()");
        SpinnerValueFactory<Integer> stepValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
                                                                                                           Integer.MAX_VALUE,
                                                                                                           1
        );
        stepValueSpinner.setValueFactory(stepValueFactory);
        stepValueSpinner.setEditable(true);
        stepValueSpinner.valueProperty()
                        .addListener((observable, oldValue, newValue) -> handleStepValueChanged(newValue));
    }

    private void configMinDigitAmountSpinner() {
        log.info("configMinDigitAmountSpinner()");
        SpinnerValueFactory<Integer> minDigitAmountFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,
                                                                                                                Integer.MAX_VALUE,
                                                                                                                0
        );
        minDigitAmountSpinner.setValueFactory(minDigitAmountFactory);
        minDigitAmountSpinner.setEditable(true);
        minDigitAmountSpinner.valueProperty()
                             .addListener((observable, oldValue, newValue) -> handleMinimalDigitAmountChanged(newValue));
    }

    private void configSortingSourceChoiceBox() {
        log.info("configSortingSourceChoiceBox()");
        sortingSourceChoiceBox.getItems().addAll(SortSource.values());
        sortingSourceChoiceBox.setValue(SortSource.FILE_NAME);
        sortingSourceChoiceBox.setConverter(converter);
    }

    private void handleStartSequenceNumberChanged(Integer newValue) {
        log.debug("handleStartSequenceNumberChanged: {}", newValue);
        updateCommand();
    }

    private void handleStepValueChanged(Integer newValue) {
        log.debug("handleStepValueChanged: {}", newValue);
        updateCommand();
    }

    private void handleMinimalDigitAmountChanged(Integer newValue) {
        log.debug("handleMinimalDigitAmountChanged: {}", newValue);
        updateCommand();
    }

    @FXML
    private void handleSortingSourceChanged() {
        SortSource sortSource = sortingSourceChoiceBox.getValue();
        log.debug("handleSortingSourceChanged: {}", sortSource);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var start = startSeqNumberSpinner.getValue();
        var step = stepValueSpinner.getValue();
        var padding = minDigitAmountSpinner.getValue();
        var source = sortingSourceChoiceBox.getValue();

        var curCmd = SequencePrepareInformationCommand.builder()
                                                      .startNumber(start)
                                                      .stepValue(step)
                                                      .padding(padding)
                                                      .sortSource(source)
                                                      .build();

        log.debug("updateCommand {}", curCmd);
        this.setCommand(curCmd);
    }

}
