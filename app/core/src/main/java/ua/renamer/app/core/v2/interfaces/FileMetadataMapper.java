package ua.renamer.app.core.v2.interfaces;

import ua.renamer.app.core.v2.model.Category;
import ua.renamer.app.core.v2.model.meta.FileMeta;

import java.io.File;

public interface FileMetadataMapper {
    FileMeta extract(File file, Category category, String mimeType);
}
