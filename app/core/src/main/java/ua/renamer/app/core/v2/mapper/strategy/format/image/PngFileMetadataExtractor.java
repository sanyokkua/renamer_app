package ua.renamer.app.core.v2.mapper.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.png.PngDirectory;
import ua.renamer.app.core.v2.interfaces.DateTimeUtils;

public class PngFileMetadataExtractor extends BaseImageMetadataExtractor {

    public PngFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return PngDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return PngDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return PngDirectory.TAG_IMAGE_HEIGHT;
    }
}
