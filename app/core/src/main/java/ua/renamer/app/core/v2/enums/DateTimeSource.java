package ua.renamer.app.core.v2.enums;

/**
 * An enumeration representing different sources for date-time values.
 *
 * <p>This is a V2-specific copy of the original enum from ua.renamer.app.core.enums package.
 * Created to support the V2 architecture redesign with independent type definitions.</p>
 */
public enum DateTimeSource {
    FILE_CREATION_DATE,
    FILE_MODIFICATION_DATE,
    CONTENT_CREATION_DATE,
    CURRENT_DATE,
    CUSTOM_DATE,
}
