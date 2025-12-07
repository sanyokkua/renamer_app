package ua.renamer.app.core.v2.interfaces;

import ua.renamer.app.core.v2.model.Category;

public interface FileMetadataExtractorResolver {
    FileMetadataExtractor getFileMetadataExtractor(Category category);
}
