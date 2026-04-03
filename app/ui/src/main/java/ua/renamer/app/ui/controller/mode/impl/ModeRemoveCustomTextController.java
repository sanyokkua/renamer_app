package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.RemoveTextParams;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.service.command.impl.preparation.RemoveTextPrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeRemoveCustomTextController extends ModeBaseController implements ModeControllerV2Api<RemoveTextParams> {

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

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.REMOVE_TEXT;
    }

    @Override
    public void bind(ModeApi<RemoveTextParams> modeApi) {
        var params = modeApi.currentParameters();

        removeTextField.setText(params.textToRemove() != null ? params.textToRemove() : "");

        if (params.position() != null) {
            var corePos = ItemPosition.valueOf(params.position().name());
            itemPositionRadioSelector.getButtons()
                    .stream()
                    .filter(btn -> btn.getValue() == corePos)
                    .findFirst()
                    .ifPresent(btn -> itemPositionRadioSelector.getToggleGroup().selectToggle(btn));
        }

        removeTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            log.debug("bind: textToRemove changed → {}", newVal);
            modeApi.updateParameters(p -> p.withTextToRemove(newVal))
                   .thenAccept(result -> {
                       if (result.isError()) {
                           Platform.runLater(() -> removeTextField.setStyle("-fx-border-color: red;"));
                       } else {
                           Platform.runLater(() -> removeTextField.setStyle(""));
                       }
                   });
        });

        itemPositionRadioSelector.addValueSelectedHandler(corePos -> {
            var apiPos = ua.renamer.app.api.enums.ItemPosition.valueOf(corePos.name());
            log.debug("bind: position changed → {}", apiPos);
            modeApi.updateParameters(p -> p.withPosition(apiPos));
        });
    }

}
