package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.SortSource;

/**
 * Configuration for adding sequence numbers to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class SequenceConfig implements TransformationConfig {
    /** Starting number for the sequence. */
    int startNumber;

    /** Step value (increment) for each file. */
    int stepValue;

    /** Number of digits for zero-padding (e.g., 3 → "001", "002"). Use 0 for no padding. Must be >= 0. */
    int padding;

    /** Criteria for sorting files before applying the sequence. */
    SortSource sortSource;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class SequenceConfigBuilder {
        /**
         * Builds the {@link SequenceConfig}, validating that padding is non-negative.
         *
         * @return a new {@link SequenceConfig} instance
         * @throws IllegalArgumentException if padding is negative
         */
        public SequenceConfig build() {
            if (padding < 0) {
                throw new IllegalArgumentException("padding must be >= 0, got: " + padding);
            }
            return new SequenceConfig(startNumber, stepValue, padding, sortSource);
        }
    }
}
