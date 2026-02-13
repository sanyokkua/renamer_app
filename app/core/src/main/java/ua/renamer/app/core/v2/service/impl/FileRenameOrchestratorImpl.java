package ua.renamer.app.core.v2.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.mapper.ThreadAwareFileMapper;
import ua.renamer.app.core.v2.model.*;
import ua.renamer.app.core.v2.service.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main orchestrator for the file rename pipeline.
 * Implements 4-phase pipeline: Extract → Transform → Deduplicate → Execute
 * Uses virtual threads for I/O-bound parallel operations.
 */
@Slf4j
@RequiredArgsConstructor
public class FileRenameOrchestratorImpl implements FileRenameOrchestrator {

    private final ThreadAwareFileMapper fileMapper;
    private final DuplicateNameResolver duplicateResolver;
    private final RenameExecutionService renameExecutor;
    private final Map<TransformationMode, FileTransformationService<?>> transformerRegistry;

    @Override
    public List<RenameResult> execute(
            List<File> files,
            TransformationMode mode,
            Object config,
            ProgressCallback progressCallback
    ) {
        log.info("Starting rename pipeline for {} files with mode {}", files.size(), mode);

        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {

            // PHASE 1: Extract metadata (Always Parallel)
            List<FileModel> fileModels = extractMetadataParallel(files, virtualExecutor, progressCallback);
            log.debug("Phase 1 complete: {} file models extracted", fileModels.size());

            // PHASE 2: Apply transformation (Conditional)
            List<PreparedFileModel> prepared = applyTransformation(
                    fileModels, mode, config, virtualExecutor, progressCallback);
            log.debug("Phase 2 complete: {} files prepared", prepared.size());

            // PHASE 2.5: Resolve duplicates (Always Sequential)
            prepared = duplicateResolver.resolve(prepared);
            log.debug("Phase 2.5 complete: {} files after deduplication", prepared.size());

            // PHASE 3: Execute renames (Always Parallel)
            List<RenameResult> results = executeRenamesParallel(
                    prepared, virtualExecutor, progressCallback);
            log.info("Pipeline complete: {} results", results.size());

            // Log summary
            logResultsSummary(results);

            return results;

        } catch (Exception e) {
            log.error("Pipeline failed with exception", e);
            // Return all as errors
            return files.stream()
                        .map(file -> buildErrorResult(file, "Pipeline error: " + e.getMessage()))
                        .toList();
        }
    }

    @Override
    public CompletableFuture<List<RenameResult>> executeAsync(
            List<File> files,
            TransformationMode mode,
            Object config,
            ProgressCallback progressCallback
    ) {
        return CompletableFuture.supplyAsync(() ->
                                                     execute(files, mode, config, progressCallback)
        );
    }

    // ==================== PHASE 1: METADATA EXTRACTION ====================

    private List<FileModel> extractMetadataParallel(
            List<File> files,
            ExecutorService executor,
            ProgressCallback progressCallback
    ) {
        int total = files.size();
        AtomicInteger completed = new AtomicInteger(0);

        updateProgress(0, total, progressCallback);

        return files.parallelStream()
                    .map(file -> CompletableFuture.supplyAsync(() -> {
                        try {
                            FileModel model = fileMapper.mapFrom(file);
                            int current = completed.incrementAndGet();
                            updateProgress(current, total, progressCallback);
                            return model;
                        } catch (Exception e) {
                            log.error("Failed to extract metadata for: {}", file.getAbsolutePath(), e);
                            int current = completed.incrementAndGet();
                            updateProgress(current, total, progressCallback);

                            // Return error FileModel that will propagate as error through pipeline
                            return FileModel.builder()
                                            .withFile(file)
                                            .withName(file.getName())
                                            .withExtension("")
                                            .withAbsolutePath(file.getAbsolutePath())
                                            .withIsFile(false)  // Mark as problematic
                                            .withFileSize(0L)
                                            .build();
                        }
                    }, executor))
                    .map(CompletableFuture::join)
                    .toList();
    }

    // ==================== PHASE 2: TRANSFORMATION ====================

