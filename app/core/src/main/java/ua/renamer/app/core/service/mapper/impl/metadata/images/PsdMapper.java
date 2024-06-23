package ua.renamer.app.core.service.mapper.impl.metadata.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.photoshop.PsdHeaderDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.Set;

@Slf4j
public class PsdMapper extends ImageBaseMapper {

    @Inject
    public PsdMapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(FileType.Psd.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return PsdHeaderDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return PsdHeaderDirectory.TAG_IMAGE_WIDTH;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return PsdHeaderDirectory.TAG_IMAGE_HEIGHT;
    }

}
