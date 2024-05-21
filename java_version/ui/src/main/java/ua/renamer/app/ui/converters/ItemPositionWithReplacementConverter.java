package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link ItemPositionWithReplacement} and their corresponding string representations.
 */
public class ItemPositionWithReplacementConverter extends StringConverter<ItemPositionWithReplacement> {

    /**
     * Converts an {@link ItemPositionWithReplacement} enum constant to its corresponding string
     * representation using {@link LanguageManager}.
     *
     * @param object the {@link ItemPositionWithReplacement} enum constant to be converted to a string.
     * @return the string representation of the provided {@link ItemPositionWithReplacement} constant.
     */
    @Override
    public String toString(ItemPositionWithReplacement object) {
        return switch (object) {
            case BEGIN -> LanguageManager.getString(TextKeys.RADIO_BTN_BEGIN);
            case END -> LanguageManager.getString(TextKeys.RADIO_BTN_END);
            case REPLACE -> LanguageManager.getString(TextKeys.RADIO_BTN_REPLACE);
        };
    }

    /**
     * Converts a string back to an {@link ItemPositionWithReplacement} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to an {@link ItemPositionWithReplacement} enum constant.
     * @return the corresponding {@link ItemPositionWithReplacement} enum constant, or {@code null} if not implemented.
     */
    @Override
    public ItemPositionWithReplacement fromString(String string) {
        return null;
    }

}
