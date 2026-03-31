package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.eps.EpsDirectory;
import ua.renamer.app.api.interfaces.DateTimeUtils;

public class EpsFileMetadataExtractor extends BaseImageMetadataExtractor {

    public EpsFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return EpsDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return EpsDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return EpsDirectory.TAG_IMAGE_HEIGHT;
    }
}
