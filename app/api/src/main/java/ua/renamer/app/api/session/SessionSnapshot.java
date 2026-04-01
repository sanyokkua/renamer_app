package ua.renamer.app.api.session;

import ua.renamer.app.api.model.TransformationMode;

import java.util.List;

/**
 * An immutable point-in-time snapshot of the rename session's observable state.
 *
 * <p>Produced by {@link SessionApi#snapshot()} and safe to read from any thread.
 * The {@code files} and {@code preview} lists are defensively copied on construction.
 *
 * @param files             files currently loaded; never null; may be empty
 * @param activeMode        currently selected mode; {@code null} when none selected
 * @param currentParameters parameters for {@code activeMode};
 *                          {@code null} when {@code activeMode} is {@code null}
 * @param preview           last computed rename preview; never null; may be empty
 * @param status            current lifecycle state; never null
 */
public record SessionSnapshot(
        List<RenameCandidate> files,
        TransformationMode activeMode,
        ModeParameters currentParameters,
        List<RenamePreview> preview,
        SessionStatus status) {

    /**
     * Defensively copies {@code files} and {@code preview} to guarantee immutability.
     *
     * @throws NullPointerException if {@code files} or {@code preview} is null,
     *                              or if either list contains a null element
     */
    public SessionSnapshot {
        files = List.copyOf(files);
        preview = List.copyOf(preview);
    }
}
