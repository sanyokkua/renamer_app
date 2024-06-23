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
public class Mp4Mapper extends VideoBaseMapper {

    @Inject
    public Mp4Mapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(FileType.Mp4.getAllExtensions());
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

}
