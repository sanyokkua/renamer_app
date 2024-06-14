package ua.renamer.app.ui.controller.mode;

import javafx.beans.property.ObjectProperty;
import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.ui.models.CommandModel;

/**
 * An abstract base controller for different modes, implementing common functionality.
 * This class provides a base implementation for controllers that manage file information commands.
 */
@Slf4j
public abstract class ModeBaseController implements ModeControllerApi, Initializable {

    private final CommandModel commandModel;

    /**
     * Constructs a ModeBaseController with a new instance of CommandModel.
     */
    protected ModeBaseController() {
        commandModel = new CommandModel();
    }

    /**
     * Gets the current file information command from the command model.
     *
     * @return The current {@link FileInformationCommand}.
     */
    @Override
    public FileInformationCommand getCommand() {
        log.debug("getCommand()");
        updateCommand();
        return commandModel.getAppFileCommand();
    }

    /**
     * Sets a new file information command in the command model.
     *
     * @param fileInformationCommand The {@link FileInformationCommand} to set.
     */
    @Override
    public void setCommand(FileInformationCommand fileInformationCommand) {
        log.debug("setCommand()");
        commandModel.setAppFileCommand(fileInformationCommand);
    }

    /**
     * Gets the property representing the file information command.
     *
     * @return The {@link ObjectProperty} representing the {@link FileInformationCommand}.
     */
    @Override
    public ObjectProperty<FileInformationCommand> commandProperty() {
        log.debug("commandProperty()");
        return commandModel.appFileCommandProperty();
    }

}
