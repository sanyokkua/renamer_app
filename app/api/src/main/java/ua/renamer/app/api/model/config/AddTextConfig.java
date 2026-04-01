package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.ItemPosition;

import java.util.Objects;

/**
 * Configuration for adding text to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class AddTextConfig implements TransformationConfig {
    /**
     * Text to add to the filename.
     */
    String textToAdd;

    /**
     * Position where to add the text (BEGIN or END).
     */
    ItemPosition position;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class AddTextConfigBuilder {
        /**
         * Builds the {@link AddTextConfig}, validating that required fields are non-null.
         *
         * @return a new {@link AddTextConfig} instance
         * @throws NullPointerException if position or textToAdd is null
         */
        public AddTextConfig build() {
            Objects.requireNonNull(position, "position must not be null");
            Objects.requireNonNull(textToAdd, "textToAdd must not be null");
            return new AddTextConfig(textToAdd, position);
        }
    }
}
