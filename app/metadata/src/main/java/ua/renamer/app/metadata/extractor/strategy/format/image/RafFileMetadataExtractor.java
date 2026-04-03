package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.file.FileTypeDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for Fujifilm RAF (Raw image File) camera files.
 * RAF files contain EXIF metadata with datetime information.
 */
public class RafFileMetadataExtractor extends BaseImageMetadataExtractor {

    @Inject
    public RafFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
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
