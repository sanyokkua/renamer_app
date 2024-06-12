package ua.renamer.app.core.service.mapper.impl.metadata.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.gif.GifImageDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.Set;

@Slf4j
public class GifMapper extends ImageBaseMapper {

    @Inject
    public GifMapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(FileType.Gif.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return GifImageDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return GifImageDirectory.TAG_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return GifImageDirectory.TAG_HEIGHT;
    }

}
