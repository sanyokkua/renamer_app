package ua.renamer.app.ui.controller.mode.impl;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ImageDimensionsParams;
import ua.renamer.app.api.session.ModeApi;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.core.service.command.impl.preparation.ImageDimensionsPrepareInformationCommand;
import ua.renamer.app.ui.controller.mode.ModeBaseController;
import ua.renamer.app.ui.controller.mode.ModeControllerV2Api;
import ua.renamer.app.ui.converter.ImageDimensionOptionsConverter;
import ua.renamer.app.ui.widget.impl.ItemPositionWithReplacementRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ModeUseImageDimensionsController extends ModeBaseController
        implements ModeControllerV2Api<ImageDimensionsParams> {

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
                .addListener((observable, oldValue, newValue) -> this.handleDimensionsSeparatorChanged(newValue));
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
                .addListener((observable, oldValue, newValue) -> this.handleDimensionsAndFileNameSeparatorChanged(newValue));
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

    @Override
    public TransformationMode supportedMode() {
        return TransformationMode.USE_IMAGE_DIMENSIONS;
    }

    @Override
    public void bind(ModeApi<ImageDimensionsParams> modeApi) {
        var params = modeApi.currentParameters();

        // ── Init: leftSide (API → core enum) ──────────────────────────────────
        if (params.leftSide() != null) {
            var coreLeft = ua.renamer.app.core.enums.ImageDimensionOptions
                    .valueOf(params.leftSide().name());
            leftDimensionChoiceBox.setValue(coreLeft);
        }

        // ── Init: rightSide (API → core enum) ─────────────────────────────────
        if (params.rightSide() != null) {
            var coreRight = ua.renamer.app.core.enums.ImageDimensionOptions
                    .valueOf(params.rightSide().name());
            rightDimensionChoiceBox.setValue(coreRight);
        }

        // ── Init: nameSeparator (null-safe) ────────────────────────────────────
        dimensionsAndFileSeparatorTextField.setText(
                params.nameSeparator() != null ? params.nameSeparator() : "");

        // ── Init: position (API → core enum, button scan) ─────────────────────
        if (params.position() != null) {
            var corePos = ua.renamer.app.core.enums.ItemPositionWithReplacement
                    .valueOf(params.position().name());
            itemPositionRadioSelector.getButtons().stream()
                    .filter(btn -> btn.getValue() == corePos)
                    .findFirst()
                    .ifPresent(btn -> itemPositionRadioSelector.getToggleGroup().selectToggle(btn));
        }

        // ── Wire: leftDimensionChoiceBox → modeApi (core → API enum) ──────────
        // Use selectedItemProperty addListener (additive) — NOT setOnAction (would replace V1 handler)
        leftDimensionChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        var apiLeft = ua.renamer.app.api.enums.ImageDimensionOptions
                                .valueOf(newVal.name());
                        log.debug("bind: leftSide changed → {}", apiLeft);
                        modeApi.updateParameters(p -> p.withLeftSide(apiLeft));
                    }
                });

        // ── Wire: rightDimensionChoiceBox → modeApi (core → API enum) ─────────
        rightDimensionChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        var apiRight = ua.renamer.app.api.enums.ImageDimensionOptions
                                .valueOf(newVal.name());
                        log.debug("bind: rightSide changed → {}", apiRight);
                        modeApi.updateParameters(p -> p.withRightSide(apiRight));
                    }
                });

        // ── Wire: dimensionsAndFileSeparatorTextField → modeApi ───────────────
        // NOTE: dimensionsSeparatorTextField is NOT wired here — it has no V2 counterpart
        dimensionsAndFileSeparatorTextField.textProperty()
                .addListener((obs, oldVal, newVal) -> {
                    log.debug("bind: nameSeparator changed → {}", newVal);
                    modeApi.updateParameters(p -> p.withNameSeparator(newVal != null ? newVal : ""));
                });

        // ── Wire: itemPositionRadioSelector → modeApi (core → API enum) ───────
        itemPositionRadioSelector.addValueSelectedHandler(corePos -> {
            var apiPos = ua.renamer.app.api.enums.ItemPositionWithReplacement
                    .valueOf(corePos.name());
            log.debug("bind: position changed → {}", apiPos);
            modeApi.updateParameters(p -> p.withPosition(apiPos));
        });
    }

}
