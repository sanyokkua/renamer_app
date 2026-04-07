package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for TIFF image files.
 */
public class TiffFileMetadataExtractor extends BaseImageMetadataExtractor {

    /**
     * @param dateTimeUtils provides date/time parsing utilities
     */
    @Inject
    public TiffFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return ExifIFD0Directory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return ExifDirectoryBase.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return ExifDirectoryBase.TAG_IMAGE_HEIGHT;
    }
}
