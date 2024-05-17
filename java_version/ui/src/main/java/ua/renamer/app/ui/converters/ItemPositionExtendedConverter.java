package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link ItemPositionExtended} and their corresponding string representations.
 */
public class ItemPositionExtendedConverter extends StringConverter<ItemPositionExtended> {

    /**
     * Converts an {@link ItemPositionExtended} enum constant to its corresponding string
     * representation using {@link LanguageManager}.
     *
     * @param object the {@link ItemPositionExtended} enum constant to be converted to a string.
     * @return the string representation of the provided {@link ItemPositionExtended} constant.
     */
    @Override
    public String toString(ItemPositionExtended object) {
        return switch (object) {
            case BEGIN -> LanguageManager.getString(TextKeys.RADIO_BTN_BEGIN);
            case END -> LanguageManager.getString(TextKeys.RADIO_BTN_END);
            case EVERYWHERE -> LanguageManager.getString(TextKeys.RADIO_BTN_EVERYWHERE);
        };
    }

    /**
     * Converts a string back to an {@link ItemPositionExtended} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to an {@link ItemPositionExtended} enum constant.
     * @return the corresponding {@link ItemPositionExtended} enum constant, or {@code null} if not implemented.
     */
    @Override
    public ItemPositionExtended fromString(String string) {
        return null;
    }
}
