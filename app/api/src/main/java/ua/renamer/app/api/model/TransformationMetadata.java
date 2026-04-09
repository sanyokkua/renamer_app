package ua.renamer.app.api.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Metadata about the transformation applied to a file.
 * Tracks what transformation was applied, when, and with what configuration.
 */
@Value
@Builder(setterPrefix = "with")
public class TransformationMetadata {
    /**
     * The transformation mode that was applied.
     */
    TransformationMode mode;

    /**
     * When the transformation was applied.
     */
    LocalDateTime appliedAt;

    /**
     * Configuration used for the transformation as a key-value map.
     */
    Map<String, Object> config;
}
