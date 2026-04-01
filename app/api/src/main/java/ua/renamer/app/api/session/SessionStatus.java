package ua.renamer.app.api.session;

/**
 * Represent the lifecycle state of a rename session.
 */
public enum SessionStatus {
    /**
     * No files have been loaded into the session.
     */
    EMPTY,

    /**
     * Files are loaded but no transformation mode has been selected.
     */
    FILES_LOADED,

    /**
     * A mode is selected and its parameters are valid; the session is ready to preview or execute.
     */
    MODE_CONFIGURED,

    /**
     * A rename operation is currently in progress.
     */
    EXECUTING,

    /**
     * The last rename execution completed (successful, partial, or with file-level errors).
     */
    COMPLETE,

    /**
     * A session-level error occurred (distinct from per-file errors).
     */
    ERROR
}
