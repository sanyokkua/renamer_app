package ua.renamer.app.ui.abstracts;

import javafx.beans.property.ObjectProperty;
import ua.renamer.app.core.abstracts.FileInformationCommand;

/**
 * An interface representing common functionality for controllers.
 */
public interface ControllerApi {

    /**
     * Gets the current command.
     *
     * @return The current command.
     */
    FileInformationCommand getCommand();

    /**
     * Sets the command.
     *
     * @param fileInformationCommand The command to set.
     */
    void setCommand(FileInformationCommand fileInformationCommand);

    /**
     * Gets the property representing the command.
     *
     * @return The property representing the command.
     */
    ObjectProperty<FileInformationCommand> commandProperty();

    /**
     * Updates the command.
     */
    void updateCommand();

}
