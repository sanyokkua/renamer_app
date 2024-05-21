package ua.renamer.app.ui.converters;

import javafx.util.StringConverter;
import ua.renamer.app.core.enums.SortSource;
import ua.renamer.app.core.lang.LanguageManager;
import ua.renamer.app.core.lang.TextKeys;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link SortSource} and their corresponding string representations.
 */
public class SortSourceConverter extends StringConverter<SortSource> {

    /**
     * Converts a {@link SortSource} enum constant to its corresponding string
     * representation using {@link LanguageManager}.
     *
     * @param object the {@link SortSource} enum constant to be converted to a string.
     * @return the string representation of the provided {@link SortSource} constant.
     */
    @Override
    public String toString(SortSource object) {
        return switch (object) {
            case FILE_NAME -> LanguageManager.getString(TextKeys.FILE_SORTING_SOURCE_FILE_NAME);
            case FILE_PATH -> LanguageManager.getString(TextKeys.FILE_SORTING_SOURCE_FILE_PATH);
            case FILE_SIZE -> LanguageManager.getString(TextKeys.FILE_SORTING_SOURCE_FILE_SIZE);
            case FILE_CREATION_DATETIME ->
                    LanguageManager.getString(TextKeys.FILE_SORTING_SOURCE_FILE_CREATION_DATETIME);
            case FILE_MODIFICATION_DATETIME ->
                    LanguageManager.getString(TextKeys.FILE_SORTING_SOURCE_FILE_MODIFICATION_DATETIME);
            case FILE_CONTENT_CREATION_DATETIME ->
                    LanguageManager.getString(TextKeys.FILE_SORTING_SOURCE_FILE_CONTENT_CREATION_DATETIME);
            case IMAGE_WIDTH -> LanguageManager.getString(TextKeys.FILE_SORTING_SOURCE_IMG_VID_WIDTH);
            case IMAGE_HEIGHT -> LanguageManager.getString(TextKeys.FILE_SORTING_SOURCE_IMG_VID_HEIGHT);
        };
    }

    /**
     * Converts a string back to a {@link SortSource} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to a {@link SortSource} enum constant.
     * @return the corresponding {@link SortSource} enum constant, or {@code null} if not implemented.
     */
    @Override
    public SortSource fromString(String string) {
        return null;
    }

}
