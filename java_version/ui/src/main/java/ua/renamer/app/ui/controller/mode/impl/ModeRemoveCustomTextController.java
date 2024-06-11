package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.service.command.impl.preparation.RemoveTextPrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeRemoveCustomTextController extends ModeBaseController {

    @FXML
    private ItemPositionRadioSelector itemPositionRadioSelector;
    @FXML
    private TextField removeTextField;

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
        removeTextField.textProperty()
                       .addListener((observable, oldValue, newValue) -> this.handleTextChanged(newValue));
    }

    private void handlePositionChanged(ItemPosition itemPosition) {
        log.debug("handlePositionChanged {}", itemPosition);
        updateCommand();
    }

    private void handleTextChanged(String newValue) {
        log.debug("handleTextChanged {}", newValue);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var textToRemove = removeTextField.getText();
        var position = itemPositionRadioSelector.getSelectedValue();

        var cmd = RemoveTextPrepareInformationCommand.builder().text(textToRemove).position(position).build();

        log.debug("updateCommand {}", cmd);
        setCommand(cmd);
    }

}
