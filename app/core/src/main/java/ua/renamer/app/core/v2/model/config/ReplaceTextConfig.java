package ua.renamer.app.core.v2.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.core.v2.enums.ItemPositionExtended;

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
}
