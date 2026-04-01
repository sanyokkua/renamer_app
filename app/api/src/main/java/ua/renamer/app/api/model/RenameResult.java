package ua.renamer.app.api.model;

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
     * Returns {@code true} if the rename completed successfully.
     *
     * @return whether the rename succeeded
     */
    public boolean isSuccess() {
        return status == RenameStatus.SUCCESS;
    }

    /**
     * Returns the original filename.
     *
     * @return the original full filename; never null
     */
    public String getOriginalFileName() {
        return preparedFile.getOldFullName();
    }

    /**
     * Returns the new filename.
     *
     * @return the new full filename; never null
     */
    public String getNewFileName() {
        return preparedFile.getNewFullName();
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
