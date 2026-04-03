package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.gif.GifHeaderDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

public class GifFileMetadataExtractor extends BaseImageMetadataExtractor {

    @Inject
    public GifFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return GifHeaderDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return GifHeaderDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return GifHeaderDirectory.TAG_IMAGE_HEIGHT;
    }
}
