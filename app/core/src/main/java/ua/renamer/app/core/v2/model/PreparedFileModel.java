package ua.renamer.app.core.v2.model;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Immutable model representing a file after transformation has been applied.
 * Contains both the original file information and the calculated new name.
 */
@Value
@Builder(setterPrefix = "with", toBuilder = true)
public class PreparedFileModel {
    /**
     * Original file model with metadata.
     */
    FileModel originalFile;

    /**
     * Calculated new name (without extension).
     */
    String newName;

    /**
     * Calculated new extension (without dot).
     */
    String newExtension;

    /**
     * Whether an error occurred during transformation.
     */
    boolean hasError;

    /**
     * Error message if transformation failed.
     */
    String errorMessage;

    /**
     * Metadata about the transformation that was applied.
     */
    TransformationMetadata transformationMeta;

    /**
     * Get the original full filename (name + extension).
     */
    public String getOldFullName() {
        return originalFile.getName() + "." + originalFile.getExtension();
    }

    /**
     * Get the new full filename (name + extension).
     */
    public String getNewFullName() {
        return newExtension.isEmpty()
                ? newName
                : newName + "." + newExtension;
    }

    /**
     * Check if the file needs to be renamed.
     * Returns false if there's an error or if old and new names are identical.
     */
    public boolean needsRename() {
        return !hasError && !getOldFullName().equals(getNewFullName());
    }

    /**
     * Get the path to the original file.
     */
    public Path getOldPath() {
        return originalFile.getFile().toPath();
    }

    /**
     * Get the path where the file should be renamed to.
     */
    public Path getNewPath() {
        return originalFile.getFile().toPath()
                           .resolveSibling(getNewFullName());
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
