package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.webp.WebpDirectory;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;

public class WebPFileMetadataExtractor extends BaseImageMetadataExtractor {

    public WebPFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return WebpDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return WebpDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return WebpDirectory.TAG_IMAGE_HEIGHT;
    }
}
