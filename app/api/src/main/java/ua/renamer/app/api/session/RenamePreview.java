package ua.renamer.app.api.session;

/**
 * FX-safe preview snapshot produced after applying a transformation to a {@link RenameCandidate}.
 * Conveys both the proposed new name and any transformation error for that file.
 *
 * @param fileId       stable identifier matching a {@link RenameCandidate#fileId()}; never null
 * @param originalName the full filename before transformation; never null
 * @param newName      the proposed full filename after transformation; {@code null} if {@code hasError} is {@code true}
 * @param hasError     {@code true} if the transformation phase produced an error for this file
 * @param errorMessage a human-readable error description; {@code null} when {@code hasError} is {@code false}
 */
public record RenamePreview(
        String fileId,
        String originalName,
        String newName,
        boolean hasError,
        String errorMessage) {
}
