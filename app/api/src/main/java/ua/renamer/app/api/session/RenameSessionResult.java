package ua.renamer.app.api.session;

import ua.renamer.app.api.model.RenameStatus;

/**
 * FX-safe result snapshot for a single file after physical rename execution.
 * Corresponds to the V2 pipeline's {@code RenameResult} but exposes only API-layer types.
 *
 * @param fileId        stable identifier matching a {@link RenameCandidate#fileId()}; never null
 * @param originalName  the full filename before the rename; never null
 * @param finalName     the actual filename on disk after execution; may equal {@code originalName} if skipped
 * @param status        the outcome of the rename operation; never null
 * @param errorMessage  a human-readable error description; {@code null} when the operation succeeded
 */
public record RenameSessionResult(
        String fileId,
        String originalName,
        String finalName,
        RenameStatus status,
        String errorMessage) {
}
