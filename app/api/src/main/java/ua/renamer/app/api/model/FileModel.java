package ua.renamer.app.api.model;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable model representing a file after metadata extraction.
 * Produced by the first phase of the rename pipeline.
 */
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
