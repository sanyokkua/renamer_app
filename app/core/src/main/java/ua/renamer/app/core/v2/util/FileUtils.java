package ua.renamer.app.core.v2.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import ua.renamer.app.core.v2.exception.FileAttributesReadException;
import ua.renamer.app.core.v2.exception.FileNotFoundException;
import ua.renamer.app.core.v2.exception.MimeTypeNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;

public class FileUtils {
    private static final ThreadLocal<Tika> TIKA_INSTANCE = ThreadLocal.withInitial(Tika::new);

    public static void validateFile(File file) {
        if (file == null) {
            throw new NullPointerException("File is null");
        }

        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }

    public static BasicFileAttributes getBasicFileAttributes(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new FileAttributesReadException(String.format("Failed to read attributes for file: %s", path), e);
        }
    }

    public static String getFileBaseName(Path path) {
        return FilenameUtils.getBaseName(path.getFileName().toString());
    }

    public static String getFileAbsolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    public static String getFileExtension(Path path) {
        return FilenameUtils.getExtension(path.getFileName().toString());
    }

    public static String getFileMimeType(Path path) {
        try {
            return TIKA_INSTANCE.get().detect(path);
        } catch (IOException e) {
            throw new MimeTypeNotFoundException(String.format("Failed to detect MIME type for: %s", path), e);
        }
    }

    public static LocalDateTime getFileCreationDate(BasicFileAttributes attributes) {
        FileTime creationTime = null;
        try {
            creationTime = attributes.creationTime();
        } catch (UnsupportedOperationException e) {
            // Creation time not supported on this platform (common on Linux)
        }

        return TimeUtils.toLocalDateTime(creationTime);
    }

    public static LocalDateTime getFileModificationDate(BasicFileAttributes attributes) {
        FileTime modificationTime = null;
        try {
            modificationTime = attributes.lastModifiedTime();
        } catch (UnsupportedOperationException e) {
            // Modification time not supported on this platform (common on Linux)
        }

        return TimeUtils.toLocalDateTime(modificationTime);
    }
}
