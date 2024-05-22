package ua.renamer.app.ui.widgets.controllers.modes;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.commands.preparation.TruncateNamePrepareInformationCommand;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.ui.abstracts.ModeBaseController;
import ua.renamer.app.ui.widgets.view.ItemPositionTruncateRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ModeTruncateFileNameController extends ModeBaseController {

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
        SpinnerValueFactory<Integer> stepValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,
                                                                                                           Integer.MAX_VALUE,
                                                                                                           0
        );
        amountOfSymbolsSpinner.setValueFactory(stepValueFactory);
        amountOfSymbolsSpinner.setEditable(true);
        amountOfSymbolsSpinner.valueProperty()
                              .addListener((observable, oldValue, newValue) -> handleAmountOfSymbolsChanged(newValue));
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

}
