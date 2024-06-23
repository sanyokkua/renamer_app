package ua.renamer.app.core.service.mapper.impl.metadata.video;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.avi.AviDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.List;
import java.util.Set;

@Slf4j
public class AviMapper extends VideoBaseMapper {

    @Inject
    public AviMapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    public Set<String> getSupportedExtensions() {
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
