package ua.renamer.app.metadata.extractor.strategy.format.video;

import com.drew.metadata.Directory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeMediaDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

import java.util.List;

public class QuickTimeFileMetadataExtractor extends BaseVideoMetadataExtractor {

    @Inject
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
