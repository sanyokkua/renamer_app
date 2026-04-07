package ua.renamer.app.api.interfaces;

import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;

/**
 * Port interface for category-specific metadata extraction strategies.
 * Each implementation handles a specific file category (image, audio, video, generic).
 */
@FunctionalInterface
public interface FileMetadataExtractor {

    /**
     * Extracts metadata from the given file.
     *
     * @param file     the file to extract metadata from; must not be null
     * @param mimeType the detected MIME type of the file; must not be null
     * @return the extracted metadata; never null; errors are captured inside the returned object
     */
    FileMeta extract(File file, String mimeType);
}
