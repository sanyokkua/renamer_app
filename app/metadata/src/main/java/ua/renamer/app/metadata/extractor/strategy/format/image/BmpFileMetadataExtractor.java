package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;

public class BmpFileMetadataExtractor extends BaseImageMetadataExtractor {

    public BmpFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return BmpHeaderDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return BmpHeaderDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return BmpHeaderDirectory.TAG_IMAGE_HEIGHT;
    }
}
