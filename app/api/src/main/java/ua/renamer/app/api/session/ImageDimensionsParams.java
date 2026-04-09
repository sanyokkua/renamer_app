package ua.renamer.app.api.session;

import ua.renamer.app.api.enums.ImageDimensionOptions;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;
import ua.renamer.app.api.model.TransformationMode;

/**
 * Parameters for the {@link TransformationMode#ADD_DIMENSIONS} transformation mode.
 * Configures how image width and/or height values are embedded in the filename.
 *
 * @param leftSide      the dimension to place on the left side of the dimension separator; may be {@link ImageDimensionOptions#DO_NOT_USE}
 * @param rightSide     the dimension to place on the right side of the dimension separator; may be {@link ImageDimensionOptions#DO_NOT_USE}
 * @param position      where the formatted dimensions are inserted or used as replacement; must not be null
 * @param nameSeparator the string placed between the dimension string and the filename (e.g. " " in "1920x1080 file.jpg"); may be null or empty
 * @param separator     the string placed between left and right dimension values (e.g. "x" in "1920x1080"); may be null or empty
 */
public record ImageDimensionsParams(
        ImageDimensionOptions leftSide,
        ImageDimensionOptions rightSide,
        ItemPositionWithReplacement position,
        String nameSeparator,
        String separator
) implements ModeParameters {

    @Override
    public TransformationMode mode() {
        return TransformationMode.ADD_DIMENSIONS;
    }

    @Override
    public ValidationResult validate() {
        if (position == null) {
            return ValidationResult.fieldError("position", "must not be null");
        }
        if (leftSide == ImageDimensionOptions.DO_NOT_USE
                && rightSide == ImageDimensionOptions.DO_NOT_USE) {
            return ValidationResult.fieldError("leftSide",
                    "at least one of leftSide or rightSide must not be DO_NOT_USE");
        }
        return ValidationResult.valid();
    }

    /**
     * Returns a copy of this record with the given left side dimension option.
     *
     * @param leftSide the new left side option; must not be null
     * @return a new {@link ImageDimensionsParams} with the updated field
     */
    public ImageDimensionsParams withLeftSide(ImageDimensionOptions leftSide) {
        return new ImageDimensionsParams(leftSide, this.rightSide, this.position,
                this.nameSeparator, this.separator);
    }

    /**
     * Returns a copy of this record with the given right side dimension option.
     *
     * @param rightSide the new right side option; must not be null
     * @return a new {@link ImageDimensionsParams} with the updated field
     */
    public ImageDimensionsParams withRightSide(ImageDimensionOptions rightSide) {
        return new ImageDimensionsParams(this.leftSide, rightSide, this.position,
                this.nameSeparator, this.separator);
    }

    /**
     * Returns a copy of this record with the given position.
     *
     * @param position the new insertion/replacement position; must not be null for valid configuration
     * @return a new {@link ImageDimensionsParams} with the updated field
     */
    public ImageDimensionsParams withPosition(ItemPositionWithReplacement position) {
        return new ImageDimensionsParams(this.leftSide, this.rightSide, position,
                this.nameSeparator, this.separator);
    }

    /**
     * Returns a copy of this record with the given name separator.
     *
     * @param nameSeparator the string to place between the dimension string and the filename; may be null or empty
     * @return a new {@link ImageDimensionsParams} with the updated field
     */
    public ImageDimensionsParams withNameSeparator(String nameSeparator) {
        return new ImageDimensionsParams(this.leftSide, this.rightSide, this.position,
                nameSeparator, this.separator);
    }

    /**
     * Returns a copy of this record with the given dimension separator.
     *
     * @param separator the string to place between left and right dimension values (e.g. "x"); may be null or empty
     * @return a new {@link ImageDimensionsParams} with the updated field
     */
    public ImageDimensionsParams withSeparator(String separator) {
        return new ImageDimensionsParams(this.leftSide, this.rightSide, this.position,
                this.nameSeparator, separator);
    }
}
