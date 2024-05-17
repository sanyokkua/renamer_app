package ua.renamer.app.ui.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import ua.renamer.app.core.abstracts.AppFileCommand;

/**
 * Model class representing a command.
 */
public class CommandModel {
    private final ObjectProperty<AppFileCommand> appFileCommand;

    /**
     * Constructs a CommandModel object.
     */
    public CommandModel() {
        this.appFileCommand = new SimpleObjectProperty<>(this, null);
    }

    /**
     * Gets the current AppFileCommand.
     *
     * @return The current AppFileCommand.
     */
    public AppFileCommand getAppFileCommand() {
        return appFileCommand.get();
    }

    /**
     * Sets the AppFileCommand.
     *
     * @param appFileCommand The AppFileCommand to set.
     */
    public void setAppFileCommand(AppFileCommand appFileCommand) {
        this.appFileCommand.set(appFileCommand);
    }

    /**
     * Gets the property representing the AppFileCommand.
     *
     * @return The property representing the AppFileCommand.
     */
    public ObjectProperty<AppFileCommand> appFileCommandProperty() {
        return appFileCommand;
    }
}
