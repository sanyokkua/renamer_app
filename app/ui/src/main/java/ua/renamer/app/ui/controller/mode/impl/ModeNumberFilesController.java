package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.SortSource;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.SequenceParams;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.converter.SortSourceConverter;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Number Files transformation mode.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeNumberFilesController
        implements ModeControllerV2Api<SequenceParams>, Initializable {

    private final SortSourceConverter converter;

    @FXML
    private Spinner<Integer> startSeqNumberSpinner;
    @FXML
    private Spinner<Integer> stepValueSpinner;
    @FXML
    private Spinner<Integer> minDigitAmountSpinner;
    @FXML
    private ChoiceBox<SortSource> sortingSourceChoiceBox;
    @FXML
    private CheckBox perFolderCountingCheckBox;

    private ChangeListener<Integer> startListener;
    private ChangeListener<Integer> stepListener;
    private ChangeListener<Integer> paddingListener;
    private ChangeListener<SortSource> sortSourceListener;
    private ChangeListener<Boolean> perFolderListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configStartSeqNumberSpinner();
        configStepValueSpinner();
        configMinDigitAmountSpinner();
        configSortingSourceChoiceBox();
    }

    private void configStartSeqNumberSpinner() {
        log.info("configStartSeqNumberSpinner()");
        SpinnerValueFactory<Integer> startSeqFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0);
        startSeqNumberSpinner.setValueFactory(startSeqFactory);
        startSeqNumberSpinner.setEditable(true);
    }

    private void configStepValueSpinner() {
        log.info("configStepValueSpinner()");
        SpinnerValueFactory<Integer> stepValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
        stepValueSpinner.setValueFactory(stepValueFactory);
        stepValueSpinner.setEditable(true);
    }

    private void configMinDigitAmountSpinner() {
        log.info("configMinDigitAmountSpinner()");
        SpinnerValueFactory<Integer> minDigitAmountFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0);
        minDigitAmountSpinner.setValueFactory(minDigitAmountFactory);
        minDigitAmountSpinner.setEditable(true);
    }

    private void configSortingSourceChoiceBox() {
        log.info("configSortingSourceChoiceBox()");
        sortingSourceChoiceBox.getItems().addAll(SortSource.values());
        sortingSourceChoiceBox.setValue(SortSource.FILE_NAME);
        sortingSourceChoiceBox.setConverter(converter);
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.NUMBER_FILES;
    }

    @Override
    public void bind(ModeApi<SequenceParams> modeApi) {
        var params = modeApi.currentParameters();

        // ── Remove old listeners ──────────────────────────────────────────────
        if (startListener != null) startSeqNumberSpinner.valueProperty().removeListener(startListener);
        if (stepListener != null) stepValueSpinner.valueProperty().removeListener(stepListener);
        if (paddingListener != null) minDigitAmountSpinner.valueProperty().removeListener(paddingListener);
        if (sortSourceListener != null)
            sortingSourceChoiceBox.getSelectionModel().selectedItemProperty().removeListener(sortSourceListener);
        if (perFolderListener != null)
            perFolderCountingCheckBox.selectedProperty().removeListener(perFolderListener);

        // ── Init ──────────────────────────────────────────────────────────────
        startSeqNumberSpinner.getValueFactory().setValue(params.startNumber());
        stepValueSpinner.getValueFactory().setValue(params.stepValue());
        minDigitAmountSpinner.getValueFactory().setValue(params.paddingDigits());
        perFolderCountingCheckBox.setSelected(params.perFolderCounting());

        if (params.sortSource() != null) {
            var coreSort = ua.renamer.app.api.enums.SortSource.valueOf(params.sortSource().name());
            sortingSourceChoiceBox.setValue(coreSort);
        }

        // ── Wire ──────────────────────────────────────────────────────────────
        startListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                log.debug("bind: startNumber changed → {}", newVal);
                modeApi.updateParameters(p -> p.withStartNumber(newVal));
            }
        };
        startSeqNumberSpinner.valueProperty().addListener(startListener);

        stepListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                log.debug("bind: stepValue changed → {}", newVal);
                modeApi.updateParameters(p -> p.withStepValue(newVal));
            }
        };
        stepValueSpinner.valueProperty().addListener(stepListener);

        paddingListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                log.debug("bind: paddingDigits changed → {}", newVal);
                modeApi.updateParameters(p -> p.withPaddingDigits(newVal));
            }
        };
        minDigitAmountSpinner.valueProperty().addListener(paddingListener);

        sortSourceListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                var apiSort = ua.renamer.app.api.enums.SortSource.valueOf(newVal.name());
                log.debug("bind: sortSource changed → {}", apiSort);
                modeApi.updateParameters(p -> p.withSortSource(apiSort));
            }
        };
        sortingSourceChoiceBox.getSelectionModel().selectedItemProperty().addListener(sortSourceListener);

        perFolderListener = (obs, oldVal, newVal) -> {
            log.debug("bind: perFolderCounting changed → {}", newVal);
            modeApi.updateParameters(p -> p.withPerFolderCounting(newVal));
        };
        perFolderCountingCheckBox.selectedProperty().addListener(perFolderListener);
    }

}
