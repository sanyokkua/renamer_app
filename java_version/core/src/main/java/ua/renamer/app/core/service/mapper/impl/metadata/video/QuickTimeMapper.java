package ua.renamer.app.core.service.mapper.impl.metadata.video;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeMediaDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.mp4.media.Mp4MediaDirectory;
import com.drew.metadata.mp4.media.Mp4VideoDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.List;
import java.util.Set;

@Slf4j
public class QuickTimeMapper extends VideoBaseMapper {

    @Inject
    public QuickTimeMapper(
            FilesOperations filesOperations,
            DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.QuickTime.getAllExtensions());
    }

    @Override
    protected List<Class<? extends Directory>> getAvailableDirectories() {
        return List.of(QuickTimeDirectory.class,
                       QuickTimeMediaDirectory.class,
                       QuickTimeVideoDirectory.class,
                       Mp4VideoDirectory.class,
                       Mp4MediaDirectory.class
                      );
    }

    @Override
    protected List<Integer> getContentCreationTags() {
        return List.of(QuickTimeDirectory.TAG_CREATION_TIME,
                       QuickTimeDirectory.TAG_MODIFICATION_TIME,
                       Mp4MediaDirectory.TAG_CREATION_TIME
                      );
    }

    @Override
    protected List<Integer> getVideoWidthTags() {
        return List.of(QuickTimeVideoDirectory.TAG_WIDTH, Mp4VideoDirectory.TAG_WIDTH);
    }

    @Override
    protected List<Integer> getVideoHeightTags() {
        return List.of(QuickTimeVideoDirectory.TAG_HEIGHT, Mp4VideoDirectory.TAG_HEIGHT);
    }

}
