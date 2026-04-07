package ua.renamer.app.metadata.util;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import ua.renamer.app.api.exception.FileAttributesReadException;
import ua.renamer.app.api.exception.FileNotFoundException;
import ua.renamer.app.api.exception.MimeTypeNotFoundException;
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
 * Implements {@link FileUtils} using Apache Tika for MIME detection and {@code java.nio} for attribute reads.
 */
@Slf4j
public class CommonFileUtils implements FileUtils {
    private static final ThreadLocal<Tika> TIKA_INSTANCE = ThreadLocal.withInitial(Tika::new);
    private final DateTimeUtils dateTimeUtils;

    @Inject
    /**
     * @param dateTimeUtils used to convert file timestamps to {@link java.time.LocalDateTime}
     */
    public CommonFileUtils(DateTimeUtils dateTimeUtils) {
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
            throw new FileAttributesReadException(String.format("Failed to read attributes for file: %s", path), e);
        }
    }

    @Override
    public String getFileBaseName(Path path) {
        return ua.renamer.app.utils.file.FileUtils.getFileBaseName(path);
    }

    @Override
    public String getFileAbsolutePath(Path path) {
        return ua.renamer.app.utils.file.FileUtils.getFileAbsolutePath(path);
    }

    @Override
    public String getFileExtension(Path path) {
        return ua.renamer.app.utils.file.FileUtils.getFileExtension(path);
    }

    @Override
    public String getFileMimeType(Path path) {
        try {
            return TIKA_INSTANCE.get().detect(path);
        } catch (IOException e) {
            throw new MimeTypeNotFoundException(String.format("Failed to detect MIME type for: %s", path), e);
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
