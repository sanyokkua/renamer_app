package ua.renamer.app.core.v2.interfaces;

import ua.renamer.app.core.v2.model.meta.FileMeta;

import java.io.File;

public interface FileMetadataExtractor {
    FileMeta extract(File file, String mimeType);
}
