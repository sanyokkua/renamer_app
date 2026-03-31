package ua.renamer.app.metadata.extractor.strategy;

import ua.renamer.app.metadata.interfaces.FileMetadataExtractor;
import ua.renamer.app.metadata.model.meta.FileMeta;

import java.io.File;

public class GenericFileMetadataExtractor implements FileMetadataExtractor {

    @Override
    public FileMeta extract(File file, String mimeType) {
        return FileMeta.empty();
    }
}
