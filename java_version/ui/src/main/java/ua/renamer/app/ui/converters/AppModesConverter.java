package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.AppModes;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link AppModes} and their corresponding string representations.
 */
public class AppModesConverter extends StringConverter<AppModes> {

    /**
     * Converts an {@link AppModes} enum constant to its corresponding string
     * representation using the {@link LanguageManager}.
     *
     * @param object the {@link AppModes} enum constant to be converted to a string.
     * @return the string representation of the provided {@link AppModes} constant.
     */
    @Override
    public String toString(AppModes object) {
        return switch (object) {
            case ADD_CUSTOM_TEXT -> LanguageManager.getString(TextKeys.MODE_ADD_CUSTOM_TEXT);
            case CHANGE_CASE -> LanguageManager.getString(TextKeys.MODE_CHANGE_CASE);
            case USE_DATETIME -> LanguageManager.getString(TextKeys.MODE_DATETIME);
            case USE_IMAGE_DIMENSIONS -> LanguageManager.getString(TextKeys.MODE_IMG_VID_DIMENSIONS);
            case USE_PARENT_FOLDER_NAME -> LanguageManager.getString(TextKeys.MODE_PARENT_FOLDERS);
            case REMOVE_CUSTOM_TEXT -> LanguageManager.getString(TextKeys.MODE_REMOVE_TEXT);
            case REPLACE_CUSTOM_TEXT -> LanguageManager.getString(TextKeys.MODE_REPLACE_TEXT);
            case ADD_SEQUENCE -> LanguageManager.getString(TextKeys.MODE_USE_DIGITAL_SEQUENCE);
            case TRUNCATE_FILE_NAME -> LanguageManager.getString(TextKeys.MODE_TRUNCATE);
            case CHANGE_EXTENSION -> LanguageManager.getString(TextKeys.MODE_CHANGE_EXTENSION);
        };
    }

    /**
     * Converts a string back to an {@link AppModes} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to an {@link AppModes} enum constant.
     * @return the corresponding {@link AppModes} enum constant, or {@code null} if not implemented.
     */
    @Override
    public AppModes fromString(String string) {
        return null;
    }
}
