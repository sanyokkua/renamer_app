package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.api.session.ParentFolderParams;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.service.command.impl.preparation.ParentFoldersPrepareInformationCommand;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.widget.impl.ItemPositionRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeUseParentFolderNameController extends ModeBaseController implements ModeControllerV2Api<ParentFolderParams> {

    private final FilesOperations filesOperations;

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
        SpinnerValueFactory<Integer> startSeqFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
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
                .filesOperations(filesOperations)
                .position(position)
                .numberOfParents(parents)
                .separator(fileSeparator)
                .build();

        log.debug("updateCommand {}", cmd);
        setCommand(cmd);
    }

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.USE_PARENT_FOLDER_NAME;
    }

    @Override
    public void bind(ModeApi<ParentFolderParams> modeApi) {
        var params = modeApi.currentParameters();

        // Initialize spinner
        parentsNumberSpinner.getValueFactory().setValue(params.numberOfParentFolders());

        // Initialize separator text field
        fileNameSeparatorTextField.setText(params.separator() != null ? params.separator() : "");

        // Initialize position selector — API enum → core enum
        if (params.position() != null) {
            var corePos = ua.renamer.app.core.enums.ItemPosition.valueOf(params.position().name());
            itemPositionRadioSelector.getButtons()
                    .stream()
                    .filter(btn -> btn.getValue() == corePos)
                    .findFirst()
                    .ifPresent(btn -> itemPositionRadioSelector.getToggleGroup().selectToggle(btn));
        }

        // Wire spinner → API
        parentsNumberSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            log.debug("bind: numberOfParentFolders changed → {}", newVal);
            modeApi.updateParameters(p -> p.withNumberOfParentFolders(newVal != null ? newVal : 1));
        });

        // Wire separator text field → API
        fileNameSeparatorTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            log.debug("bind: separator changed → {}", newVal);
            modeApi.updateParameters(p -> p.withSeparator(newVal != null ? newVal : ""));
        });

        // Wire position selector → API (core enum → API enum)
        itemPositionRadioSelector.addValueSelectedHandler(corePos -> {
            var apiPos = ua.renamer.app.api.enums.ItemPosition.valueOf(corePos.name());
            log.debug("bind: position changed → {}", apiPos);
            modeApi.updateParameters(p -> p.withPosition(apiPos));
        });
    }

}
