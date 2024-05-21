package ua.renamer.app.ui.widgets.controllers.modes;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.commands.preparation.AddTextPrepareInformationCommand;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.ui.abstracts.ModeBaseController;
import ua.renamer.app.ui.widgets.view.ItemPositionRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ModeAddCustomTextController extends ModeBaseController {

    @FXML
    private TextField textField;
    @FXML
    private ItemPositionRadioSelector itemPositionRadioSelector;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configItemPosition();
        configTextField();
    }

    private void configItemPosition() {
        log.info("configItemPosition");
        itemPositionRadioSelector.addValueSelectedHandler(this::handlePositionChanged);
    }

    private void configTextField() {
        log.info("configTextField");
        textField.textProperty().addListener((observable, oldValue, newValue) -> this.handleTextChanged(newValue));
    }

    public void handleTextChanged(String newTextValue) {
        log.debug("handleTextChanged {}", newTextValue);
        updateCommand();
    }

    public void handlePositionChanged(ItemPosition itemPosition) {
        log.debug("handlePositionChanged {}", itemPosition);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var textToAdd = textField.getText();
        var position = itemPositionRadioSelector.getSelectedValue();
        var curCmd = AddTextPrepareInformationCommand.builder().text(textToAdd).position(position).build();

        log.debug("updateCommand {}", curCmd);
        this.setCommand(curCmd);
    }

}
