package ua.renamer.app.metadata.interfaces;

import ua.renamer.app.metadata.enums.Category;
import ua.renamer.app.metadata.model.meta.FileMeta;

import java.io.File;

public interface FileMetadataMapper {
    FileMeta extract(File file, Category category, String mimeType);
}
