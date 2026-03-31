package ua.renamer.app.metadata.util;

import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class CommonFileUtils implements FileUtils {
    private static final ThreadLocal<Tika> TIKA_INSTANCE = ThreadLocal.withInitial(Tika::new);
    private final DateTimeUtils dateTimeUtils;

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
            // Creation time not supported on this platform (common on Linux)
        }

        return dateTimeUtils.toLocalDateTime(creationTime);
    }

    @Override
    public LocalDateTime getFileModificationDate(BasicFileAttributes attributes) {
        FileTime modificationTime = null;
        try {
            modificationTime = attributes.lastModifiedTime();
        } catch (UnsupportedOperationException e) {
            // Modification time not supported on this platform (common on Linux)
        }

        return dateTimeUtils.toLocalDateTime(modificationTime);
    }
}
