package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link ImageDimensionOptions} and their corresponding string representations.
 */
public class ImageDimensionOptionsConverter extends StringConverter<ImageDimensionOptions> {

    /**
     * Converts an {@link ImageDimensionOptions} enum constant to its corresponding string
     * representation using {@link LanguageManager}.
     *
     * @param object the {@link ImageDimensionOptions} enum constant to be converted to a string.
     * @return the string representation of the provided {@link ImageDimensionOptions} constant.
     */
    @Override
    public String toString(ImageDimensionOptions object) {
        return switch (object) {
            case DO_NOT_USE -> LanguageManager.getString(TextKeys.DO_NOT_USE);
            case WIDTH -> LanguageManager.getString(TextKeys.IMG_VID_DIMENSION_WIDTH);
            case HEIGHT -> LanguageManager.getString(TextKeys.IMG_VID_DIMENSION_HEIGHT);
        };
    }

    /**
     * Converts a string back to an {@link ImageDimensionOptions} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to an {@link ImageDimensionOptions} enum constant.
     * @return the corresponding {@link ImageDimensionOptions} enum constant, or {@code null} if not implemented.
     */
    @Override
    public ImageDimensionOptions fromString(String string) {
        return null;
    }

}
