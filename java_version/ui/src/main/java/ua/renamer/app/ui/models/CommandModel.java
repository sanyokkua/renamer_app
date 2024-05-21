package ua.renamer.app.ui.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.abstracts.FileInformationCommand;

/**
 * Model class representing a command.
 */
@Slf4j
public class CommandModel {

    private final ObjectProperty<FileInformationCommand> appFileCommand;

    /**
     * Constructs a CommandModel object.
     */
    public CommandModel() {
        this.appFileCommand = new SimpleObjectProperty<>(this, null);
    }

    /**
     * Gets the current FileInformationCommand.
     *
     * @return The current FileInformationCommand.
     */
    public FileInformationCommand getAppFileCommand() {
        log.info("getAppFileCommand");
        return appFileCommand.get();
    }

    /**
     * Sets the FileInformationCommand.
     *
     * @param fileInformationCommand The FileInformationCommand to set.
     */
    public void setAppFileCommand(FileInformationCommand fileInformationCommand) {
        log.info("setAppFileCommand");
        this.appFileCommand.set(fileInformationCommand);
    }

    /**
     * Gets the property representing the FileInformationCommand.
     *
     * @return The property representing the FileInformationCommand.
     */
    public ObjectProperty<FileInformationCommand> appFileCommandProperty() {
        log.info("appFileCommandProperty");
        return appFileCommand;
    }

}
