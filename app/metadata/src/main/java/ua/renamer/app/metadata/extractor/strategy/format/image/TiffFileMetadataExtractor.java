package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.exif.ExifIFD0Directory;
import ua.renamer.app.api.interfaces.DateTimeUtils;

public class TiffFileMetadataExtractor extends BaseImageMetadataExtractor {

    public TiffFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return ExifIFD0Directory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return ExifIFD0Directory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return ExifIFD0Directory.TAG_IMAGE_HEIGHT;
    }
}
