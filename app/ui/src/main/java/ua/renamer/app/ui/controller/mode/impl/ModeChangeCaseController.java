package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.TextCaseOptions;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ChangeCaseParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.core.service.command.impl.preparation.ChangeCasePreparePrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.converter.TextCaseOptionsConverter;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeChangeCaseController extends ModeBaseController implements ModeControllerV2Api<ChangeCaseParams> {

    private final TextCaseOptionsConverter converter;

    @FXML
    private ChoiceBox<TextCaseOptions> caseChoiceBox;
    @FXML
    private CheckBox capitalizeCheckBox;

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.CHANGE_CASE;
    }

    @Override
    public void bind(ModeApi<ChangeCaseParams> modeApi) {
        caseChoiceBox.setValue(modeApi.currentParameters().caseOption());
        capitalizeCheckBox.setSelected(modeApi.currentParameters().capitalizeFirstLetter());

        caseChoiceBox.setOnAction(event -> {
            TextCaseOptions selected = caseChoiceBox.getValue();
            log.debug("bind: caseOption changed → {}", selected);
            modeApi.updateParameters(p -> p.withCaseOption(selected))
                   .thenAccept(result -> {
                       if (result.isError()) {
                           Platform.runLater(() -> caseChoiceBox.setStyle("-fx-border-color: red;"));
                       } else {
                           Platform.runLater(() -> caseChoiceBox.setStyle(""));
                       }
                   });
        });

        capitalizeCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            log.debug("bind: capitalizeFirstLetter changed → {}", newVal);
            modeApi.updateParameters(p -> p.withCapitalizeFirstLetter(newVal));
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configCaseChoiceBox();
        configCapitalizeCheckBox();
    }

    private void configCaseChoiceBox() {
        log.info("configCaseChoiceBox()");
        caseChoiceBox.getItems().addAll(TextCaseOptions.values());
        caseChoiceBox.setValue(TextCaseOptions.CAMEL_CASE);
        caseChoiceBox.setConverter(converter);
        caseChoiceBox.setOnAction(event -> handleCaseChanged());
    }

    private void configCapitalizeCheckBox() {
        log.info("configCapitalizeCheckBox()");
        BooleanProperty selectedProperty = capitalizeCheckBox.selectedProperty();
        selectedProperty.addListener((observable, oldValue, newValue) -> this.handleCapitalizeChanged(newValue));
    }

    private void handleCaseChanged() {
        TextCaseOptions caseOption = caseChoiceBox.getValue();
        log.debug("caseOption: {}", caseOption);
        updateCommand();
    }

    private void handleCapitalizeChanged(boolean isChecked) {
        log.debug("isChecked: {}", isChecked);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var apiEnum = caseChoiceBox.getValue();
        var coreEnum = ua.renamer.app.core.enums.TextCaseOptions.valueOf(apiEnum.name());
        var capitalize = capitalizeCheckBox.isSelected();

        var curCmd = ChangeCasePreparePrepareInformationCommand.builder()
                .textCase(coreEnum)
                .capitalize(capitalize)
                .build();

        log.debug("updateCommand {}", curCmd);
        setCommand(curCmd);
    }

}
