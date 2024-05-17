package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.TimeFormat;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link TimeFormat} and their corresponding string representations.
 */
public class TimeFormatConverter extends StringConverter<TimeFormat> {

    /**
     * Converts a {@link TimeFormat} enum constant to its corresponding string
     * representation. If the {@link TimeFormat} is {@link TimeFormat#DO_NOT_USE_TIME},
     * it uses the {@link LanguageManager} to get the appropriate string.
     *
     * @param object the {@link TimeFormat} enum constant to be converted to a string.
     * @return the string representation of the provided {@link TimeFormat} constant.
     */
    @Override
    public String toString(TimeFormat object) {
        if (object == TimeFormat.DO_NOT_USE_TIME) {
            return LanguageManager.getString(TextKeys.DO_NOT_USE);
        }
        return object.getExampleString();
    }

    /**
     * Converts a string back to a {@link TimeFormat} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to a {@link TimeFormat} enum constant.
     * @return the corresponding {@link TimeFormat} enum constant, or {@code null} if not implemented.
     */
    @Override
    public TimeFormat fromString(String string) {
        return null;
    }
}
