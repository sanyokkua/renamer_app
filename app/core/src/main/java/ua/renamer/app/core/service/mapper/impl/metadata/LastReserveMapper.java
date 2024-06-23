package ua.renamer.app.core.service.mapper.impl.metadata;

import com.google.inject.Inject;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;

import java.io.File;
import java.util.Set;

public abstract class LastReserveMapper extends FileToMetadataMapper {

    @Inject
    protected LastReserveMapper(FilesOperations filesOperations) {
        super(filesOperations);
    }

    @Override
    public boolean canHandle(File input) {
        return true;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of();
    }
}
