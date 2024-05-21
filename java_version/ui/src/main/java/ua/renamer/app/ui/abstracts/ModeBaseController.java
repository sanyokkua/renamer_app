package ua.renamer.app.ui.abstracts;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.ui.models.CommandModel;

/**
 * An abstract base controller for modes.
 */
@Slf4j
public abstract class ModeBaseController implements ControllerApi, Initializable {

    private final CommandModel commandModel;

    /**
     * Constructs a ModeBaseController.
     */
    protected ModeBaseController() {
        commandModel = new CommandModel();
    }

    @Override
    public FileInformationCommand getCommand() {
        log.debug("getCommand()");
        return commandModel.getAppFileCommand();
    }

    @Override
    public void setCommand(FileInformationCommand fileInformationCommand) {
        log.debug("setCommand()");
        commandModel.setAppFileCommand(fileInformationCommand);
    }

    @Override
    public ObjectProperty<FileInformationCommand> commandProperty() {
        log.debug("commandProperty()");
        return commandModel.appFileCommandProperty();
    }

}
