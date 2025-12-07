package ua.renamer.app.core.v2.interfaces;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;

public interface FileUtils {
    void validateFile(File file);

    BasicFileAttributes getBasicFileAttributes(Path path);

    String getFileBaseName(Path path);

    String getFileAbsolutePath(Path path);

    String getFileExtension(Path path);

    String getFileMimeType(Path path);

    LocalDateTime getFileCreationDate(BasicFileAttributes attributes);

    LocalDateTime getFileModificationDate(BasicFileAttributes attributes);
}
