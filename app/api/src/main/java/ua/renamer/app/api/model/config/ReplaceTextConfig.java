package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.ItemPositionExtended;

import java.util.Objects;

/**
 * Configuration for replacing text in filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class ReplaceTextConfig implements TransformationConfig {
    /**
     * Text to find and replace.
     */
    String textToReplace;

    /**
     * Replacement text.
     */
    String replacementText;

    /**
     * Position where to replace (BEGIN, END, or EVERYWHERE).
     */
    ItemPositionExtended position;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class ReplaceTextConfigBuilder {
        /**
         * Builds the {@link ReplaceTextConfig}, validating that required fields are non-null.
         *
         * @return a new {@link ReplaceTextConfig} instance
         * @throws NullPointerException if position, textToReplace, or replacementText is null
         */
        public ReplaceTextConfig build() {
            Objects.requireNonNull(position, "position must not be null");
            Objects.requireNonNull(textToReplace, "textToReplace must not be null");
            Objects.requireNonNull(replacementText, "replacementText must not be null");
            return new ReplaceTextConfig(textToReplace, replacementText, position);
        }
    }
}
