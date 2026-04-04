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
            case ADD_TEXT -> languageTextRetriever.getString(TextKeys.MODE_ADD_CUSTOM_TEXT);
            case CHANGE_CASE -> languageTextRetriever.getString(TextKeys.MODE_CHANGE_CASE);
            case USE_DATETIME -> languageTextRetriever.getString(TextKeys.MODE_DATETIME);
            case USE_IMAGE_DIMENSIONS -> languageTextRetriever.getString(TextKeys.MODE_IMG_VID_DIMENSIONS);
            case USE_PARENT_FOLDER_NAME -> languageTextRetriever.getString(TextKeys.MODE_PARENT_FOLDERS);
            case REMOVE_TEXT -> languageTextRetriever.getString(TextKeys.MODE_REMOVE_TEXT);
            case REPLACE_TEXT -> languageTextRetriever.getString(TextKeys.MODE_REPLACE_TEXT);
            case ADD_SEQUENCE -> languageTextRetriever.getString(TextKeys.MODE_USE_DIGITAL_SEQUENCE);
            case TRUNCATE_FILE_NAME -> languageTextRetriever.getString(TextKeys.MODE_TRUNCATE);
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
