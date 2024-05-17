package ua.renamer.app.ui.abstracts;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.Initializable;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.ui.models.CommandModel;

/**
 * An abstract base controller for modes.
 */
public abstract class ModeBaseController implements ControllerApi, Initializable {
    private final CommandModel commandModel;

    /**
     * Constructs a ModeBaseController.
     */
    protected ModeBaseController() {
        commandModel = new CommandModel();
    }

    @Override
    public AppFileCommand getCommand() {
        return commandModel.getAppFileCommand();
    }

    @Override
    public void setCommand(AppFileCommand appFileCommand) {
        commandModel.setAppFileCommand(appFileCommand);
    }

    @Override
    public ObjectProperty<AppFileCommand> commandProperty() {
        return commandModel.appFileCommandProperty();
    }
}
