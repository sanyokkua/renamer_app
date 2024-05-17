package ua.renamer.app.core.abstracts;

import ua.renamer.app.core.model.AppFile;

/**
 * An abstract class representing a command for processing {@link AppFile} objects.
 * Extends {@link ListProcessingCommand} with {@link AppFile} type for both input and output.
 */
public abstract class AppFileCommand extends ListProcessingCommand<AppFile, AppFile> {
}
