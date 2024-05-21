package ua.renamer.app.ui.widgets.controllers.modes;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.commands.preparation.ExtensionChangePrepareInformationCommand;
import ua.renamer.app.ui.abstracts.ModeBaseController;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ModeChangeExtensionController extends ModeBaseController {

    @FXML
    private TextField extensionTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configTextField();
    }

    private void configTextField() {
        log.info("configTextField()");
        extensionTextField.textProperty()
                          .addListener((observable, oldValue, newValue) -> this.handleTextChanged(newValue));
    }

    private void handleTextChanged(String newValue) {
        log.debug("handleTextChanged: {}", newValue);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var newExtension = extensionTextField.getText();

        var cmd = ExtensionChangePrepareInformationCommand.builder()
                                                          .newExtension(newExtension)
                                                          .build();

        log.debug("updateCommand: {}", cmd);
        setCommand(cmd);
    }

}
