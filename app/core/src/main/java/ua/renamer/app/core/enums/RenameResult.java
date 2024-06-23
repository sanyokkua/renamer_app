package ua.renamer.app.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This enum represents the possible outcomes of a file renaming operation.
 */
@RequiredArgsConstructor
@Getter
public enum RenameResult {
    /**
     * No renaming actions were performed. For example renaming process is not started
     */
    NO_ACTIONS_HAPPEN("no_actions_happen"),
    /**
     * The file was successfully renamed without any errors encountered during the process.
     */
    RENAMED_WITHOUT_ERRORS("renamed_without_errors"),
    /**
     * The file was not renamed because it did not require any changes based on the provided information.
     * This could be because the original and new names/extensions were identical.
     */
    NOT_RENAMED_BECAUSE_NOT_NEEDED("not_renamed_because_not_needed"),
    /**
     * The file renaming operation failed due to an error.
     * The specific error details might be available elsewhere in the application logic.
     */
    NOT_RENAMED_BECAUSE_OF_ERROR("not_renamed_because_of_error");

    private final String value;
}
