package ua.renamer.app.core.mappers.video;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.avi.AviDirectory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
public class AviMapper extends VideoBaseMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Avi.getAllExtensions());
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

}
