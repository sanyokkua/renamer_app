package ua.renamer.app.ui.abstracts;

import javafx.beans.property.ObjectProperty;
import ua.renamer.app.core.abstracts.AppFileCommand;

/**
 * An interface representing common functionality for controllers.
 */
public interface ControllerApi {
    /**
     * Gets the current command.
     *
     * @return The current command.
     */
    AppFileCommand getCommand();

    /**
     * Sets the command.
     *
     * @param appFileCommand The command to set.
     */
    void setCommand(AppFileCommand appFileCommand);

    /**
     * Gets the property representing the command.
     *
     * @return The property representing the command.
     */
    ObjectProperty<AppFileCommand> commandProperty();

    /**
     * Updates the command.
     */
    void updateCommand();
}
