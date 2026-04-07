package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for Adobe DNG (Digital Negative) camera files.
 * DNG is an open standard RAW format that contains EXIF metadata with datetime information.
 */
public class DngFileMetadataExtractor extends BaseImageMetadataExtractor {

    /**
     * @param dateTimeUtils provides date/time parsing utilities
     */
    @Inject
    public DngFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return FileTypeDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return null;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return null;
    }

}
