package ua.renamer.app.core.service.command;

import ua.renamer.app.core.model.FileInformation;

/**
 * An abstract class representing a command for processing {@link FileInformation} objects.
 * Extends {@link ListProcessingCommand} with {@link FileInformation} type for both input and output.
 */
public abstract class FileInformationCommand extends ListProcessingCommand<FileInformation, FileInformation> {
}
