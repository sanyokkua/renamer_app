package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.DateFormat;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link DateFormat} and their corresponding string representations.
 */
public class DateFormatConverter extends StringConverter<DateFormat> {

    /**
     * Converts a {@link DateFormat} enum constant to its corresponding string
     * representation. If the {@link DateFormat} is {@link DateFormat#DO_NOT_USE_DATE},
     * it uses the {@link LanguageManager} to get the appropriate string.
     *
     * @param object the {@link DateFormat} enum constant to be converted to a string.
     * @return the string representation of the provided {@link DateFormat} constant.
     */
    @Override
    public String toString(DateFormat object) {
        if (object == DateFormat.DO_NOT_USE_DATE) {
            return LanguageManager.getString(TextKeys.DO_NOT_USE);
        }
        return object.getExampleString();
    }

    /**
     * Converts a string back to a {@link DateFormat} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to a {@link DateFormat} enum constant.
     * @return the corresponding {@link DateFormat} enum constant, or {@code null} if not implemented.
     */
    @Override
    public DateFormat fromString(String string) {
        return null;
    }

}
