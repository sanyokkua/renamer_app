package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.ItemPosition;

import java.util.Objects;

/**
 * Configuration for removing text from filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class RemoveTextConfig implements TransformationConfig {
    /**
     * Text to remove from the filename.
     */
    String textToRemove;

    /**
     * Position where to remove the text (BEGIN or END).
     */
    ItemPosition position;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class RemoveTextConfigBuilder {
        /**
         * Builds the {@link RemoveTextConfig}, validating that required fields are non-null.
         *
         * @return a new {@link RemoveTextConfig} instance
         * @throws NullPointerException if position or textToRemove is null
         */
        public RemoveTextConfig build() {
            Objects.requireNonNull(position, "position must not be null");
            Objects.requireNonNull(textToRemove, "textToRemove must not be null");
            return new RemoveTextConfig(textToRemove, position);
        }
    }
}
