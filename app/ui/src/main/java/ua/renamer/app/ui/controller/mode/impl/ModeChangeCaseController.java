package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.TextCaseOptions;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ChangeCaseParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.converter.TextCaseOptionsConverter;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Change Case transformation mode.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeChangeCaseController implements ModeControllerV2Api<ChangeCaseParams>, Initializable {

    private final TextCaseOptionsConverter converter;

    @FXML
    private ChoiceBox<TextCaseOptions> caseChoiceBox;
    @FXML
    private CheckBox capitalizeCheckBox;

    private ChangeListener<Boolean> capitalizeListener;

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.CHANGE_CASE;
    }

    @Override
    public void bind(ModeApi<ChangeCaseParams> modeApi) {
        // ── Remove old listeners ──────────────────────────────────────────────
        if (capitalizeListener != null) {
            capitalizeCheckBox.selectedProperty().removeListener(capitalizeListener);
        }

        // ── Init ──────────────────────────────────────────────────────────────
        caseChoiceBox.setValue(modeApi.currentParameters().caseOption());
        capitalizeCheckBox.setSelected(modeApi.currentParameters().capitalizeFirstLetter());

        // ── Wire (ChoiceBox uses setOnAction — replace semantics, no field needed) ──
        caseChoiceBox.setOnAction(event -> {
            TextCaseOptions selected = caseChoiceBox.getValue();
            log.debug("bind: caseOption changed → {}", selected);
            modeApi.updateParameters(p -> p.withCaseOption(selected))
                    .thenAccept(result -> {
                        if (result.isError()) {
                            Platform.runLater(() -> caseChoiceBox.getStyleClass().add("validation-error"));
                        } else {
                            Platform.runLater(() -> caseChoiceBox.getStyleClass().remove("validation-error"));
                        }
                    });
        });

        capitalizeListener = (obs, oldVal, newVal) -> {
            log.debug("bind: capitalizeFirstLetter changed → {}", newVal);
            modeApi.updateParameters(p -> p.withCapitalizeFirstLetter(newVal));
        };
        capitalizeCheckBox.selectedProperty().addListener(capitalizeListener);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configCaseChoiceBox();
    }

    private void configCaseChoiceBox() {
        log.info("configCaseChoiceBox()");
        caseChoiceBox.getItems().addAll(TextCaseOptions.values());
        caseChoiceBox.setValue(TextCaseOptions.CAMEL_CASE);
        caseChoiceBox.setConverter(converter);
    }

}
