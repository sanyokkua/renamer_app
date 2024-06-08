package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.core.service.command.impl.preparation.ImageDimensionsPrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.converter.ImageDimensionOptionsConverter;
import ua.renamer.app.ui.widget.impl.ItemPositionWithReplacementRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeUseImageDimensionsController extends ModeBaseController {

    private final ImageDimensionOptionsConverter converter;

    @FXML
    private ItemPositionWithReplacementRadioSelector itemPositionRadioSelector;
    @FXML
    private ChoiceBox<ImageDimensionOptions> leftDimensionChoiceBox;
    @FXML
    private TextField dimensionsSeparatorTextField;
    @FXML
    private ChoiceBox<ImageDimensionOptions> rightDimensionChoiceBox;
    @FXML
    private TextField dimensionsAndFileSeparatorTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configItemPositionSelector();
        configLeftDimensionChoiceBox();
        configDimensionsSeparatorTextField();
        configRightDimensionChoiceBox();
        configDimensionsAndFileNameSeparatorTextField();
    }

    private void configItemPositionSelector() {
        log.info("configItemPositionSelector");
        itemPositionRadioSelector.addValueSelectedHandler(this::handlePositionChanged);
    }

    private void configLeftDimensionChoiceBox() {
        log.info("configLeftDimensionChoiceBox");
        leftDimensionChoiceBox.getItems().addAll(ImageDimensionOptions.values());
        leftDimensionChoiceBox.setValue(ImageDimensionOptions.DO_NOT_USE);
        leftDimensionChoiceBox.setConverter(converter);
        leftDimensionChoiceBox.setOnAction(event -> handleLeftDimensionChanged());
    }

    private void configDimensionsSeparatorTextField() {
        log.info("configDimensionsSeparatorTextField");
        dimensionsSeparatorTextField.textProperty()
                                    .addListener((observable, oldValue, newValue) -> this.handleDimensionsSeparatorChanged(
                                            newValue));
    }

    private void configRightDimensionChoiceBox() {
        log.info("configRightDimensionChoiceBox");
        rightDimensionChoiceBox.getItems().addAll(ImageDimensionOptions.values());
        rightDimensionChoiceBox.setValue(ImageDimensionOptions.DO_NOT_USE);
        rightDimensionChoiceBox.setConverter(converter);
        rightDimensionChoiceBox.setOnAction(event -> handleRightDimensionChanged());
    }

    private void configDimensionsAndFileNameSeparatorTextField() {
        log.info("configDimensionsAndFileNameSeparatorTextField");
        dimensionsAndFileSeparatorTextField.textProperty()
                                           .addListener((observable, oldValue, newValue) -> this.handleDimensionsAndFileNameSeparatorChanged(
                                                   newValue));
    }

    private void handlePositionChanged(ItemPositionWithReplacement itemPositionWithReplacement) {
        log.debug("itemPositionWithReplacement {}", itemPositionWithReplacement);
        updateCommand();
    }

    private void handleLeftDimensionChanged() {
        log.debug("LeftDimensionChanged");
        updateCommand();
    }

    private void handleDimensionsSeparatorChanged(String newValue) {
        log.debug("DimensionsSeparatorChanged {}", newValue);
        updateCommand();
    }

    private void handleRightDimensionChanged() {
        log.debug("RightDimensionChanged");
        updateCommand();
    }

    private void handleDimensionsAndFileNameSeparatorChanged(String newValue) {
        log.debug("DimensionsAndFileNameSeparatorChanged {}", newValue);
        updateCommand();
    }

    @Override
    public void updateCommand() {
        var position = itemPositionRadioSelector.getSelectedValue();
        var leftSide = leftDimensionChoiceBox.getSelectionModel().getSelectedItem();
        var rightSide = rightDimensionChoiceBox.getSelectionModel().getSelectedItem();
        var dimSep = dimensionsSeparatorTextField.getText();
        var nameSep = dimensionsAndFileSeparatorTextField.getText();

        var cmd = ImageDimensionsPrepareInformationCommand.builder()
                                                          .position(position)
                                                          .leftSide(leftSide)
                                                          .rightSide(rightSide)
                                                          .dimensionSeparator(dimSep)
                                                          .nameSeparator(nameSep)
                                                          .build();

        log.debug("updateCommand {}", cmd);
        setCommand(cmd);
    }

}
