package ua.renamer.app.core.service.mapper.impl.metadata.images;

import com.drew.imaging.FileType;
import com.drew.metadata.Directory;
import com.drew.metadata.pcx.PcxDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.util.Set;

@Slf4j
public class PcxMapper extends ImageBaseMapper {

    @Inject
    public PcxMapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations, dateTimeOperations);
    }

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Pcx.getAllExtensions());
    }

    @Override
    protected Class<? extends Directory> getBaseDirectoryClass() {
        return PcxDirectory.class;
    }

    @Override
    protected Integer getBaseWidthTag() {
        return PcxDirectory.TAG_HSCR_SIZE;
    }

    @Override
    protected Integer getBaseHeightTag() {
        return PcxDirectory.TAG_VSCR_SIZE;
    }

}
