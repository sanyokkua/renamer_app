package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.TextCaseOptions;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link TextCaseOptions} and their corresponding string representations.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TextCaseOptionsConverter extends StringConverter<TextCaseOptions> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts a {@link TextCaseOptions} enum constant to its corresponding string representation.
     *
     * @param object The {@link TextCaseOptions} enum constant to be converted to a string.
     *
     * @return The string representation of the provided {@link TextCaseOptions} constant.
     */
    @Override
    public String toString(TextCaseOptions object) {
        return object.getExampleString();
    }

    /**
     * Converts a string back to a {@link TextCaseOptions} enum constant.
     *
     * @param string The string to be converted back to a {@link TextCaseOptions} enum constant.
     *
     * @return The corresponding {@link TextCaseOptions} enum constant, or {@code null} if not implemented.
     *
     * @implNote This method is not yet implemented and always returns {@code null}.
     */
    @Override
    public TextCaseOptions fromString(String string) {
        return null;
    }

}
