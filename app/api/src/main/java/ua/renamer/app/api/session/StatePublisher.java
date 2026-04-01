package ua.renamer.app.api.session;

import ua.renamer.app.api.model.TransformationMode;

import java.util.List;

/**
 * Callback interface for backend → UI state notifications.
 *
 * <p>Lives in {@code app/api} (no JavaFX). Implemented by {@code FxStateMirror}
 * in {@code app/ui}, which wraps each method body in {@code Platform.runLater()}.
 * The backend ({@code RenameSessionService}) calls these methods from the state
 * thread; {@code FxStateMirror} dispatches to the FX thread transparently.
 *
 * <p>By using this interface, {@code app/backend} has zero JavaFX dependency —
 * JPMS enforces this at compile time.
 */
public interface StatePublisher {

    /**
     * Published when the set of loaded files changes and a new preview was computed.
     *
     * @param files   updated file list; never null
     * @param preview updated preview list; never null
     */
    void publishFilesChanged(List<RenameCandidate> files, List<RenamePreview> preview);

    /**
     * Published when the preview is recomputed without a file-list change
     * (e.g. after a parameter update).
     *
     * @param preview updated preview list; never null
     */
    void publishPreviewChanged(List<RenamePreview> preview);

    /**
     * Published when the active transformation mode or its parameters change.
     *
     * @param mode   the newly selected mode; never null
     * @param params the initial or updated parameters; never null
     */
    void publishModeChanged(TransformationMode mode, ModeParameters params);

    /**
     * Published when a rename execution finishes.
     *
     * @param results per-file rename outcomes; never null
     * @param status  resulting session status ({@link SessionStatus#COMPLETE}
     *                or {@link SessionStatus#ERROR}); never null
     */
    void publishRenameComplete(List<RenameSessionResult> results, SessionStatus status);

    /**
     * Published when session lifecycle status changes without an accompanying
     * file-list or rename-complete event (e.g. transitioning to
     * {@link SessionStatus#EXECUTING}).
     *
     * @param status the new status; never null
     */
    void publishStatusChanged(SessionStatus status);
}
