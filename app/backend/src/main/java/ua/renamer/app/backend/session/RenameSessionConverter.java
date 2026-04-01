package ua.renamer.app.backend.session;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.session.RenameCandidate;
import ua.renamer.app.api.session.RenamePreview;
import ua.renamer.app.api.session.RenameSessionResult;

/**
 * Static utility that converts V2 internal pipeline types to FX-safe API types.
 * {@link FileModel} must never escape {@code app/backend} — only {@link RenameCandidate} is exposed to the UI.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenameSessionConverter {

    /**
     * Converts a {@link FileModel} to a {@link RenameCandidate}.
     * Uses {@code absolutePath} as {@code fileId} — stable and unique within a session.
     *
     * @param model the file model produced by the metadata extraction phase; must not be null
     * @return a new {@link RenameCandidate}; never null
     */
    public static RenameCandidate toCandidate(FileModel model) {
        return new RenameCandidate(
            model.getAbsolutePath(),    // fileId — stable, unique within session
            model.getName(),            // base name without extension
            model.getExtension(),       // no leading dot (ThreadAwareFileMapper convention)
            model.getFile().toPath()    // java.nio.file.Path from java.io.File
        );
    }

    /**
     * Converts a {@link PreparedFileModel} to a {@link RenamePreview}.
     * {@code newName} is {@code null} when {@code hasError} is {@code true}, per {@link RenamePreview} contract.
     *
     * @param model the prepared file model produced by the transformation phase; must not be null
     * @return a new {@link RenamePreview}; never null
     */
    public static RenamePreview toPreview(PreparedFileModel model) {
        boolean hasError = model.isHasError();
        return new RenamePreview(
            model.getOriginalFile().getAbsolutePath(),    // fileId — matches toCandidate
            model.getOldFullName(),                        // originalName
            hasError ? null : model.getNewFullName(),      // null when error
            hasError,
            model.getErrorMessage().orElse(null)           // unwrap Optional → nullable
        );
    }

    /**
     * Converts a {@link RenameResult} to a {@link RenameSessionResult}.
     * {@code finalName} reflects actual disk state: new name on success, original name otherwise.
     *
     * @param result the rename result produced by the physical rename phase; must not be null
     * @return a new {@link RenameSessionResult}; never null
     */
    public static RenameSessionResult toSessionResult(RenameResult result) {
        boolean succeeded = result.isSuccess();
        return new RenameSessionResult(
            result.getPreparedFile().getOriginalFile().getAbsolutePath(), // fileId
            result.getOriginalFileName(),                                  // originalName
            succeeded ? result.getNewFileName() : result.getOriginalFileName(), // actual disk name
            result.getStatus(),
            result.getErrorMessage().orElse(null)
        );
    }
}
