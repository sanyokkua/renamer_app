package ua.renamer.app.core.v2.mapper.strategy;

import ua.renamer.app.core.v2.interfaces.FileMetadataExtractor;
import ua.renamer.app.core.v2.model.meta.FileMeta;

import java.io.File;

public class ImageFileMetadataExtractionExtractor implements FileMetadataExtractor {

    @Override
    public FileMeta extract(File file, String mimeType) {
        return null;
    }
}
