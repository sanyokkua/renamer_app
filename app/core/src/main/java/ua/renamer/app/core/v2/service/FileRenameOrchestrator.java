package ua.renamer.app.core.v2.service;

import ua.renamer.app.core.v2.model.RenameResult;
import ua.renamer.app.core.v2.model.TransformationMode;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main orchestrator for the file rename pipeline.
 * Coordinates metadata extraction, transformation, duplicate resolution, and physical rename.
 */
public interface FileRenameOrchestrator {
    /**
     * Execute complete rename pipeline synchronously.
     *
     * @param files            Files to rename
     * @param mode             Transformation mode to apply
     * @param config           Configuration for the transformation mode
     * @param progressCallback Optional callback for progress updates
     * @return List of rename results
     */
    List<RenameResult> execute(
            List<File> files,
            TransformationMode mode,
            Object config,
            ProgressCallback progressCallback
    );

    /**
     * Execute complete rename pipeline asynchronously.
     *
     * @param files            Files to rename
     * @param mode             Transformation mode to apply
     * @param config           Configuration for the transformation mode
     * @param progressCallback Optional callback for progress updates
     * @return CompletableFuture with list of rename results
     */
    CompletableFuture<List<RenameResult>> executeAsync(
            List<File> files,
            TransformationMode mode,
            Object config,
            ProgressCallback progressCallback
    );
}
