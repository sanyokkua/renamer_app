package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.AppModes;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link AppModes} and their corresponding string representations.
 * This converter utilizes the {@link LanguageTextRetrieverApi} to fetch
 * localized text based on the application's current language settings.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AppModesConverter extends StringConverter<AppModes> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts an {@link AppModes} enum constant to its corresponding string
     * representation using the {@link LanguageTextRetrieverApi}.
     *
     * @param object The {@link AppModes} enum constant to be converted to a string.
     * @return The string representation of the provided {@link AppModes} constant.
     */
    @Override
    public String toString(AppModes object) {
        return switch (object) {
            case ADD_CUSTOM_TEXT -> languageTextRetriever.getString(TextKeys.MODE_ADD_CUSTOM_TEXT);
            case CHANGE_CASE -> languageTextRetriever.getString(TextKeys.MODE_CHANGE_CASE);
            case USE_DATETIME -> languageTextRetriever.getString(TextKeys.MODE_DATETIME);
            case USE_IMAGE_DIMENSIONS -> languageTextRetriever.getString(TextKeys.MODE_IMG_VID_DIMENSIONS);
            case USE_PARENT_FOLDER_NAME -> languageTextRetriever.getString(TextKeys.MODE_PARENT_FOLDERS);
            case REMOVE_CUSTOM_TEXT -> languageTextRetriever.getString(TextKeys.MODE_REMOVE_TEXT);
            case REPLACE_CUSTOM_TEXT -> languageTextRetriever.getString(TextKeys.MODE_REPLACE_TEXT);
            case ADD_SEQUENCE -> languageTextRetriever.getString(TextKeys.MODE_USE_DIGITAL_SEQUENCE);
            case TRUNCATE_FILE_NAME -> languageTextRetriever.getString(TextKeys.MODE_TRUNCATE);
            case CHANGE_EXTENSION -> languageTextRetriever.getString(TextKeys.MODE_CHANGE_EXTENSION);
        };
    }

    /**
     * Converts a string back to an {@link AppModes} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string The string to be converted back to an {@link AppModes} enum constant.
     * @return The corresponding {@link AppModes} enum constant, or {@code null} if not implemented.
     */
    @Override
    public AppModes fromString(String string) {
        return null;
    }

}
