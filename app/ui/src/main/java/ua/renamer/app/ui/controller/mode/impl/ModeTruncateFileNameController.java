package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.TruncateParams;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.core.service.command.impl.preparation.TruncateNamePrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.widget.impl.ItemPositionTruncateRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeTruncateFileNameController extends ModeBaseController implements ModeControllerV2Api<TruncateParams> {

    @FXML
    private Label amountOfSymbolsLabel;
    @FXML
    private ItemPositionTruncateRadioSelector itemPositionRadioSelector;
    @FXML
    private Spinner<Integer> amountOfSymbolsSpinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configItemPositionRadioSelector();
        configAmountOfSymbolsSpinner();
        updateDisplayedItems();
    }

    private void configItemPositionRadioSelector() {
        log.info("configItemPositionRadioSelector");
        itemPositionRadioSelector.addValueSelectedHandler(this::handlePositionChanged);
    }

    private void configAmountOfSymbolsSpinner() {
        log.info("configAmountOfSymbolsSpinner()");
        SpinnerValueFactory<Integer> stepValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0);
        amountOfSymbolsSpinner.setValueFactory(stepValueFactory);
        amountOfSymbolsSpinner.setEditable(true);
        amountOfSymbolsSpinner.valueProperty()
                .addListener((observable, oldValue, newValue) -> handleAmountOfSymbolsChanged(newValue));
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

    private void handlePositionChanged(TruncateOptions truncateOptions) {
        log.debug("handlePositionChanged(): {}", truncateOptions);
        updateDisplayedItems();
        updateCommand();
    }

    private void handleAmountOfSymbolsChanged(Integer newValue) {
        log.debug("handleAmountOfSymbolsChanged(): {}", newValue);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var truncateOption = itemPositionRadioSelector.getSelectedValue();
        var numberOfSymbols = amountOfSymbolsSpinner.getValue();

        var cmd = TruncateNamePrepareInformationCommand.builder()
                .truncateOptions(truncateOption)
                .numberOfSymbols(numberOfSymbols)
                .build();

        log.debug("updateCommand {}", cmd);
        setCommand(cmd);
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.TRUNCATE_FILE_NAME;
    }

    @Override
    public void bind(ModeApi<TruncateParams> modeApi) {
        var params = modeApi.currentParameters();

        amountOfSymbolsSpinner.getValueFactory().setValue(params.numberOfSymbols());

        if (params.truncateOption() != null) {
            var coreOpt = ua.renamer.app.core.enums.TruncateOptions.valueOf(params.truncateOption().name());
            itemPositionRadioSelector.getButtons()
                    .stream()
                    .filter(btn -> btn.getValue() == coreOpt)
                    .findFirst()
                    .ifPresent(btn -> itemPositionRadioSelector.getToggleGroup().selectToggle(btn));
        }

        updateDisplayedItems();

        amountOfSymbolsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            log.debug("bind: numberOfSymbols changed → {}", newVal);
            modeApi.updateParameters(p -> p.withNumberOfSymbols(newVal != null ? newVal : 0));
        });

        itemPositionRadioSelector.addValueSelectedHandler(coreOpt -> {
            var apiOpt = ua.renamer.app.api.enums.TruncateOptions.valueOf(coreOpt.name());
            log.debug("bind: truncateOption changed → {}", apiOpt);
            modeApi.updateParameters(p -> p.withTruncateOption(apiOpt));
            updateDisplayedItems();
        });
    }

}
