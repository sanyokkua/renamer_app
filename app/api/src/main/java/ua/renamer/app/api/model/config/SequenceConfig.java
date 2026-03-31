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

    /** Number of digits for zero-padding (e.g., 3 → "001", "002"). Use 0 for no padding. */
    int padding;

    /** Criteria for sorting files before applying the sequence. */
    SortSource sortSource;
}
