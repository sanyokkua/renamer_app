package ua.renamer.app.core.mappers.images;

import ua.renamer.app.core.abstracts.FileToMetadataMapper;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.util.Set;

public class NullMapper extends FileToMetadataMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of();
    }

    @Override
    public FileInformationMetadata process(File input) {
        return null;
    }

}
