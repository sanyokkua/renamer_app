package ua.renamer.app.ui.widgets.controllers.modes;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import ua.renamer.app.core.commands.preparation.AddTextPrepareCommand;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.ui.abstracts.ModeBaseController;
import ua.renamer.app.ui.widgets.view.ItemPositionRadioSelector;

import java.net.URL;
import java.util.ResourceBundle;

public class ModeAddCustomTextController extends ModeBaseController {
    @FXML
    private TextField textField;
    @FXML
    private ItemPositionRadioSelector itemPositionRadioSelector;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> this.handleTextChanged(newValue));
        itemPositionRadioSelector.addValueSelectedHandler(this::handlePositionChanged);
    }

    public void handleTextChanged(String newTextValue) {
        System.out.println(newTextValue);
        this.updateCommand();
    }

    public void handlePositionChanged(ItemPosition itemPosition) {
        System.out.println(itemPosition);
        this.updateCommand();
    }

    @Override
    public void updateCommand() {
        var textToAdd = textField.getText();
        var position = itemPositionRadioSelector.getSelectedValue();
        var curCmd = AddTextPrepareCommand.builder().text(textToAdd).position(position).build();
        this.setCommand(curCmd);

    }
}