    private List<PreparedFileModel> applyTransformation(
            List<FileModel> fileModels,
            TransformationMode mode,
            Object config,
            ExecutorService executor,
            ProgressCallback progressCallback
    ) {
        FileTransformationService transformer = transformerRegistry.get(mode);

        if (transformer == null) {
            throw new IllegalArgumentException("No transformer registered for mode: " + mode);
        }

        // Check if sequential execution required (sequence mode)
        if (transformer.requiresSequentialExecution()) {
            log.debug("Using sequential transformation for mode: {}", mode);
            return transformer.transformBatch(fileModels, config);
        } else {
            log.debug("Using parallel transformation for mode: {}", mode);
            return applyTransformationParallel(fileModels, transformer, config, executor, progressCallback);
        }
    }

    @SuppressWarnings("unchecked")
    private List<PreparedFileModel> applyTransformationParallel(
            List<FileModel> fileModels,
            FileTransformationService transformer,
            Object config,
            ExecutorService executor,
            ProgressCallback progressCallback
    ) {
        int total = fileModels.size();
        AtomicInteger completed = new AtomicInteger(0);

        updateProgress(0, total, progressCallback);

        return fileModels.parallelStream()
                         .map(model -> CompletableFuture.supplyAsync(() -> {
                             PreparedFileModel result = transformer.transform(model, config);
                             int current = completed.incrementAndGet();
                             updateProgress(current, total, progressCallback);
                             return result;
                         }, executor))
                         .map(CompletableFuture::join)
                         .toList();
    }

    // ==================== PHASE 3: PHYSICAL RENAME ====================

    private List<RenameResult> executeRenamesParallel(
            List<PreparedFileModel> prepared,
            ExecutorService executor,
            ProgressCallback progressCallback
    ) {
        int total = prepared.size();
        AtomicInteger completed = new AtomicInteger(0);

        updateProgress(0, total, progressCallback);

        return prepared.parallelStream()
                       .map(preparedFile -> CompletableFuture.supplyAsync(() -> {
                           RenameResult result = renameExecutor.execute(preparedFile);
                           int current = completed.incrementAndGet();
                           updateProgress(current, total, progressCallback);
                           return result;
                       }, executor))
                       .map(CompletableFuture::join)
                       .toList();
    }

    // ==================== UTILITIES ====================

    private void updateProgress(int current, int max, ProgressCallback callback) {
        if (callback != null) {
            callback.updateProgress(current, max);
        }
    }

    private void logResultsSummary(List<RenameResult> results) {
        long success = results.stream().filter(Objects::nonNull).filter(RenameResult::isSuccess).count();
        long skipped = results.stream().filter(Objects::nonNull).filter(r -> r.getStatus() == RenameStatus.SKIPPED).count();
        long errors = results.stream().filter(Objects::nonNull).filter(r -> r.getStatus().name().startsWith("ERROR")).count();

        log.info("Results summary: {} success, {} skipped, {} errors", success, skipped, errors);
    }

    private RenameResult buildErrorResult(File file, String errorMessage) {
        FileModel errorModel = FileModel.builder()
                                        .withFile(file)
                                        .withName(file.getName())
                                        .withExtension("")
                                        .withAbsolutePath(file.getAbsolutePath())
                                        .withIsFile(true)
                                        .withFileSize(0L)
                                        .withCreationDate(null)
                                        .withModificationDate(null)
                                        .withDetectedMimeType("")
                                        .withMetadata(null)
                                        .build();

        PreparedFileModel preparedError = PreparedFileModel.builder()
                                                           .withOriginalFile(errorModel)
                                                           .withNewName(file.getName())
                                                           .withNewExtension("")
                                                           .withHasError(true)
                                                           .withErrorMessage(errorMessage)
                                                           .withTransformationMeta(null)
                                                           .build();

        return RenameResult.builder()
                           .withPreparedFile(preparedError)
                           .withStatus(RenameStatus.ERROR_EXTRACTION)
                           .withErrorMessage(errorMessage)
                           .withExecutedAt(LocalDateTime.now())
                           .build();
    }
}
