package ua.renamer.app.core.v2.mapper.strategy.format;

import ua.renamer.app.core.v2.interfaces.FileMetadataExtractor;
import ua.renamer.app.core.v2.model.meta.FileMeta;

import java.io.File;

public class JpegFileMetadataExtractor implements FileMetadataExtractor {
    @Override
    public FileMeta extract(File file, String mimeType) {
        return null;
    }
}
