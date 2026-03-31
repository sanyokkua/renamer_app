package ua.renamer.app.api.interfaces;

import ua.renamer.app.api.enums.Category;

/**
 * Port interface for resolving the appropriate {@link FileMetadataExtractor} by file category.
 */
public interface FileMetadataExtractorResolver {

    /**
     * Returns the metadata extractor appropriate for the given category.
     *
     * @param category the file category; must not be null
     * @return the extractor for the category; never null
     */
    FileMetadataExtractor getFileMetadataExtractor(Category category);
}
