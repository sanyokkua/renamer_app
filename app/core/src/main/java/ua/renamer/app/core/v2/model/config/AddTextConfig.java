package ua.renamer.app.core.v2.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.core.v2.enums.ItemPosition;

/**
 * Configuration for adding text to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class AddTextConfig {
    /**
     * Text to add to the filename.
     */
    String textToAdd;

    /**
     * Position where to add the text (BEGIN or END).
     */
    ItemPosition position;
}
