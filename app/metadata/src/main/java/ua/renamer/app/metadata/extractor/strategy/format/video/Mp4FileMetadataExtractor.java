package ua.renamer.app.metadata.extractor.strategy.format.video;

import com.drew.metadata.Directory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeMediaDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.mp4.media.Mp4MediaDirectory;
import com.drew.metadata.mp4.media.Mp4VideoDirectory;
import jakarta.inject.Inject;
import ua.renamer.app.api.interfaces.DateTimeUtils;

import java.util.List;

public class Mp4FileMetadataExtractor extends BaseVideoMetadataExtractor {

    @Inject
    public Mp4FileMetadataExtractor(DateTimeUtils dateTimeUtils) {
        super(dateTimeUtils);
    }

    @Override
    protected List<Class<? extends Directory>> getAvailableDirectories() {
        return List.of(Mp4VideoDirectory.class,
                Mp4MediaDirectory.class,
                QuickTimeDirectory.class,
                QuickTimeMediaDirectory.class,
                QuickTimeVideoDirectory.class);
    }

    @Override
    protected List<Integer> getContentCreationTags() {
        return List.of(Mp4MediaDirectory.TAG_CREATION_TIME,
                QuickTimeDirectory.TAG_CREATION_TIME,
                QuickTimeDirectory.TAG_MODIFICATION_TIME);
    }

    @Override
    protected List<Integer> getVideoWidthTags() {
        return List.of(Mp4VideoDirectory.TAG_WIDTH, QuickTimeVideoDirectory.TAG_WIDTH);
    }

    @Override
    protected List<Integer> getVideoHeightTags() {
        return List.of(Mp4VideoDirectory.TAG_HEIGHT, QuickTimeVideoDirectory.TAG_HEIGHT);
    }

    @Override
    protected List<Integer> getDurationTags() {
        return List.of(Mp4MediaDirectory.TAG_DURATION, QuickTimeMediaDirectory.TAG_DURATION);
    }
}
