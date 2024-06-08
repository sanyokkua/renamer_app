package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.DateFormat;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link DateFormat} enum constants and their corresponding string representations.
 * This converter uses {@link LanguageTextRetrieverApi} to retrieve localized text for specific date formats.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DateFormatConverter extends StringConverter<DateFormat> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts a {@link DateFormat} enum constant to its corresponding string
     * representation. If the {@link DateFormat} is {@link DateFormat#DO_NOT_USE_DATE},
     * it uses the {@link LanguageTextRetrieverApi} to get the appropriate string.
     *
     * @param object The {@link DateFormat} enum constant to be converted to a string.
     * @return The string representation of the provided {@link DateFormat} constant.
     */
    @Override
    public String toString(DateFormat object) {
        if (object == DateFormat.DO_NOT_USE_DATE) {
            return languageTextRetriever.getString(TextKeys.DO_NOT_USE);
        }
        return object.getExampleString();
    }

    /**
     * Converts a string back to a {@link DateFormat} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string The string to be converted back to a {@link DateFormat} enum constant.
     * @return The corresponding {@link DateFormat} enum constant, or {@code null} if not implemented.
     */
    @Override
    public DateFormat fromString(String string) {
        return null;
    }

}
