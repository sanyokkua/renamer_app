package ua.renamer.app.core.v2.mapper.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import ua.renamer.app.core.v2.interfaces.DateTimeUtils;

/**
 * Metadata extractor for Adobe DNG (Digital Negative) camera files.
 * DNG is an open standard RAW format that contains EXIF metadata with datetime information.
 */
public class DngFileMetadataExtractor extends BaseImageMetadataExtractor {

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
