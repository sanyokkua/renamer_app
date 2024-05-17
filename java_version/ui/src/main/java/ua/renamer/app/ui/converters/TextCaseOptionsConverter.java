package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.TextCaseOptions;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link TextCaseOptions} and their corresponding string representations.
 */
public class TextCaseOptionsConverter extends StringConverter<TextCaseOptions> {

    /**
     * Converts a {@link TextCaseOptions} enum constant to its corresponding string representation.
     *
     * @param object the {@link TextCaseOptions} enum constant to be converted to a string.
     * @return the string representation of the provided {@link TextCaseOptions} constant.
     */
    @Override
    public String toString(TextCaseOptions object) {
        return object.getExampleString();
    }

    /**
     * Converts a string back to a {@link TextCaseOptions} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to a {@link TextCaseOptions} enum constant.
     * @return the corresponding {@link TextCaseOptions} enum constant, or {@code null} if not implemented.
     */
    @Override
    public TextCaseOptions fromString(String string) {
        return null;
    }
}
