package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.png.PngDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for PNG image files.
 */
public class PngFileMetadataExtractor extends BaseImageMetadataExtractor {

    /**
     * @param dateTimeUtils provides date/time parsing utilities
     */
    @Inject
    public PngFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return PngDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return PngDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return PngDirectory.TAG_IMAGE_HEIGHT;
    }
}
