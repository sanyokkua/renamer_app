package ua.renamer.app.core.v2.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Final result of a file rename operation after physical execution.
 * Contains the prepared file model, execution status, and any error information.
 */
@Value
@Builder(setterPrefix = "with")
public class RenameResult {
    /**
     * The prepared file model that was executed.
     */
    PreparedFileModel preparedFile;

    /**
     * Status of the rename operation.
     */
    RenameStatus status;

    /**
     * Error message if the operation failed.
     */
    String errorMessage;

    /**
     * When the rename was executed.
     */
    LocalDateTime executedAt;

    /**
     * Check if the rename was successful.
     */
    public boolean isSuccess() {
        return status == RenameStatus.SUCCESS;
    }

    /**
     * Get the original filename.
     */
    public String getOriginalFileName() {
        return preparedFile.getOldFullName();
    }

    /**
     * Get the new filename.
     */
    public String getNewFileName() {
        return preparedFile.getNewFullName();
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
