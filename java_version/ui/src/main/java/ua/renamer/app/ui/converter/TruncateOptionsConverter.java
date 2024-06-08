package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link TruncateOptions} and their corresponding string representations.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TruncateOptionsConverter extends StringConverter<TruncateOptions> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts a {@link TruncateOptions} enum constant to its corresponding string
     * representation using {@link LanguageTextRetrieverApi}.
     *
     * @param object The {@link TruncateOptions} enum constant to be converted to a string.
     * @return The string representation of the provided {@link TruncateOptions} constant.
     */
    @Override
    public String toString(TruncateOptions object) {
        return switch (object) {
            case REMOVE_SYMBOLS_IN_BEGIN -> languageTextRetriever.getString(TextKeys.RADIO_BTN_BEGIN);
            case REMOVE_SYMBOLS_FROM_END -> languageTextRetriever.getString(TextKeys.RADIO_BTN_END);
            case TRUNCATE_EMPTY_SYMBOLS -> languageTextRetriever.getString(TextKeys.RADIO_BTN_TRIM_EMPTY);
        };
    }

    /**
     * Converts a string back to a {@link TruncateOptions} enum constant.
     *
     * @param string The string to be converted back to a {@link TruncateOptions} enum constant.
     * @return The corresponding {@link TruncateOptions} enum constant, or {@code null} if not implemented.
     * @implNote This method is not yet implemented and always returns {@code null}.
     */
    @Override
    public TruncateOptions fromString(String string) {
        return null;
    }

}
