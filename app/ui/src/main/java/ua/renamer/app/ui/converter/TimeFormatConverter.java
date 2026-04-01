package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.TimeFormat;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link TimeFormat} and their corresponding string representations.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TimeFormatConverter extends StringConverter<TimeFormat> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts a {@link TimeFormat} enum constant to its corresponding string representation.
     *
     * @param object The {@link TimeFormat} enum constant to be converted to a string.
     * @return The string representation of the provided {@link TimeFormat} constant.
     */
    @Override
    public String toString(TimeFormat object) {
        if (object == TimeFormat.DO_NOT_USE_TIME) {
            return languageTextRetriever.getString(TextKeys.DO_NOT_USE);
        }
        return object.getExampleString();
    }

    /**
     * Converts a string back to a {@link TimeFormat} enum constant.
     *
     * @param string The string to be converted back to a {@link TimeFormat} enum constant.
     * @return The corresponding {@link TimeFormat} enum constant, or {@code null} if not implemented.
     * @implNote This method is not yet implemented and always returns {@code null}.
     */
    @Override
    public TimeFormat fromString(String string) {
        return null;
    }

}
