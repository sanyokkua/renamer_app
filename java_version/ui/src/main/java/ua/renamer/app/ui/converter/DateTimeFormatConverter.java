package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.DateTimeFormat;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link DateTimeFormat} enum constants and their corresponding string representations.
 * This converter uses {@link LanguageTextRetrieverApi} to fetch localized text based on the application's current language settings.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DateTimeFormatConverter extends StringConverter<DateTimeFormat> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts a {@link DateTimeFormat} enum constant to its corresponding string
     * representation. If the {@link DateTimeFormat} is
     * {@link DateTimeFormat#NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970},
     * it uses the {@link LanguageTextRetrieverApi} to get the appropriate string.
     *
     * @param object The {@link DateTimeFormat} enum constant to be converted to a string.
     *
     * @return The string representation of the provided {@link DateTimeFormat} constant.
     */
    @Override
    public String toString(DateTimeFormat object) {
        if (object == DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970) {
            return languageTextRetriever.getString(TextKeys.DATE_TIME_NUMBER_OF_SECONDS);
        }
        return object.getExampleString();
    }

    /**
     * Converts a string back to a {@link DateTimeFormat} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string The string to be converted back to a {@link DateTimeFormat} enum constant.
     *
     * @return The corresponding {@link DateTimeFormat} enum constant, or {@code null} if not implemented.
     */
    @Override
    public DateTimeFormat fromString(String string) {
        return null;
    }

}
