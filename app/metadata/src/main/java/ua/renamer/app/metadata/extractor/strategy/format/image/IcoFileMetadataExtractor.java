package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.ico.IcoDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for ICO (icon) image files.
 */
public class IcoFileMetadataExtractor extends BaseImageMetadataExtractor {

    /**
     * @param dateTimeUtils provides date/time parsing utilities
     */
    @Inject
    public IcoFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return IcoDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return IcoDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return IcoDirectory.TAG_IMAGE_HEIGHT;
    }
}
