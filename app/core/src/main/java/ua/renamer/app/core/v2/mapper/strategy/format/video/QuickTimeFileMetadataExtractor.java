package ua.renamer.app.core.v2.mapper.strategy.format.video;

import com.drew.metadata.Directory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeMediaDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import ua.renamer.app.core.v2.interfaces.DateTimeUtils;

import java.util.List;

public class QuickTimeFileMetadataExtractor extends BaseVideoMetadataExtractor {

    public QuickTimeFileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected List<Class<? extends Directory>> getAvailableDirectories() {
        return List.of(QuickTimeVideoDirectory.class,
                       QuickTimeMediaDirectory.class,
                       QuickTimeDirectory.class);
    }

    @Override
    protected List<Integer> getContentCreationTags() {
        return List.of(QuickTimeDirectory.TAG_CREATION_TIME, QuickTimeDirectory.TAG_MODIFICATION_TIME);
    }

    @Override
    protected List<Integer> getVideoWidthTags() {
        return List.of(QuickTimeVideoDirectory.TAG_WIDTH);
    }

    @Override
    protected List<Integer> getVideoHeightTags() {
        return List.of(QuickTimeVideoDirectory.TAG_HEIGHT);
    }

    @Override
    protected List<Integer> getDurationTags() {
        return List.of(QuickTimeMediaDirectory.TAG_DURATION);
    }
}
