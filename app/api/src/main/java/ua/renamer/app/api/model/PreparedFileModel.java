package ua.renamer.app.api.model;

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
     * Returns the original full filename (name + dot + extension).
     *
     * @return the original full filename; never null
     */
    public String getOldFullName() {
        String ext = originalFile.getExtension();
        return (ext == null || ext.isEmpty())
                ? originalFile.getName()
                : originalFile.getName() + "." + ext;
    }

    /**
     * Returns the new full filename (name + dot + extension).
     * If the new extension is empty the filename has no dot suffix.
     *
     * @return the new full filename; never null
     */
    public String getNewFullName() {
        return newExtension.isEmpty()
                ? newName
                : newName + "." + newExtension;
    }

    /**
     * Returns {@code true} if the file needs to be physically renamed.
     * Returns {@code false} if there is an error or the old and new names are identical.
     *
     * @return whether a rename is required
     */
    public boolean needsRename() {
        return !hasError && !getOldFullName().equals(getNewFullName());
    }

    /**
     * Returns the path to the original file.
     *
     * @return the original path; never null
     */
    public Path getOldPath() {
        return originalFile.getFile().toPath();
    }

    /**
     * Returns the path the file should be renamed to.
     *
     * @return the new path; never null
     */
    public Path getNewPath() {
        return originalFile.getFile().toPath().resolveSibling(getNewFullName());
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
