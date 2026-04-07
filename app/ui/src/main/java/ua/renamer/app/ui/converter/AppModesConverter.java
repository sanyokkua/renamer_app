package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link TransformationMode} and their corresponding string representations.
 * This converter utilizes the {@link LanguageTextRetrieverApi} to fetch
 * localized text based on the application's current language settings.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AppModesConverter extends StringConverter<TransformationMode> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts a {@link TransformationMode} enum constant to its corresponding string
     * representation using the {@link LanguageTextRetrieverApi}.
     *
     * @param object The {@link TransformationMode} enum constant to be converted to a string.
     * @return The string representation of the provided {@link TransformationMode} constant.
     */
    @Override
    public String toString(TransformationMode object) {
        return switch (object) {
            case ADD_TEXT -> languageTextRetriever.getString(TextKeys.MODE_ADD_TEXT);
            case CHANGE_CASE -> languageTextRetriever.getString(TextKeys.MODE_CHANGE_CASE);
            case ADD_DATETIME -> languageTextRetriever.getString(TextKeys.MODE_ADD_DATETIME);
            case ADD_DIMENSIONS -> languageTextRetriever.getString(TextKeys.MODE_ADD_DIMENSIONS);
            case ADD_FOLDER_NAME -> languageTextRetriever.getString(TextKeys.MODE_ADD_FOLDER_NAME);
            case REMOVE_TEXT -> languageTextRetriever.getString(TextKeys.MODE_REMOVE_TEXT);
            case REPLACE_TEXT -> languageTextRetriever.getString(TextKeys.MODE_REPLACE_TEXT);
            case NUMBER_FILES -> languageTextRetriever.getString(TextKeys.MODE_NUMBER_FILES);
            case TRIM_NAME -> languageTextRetriever.getString(TextKeys.MODE_TRIM_NAME);
            case CHANGE_EXTENSION -> languageTextRetriever.getString(TextKeys.MODE_CHANGE_EXTENSION);
        };
    }

    /**
     * Converts a string back to a {@link TransformationMode} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string The string to be converted back to a {@link TransformationMode} enum constant.
     * @return The corresponding {@link TransformationMode} enum constant, or {@code null} if not implemented.
     */
    @Override
    public TransformationMode fromString(String string) {
        return null;
    }

}
