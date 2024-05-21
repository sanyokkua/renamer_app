package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.DateTimeFormat;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link DateTimeFormat} and their corresponding string representations.
 */
public class DateTimeFormatConverter extends StringConverter<DateTimeFormat> {

    /**
     * Converts a {@link DateTimeFormat} enum constant to its corresponding string
     * representation. If the {@link DateTimeFormat} is
     * {@link DateTimeFormat#NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970},
     * it uses the {@link LanguageManager} to get the appropriate string.
     *
     * @param object the {@link DateTimeFormat} enum constant to be converted to a string.
     * @return the string representation of the provided {@link DateTimeFormat} constant.
     */
    @Override
    public String toString(DateTimeFormat object) {
        if (object == DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970) {
            return LanguageManager.getString(TextKeys.DATE_TIME_NUMBER_OF_SECONDS);
        }
        return object.getExampleString();
    }

    /**
     * Converts a string back to a {@link DateTimeFormat} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to a {@link DateTimeFormat} enum constant.
     * @return the corresponding {@link DateTimeFormat} enum constant, or {@code null} if not implemented.
     */
    @Override
    public DateTimeFormat fromString(String string) {
        return null;
    }

}
