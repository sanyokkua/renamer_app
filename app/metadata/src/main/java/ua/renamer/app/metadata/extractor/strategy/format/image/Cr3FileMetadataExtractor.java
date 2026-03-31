package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;

/**
 * Metadata extractor for Canon CR3 (Canon Raw 3) camera files.
 * CR3 files contain EXIF metadata with datetime information.
 */
public class Cr3FileMetadataExtractor extends BaseImageMetadataExtractor {

    public Cr3FileMetadataExtractor(DateTimeUtils dateTimeUtils) {
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
