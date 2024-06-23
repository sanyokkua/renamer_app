package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link ItemPositionExtended} enum constants and their corresponding string representations.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ItemPositionExtendedConverter extends StringConverter<ItemPositionExtended> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts an {@link ItemPositionExtended} enum constant to its corresponding string
     * representation using {@link LanguageTextRetrieverApi}.
     *
     * @param object The {@link ItemPositionExtended} enum constant to be converted to a string.
     *
     * @return The string representation of the provided {@link ItemPositionExtended} constant.
     */
    @Override
    public String toString(ItemPositionExtended object) {
        return switch (object) {
            case BEGIN -> languageTextRetriever.getString(TextKeys.RADIO_BTN_BEGIN);
            case END -> languageTextRetriever.getString(TextKeys.RADIO_BTN_END);
            case EVERYWHERE -> languageTextRetriever.getString(TextKeys.RADIO_BTN_EVERYWHERE);
        };
    }

    /**
     * Converts a string back to an {@link ItemPositionExtended} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string The string to be converted back to an {@link ItemPositionExtended} enum constant.
     *
     * @return The corresponding {@link ItemPositionExtended} enum constant, or {@code null} if not implemented.
     */
    @Override
    public ItemPositionExtended fromString(String string) {
        return null;
    }

}
