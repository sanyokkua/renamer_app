package ua.renamer.app.core.mappers.video;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeMediaDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.mp4.media.Mp4MediaDirectory;
import com.drew.metadata.mp4.media.Mp4VideoDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
public class Mp4Mapper extends VideoBaseMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Mp4.getAllExtensions());
    }

    @Override
    protected List<Class<? extends Directory>> getAvailableDirectories() {
        return List.of(Mp4VideoDirectory.class,
                       Mp4MediaDirectory.class,
                       QuickTimeDirectory.class,
                       QuickTimeMediaDirectory.class,
                       QuickTimeVideoDirectory.class
                      );
    }

    @Override
    protected List<Integer> getContentCreationTags() {
        return List.of(Mp4MediaDirectory.TAG_CREATION_TIME,
                       QuickTimeDirectory.TAG_CREATION_TIME,
                       QuickTimeDirectory.TAG_MODIFICATION_TIME
                      );
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
