package ua.renamer.app.metadata.extractor.strategy.format.video;

import com.drew.metadata.Directory;
import com.drew.metadata.avi.AviDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

import java.util.List;

/**
 * Metadata extractor for AVI video files.
 */
public class AviFileMetadataExtractor extends BaseVideoMetadataExtractor {

    /**
     * @param dateTimeUtils provides date/time parsing utilities
     */
    @Inject
    public AviFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected List<Class<? extends Directory>> getAvailableDirectories() {
        return List.of(AviDirectory.class);
    }

    @Override
    protected List<Integer> getContentCreationTags() {
        return List.of(AviDirectory.TAG_DATETIME_ORIGINAL);
    }

    @Override
    protected List<Integer> getVideoWidthTags() {
        return List.of(AviDirectory.TAG_WIDTH);
    }

    @Override
    protected List<Integer> getVideoHeightTags() {
        return List.of(AviDirectory.TAG_HEIGHT);
    }

    @Override
    protected List<Integer> getDurationTags() {
        return List.of(AviDirectory.TAG_DURATION);
    }
}
