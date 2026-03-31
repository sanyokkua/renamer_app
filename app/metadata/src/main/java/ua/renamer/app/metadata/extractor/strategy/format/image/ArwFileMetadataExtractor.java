package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;

/**
 * Metadata extractor for Sony ARW (Sony Raw) camera files.
 * ARW files contain EXIF metadata with datetime information.
 */
public class ArwFileMetadataExtractor extends BaseImageMetadataExtractor {

    public ArwFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        // ARW uses EXIF directories, no specific ARW directory
        return FileTypeDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        // ARW metadata in EXIF, return null to use EXIF fallback
        return null;
    }

    @Override
    protected Integer getBaseHeightTag() {
        // ARW metadata in EXIF, return null to use EXIF fallback
        return null;
    }

}
