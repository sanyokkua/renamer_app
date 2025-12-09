package ua.renamer.app.core.v2.mapper.strategy.format.video;

import com.drew.metadata.Directory;
import com.drew.metadata.avi.AviDirectory;
import ua.renamer.app.core.v2.interfaces.DateTimeUtils;

import java.util.List;

public class AviFileMetadataExtractor extends BaseVideoMetadataExtractor {

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
