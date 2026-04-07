package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for Canon CR2 (Canon Raw 2) camera files.
 * CR2 files contain EXIF metadata with datetime information.
 */
public class Cr2FileMetadataExtractor extends BaseImageMetadataExtractor {

    /**
     * @param dateTimeUtils provides date/time parsing utilities
     */
    @Inject
    public Cr2FileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        // CR2 uses EXIF directories
        return FileTypeDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return null; // Use EXIF fallback
    }

    @Override
    protected Integer getBaseHeightTag() {
        return null; // Use EXIF fallback
    }

}
