package ua.renamer.app.api.model;

/**
 * Status of a file rename operation.
 */
public enum RenameStatus {
    /** File was successfully renamed. */
    SUCCESS,

    /** Rename was skipped (same name or had errors from previous phases). */
    SKIPPED,

    /** Failed during metadata extraction phase. */
    ERROR_EXTRACTION,

    /** Failed during transformation phase. */
    ERROR_TRANSFORMATION,

    /** Failed during physical rename execution. */
    ERROR_EXECUTION
}
