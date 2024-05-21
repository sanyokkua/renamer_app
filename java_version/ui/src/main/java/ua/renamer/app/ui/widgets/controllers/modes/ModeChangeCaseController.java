package ua.renamer.app.ui.widgets.controllers.modes;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.commands.preparation.ChangeCasePreparePrepareInformationCommand;
import ua.renamer.app.core.enums.TextCaseOptions;
import ua.renamer.app.ui.abstracts.ModeBaseController;
import ua.renamer.app.ui.converters.TextCaseOptionsConverter;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ModeChangeCaseController extends ModeBaseController {

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
        caseChoiceBox.setConverter(new TextCaseOptionsConverter());
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
