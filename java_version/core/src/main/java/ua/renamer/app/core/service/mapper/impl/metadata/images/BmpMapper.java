package ua.renamer.app.core.service.mapper.impl.metadata.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.Set;

@Slf4j
public class BmpMapper extends ImageBaseMapper {

    @Inject
    public BmpMapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Bmp.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return BmpHeaderDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return BmpHeaderDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return BmpHeaderDirectory.TAG_IMAGE_HEIGHT;
    }

}
