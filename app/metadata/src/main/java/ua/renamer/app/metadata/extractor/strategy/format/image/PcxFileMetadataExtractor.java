package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.metadata.Directory;
import com.drew.metadata.pcx.PcxDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

/**
 * Metadata extractor for PCX image files.
 */
public class PcxFileMetadataExtractor extends BaseImageMetadataExtractor {

    /**
     * @param dateTimeUtils provides date/time parsing utilities
     */
    @Inject
    public PcxFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return PcxDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return PcxDirectory.TAG_XMAX;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return PcxDirectory.TAG_YMAX;
    }
}
