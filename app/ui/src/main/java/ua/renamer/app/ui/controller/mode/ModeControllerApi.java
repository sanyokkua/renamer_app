package ua.renamer.app.ui.controller.mode;

import javafx.beans.property.ObjectProperty;
import ua.renamer.app.core.service.command.FileInformationCommand;

/**
 * An interface representing common functionality for controllers that manage file information commands.
 */
public interface ModeControllerApi {

    /**
     * Gets the current command used by the controller.
     *
     * @return The current {@link FileInformationCommand}.
     */
    FileInformationCommand getCommand();

    /**
     * Sets the command to be used by the controller.
     *
     * @param fileInformationCommand The {@link FileInformationCommand} to set.
     */
    void setCommand(FileInformationCommand fileInformationCommand);

    /**
     * Gets the property representing the command used by the controller.
     *
     * @return The {@link ObjectProperty} representing the {@link FileInformationCommand}.
     */
    ObjectProperty<FileInformationCommand> commandProperty();

    /**
     * Updates the command, typically used to refresh or modify the existing command state.
     */
    void updateCommand();

}
