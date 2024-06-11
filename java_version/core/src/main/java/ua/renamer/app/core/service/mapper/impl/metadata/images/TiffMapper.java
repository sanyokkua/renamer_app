package ua.renamer.app.core.service.mapper.impl.metadata.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.Set;

@Slf4j
public class TiffMapper extends ImageBaseMapper {

    @Inject
    public TiffMapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Tiff.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return null;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return null;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return null;
    }

}
