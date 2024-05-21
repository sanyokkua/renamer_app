package ua.renamer.app.ui.widgets.controllers.modes;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.commands.preparation.ParentFoldersPrepareInformationCommand;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.ui.abstracts.ModeBaseController;
import ua.renamer.app.ui.widgets.view.ItemPositionRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ModeUseParentFolderNameController extends ModeBaseController {

    @FXML
    private ItemPositionRadioSelector itemPositionRadioSelector;
    @FXML
    private Spinner<Integer> parentsNumberSpinner;
    @FXML
    private TextField fileNameSeparatorTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configItemPosition();
        configTextField();
        configParentNumberSpinner();
    }

    private void configItemPosition() {
        log.info("configItemPosition");
        itemPositionRadioSelector.addValueSelectedHandler(this::handlePositionChanged);
    }

    private void configTextField() {
        log.info("configTextField");
        fileNameSeparatorTextField.textProperty()
                                  .addListener((observable, oldValue, newValue) -> this.handleTextChanged(newValue));
    }

    private void configParentNumberSpinner() {
        log.info("configParentNumberSpinner");
        SpinnerValueFactory<Integer> startSeqFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
                                                                                                          Integer.MAX_VALUE,
                                                                                                          1
        );
        parentsNumberSpinner.setValueFactory(startSeqFactory);
        parentsNumberSpinner.setEditable(true);
        parentsNumberSpinner.valueProperty()
                            .addListener((observable, oldValue, newValue) -> handleParentsNumberChanged(newValue));
    }

    private void handlePositionChanged(ItemPosition itemPosition) {
        log.info("handlePositionChanged: {}", itemPosition);
        updateCommand();
    }

    private void handleTextChanged(String newValue) {
        log.debug("handleTextChanged: {}", newValue);
        updateCommand();
    }

    private void handleParentsNumberChanged(Integer newValue) {
        log.debug("handleParentsNumberChanged: {}", newValue);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var position = itemPositionRadioSelector.getSelectedValue();
        var parents = parentsNumberSpinner.getValue();
        var fileSeparator = fileNameSeparatorTextField.getText();

        var cmd = ParentFoldersPrepareInformationCommand.builder()
                                                        .position(position)
                                                        .numberOfParents(parents)
                                                        .separator(fileSeparator)
                                                        .build();

        log.debug("updateCommand {}", cmd);
        setCommand(cmd);
    }

}
