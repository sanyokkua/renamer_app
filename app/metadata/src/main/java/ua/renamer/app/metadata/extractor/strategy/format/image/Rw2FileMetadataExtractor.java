package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for Panasonic RW2 (Raw version 2) camera files.
 * RW2 files contain EXIF metadata with datetime information.
 */
public class Rw2FileMetadataExtractor extends BaseImageMetadataExtractor {

    public Rw2FileMetadataExtractor(DateTimeUtils dateTimeUtils) {
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
