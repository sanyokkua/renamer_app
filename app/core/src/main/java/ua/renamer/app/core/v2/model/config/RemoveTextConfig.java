package ua.renamer.app.core.v2.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.core.v2.enums.ItemPosition;

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
}
