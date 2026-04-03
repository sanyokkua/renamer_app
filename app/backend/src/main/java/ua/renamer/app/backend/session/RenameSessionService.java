package ua.renamer.app.backend.session;

import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.*;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.service.FileRenameOrchestrator;
import ua.renamer.app.api.session.*;
import ua.renamer.app.backend.service.BackendExecutor;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static ua.renamer.app.api.session.AvailableAction.*;

/**
 * Primary implementation of {@link SessionApi}.
 *
 * <p>Orchestrates all backend operations — loading files, computing rename previews,
 * executing renames, and notifying the UI via {@link StatePublisher}. Runs in
 * {@code app/backend} with zero JavaFX dependencies (JPMS enforces this at compile time).
 *
 * <p>Threading model:
 * <ul>
 *   <li>All mutations to {@link RenameSession} are serialised on the state thread via
 *       {@link BackendExecutor#submitStateChange(java.util.concurrent.Callable)}.
 *   <li>I/O-bound work (metadata extraction, physical rename) runs on virtual threads
 *       via {@link BackendExecutor#submitWork(java.util.concurrent.Callable)}.
 *   <li>Read-only methods ({@link #canExecute()}, {@link #availableActions()},
 *       {@link #snapshot()}) are safe to call from any thread without locking.
 * </ul>
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = {@jakarta.inject.Inject})
public class RenameSessionService implements SessionApi {

    private final FileRenameOrchestrator orchestrator;
    private final BackendExecutor executor;
    private final StatePublisher publisher;

    // Mutable state — ONLY accessed/mutated on the state thread (enforced by submitStateChange)
    private final RenameSession session = new RenameSession();

    // Snapshot cache — updated at the end of every state mutation, read by snapshot() from any thread
    private final AtomicReference<SessionSnapshot> snapshotRef =
            new AtomicReference<>(emptySnapshot());

    /**
     * Creates an empty snapshot for the initial (EMPTY) session state.
     *
     * @return a new empty {@link SessionSnapshot}; never null
     */
    private static SessionSnapshot emptySnapshot() {
        return new SessionSnapshot(List.of(), null, null, List.of(), SessionStatus.EMPTY);
    }

    /**
     * Returns a default {@link ModeParameters} instance for the given mode.
     * Used when a mode is first selected via {@link #selectMode(TransformationMode)}.
     *
     * @param mode the transformation mode; must not be null
     * @return a valid default parameters instance; never null
     */
    private static ModeParameters defaultParamsFor(TransformationMode mode) {
        return switch (mode) {
            case ADD_TEXT -> new AddTextParams("", ItemPosition.BEGIN);
            case REMOVE_TEXT -> new RemoveTextParams("", ItemPosition.BEGIN);
            case REPLACE_TEXT -> new ReplaceTextParams("", "", ItemPositionExtended.BEGIN);
            case CHANGE_CASE -> new ChangeCaseParams(TextCaseOptions.UPPERCASE, false);
            case ADD_SEQUENCE -> new SequenceParams(1, 1, 2, SortSource.FILE_NAME);
            case TRUNCATE_FILE_NAME -> new TruncateParams(0, TruncateOptions.REMOVE_SYMBOLS_FROM_END);
            case CHANGE_EXTENSION -> new ExtensionChangeParams("");
            case USE_DATETIME -> new DateTimeParams(
                    DateTimeSource.FILE_CREATION_DATE, DateFormat.YYYY_MM_DD_DASHED,
                    TimeFormat.DO_NOT_USE_TIME, ItemPositionWithReplacement.BEGIN,
                    true, false, false, true, false, null, true,
                    DateTimeFormat.DATE_TIME_TOGETHER, "");
            case USE_IMAGE_DIMENSIONS -> new ImageDimensionsParams(
                    ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT,
                    ItemPositionWithReplacement.BEGIN, " ", "x");
            case USE_PARENT_FOLDER_NAME -> new ParentFolderParams(1, ItemPosition.BEGIN, " ");
        };
    }

    /**
     * Builds the preview DTOs to publish via {@link StatePublisher}.
     * Returns the real preview when available; falls back to placeholder DTOs (original name,
     * no new name) so the table always shows the file list when a mode is selected.
     *
     * @param preview real preview from the orchestrator; may be empty
     * @param files   current session files
     * @param mode    current active mode; {@code null} when no mode is selected
     * @return list of preview DTOs; empty only when no mode is selected or no files are present
     */
    private static List<RenamePreview> buildPreviewDtos(
            List<PreparedFileModel> preview, List<FileModel> files, TransformationMode mode) {
        if (!preview.isEmpty()) {
            return preview.stream().map(RenameSessionConverter::toPreview).toList();
        }
        // Mode selected but params invalid — show placeholders so the table is not blank
        if (mode != null && !files.isEmpty()) {
            return files.stream().map(RenameSessionConverter::toPlaceholderPreview).toList();
        }
        return List.of();
    }

    @Override
    public CompletableFuture<CommandResult> addFiles(List<Path> paths) {
        Objects.requireNonNull(paths, "paths must not be null");
        List<File> files = paths.stream().map(Path::toFile).toList();

        return executor.submitWork(() ->
                orchestrator.extractMetadata(files, null)
        ).thenCompose(fileModels ->
                executor.submitStateChange(() -> {
                    session.addFiles(fileModels);

                    List<PreparedFileModel> preview = computePreviewIfPossible();
                    session.setLastPreview(preview);

                    List<RenamePreview> previewDtos = buildPreviewDtos(
                            preview, session.getFiles(), session.getActiveMode());
                    List<ua.renamer.app.api.session.RenameCandidate> candidates = session.getFiles().stream()
                            .map(RenameSessionConverter::toCandidate).toList();

                    publisher.publishFilesChanged(candidates, previewDtos);
                    updateSnapshotCache(previewDtos);
                    return CommandResult.succeeded();
                })
        ).exceptionally(ex -> {
            log.error("addFiles failed", ex);
            return CommandResult.failure("Failed to add files: " + ex.getMessage());
        });
    }

    @Override
    public CompletableFuture<CommandResult> removeFiles(List<String> fileIds) {
        Objects.requireNonNull(fileIds, "fileIds must not be null");
        return executor.submitStateChange(() -> {
            if (session.getStatus() == SessionStatus.EXECUTING) {
                return CommandResult.failure("Cannot remove files during execution");
            }
            session.removeFiles(fileIds);

            List<PreparedFileModel> preview = computePreviewIfPossible();
            session.setLastPreview(preview);

            List<RenamePreview> previewDtos = buildPreviewDtos(
                    preview, session.getFiles(), session.getActiveMode());
            List<ua.renamer.app.api.session.RenameCandidate> candidates = session.getFiles().stream()
                    .map(RenameSessionConverter::toCandidate).toList();

            publisher.publishFilesChanged(candidates, previewDtos);
            updateSnapshotCache(previewDtos);
            return CommandResult.succeeded();
        });
    }

    @Override
    public CompletableFuture<CommandResult> clearFiles() {
        return executor.submitStateChange(() -> {
            session.clearFiles();
            publisher.publishFilesChanged(List.of(), List.of());
            snapshotRef.set(emptySnapshot());
            return CommandResult.succeeded();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends ModeParameters> CompletableFuture<ModeApi<P>> selectMode(TransformationMode mode) {
        Objects.requireNonNull(mode, "mode must not be null");
        return executor.submitStateChange(() -> {
            ModeParameters defaults = defaultParamsFor(mode);
            session.setActiveMode(mode, defaults);

            List<PreparedFileModel> preview = computePreviewIfPossible();
            session.setLastPreview(preview);

            List<RenamePreview> previewDtos = buildPreviewDtos(preview, session.getFiles(), mode);

            publisher.publishModeChanged(mode, defaults);
            if (!previewDtos.isEmpty()) {
                publisher.publishPreviewChanged(previewDtos);
            }
            updateSnapshotCache(previewDtos);

            return (ModeApi<P>) new ModeApiImpl<>((P) defaults, mode, this);
        });
    }

    @Override
    public TaskHandle<List<RenameSessionResult>> execute() {
        if (!canExecute()) {
            CompletableFuture<List<RenameSessionResult>> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException(
                    "execute() called when canExecute() is false; status=" + session.getStatus()));
            return new TaskHandleImpl<>(failed);
        }

        List<File> filesToRename = session.getFiles().stream()
                .map(FileModel::getFile).toList();
        TransformationMode mode = session.getActiveMode();
        ModeParameters params = session.getCurrentParams();
        Object config = ModeParametersConverter.toConfig(params);

        CompletableFuture<List<RenameSessionResult>> resultFuture = new CompletableFuture<>();
        TaskHandleImpl<List<RenameSessionResult>> handle = new TaskHandleImpl<>(resultFuture);

        executor.submitStateChange(() -> {
            session.setStatus(SessionStatus.EXECUTING);
            publisher.publishStatusChanged(SessionStatus.EXECUTING);
            return null;
        }).thenCompose(ignored ->
                executor.submitWork(() ->
                        orchestrator.execute(
                                filesToRename, mode, config,
                                (current, total) -> handle.notifyProgress(current, total, null))
                )
        ).thenCompose(renameResults ->
                executor.submitStateChange(() -> {
                    List<RenameSessionResult> sessionResults = renameResults.stream()
                            .map(RenameSessionConverter::toSessionResult).toList();

                    boolean anyError = renameResults.stream()
                            .anyMatch(r -> r.getStatus() == RenameStatus.ERROR_EXECUTION
                                    || r.getStatus() == RenameStatus.ERROR_EXTRACTION
                                    || r.getStatus() == RenameStatus.ERROR_TRANSFORMATION);
                    SessionStatus finalStatus = anyError ? SessionStatus.ERROR : SessionStatus.COMPLETE;

                    session.setStatus(finalStatus);
                    publisher.publishRenameComplete(sessionResults, finalStatus);
                    resultFuture.complete(sessionResults);
                    return sessionResults;
                })
        ).exceptionally(ex -> {
            log.error("execute() pipeline failed", ex);
            executor.submitStateChange(() -> {
                session.setStatus(SessionStatus.ERROR);
                publisher.publishStatusChanged(SessionStatus.ERROR);
                resultFuture.completeExceptionally(ex);
                return null;
            });
            return null;
        });

        return handle;
    }

    @Override
    public boolean canExecute() {
        return session.getStatus() == SessionStatus.MODE_CONFIGURED;
    }

    // ==================== PRIVATE HELPERS ====================

    @Override
    public List<AvailableAction> availableActions() {
        return switch (session.getStatus()) {
            case EMPTY -> List.of(ADD_FILES);
            case FILES_LOADED -> List.of(ADD_FILES, REMOVE_FILES, CLEAR, SELECT_MODE);
            case MODE_CONFIGURED -> List.of(ADD_FILES, REMOVE_FILES, CLEAR, SELECT_MODE, EXECUTE);
            case EXECUTING -> List.of(CANCEL);
            case COMPLETE, ERROR -> List.of(ADD_FILES, CLEAR, SELECT_MODE);
        };
    }

    @Override
    public SessionSnapshot snapshot() {
        return snapshotRef.get();
    }

    /**
     * Package-private — only {@link ModeApiImpl} may call this (same package).
     * Validates the parameters, stores them if valid, recomputes the preview, and notifies the UI.
     *
     * @param params the updated parameters to store; must not be null
     * @return future completing with the {@link ValidationResult}; never null
     */
    CompletableFuture<ValidationResult> updateParameters(ModeParameters params) {
        Objects.requireNonNull(params, "params must not be null");
        return executor.submitStateChange(() -> {
            ValidationResult validation = params.validate();
            if (validation.isError()) {
                return validation;
            }
            session.setParameters(params);

            List<PreparedFileModel> preview = computePreviewIfPossible();
            session.setLastPreview(preview);

            List<RenamePreview> previewDtos = preview.stream()
                    .map(RenameSessionConverter::toPreview).toList();
            publisher.publishPreviewChanged(previewDtos);
            updateSnapshotCache(previewDtos);
            return ValidationResult.valid();
        });
    }

    /**
     * Computes a preview if a mode and params are set and the file list is non-empty.
     * Called synchronously on the state thread — no I/O, pure transformation.
     *
     * @return list of prepared file models; empty if preview cannot be computed
     */
    private List<PreparedFileModel> computePreviewIfPossible() {
        TransformationMode mode = session.getActiveMode();
        ModeParameters params = session.getCurrentParams();
        List<FileModel> files = session.getFiles();
        if (mode == null || params == null || files.isEmpty()) {
            return List.of();
        }
        if (params.validate().isError()) {
            return List.of();
        }
        return orchestrator.computePreview(files, mode, ModeParametersConverter.toConfig(params), null);
    }

    /**
     * Updates the snapshot cache with the latest session state and the provided preview DTOs.
     *
     * @param previewDtos the most recently computed preview DTOs; must not be null
     */
    private void updateSnapshotCache(List<RenamePreview> previewDtos) {
        snapshotRef.set(session.toSnapshot(previewDtos));
    }
}
