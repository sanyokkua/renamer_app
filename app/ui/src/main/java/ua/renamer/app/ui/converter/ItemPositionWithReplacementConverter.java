package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link ItemPositionWithReplacement} enum constants and their corresponding string representations.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ItemPositionWithReplacementConverter extends StringConverter<ItemPositionWithReplacement> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts an {@link ItemPositionWithReplacement} enum constant to its corresponding string
     * representation using {@link LanguageTextRetrieverApi}.
     *
     * @param object The {@link ItemPositionWithReplacement} enum constant to be converted to a string.
     * @return The string representation of the provided {@link ItemPositionWithReplacement} constant.
     */
    @Override
    public String toString(ItemPositionWithReplacement object) {
        return switch (object) {
            case BEGIN -> languageTextRetriever.getString(TextKeys.RADIO_BTN_BEGIN);
            case END -> languageTextRetriever.getString(TextKeys.RADIO_BTN_END);
            case REPLACE -> languageTextRetriever.getString(TextKeys.RADIO_BTN_REPLACE);
        };
    }

    /**
     * Converts a string back to an {@link ItemPositionWithReplacement} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string The string to be converted back to an {@link ItemPositionWithReplacement} enum constant.
     * @return The corresponding {@link ItemPositionWithReplacement} enum constant, or {@code null} if not implemented.
     */
    @Override
    public ItemPositionWithReplacement fromString(String string) {
        return null;
    }

}
