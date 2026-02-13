package ua.renamer.app.core.v2.service;

import ua.renamer.app.core.v2.model.PreparedFileModel;
import ua.renamer.app.core.v2.model.RenameResult;

/**
 * Service for executing physical file renames.
 */
public interface RenameExecutionService {
    /**
     * Execute physical rename for a single prepared file.
     * Returns result with success/error status.
     *
     * @param preparedFile File to rename
     * @return Result with status and error information if failed
     */
    RenameResult execute(PreparedFileModel preparedFile);
}
