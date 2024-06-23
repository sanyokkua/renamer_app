package ua.renamer.app.core.service.mapper.impl.metadata;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;

import java.io.File;
import java.util.Set;

@Slf4j
public class NullMapper extends FileToMetadataMapper {

    @Inject
    public NullMapper(FilesOperations filesOperations) {
        super(filesOperations);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of();
    }

    @Override
    public FileInformationMetadata process(File input) {
        return null;
    }

}
