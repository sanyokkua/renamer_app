package ua.renamer.app.api.service;

import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.TransformationMode;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main orchestrator for the file rename pipeline.
 * Coordinates metadata extraction, transformation, duplicate resolution, and physical rename.
 */
public interface FileRenameOrchestrator {

    /**
     * Executes the complete rename pipeline synchronously.
     *
     * @param files            the files to rename; must not be null
     * @param mode             the transformation mode to apply; must not be null
     * @param config           the configuration for the transformation mode; must not be null
     * @param progressCallback optional callback for progress updates; may be null
     * @return the list of rename results; never null; never throws
     */
    List<RenameResult> execute(
            List<File> files,
            TransformationMode mode,
            Object config,
            ProgressCallback progressCallback
    );

    /**
     * Executes the complete rename pipeline asynchronously.
     *
     * @param files            the files to rename; must not be null
     * @param mode             the transformation mode to apply; must not be null
     * @param config           the configuration for the transformation mode; must not be null
     * @param progressCallback optional callback for progress updates; may be null
     * @return a future that completes with the list of rename results; never null
     */
    CompletableFuture<List<RenameResult>> executeAsync(
            List<File> files,
            TransformationMode mode,
            Object config,
            ProgressCallback progressCallback
    );

    /**
     * Phase 1 only: extracts file metadata (name, size, timestamps, EXIF, etc.) in parallel.
     * Used by the backend session when files are added to the session.
     * Never throws — per-file errors are captured as error FileModel entries.
     *
     * @param files            the files to extract metadata for; must not be null
     * @param progressCallback optional callback for progress updates; may be null
     * @return the list of file models; never null
     */
    List<FileModel> extractMetadata(List<File> files, ProgressCallback progressCallback);

    /**
     * Phases 2–2.5 only: applies transformation and deduplicates filenames, but does NOT rename on disk.
     * Used by the backend session to compute a rename preview from cached FileModel objects.
     * Never throws — per-file errors are captured in PreparedFileModel.hasError().
     *
     * @param fileModels       the file models produced by Phase 1; must not be null
     * @param mode             the transformation mode to apply; must not be null
     * @param config           the configuration for the transformation mode; must not be null
     * @param progressCallback optional callback for progress updates; may be null
     * @return the list of prepared file models after transformation and deduplication; never null
     */
    List<PreparedFileModel> computePreview(
            List<FileModel> fileModels,
            TransformationMode mode,
            Object config,
            ProgressCallback progressCallback
    );
}
