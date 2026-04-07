package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.TruncateOptions;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.TruncateParams;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.widget.impl.ItemPositionTruncateRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Trim Name transformation mode.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeTrimNameController implements ModeControllerV2Api<TruncateParams>, Initializable {

    @FXML
    private Label amountOfSymbolsLabel;
    @FXML
    private ItemPositionTruncateRadioSelector itemPositionRadioSelector;
    @FXML
    private Spinner<Integer> amountOfSymbolsSpinner;

    private ChangeListener<Integer> symbolsListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configAmountOfSymbolsSpinner();
        updateDisplayedItems();
    }

    private void configAmountOfSymbolsSpinner() {
        log.info("configAmountOfSymbolsSpinner()");
        SpinnerValueFactory<Integer> stepValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0);
        amountOfSymbolsSpinner.setValueFactory(stepValueFactory);
        amountOfSymbolsSpinner.setEditable(true);
    }

    private void updateDisplayedItems() {
        if (itemPositionRadioSelector.getSelectedValue() == TruncateOptions.TRUNCATE_EMPTY_SYMBOLS) {
            amountOfSymbolsSpinner.setDisable(true);
            amountOfSymbolsSpinner.setVisible(false);
            amountOfSymbolsLabel.setVisible(false);
        } else {
            amountOfSymbolsSpinner.setDisable(false);
            amountOfSymbolsSpinner.setVisible(true);
            amountOfSymbolsLabel.setVisible(true);
        }
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.TRIM_NAME;
    }

    @Override
    public void bind(ModeApi<TruncateParams> modeApi) {
        var params = modeApi.currentParameters();

        // ── Remove old listeners ──────────────────────────────────────────────
        if (symbolsListener != null) {
            amountOfSymbolsSpinner.valueProperty().removeListener(symbolsListener);
        }

        // ── Init ──────────────────────────────────────────────────────────────
        amountOfSymbolsSpinner.getValueFactory().setValue(params.numberOfSymbols());

        if (params.truncateOption() != null) {
            var coreOpt = ua.renamer.app.api.enums.TruncateOptions.valueOf(params.truncateOption().name());
            itemPositionRadioSelector.getButtons()
                    .stream()
                    .filter(btn -> btn.getValue() == coreOpt)
                    .findFirst()
                    .ifPresent(btn -> itemPositionRadioSelector.getToggleGroup().selectToggle(btn));
        }

        updateDisplayedItems();

        // ── Wire ──────────────────────────────────────────────────────────────
        symbolsListener = (obs, oldVal, newVal) -> {
            log.debug("bind: numberOfSymbols changed → {}", newVal);
            modeApi.updateParameters(p -> p.withNumberOfSymbols(newVal != null ? newVal : 0));
        };
        amountOfSymbolsSpinner.valueProperty().addListener(symbolsListener);

        itemPositionRadioSelector.setValueSelectedHandler(coreOpt -> {
            var apiOpt = ua.renamer.app.api.enums.TruncateOptions.valueOf(coreOpt.name());
            log.debug("bind: truncateOption changed → {}", apiOpt);
            modeApi.updateParameters(p -> p.withTruncateOption(apiOpt));
            updateDisplayedItems();
        });
    }
}
