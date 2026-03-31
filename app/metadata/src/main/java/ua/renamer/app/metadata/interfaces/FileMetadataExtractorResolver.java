package ua.renamer.app.metadata.interfaces;

import ua.renamer.app.metadata.enums.Category;

public interface FileMetadataExtractorResolver {
    FileMetadataExtractor getFileMetadataExtractor(Category category);
}
