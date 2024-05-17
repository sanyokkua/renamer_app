package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link TruncateOptions} and their corresponding string representations.
 */
public class TruncateOptionsConverter extends StringConverter<TruncateOptions> {

    /**
     * Converts a {@link TruncateOptions} enum constant to its corresponding string
     * representation using {@link LanguageManager}.
     *
     * @param object the {@link TruncateOptions} enum constant to be converted to a string.
     * @return the string representation of the provided {@link TruncateOptions} constant.
     */
    @Override
    public String toString(TruncateOptions object) {
        return switch (object) {
            case REMOVE_SYMBOLS_IN_BEGIN -> LanguageManager.getString(TextKeys.RADIO_BTN_BEGIN);
            case REMOVE_SYMBOLS_FROM_END -> LanguageManager.getString(TextKeys.RADIO_BTN_END);
            case TRUNCATE_EMPTY_SYMBOLS -> LanguageManager.getString(TextKeys.RADIO_BTN_TRIM_EMPTY);
        };
    }

    /**
     * Converts a string back to a {@link TruncateOptions} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to a {@link TruncateOptions} enum constant.
     * @return the corresponding {@link TruncateOptions} enum constant, or {@code null} if not implemented.
     */
    @Override
    public TruncateOptions fromString(String string) {
        return null;
    }
}
