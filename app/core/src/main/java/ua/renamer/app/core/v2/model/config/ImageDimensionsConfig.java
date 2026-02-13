package ua.renamer.app.core.v2.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.core.v2.enums.ImageDimensionOptions;
import ua.renamer.app.core.v2.enums.ItemPositionWithReplacement;

/**
 * Configuration for adding image dimensions to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class ImageDimensionsConfig {
    /**
     * What to show on the left side (DO_NOT_USE, WIDTH, or HEIGHT).
     */
    ImageDimensionOptions leftSide;

    /**
     * What to show on the right side (DO_NOT_USE, WIDTH, or HEIGHT).
     */
    ImageDimensionOptions rightSide;

    /**
     * Separator between dimensions (usually "x").
     */
    String separator;

    /**
     * Position where to add dimensions (BEGIN, END, or REPLACE).
     */
    ItemPositionWithReplacement position;
}
