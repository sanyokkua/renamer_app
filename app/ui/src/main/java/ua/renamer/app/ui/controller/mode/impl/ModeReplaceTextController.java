package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ReplaceTextParams;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.widget.impl.ItemPositionExtendedRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Replace Text transformation mode.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeReplaceTextController implements ModeControllerV2Api<ReplaceTextParams>, Initializable {

    @FXML
    private ItemPositionExtendedRadioSelector itemPositionRadioSelector;
    @FXML
    private TextField textToReplaceTextField;
    @FXML
    private TextField textToAddTextField;

    private ChangeListener<String> textToReplaceListener;
    private ChangeListener<String> replacementTextListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("initialize()");
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.REPLACE_TEXT;
    }

    @Override
    public void bind(ModeApi<ReplaceTextParams> modeApi) {
        var params = modeApi.currentParameters();

        // ── Remove old listeners ──────────────────────────────────────────────
        if (textToReplaceListener != null) textToReplaceTextField.textProperty().removeListener(textToReplaceListener);
        if (replacementTextListener != null) textToAddTextField.textProperty().removeListener(replacementTextListener);

        // ── Init ──────────────────────────────────────────────────────────────
        textToReplaceTextField.setText(params.textToReplace() != null ? params.textToReplace() : "");
        textToAddTextField.setText(params.replacementText() != null ? params.replacementText() : "");

        if (params.position() != null) {
            var corePos = ua.renamer.app.api.enums.ItemPositionExtended.valueOf(params.position().name());
            itemPositionRadioSelector.getButtons()
                    .stream()
                    .filter(btn -> btn.getValue() == corePos)
                    .findFirst()
                    .ifPresent(btn -> itemPositionRadioSelector.getToggleGroup().selectToggle(btn));
        }

        // ── Wire ──────────────────────────────────────────────────────────────
        textToReplaceListener = (obs, oldVal, newVal) -> {
            log.debug("bind: textToReplace changed → {}", newVal);
            modeApi.updateParameters(p -> p.withTextToReplace(newVal))
                    .thenAccept(result -> {
                        if (result.isError()) {
                            Platform.runLater(() -> textToReplaceTextField.getStyleClass().add("validation-error"));
                        } else {
                            Platform.runLater(() -> textToReplaceTextField.getStyleClass().remove("validation-error"));
                        }
                    });
        };
        textToReplaceTextField.textProperty().addListener(textToReplaceListener);

        replacementTextListener = (obs, oldVal, newVal) -> {
            log.debug("bind: replacementText changed → {}", newVal);
            modeApi.updateParameters(p -> p.withReplacementText(newVal))
                    .thenAccept(result -> {
                        if (result.isError()) {
                            Platform.runLater(() -> textToAddTextField.getStyleClass().add("validation-error"));
                        } else {
                            Platform.runLater(() -> textToAddTextField.getStyleClass().remove("validation-error"));
                        }
                    });
        };
        textToAddTextField.textProperty().addListener(replacementTextListener);

        itemPositionRadioSelector.setValueSelectedHandler(corePos -> {
            var apiPos = ua.renamer.app.api.enums.ItemPositionExtended.valueOf(corePos.name());
            log.debug("bind: position changed → {}", apiPos);
            modeApi.updateParameters(p -> p.withPosition(apiPos));
        });
    }
}
