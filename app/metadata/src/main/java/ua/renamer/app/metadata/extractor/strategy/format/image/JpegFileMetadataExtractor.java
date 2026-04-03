package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

public class JpegFileMetadataExtractor extends BaseImageMetadataExtractor {

    @Inject
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
