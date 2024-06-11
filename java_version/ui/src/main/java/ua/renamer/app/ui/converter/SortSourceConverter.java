package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.SortSource;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link SortSource} enum constants and their corresponding string representations.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SortSourceConverter extends StringConverter<SortSource> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts a {@link SortSource} enum constant to its corresponding string representation.
     *
     * @param object The {@link SortSource} enum constant to be converted to a string.
     *
     * @return The string representation of the provided {@link SortSource} constant.
     */
    @Override
    public String toString(SortSource object) {
        // @formatter:off
        return switch (object) {
            case FILE_NAME -> languageTextRetriever.getString(TextKeys.FILE_SORTING_SOURCE_FILE_NAME);
            case FILE_PATH -> languageTextRetriever.getString(TextKeys.FILE_SORTING_SOURCE_FILE_PATH);
            case FILE_SIZE -> languageTextRetriever.getString(TextKeys.FILE_SORTING_SOURCE_FILE_SIZE);
            case FILE_CREATION_DATETIME -> languageTextRetriever.getString(TextKeys.FILE_SORTING_SOURCE_FILE_CREATION_DATETIME);
            case FILE_MODIFICATION_DATETIME -> languageTextRetriever.getString(TextKeys.FILE_SORTING_SOURCE_FILE_MODIFICATION_DATETIME);
            case FILE_CONTENT_CREATION_DATETIME -> languageTextRetriever.getString(TextKeys.FILE_SORTING_SOURCE_FILE_CONTENT_CREATION_DATETIME);
            case IMAGE_WIDTH -> languageTextRetriever.getString(TextKeys.FILE_SORTING_SOURCE_IMG_VID_WIDTH);
            case IMAGE_HEIGHT -> languageTextRetriever.getString(TextKeys.FILE_SORTING_SOURCE_IMG_VID_HEIGHT);
        };
        // @formatter:on
    }

    /**
     * Converts a string back to a {@link SortSource} enum constant.
     *
     * @param string The string to be converted back to a {@link SortSource} enum constant.
     *
     * @return The corresponding {@link SortSource} enum constant, or {@code null} if not implemented.
     *
     * @implNote This method is not yet implemented and always returns {@code null}.
     */
    @Override
    public SortSource fromString(String string) {
        return null;
    }

}
