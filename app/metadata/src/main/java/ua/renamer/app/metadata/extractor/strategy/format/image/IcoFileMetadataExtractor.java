package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.ico.IcoDirectory;
import ua.renamer.app.api.interfaces.DateTimeUtils;

public class IcoFileMetadataExtractor extends BaseImageMetadataExtractor {

    public IcoFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return IcoDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return IcoDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return IcoDirectory.TAG_IMAGE_HEIGHT;
    }
}
