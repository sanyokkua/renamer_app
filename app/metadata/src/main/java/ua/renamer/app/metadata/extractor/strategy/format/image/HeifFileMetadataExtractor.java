package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.heif.HeifDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

public class HeifFileMetadataExtractor extends BaseImageMetadataExtractor {

    @Inject
    public HeifFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return HeifDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return HeifDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return HeifDirectory.TAG_IMAGE_HEIGHT;
    }
}
