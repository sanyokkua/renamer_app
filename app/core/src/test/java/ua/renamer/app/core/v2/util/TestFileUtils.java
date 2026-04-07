package ua.renamer.app.core.v2.util;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.exception.FileAttributesReadException;
import ua.renamer.app.api.exception.FileNotFoundException;
import ua.renamer.app.api.interfaces.DateTimeUtils;
import ua.renamer.app.api.interfaces.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;

/**
 * Test-only implementation of FileUtils backed by java.nio.
 * Used in core integration tests instead of the real CommonFileUtils (which lives in metadata).
 */
@Slf4j
public class TestFileUtils implements FileUtils {

    private final DateTimeUtils dateTimeUtils;

    /**
     * Creates a new TestFileUtils backed by the given date-time utility.
     *
     * @param dateTimeUtils the date-time utility to use for file timestamp conversion; must not be null
     */
    public TestFileUtils(DateTimeUtils dateTimeUtils) {
        this.dateTimeUtils = dateTimeUtils;
    }

    @Override
    public void validateFile(File file) {
        if (file == null) {
            throw new NullPointerException("File is null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }

    @Override
    public BasicFileAttributes getBasicFileAttributes(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new FileAttributesReadException("Failed to read attributes for: " + path, e);
        }
    }

    @Override
    public String getFileBaseName(Path path) {
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    @Override
    public String getFileAbsolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    @Override
    public String getFileExtension(Path path) {
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }

    @Override
    public String getFileMimeType(Path path) {
        try {
            String probedType = Files.probeContentType(path);
            return probedType != null ? probedType : "application/octet-stream";
        } catch (IOException e) {
            log.debug("Failed to probe content type for '{}', falling back to application/octet-stream. Exception: {}", path, e.getMessage());
            return "application/octet-stream";
        }
    }

    @Override
    public LocalDateTime getFileCreationDate(BasicFileAttributes attributes) {
        FileTime creationTime = null;
        try {
            creationTime = attributes.creationTime();
        } catch (UnsupportedOperationException e) {
            log.debug("Creation time not supported by file system on this platform, returning null. Exception: {}", e.getMessage());
        }
        return dateTimeUtils.toLocalDateTime(creationTime);
    }

    @Override
    public LocalDateTime getFileModificationDate(BasicFileAttributes attributes) {
        FileTime modificationTime = null;
        try {
            modificationTime = attributes.lastModifiedTime();
        } catch (UnsupportedOperationException e) {
            log.debug("Modification time not supported by file system on this platform, returning null. Exception: {}", e.getMessage());
        }
        return dateTimeUtils.toLocalDateTime(modificationTime);
    }
}
