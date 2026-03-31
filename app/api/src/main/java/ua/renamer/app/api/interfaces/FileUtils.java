package ua.renamer.app.api.interfaces;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;

/**
 * Port interface for file system operations needed by the business pipeline.
 * Implementations live in the infrastructure (metadata) module.
 */
public interface FileUtils {

    /**
     * Validates that the file exists and is accessible.
     *
     * @param file the file to validate; must not be null
     * @throws NullPointerException if file is null
     * @throws ua.renamer.app.api.exception.FileNotFoundException if file does not exist
     */
    void validateFile(File file);

    /**
     * Reads basic file attributes for the given path.
     *
     * @param path the path to read attributes for; must not be null
     * @return the file attributes; never null
     * @throws ua.renamer.app.api.exception.FileAttributesReadException if attributes cannot be read
     */
    BasicFileAttributes getBasicFileAttributes(Path path);

    /**
     * Returns the base name (without extension) of the file at the given path.
     *
     * @param path the file path; must not be null
     * @return the base name; never null
     */
    String getFileBaseName(Path path);

    /**
     * Returns the absolute path string for the given path.
     *
     * @param path the file path; must not be null
     * @return the absolute path string; never null
     */
    String getFileAbsolutePath(Path path);

    /**
     * Returns the extension (without dot) of the file at the given path.
     *
     * @param path the file path; must not be null
     * @return the extension, or an empty string if none
     */
    String getFileExtension(Path path);

    /**
     * Detects the MIME type of the file at the given path.
     *
     * @param path the file path; must not be null
     * @return the detected MIME type string; never null
     * @throws ua.renamer.app.api.exception.MimeTypeNotFoundException if detection fails
     */
    String getFileMimeType(Path path);

    /**
     * Returns the creation date of the file, or a minimal fallback if not supported.
     *
     * @param attributes the file attributes; must not be null
     * @return the creation date; never null
     */
    LocalDateTime getFileCreationDate(BasicFileAttributes attributes);

    /**
     * Returns the last modification date of the file, or a minimal fallback if not supported.
     *
     * @param attributes the file attributes; must not be null
     * @return the modification date; never null
     */
    LocalDateTime getFileModificationDate(BasicFileAttributes attributes);
}
