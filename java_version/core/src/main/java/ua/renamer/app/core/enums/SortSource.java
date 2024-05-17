package ua.renamer.app.core.enums;

/**
 * An enumeration representing different sources for sorting.
 * It includes various file attributes such as name, path, size, and date-time,
 * as well as image width and height.
 */
public enum SortSource {
    FILE_NAME,
    FILE_PATH,
    FILE_SIZE,
    FILE_CREATION_DATETIME,
    FILE_MODIFICATION_DATETIME,
    FILE_CONTENT_CREATION_DATETIME,
    IMAGE_WIDTH,
    IMAGE_HEIGHT,
}
