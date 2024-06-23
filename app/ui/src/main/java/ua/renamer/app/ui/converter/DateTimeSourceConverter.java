package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.DateTimeSource;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link DateTimeSource} enum constants and their corresponding string representations.
 * This converter uses {@link LanguageTextRetrieverApi} to fetch localized text based on the application's current language settings.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DateTimeSourceConverter extends StringConverter<DateTimeSource> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts a {@link DateTimeSource} enum constant to its corresponding string
     * representation. Uses {@link LanguageTextRetrieverApi} to get the appropriate string
     * based on the type of DateTimeSource.
     *
     * @param object The {@link DateTimeSource} enum constant to be converted to a string.
     *
     * @return The string representation of the provided {@link DateTimeSource} constant.
     */
    @Override
    public String toString(DateTimeSource object) {
        // @formatter:off
        return switch (object) {
            case FILE_CREATION_DATE -> languageTextRetriever.getString(TextKeys.DATE_TIME_SOURCE_FILE_CREATION_DATETIME);
            case FILE_MODIFICATION_DATE -> languageTextRetriever.getString(TextKeys.DATE_TIME_SOURCE_FILE_MODIFICATION_DATETIME);
            case CONTENT_CREATION_DATE -> languageTextRetriever.getString(TextKeys.DATE_TIME_SOURCE_FILE_CONTENT_CREATION_DATETIME);
            case CURRENT_DATE -> languageTextRetriever.getString(TextKeys.DATE_TIME_SOURCE_CURRENT_DATETIME);
            case CUSTOM_DATE -> languageTextRetriever.getString(TextKeys.DATE_TIME_SOURCE_CUSTOM_DATETIME);
        };
        // @formatter:on
    }

    /**
     * Converts a string back to a {@link DateTimeSource} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string The string to be converted back to a {@link DateTimeSource} enum constant.
     *
     * @return The corresponding {@link DateTimeSource} enum constant, or {@code null} if not implemented.
     */
    @Override
    public DateTimeSource fromString(String string) {
        return null;
    }

}
