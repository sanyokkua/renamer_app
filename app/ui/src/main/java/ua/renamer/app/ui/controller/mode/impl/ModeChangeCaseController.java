package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.TextCaseOptions;
import ua.renamer.app.core.service.command.impl.preparation.ChangeCasePreparePrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.converter.TextCaseOptionsConverter;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeChangeCaseController extends ModeBaseController {

    private final TextCaseOptionsConverter converter;

    @FXML
    private ChoiceBox<TextCaseOptions> caseChoiceBox;
    @FXML
    private CheckBox capitalizeCheckBox;

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
        var textCase = caseChoiceBox.getValue();
        var capitalize = capitalizeCheckBox.isSelected();

        var curCmd = ChangeCasePreparePrepareInformationCommand.builder()
                                                               .textCase(textCase)
                                                               .capitalize(capitalize)
                                                               .build();

        log.debug("updateCommand {}", curCmd);
        this.setCommand(curCmd);
    }

}
