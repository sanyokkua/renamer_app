package ua.renamer.app.api.service;

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
}
