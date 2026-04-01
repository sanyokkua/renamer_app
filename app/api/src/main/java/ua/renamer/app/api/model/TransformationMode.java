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
     * Use datetime in filename from various sources.
     */
    USE_DATETIME,

    /**
     * Use image dimensions in filename (e.g., 1920x1080).
     */
    USE_IMAGE_DIMENSIONS,

    /**
     * Add sequence numbers to files.
     */
    ADD_SEQUENCE,

    /**
     * Use parent folder name in filename.
     */
    USE_PARENT_FOLDER_NAME,

    /**
     * Truncate filename by removing characters.
     */
    TRUNCATE_FILE_NAME,

    /**
     * Change file extension.
     */
    CHANGE_EXTENSION
}
