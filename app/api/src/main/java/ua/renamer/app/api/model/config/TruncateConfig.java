package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.TruncateOptions;

import java.util.Objects;

/**
 * Configuration for truncating filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class TruncateConfig implements TransformationConfig {
    /** Number of characters to remove. */
    int numberOfSymbols;

    /** Truncation option (REMOVE_SYMBOLS_IN_BEGIN, REMOVE_SYMBOLS_FROM_END, TRUNCATE_EMPTY_SYMBOLS). */
    TruncateOptions truncateOption;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class TruncateConfigBuilder {
        /**
         * Builds the {@link TruncateConfig}, validating that required fields are non-null and
         * numberOfSymbols is non-negative.
         *
         * @return a new {@link TruncateConfig} instance
         * @throws NullPointerException     if truncateOption is null
         * @throws IllegalArgumentException if numberOfSymbols is negative
         */
        public TruncateConfig build() {
            Objects.requireNonNull(truncateOption, "truncateOption must not be null");
            if (numberOfSymbols < 0) {
                throw new IllegalArgumentException("numberOfSymbols must be >= 0, got: " + numberOfSymbols);
            }
            return new TruncateConfig(numberOfSymbols, truncateOption);
        }
    }
}
