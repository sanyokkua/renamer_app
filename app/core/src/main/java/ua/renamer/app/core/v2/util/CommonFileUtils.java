package ua.renamer.app.core.v2.util;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import ua.renamer.app.core.v2.exception.FileAttributesReadException;
import ua.renamer.app.core.v2.exception.FileNotFoundException;
import ua.renamer.app.core.v2.exception.MimeTypeNotFoundException;
import ua.renamer.app.core.v2.interfaces.DateTimeUtils;
import ua.renamer.app.core.v2.interfaces.FileUtils;

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
        String fileName = path.getFileName().toString();

        // Handle hidden files (files starting with .)
        if (fileName.startsWith(".")) {
            int lastDotIndex = fileName.lastIndexOf('.');
            // If it's just a dot or only one dot at the start, the whole name is the base name
            if (lastDotIndex == 0) {
                return fileName;
            }
            // If there are multiple dots, return everything up to the last dot
            return fileName.substring(0, lastDotIndex);
        }

        return FilenameUtils.getBaseName(fileName);
    }

    @Override
    public String getFileAbsolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    @Override
    public String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();

        // Handle hidden files (files starting with .)
        if (fileName.startsWith(".")) {
            int lastDotIndex = fileName.lastIndexOf('.');
            // If there's only one dot at the start, there's no extension
            if (lastDotIndex == 0) {
                return "";
            }
            // Return the part after the last dot
            return fileName.substring(lastDotIndex + 1);
        }

        return FilenameUtils.getExtension(fileName);
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
