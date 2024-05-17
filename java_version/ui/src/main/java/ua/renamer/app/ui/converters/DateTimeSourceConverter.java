package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.DateTimeSource;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link DateTimeSource} and their corresponding string representations.
 */
public class DateTimeSourceConverter extends StringConverter<DateTimeSource> {

    /**
     * Converts a {@link DateTimeSource} enum constant to its corresponding string
     * representation. Uses {@link LanguageManager} to get the appropriate string
     * based on the type of DateTimeSource.
     *
     * @param object the {@link DateTimeSource} enum constant to be converted to a string.
     * @return the string representation of the provided {@link DateTimeSource} constant.
     */
    @Override
    public String toString(DateTimeSource object) {
        return switch (object) {
            case FILE_CREATION_DATE -> LanguageManager.getString(TextKeys.DATE_TIME_SOURCE_FILE_CREATION_DATETIME);
            case FILE_MODIFICATION_DATE ->
                    LanguageManager.getString(TextKeys.DATE_TIME_SOURCE_FILE_MODIFICATION_DATETIME);
            case CONTENT_CREATION_DATE ->
                    LanguageManager.getString(TextKeys.DATE_TIME_SOURCE_FILE_CONTENT_CREATION_DATETIME);
            case CURRENT_DATE -> LanguageManager.getString(TextKeys.DATE_TIME_SOURCE_CURRENT_DATETIME);
            case CUSTOM_DATE -> LanguageManager.getString(TextKeys.DATE_TIME_SOURCE_CUSTOM_DATETIME);
        };
    }

    /**
     * Converts a string back to a {@link DateTimeSource} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to a {@link DateTimeSource} enum constant.
     * @return the corresponding {@link DateTimeSource} enum constant, or {@code null} if not implemented.
     */
    @Override
    public DateTimeSource fromString(String string) {
        return null;
    }
}
