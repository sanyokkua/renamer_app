package ua.renamer.app.metadata.interfaces;

import ua.renamer.app.metadata.model.meta.FileMeta;

import java.io.File;

public interface FileMetadataExtractor {
    FileMeta extract(File file, String mimeType);
}
