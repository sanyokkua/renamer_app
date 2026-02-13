package ua.renamer.app.core.v2.model;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.core.v2.model.meta.FileMeta;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Value
@Builder(setterPrefix = "with")
public class FileModel {
    File file;
    boolean isFile;
    long fileSize;
    String name;
    String extension;
    String absolutePath;
    LocalDateTime creationDate;
    LocalDateTime modificationDate;

    String detectedMimeType;
    Set<String> detectedExtensions;
    Category category;
    FileMeta metadata;


    public Optional<LocalDateTime> getCreationDate() {
        return Optional.ofNullable(creationDate);
    }

    public Optional<LocalDateTime> getModificationDate() {
        return Optional.ofNullable(modificationDate);
    }

    public Optional<FileMeta> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    // Explicit getter for boolean field to ensure Lombok compatibility
    public boolean isFile() {
        return isFile;
    }
}
