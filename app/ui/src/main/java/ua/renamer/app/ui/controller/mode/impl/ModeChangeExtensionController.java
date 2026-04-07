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
import ua.renamer.app.api.session.ExtensionChangeParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Change Extension transformation mode.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeChangeExtensionController implements ModeControllerV2Api<ExtensionChangeParams>, Initializable {

    @FXML
    private TextField extensionTextField;

    private ChangeListener<String> extensionListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("initialize()");
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.CHANGE_EXTENSION;
    }

    @Override
    public void bind(ModeApi<ExtensionChangeParams> modeApi) {
        // ── Remove old listener ───────────────────────────────────────────────
        if (extensionListener != null) {
            extensionTextField.textProperty().removeListener(extensionListener);
        }

        // ── Init ──────────────────────────────────────────────────────────────
        extensionTextField.setText(modeApi.currentParameters().newExtension());

        // ── Wire ──────────────────────────────────────────────────────────────
        extensionListener = (obs, oldVal, newVal) ->
                modeApi.updateParameters(p -> p.withNewExtension(newVal))
                        .thenAccept(result -> {
                            if (result.isError()) {
                                Platform.runLater(() -> extensionTextField.getStyleClass().add("validation-error"));
                            } else {
                                Platform.runLater(() -> extensionTextField.getStyleClass().remove("validation-error"));
                            }
                        });
        extensionTextField.textProperty().addListener(extensionListener);
    }
}
