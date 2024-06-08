package ua.renamer.app.core.service.mapper.impl.metadata.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.png.PngDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.Set;

@Slf4j
public class PngMapper extends ImageBaseMapper {

    @Inject
    public PngMapper(
            FilesOperations filesOperations,
            DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Png.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return PngDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return PngDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return PngDirectory.TAG_IMAGE_HEIGHT;
    }

}
