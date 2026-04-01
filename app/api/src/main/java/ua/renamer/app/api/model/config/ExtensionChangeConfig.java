package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;

import java.util.Objects;

/**
 * Configuration for changing file extensions.
 */
@Value
@Builder(setterPrefix = "with")
public class ExtensionChangeConfig implements TransformationConfig {
    /** New extension to use (with or without leading dot). */
    String newExtension;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class ExtensionChangeConfigBuilder {
        /**
         * Builds the {@link ExtensionChangeConfig}, validating that newExtension is non-null and non-blank.
         *
         * @return a new {@link ExtensionChangeConfig} instance
         * @throws NullPointerException     if newExtension is null
         * @throws IllegalArgumentException if newExtension is blank
         */
        public ExtensionChangeConfig build() {
            Objects.requireNonNull(newExtension, "newExtension must not be null");
            if (newExtension.trim().isEmpty()) {
                throw new IllegalArgumentException("newExtension must not be blank");
            }
            return new ExtensionChangeConfig(newExtension);
        }
    }
}
