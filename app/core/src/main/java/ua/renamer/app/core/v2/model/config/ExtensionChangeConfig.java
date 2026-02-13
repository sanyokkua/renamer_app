package ua.renamer.app.core.v2.model.config;

import lombok.Builder;
import lombok.Value;

/**
 * Configuration for changing file extensions.
 */
@Value
@Builder(setterPrefix = "with")
public class ExtensionChangeConfig implements TransformationConfig {
    /**
     * New extension to use (with or without leading dot).
     */
    String newExtension;
}
