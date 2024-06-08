package ua.renamer.app.core.service.mapper.impl.metadata.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.Set;

@Slf4j
public class JpegMapper extends ImageBaseMapper {

    @Inject
    public JpegMapper(
            FilesOperations filesOperations,
            DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Jpeg.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return JpegDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return JpegDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return JpegDirectory.TAG_IMAGE_HEIGHT;
    }

}
