package ua.renamer.app.ui.widgets.controllers.modes;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.commands.preparation.ReplaceTextPrepareInformationCommand;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.ui.abstracts.ModeBaseController;
import ua.renamer.app.ui.widgets.view.ItemPositionExtendedRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ModeReplaceCustomTextController extends ModeBaseController {

    @FXML
    private ItemPositionExtendedRadioSelector itemPositionRadioSelector;
    @FXML
    private TextField textToReplaceTextField;
    @FXML
    private TextField textToAddTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configItemPositionRadioSelector();
        configTextToReplaceTextField();
        configTextToAddTextField();
    }

    private void configItemPositionRadioSelector() {
        log.info("configItemPositionRadioSelector");
        itemPositionRadioSelector.addValueSelectedHandler(this::handlePositionChanged);
    }

    private void configTextToReplaceTextField() {
        log.info("configTextToReplaceTextField");
        textToReplaceTextField.textProperty()
                              .addListener((observable, oldValue, newValue) -> this.handleTextToReplaceChanged(newValue));
    }

    private void configTextToAddTextField() {
        log.info("configTextToAddTextField");
        textToAddTextField.textProperty()
                          .addListener((observable, oldValue, newValue) -> this.handleTextToAddChanged(newValue));
    }

    private void handlePositionChanged(ItemPositionExtended itemPositionExtended) {
        log.debug("handlePositionChanged: {}", itemPositionExtended);
        updateCommand();
    }

    private void handleTextToReplaceChanged(String newValue) {
        log.debug("handleTextToReplaceChanged: {}", newValue);
        updateCommand();
    }

    private void handleTextToAddChanged(String newValue) {
        log.debug("handleTextToAddChanged: {}", newValue);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var position = itemPositionRadioSelector.getSelectedValue();
        var textToReplace = textToReplaceTextField.getText();
        var textToAdd = textToAddTextField.getText();

        var cmd = ReplaceTextPrepareInformationCommand.builder()
                                                      .position(position)
                                                      .textToReplace(textToReplace)
                                                      .newValueToAdd(textToAdd)
                                                      .build();

        log.debug("updateCommand(): {}", cmd);
        setCommand(cmd);
    }

}
