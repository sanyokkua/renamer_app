package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.RemoveTextParams;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Remove Custom Text transformation mode.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeRemoveCustomTextController implements ModeControllerV2Api<RemoveTextParams>, Initializable {

    @FXML
    private ItemPositionRadioSelector itemPositionRadioSelector;
    @FXML
    private TextField removeTextField;

    private ChangeListener<String> textToRemoveListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("initialize()");
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.REMOVE_TEXT;
    }

    @Override
    public void bind(ModeApi<RemoveTextParams> modeApi) {
        var params = modeApi.currentParameters();

        // ── Remove old listeners ──────────────────────────────────────────────
        if (textToRemoveListener != null) removeTextField.textProperty().removeListener(textToRemoveListener);

        // ── Init ──────────────────────────────────────────────────────────────
        removeTextField.setText(params.textToRemove() != null ? params.textToRemove() : "");

        if (params.position() != null) {
            var corePos = ItemPosition.valueOf(params.position().name());
            itemPositionRadioSelector.getButtons()
                    .stream()
                    .filter(btn -> btn.getValue() == corePos)
                    .findFirst()
                    .ifPresent(btn -> itemPositionRadioSelector.getToggleGroup().selectToggle(btn));
        }

        // ── Wire ──────────────────────────────────────────────────────────────
        textToRemoveListener = (obs, oldVal, newVal) -> {
            log.debug("bind: textToRemove changed → {}", newVal);
            modeApi.updateParameters(p -> p.withTextToRemove(newVal))
                    .thenAccept(result -> {
                        if (result.isError()) {
                            Platform.runLater(() -> removeTextField.getStyleClass().add("validation-error"));
                        } else {
                            Platform.runLater(() -> removeTextField.getStyleClass().remove("validation-error"));
                        }
                    });
        };
        removeTextField.textProperty().addListener(textToRemoveListener);

        itemPositionRadioSelector.setValueSelectedHandler(corePos -> {
            var apiPos = ua.renamer.app.api.enums.ItemPosition.valueOf(corePos.name());
            log.debug("bind: position changed → {}", apiPos);
            modeApi.updateParameters(p -> p.withPosition(apiPos));
        });
    }
}
