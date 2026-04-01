package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.ImageDimensionOptions;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;

import java.util.Objects;

/**
 * Configuration for adding image dimensions to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class ImageDimensionsConfig implements TransformationConfig {
    /** What to show on the left side (DO_NOT_USE, WIDTH, or HEIGHT). */
    ImageDimensionOptions leftSide;

    /** What to show on the right side (DO_NOT_USE, WIDTH, or HEIGHT). */
    ImageDimensionOptions rightSide;

    /** Separator between dimensions (usually "x"). */
    String separator;

    /** Position where to add dimensions (BEGIN, END, or REPLACE). */
    ItemPositionWithReplacement position;

    // Partial Lombok builder — Lombok adds with* methods; we override build() for validation
    public static class ImageDimensionsConfigBuilder {
        /**
         * Builds the {@link ImageDimensionsConfig}, validating that required fields are non-null and
         * that at least one of leftSide or rightSide is not {@link ImageDimensionOptions#DO_NOT_USE}.
         *
         * @return a new {@link ImageDimensionsConfig} instance
         * @throws NullPointerException     if position, leftSide, or rightSide is null
         * @throws IllegalArgumentException if both leftSide and rightSide are DO_NOT_USE
         */
        public ImageDimensionsConfig build() {
            Objects.requireNonNull(position, "position must not be null");
            Objects.requireNonNull(leftSide, "leftSide must not be null");
            Objects.requireNonNull(rightSide, "rightSide must not be null");
            if (leftSide == ImageDimensionOptions.DO_NOT_USE && rightSide == ImageDimensionOptions.DO_NOT_USE) {
                throw new IllegalArgumentException(
                        "at least one of leftSide/rightSide must not be DO_NOT_USE");
            }
            return new ImageDimensionsConfig(leftSide, rightSide, separator, position);
        }
    }
}
