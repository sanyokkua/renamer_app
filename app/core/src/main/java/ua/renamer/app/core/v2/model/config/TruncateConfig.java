package ua.renamer.app.core.v2.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.core.v2.enums.TruncateOptions;

/**
 * Configuration for truncating filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class TruncateConfig {
    /**
     * Number of characters to remove.
     */
    int numberOfSymbols;

    /**
     * Truncation option (REMOVE_SYMBOLS_IN_BEGIN, REMOVE_SYMBOLS_FROM_END, TRUNCATE_EMPTY_SYMBOLS).
     */
    TruncateOptions truncateOption;
}
