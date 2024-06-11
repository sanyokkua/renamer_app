package ua.renamer.app.ui.converter;

import com.google.inject.Inject;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.ui.enums.TextKeys;
import ua.renamer.app.ui.service.LanguageTextRetrieverApi;

/**
 * A converter class that extends {@link StringConverter} to convert between
 * {@link ImageDimensionOptions} enum constants and their corresponding string representations.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ImageDimensionOptionsConverter extends StringConverter<ImageDimensionOptions> {

    private final LanguageTextRetrieverApi languageTextRetriever;

    /**
     * Converts an {@link ImageDimensionOptions} enum constant to its corresponding string
     * representation using the {@link LanguageTextRetrieverApi}.
     *
     * @param object the {@link ImageDimensionOptions} enum constant to be converted to a string.
     *
     * @return the string representation of the provided {@link ImageDimensionOptions} constant.
     */
    @Override
    public String toString(ImageDimensionOptions object) {
        return switch (object) {
            case DO_NOT_USE -> languageTextRetriever.getString(TextKeys.DO_NOT_USE);
            case WIDTH -> languageTextRetriever.getString(TextKeys.IMG_VID_DIMENSION_WIDTH);
            case HEIGHT -> languageTextRetriever.getString(TextKeys.IMG_VID_DIMENSION_HEIGHT);
        };
    }

    /**
     * Converts a string back to an {@link ImageDimensionOptions} enum constant.
     * Currently, this method is not implemented and always returns {@code null}.
     *
     * @param string the string to be converted back to an {@link ImageDimensionOptions} enum constant.
     *
     * @return the corresponding {@link ImageDimensionOptions} enum constant, or {@code null} if not implemented.
     */
    @Override
    public ImageDimensionOptions fromString(String string) {
        return null;
    }

}
