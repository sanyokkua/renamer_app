package ua.renamer.app.core.v2.enums;

/**
 * An enumeration representing different options for truncating text.
 * It includes options to remove symbols from the beginning, from the end, or to truncate empty symbols.
 *
 * <p>This is a V2-specific copy of the original enum from ua.renamer.app.core.enums package.
 * Created to support the V2 architecture redesign with independent type definitions.</p>
 */
public enum TruncateOptions {
    REMOVE_SYMBOLS_IN_BEGIN,
    REMOVE_SYMBOLS_FROM_END,
    TRUNCATE_EMPTY_SYMBOLS,
}
