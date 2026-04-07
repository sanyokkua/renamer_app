package ua.renamer.app.api.interfaces;

import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;

/**
 * Port interface for orchestrating metadata extraction from a file.
 * Delegates to the appropriate {@link FileMetadataExtractor} via a resolver.
 */
@FunctionalInterface
public interface FileMetadataMapper {

    /**
     * Extracts metadata from the given file using the appropriate strategy for the category.
     *
     * @param file     the file to extract metadata from; must not be null
     * @param category the detected category of the file; must not be null
     * @param mimeType the detected MIME type of the file; must not be null
     * @return the extracted metadata; never null; errors are captured inside the returned object
     */
    FileMeta extract(File file, Category category, String mimeType);
}
