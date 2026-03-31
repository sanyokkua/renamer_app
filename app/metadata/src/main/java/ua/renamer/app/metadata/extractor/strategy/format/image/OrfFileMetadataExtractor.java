package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;

/**
 * Metadata extractor for Olympus ORF (Olympus Raw Format) camera files.
 * ORF files contain EXIF metadata with datetime information.
 */
public class OrfFileMetadataExtractor extends BaseImageMetadataExtractor {

    public OrfFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
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
