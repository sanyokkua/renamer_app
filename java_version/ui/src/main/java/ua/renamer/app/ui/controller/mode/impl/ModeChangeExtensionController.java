package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.command.impl.preparation.ExtensionChangePrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
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
