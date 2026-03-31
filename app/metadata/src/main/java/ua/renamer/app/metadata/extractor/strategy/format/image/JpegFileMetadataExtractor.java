package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;

public class JpegFileMetadataExtractor extends BaseImageMetadataExtractor {

    public JpegFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return JpegDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return JpegDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return JpegDirectory.TAG_IMAGE_HEIGHT;
    }

}
