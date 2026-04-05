package ua.renamer.app.backend.session;

import lombok.Getter;
import lombok.Setter;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.session.ModeParameters;
import ua.renamer.app.api.session.RenamePreview;
import ua.renamer.app.api.session.SessionSnapshot;
import ua.renamer.app.api.session.SessionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable session state holder.
 *
 * <p>NOT thread-safe on its own — all mutations must be called from the
 * BackendExecutor state thread (enforced in TASK-3.2).
 */
@Getter
public class RenameSession {

    private final List<FileModel> files = new ArrayList<>();
    private TransformationMode activeMode = null;
    private ModeParameters currentParams = null;
    private List<PreparedFileModel> lastPreview = List.of();
    @Setter
    private SessionStatus status = SessionStatus.EMPTY;

    /**
     * Adds files to the session.
     * Clears {@code lastPreview}. Status advances to {@link SessionStatus#MODE_CONFIGURED}
     * if a mode and params are set, otherwise to {@link SessionStatus#FILES_LOADED}.
     *
     * @param newFiles files to add; must not be null; elements must not be null
     */
    public void addFiles(List<FileModel> newFiles) {
        files.addAll(newFiles);
        lastPreview = List.of();
        if (activeMode != null && currentParams != null) {
            status = SessionStatus.MODE_CONFIGURED;
        } else {
            status = SessionStatus.FILES_LOADED;
        }
    }

    /**
     * Removes files by their fileId (absolutePath).
     * Clears {@code lastPreview}. Status becomes {@link SessionStatus#EMPTY} if the list is now
     * empty; mode and params are preserved.
     *
     * @param fileIds absolute paths of files to remove; must not be null
     */
    public void removeFiles(List<String> fileIds) {
        files.removeIf(fm -> fileIds.contains(fm.getAbsolutePath()));
        lastPreview = List.of();
        if (files.isEmpty()) {
            status = SessionStatus.EMPTY;
        } else if (activeMode != null && currentParams != null) {
            status = SessionStatus.MODE_CONFIGURED;
        } else {
            status = SessionStatus.FILES_LOADED;
        }
    }

    /**
     * Clears all files and preview. Resets status to {@link SessionStatus#EMPTY}.
     * Mode and parameters are preserved so that the next {@link #addFiles} call
     * can immediately compute a preview without requiring the user to re-select a mode.
     */
    public void clearFiles() {
        files.clear();
        lastPreview = List.of();
        status = SessionStatus.EMPTY;
    }

    /**
     * Sets the active transformation mode with default parameters.
     * Status becomes {@link SessionStatus#MODE_CONFIGURED} if files are present,
     * stays {@link SessionStatus#EMPTY} otherwise.
     *
     * @param mode          the transformation mode to activate; must not be null
     * @param defaultParams the default parameters for the mode; must not be null
     */
    public void setActiveMode(TransformationMode mode, ModeParameters defaultParams) {
        activeMode = mode;
        currentParams = defaultParams;
        lastPreview = List.of();
        if (!files.isEmpty()) {
            status = SessionStatus.MODE_CONFIGURED;
        }
    }

    /**
     * Updates the current parameters. Status is unchanged.
     *
     * @param params the new parameters; must not be null
     */
    public void setParameters(ModeParameters params) {
        currentParams = params;
        lastPreview = List.of();
    }

    /**
     * Stores the last computed preview. Makes a defensive copy.
     *
     * @param preview the preview list to store; must not be null
     */
    public void setLastPreview(List<PreparedFileModel> preview) {
        lastPreview = List.copyOf(preview);
    }

    /**
     * Returns an immutable snapshot of the current session state.
     *
     * @param previewDtos the current preview DTOs; caller converts {@code lastPreview} externally; must not be null
     * @return a new {@link SessionSnapshot}; never null
     */
    public SessionSnapshot toSnapshot(List<RenamePreview> previewDtos) {
        return new SessionSnapshot(
                files.stream().map(RenameSessionConverter::toCandidate).toList(),
                activeMode,
                currentParams,
                previewDtos,
                status
        );
    }
}
