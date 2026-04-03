package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for AVIF (AV1 Image File Format) images.
 * AVIF uses EXIF directories for metadata storage (via HEIF container format).
 */
public class AvifFileMetadataExtractor extends BaseImageMetadataExtractor {

    @Inject
    public AvifFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        // AVIF doesn't have a specific directory, relies on EXIF and FileTypeDirectory
        return FileTypeDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        // AVIF metadata typically in EXIF, return null to use EXIF fallback
        return null;
    }

    @Override
    protected Integer getBaseHeightTag() {
        // AVIF metadata typically in EXIF, return null to use EXIF fallback
        return null;
    }

}
