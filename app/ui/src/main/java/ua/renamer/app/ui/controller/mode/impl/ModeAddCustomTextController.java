package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.AddTextParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.service.command.impl.preparation.AddTextPrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeAddCustomTextController extends ModeBaseController implements ModeControllerV2Api<AddTextParams> {

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

    public void handlePositionChanged(ItemPosition itemPosition) {
        log.debug("handlePositionChanged {}", itemPosition);
        updateCommand();
    }

    public void handleTextChanged(String newTextValue) {
        log.debug("handleTextChanged {}", newTextValue);
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

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.ADD_TEXT;
    }

    @Override
    public void bind(ModeApi<AddTextParams> modeApi) {
        var params = modeApi.currentParameters();

        textField.setText(params.textToAdd() != null ? params.textToAdd() : "");

        if (params.position() != null) {
            var corePos = ItemPosition.valueOf(params.position().name());
            itemPositionRadioSelector.getButtons()
                    .stream()
                    .filter(btn -> btn.getValue() == corePos)
                    .findFirst()
                    .ifPresent(btn -> itemPositionRadioSelector.getToggleGroup().selectToggle(btn));
        }

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            log.debug("bind: textToAdd changed → {}", newVal);
            modeApi.updateParameters(p -> p.withTextToAdd(newVal))
                   .thenAccept(result -> {
                       if (result.isError()) {
                           Platform.runLater(() -> textField.setStyle("-fx-border-color: red;"));
                       } else {
                           Platform.runLater(() -> textField.setStyle(""));
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
