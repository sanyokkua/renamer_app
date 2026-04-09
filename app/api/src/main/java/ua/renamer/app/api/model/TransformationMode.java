package ua.renamer.app.api.model;

/**
 * Available file name transformation modes.
 */
public enum TransformationMode {
    /**
     * Add text to the beginning or end of filename.
     */
    ADD_TEXT,

    /**
     * Remove text from the beginning or end of filename.
     */
    REMOVE_TEXT,

    /**
     * Replace text in filename.
     */
    REPLACE_TEXT,

    /**
     * Change the case of filename (camelCase, snake_case, etc.).
     */
    CHANGE_CASE,

    /**
     * Add datetime to the filename from various sources.
     */
    ADD_DATETIME,

    /**
     * Add image dimensions to the filename (e.g., 1920x1080).
     */
    ADD_DIMENSIONS,

    /**
     * Number files with a sequential numeric sequence.
     */
    NUMBER_FILES,

    /**
     * Add parent folder name to the filename.
     */
    ADD_FOLDER_NAME,

    /**
     * Trim filename by removing characters from the beginning or end.
     */
    TRIM_NAME,

    /**
     * Change file extension.
     */
    CHANGE_EXTENSION
}
