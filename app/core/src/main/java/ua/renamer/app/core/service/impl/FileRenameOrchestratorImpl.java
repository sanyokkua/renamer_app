package ua.renamer.app.core.service.impl;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.*;
import ua.renamer.app.api.model.config.*;
import ua.renamer.app.api.service.FileRenameOrchestrator;
import ua.renamer.app.api.service.ProgressCallback;
import ua.renamer.app.core.mapper.ThreadAwareFileMapper;
import ua.renamer.app.core.service.DuplicateNameResolver;
import ua.renamer.app.core.service.FileTransformationService;
import ua.renamer.app.core.service.RenameExecutionService;
import ua.renamer.app.core.service.transformation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class FileRenameOrchestratorImpl implements FileRenameOrchestrator {

    private final ThreadAwareFileMapper fileMapper;
    private final DuplicateNameResolver duplicateResolver;
    private final RenameExecutionService renameExecutor;

    // Individual transformers - no registry needed with pattern matching
    private final AddTextTransformer addTextTransformer;
    private final RemoveTextTransformer removeTextTransformer;
    private final ReplaceTextTransformer replaceTextTransformer;
    private final CaseChangeTransformer caseChangeTransformer;
    private final DateTimeTransformer dateTimeTransformer;
    private final ImageDimensionsTransformer imageDimensionsTransformer;
    private final SequenceTransformer sequenceTransformer;
    private final ParentFolderTransformer parentFolderTransformer;
    private final TruncateTransformer truncateTransformer;
    private final ExtensionChangeTransformer extensionChangeTransformer;

    @Override
    public List<RenameResult> execute(List<File> files, TransformationMode mode, Object config, ProgressCallback progressCallback) {
        log.info("Starting rename pipeline for {} files with mode {}", files.size(), mode);

        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {

            // PHASE 1: Extract metadata (Always Parallel)
            List<FileModel> fileModels = extractMetadataParallel(files, virtualExecutor, progressCallback);
            log.debug("Phase 1 complete: {} file models extracted", fileModels.size());

            // PHASE 2: Apply transformation (Conditional)
            List<PreparedFileModel> prepared = applyTransformation(fileModels, mode, config, virtualExecutor, progressCallback);
            log.debug("Phase 2 complete: {} files prepared", prepared.size());

            // PHASE 2.5: Resolve duplicates (Always Sequential)
            prepared = duplicateResolver.resolve(prepared);
            log.debug("Phase 2.5 complete: {} files after deduplication", prepared.size());

            // PHASE 3: Execute renames (depth-ordered to avoid parent-before-child race)
            List<RenameResult> results = executeRenamesOrdered(prepared, progressCallback);
            log.info("Pipeline complete: {} results", results.size());

            // Log summary
            logResultsSummary(results);

            return results;

        } catch (Exception e) {
            log.error("Pipeline failed with exception", e);
            // Return all as errors
            return files.stream().map(file -> buildErrorResult(file, "Pipeline error: " + e.getMessage())).toList();
        }
    }

    @Override
    public CompletableFuture<List<RenameResult>> executeAsync(List<File> files, TransformationMode mode, Object config, ProgressCallback progressCallback) {
        return CompletableFuture.supplyAsync(() -> execute(files, mode, config, progressCallback));
    }

    @Override
    public List<FileModel> extractMetadata(List<File> files, ProgressCallback progressCallback) {
        log.info("Phase 1 only: extracting metadata for {} files", files.size());
        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            return extractMetadataParallel(files, virtualExecutor, progressCallback);
        } catch (Exception e) {
            log.error("Metadata extraction failed", e);
            return files.stream()
                    .map(f -> FileModel.builder()
                            .withFile(f)
                            .withName(f.getName())
                            .withExtension("")
                            .withAbsolutePath(f.getAbsolutePath())
                            .withIsFile(false)
                            .withFileSize(0L)
                            .build())
                    .toList();
        }
    }

    @Override
    public List<PreparedFileModel> computePreview(
            List<FileModel> fileModels,
            TransformationMode mode,
            Object config,
            ProgressCallback progressCallback) {
        log.info("Phases 2-2.5: computing preview for {} files, mode={}", fileModels.size(), mode);
        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<PreparedFileModel> prepared =
                    applyTransformation(fileModels, mode, config, virtualExecutor, progressCallback);
            log.debug("Phase 2 complete: {} files prepared", prepared.size());
            prepared = duplicateResolver.resolve(prepared);
            log.debug("Phase 2.5 complete: {} files after dedup", prepared.size());
            return prepared;
        } catch (Exception e) {
            log.error("Preview computation failed", e);
            return fileModels.stream()
                    .map(fm -> PreparedFileModel.builder()
                            .withOriginalFile(fm)
                            .withNewName(fm.getName())
                            .withNewExtension(fm.getExtension())
                            .withHasError(true)
                            .withErrorMessage("Preview error: " + e.getMessage())
                            .build())
                    .toList();
        }
    }

    // ==================== PHASE 1: METADATA EXTRACTION ====================

    private List<FileModel> extractMetadataParallel(List<File> files, ExecutorService executor, ProgressCallback progressCallback) {
        int total = files.size();
        AtomicInteger completed = new AtomicInteger(0);

        updateProgress(0, total, progressCallback);

        return files.parallelStream().map(file -> CompletableFuture.supplyAsync(() -> {
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
        }, executor)).map(CompletableFuture::join).toList();
    }

    // ==================== PHASE 2: TRANSFORMATION ====================

    private List<PreparedFileModel> applyTransformation(List<FileModel> fileModels, TransformationMode mode, Object config, ExecutorService executor, ProgressCallback progressCallback) {
        // Pattern matching provides compile-time exhaustiveness checking
        // and runtime config type validation
        final String configClassName = config == null ? "null" : config.getClass().getName();
        return switch (mode) {
            case ADD_TEXT -> {
                if (!(config instanceof AddTextConfig typedConfig)) {
                    throw new IllegalArgumentException("ADD_TEXT requires AddTextConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, addTextTransformer, typedConfig, executor, progressCallback);
            }
            case REMOVE_TEXT -> {
                if (!(config instanceof RemoveTextConfig typedConfig)) {
                    throw new IllegalArgumentException("REMOVE_TEXT requires RemoveTextConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, removeTextTransformer, typedConfig, executor, progressCallback);
            }
            case REPLACE_TEXT -> {
                if (!(config instanceof ReplaceTextConfig typedConfig)) {
                    throw new IllegalArgumentException("REPLACE_TEXT requires ReplaceTextConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, replaceTextTransformer, typedConfig, executor, progressCallback);
            }
            case CHANGE_CASE -> {
                if (!(config instanceof CaseChangeConfig typedConfig)) {
                    throw new IllegalArgumentException("CHANGE_CASE requires CaseChangeConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, caseChangeTransformer, typedConfig, executor, progressCallback);
            }
            case ADD_DATETIME -> {
                if (!(config instanceof DateTimeConfig typedConfig)) {
                    throw new IllegalArgumentException("ADD_DATETIME requires DateTimeConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, dateTimeTransformer, typedConfig, executor, progressCallback);
            }
            case ADD_DIMENSIONS -> {
                if (!(config instanceof ImageDimensionsConfig typedConfig)) {
                    throw new IllegalArgumentException("ADD_DIMENSIONS requires ImageDimensionsConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, imageDimensionsTransformer, typedConfig, executor, progressCallback);
            }
            case NUMBER_FILES -> {
                if (!(config instanceof SequenceConfig typedConfig)) {
                    throw new IllegalArgumentException("NUMBER_FILES requires SequenceConfig, got: " + configClassName);
                }
                // Sequence mode always uses sequential processing
                log.debug("Using sequential transformation for NUMBER_FILES mode");
                yield sequenceTransformer.transformBatch(fileModels, typedConfig);
            }
            case ADD_FOLDER_NAME -> {
                if (!(config instanceof ParentFolderConfig typedConfig)) {
                    throw new IllegalArgumentException("ADD_FOLDER_NAME requires ParentFolderConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, parentFolderTransformer, typedConfig, executor, progressCallback);
            }
            case TRIM_NAME -> {
                if (!(config instanceof TruncateConfig typedConfig)) {
                    throw new IllegalArgumentException("TRIM_NAME requires TruncateConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, truncateTransformer, typedConfig, executor, progressCallback);
            }
            case CHANGE_EXTENSION -> {
                if (!(config instanceof ExtensionChangeConfig typedConfig)) {
                    throw new IllegalArgumentException("CHANGE_EXTENSION requires ExtensionChangeConfig, got: " + configClassName);
                }
                yield applyTransformationParallel(fileModels, extensionChangeTransformer, typedConfig, executor, progressCallback);
            }
        };
    }

    private <C> List<PreparedFileModel> applyTransformationParallel(List<FileModel> fileModels, FileTransformationService<C> transformer, C config, ExecutorService executor, ProgressCallback progressCallback) {
        int total = fileModels.size();
        AtomicInteger completed = new AtomicInteger(0);

        updateProgress(0, total, progressCallback);

        return fileModels.parallelStream().map(model -> CompletableFuture.supplyAsync(() -> {
            PreparedFileModel result = transformer.transform(model, config);
            int current = completed.incrementAndGet();
            updateProgress(current, total, progressCallback);
            return result;
        }, executor)).map(CompletableFuture::join).toList();
    }

    // ==================== PHASE 3: PHYSICAL RENAME ====================

    private List<RenameResult> executeRenamesOrdered(List<PreparedFileModel> prepared, ProgressCallback progressCallback) {
        int total = prepared.size();
        AtomicInteger completed = new AtomicInteger(0);

        updateProgress(0, total, progressCallback);

        // Sort deepest paths first so children are renamed before their parent folders.
        // This prevents NoSuchFileException when a parent is renamed before its children.
        List<PreparedFileModel> ordered = prepared.stream()
                .sorted(Comparator.comparingInt(
                        (PreparedFileModel p) -> p.getOldPath().getNameCount()
                ).reversed())
                .toList();

        // Sequential stream respects the sort order; parallelStream() would not.
        return ordered.stream().map(preparedFile -> {
            RenameResult result = renameExecutor.execute(preparedFile);
            int current = completed.incrementAndGet();
            updateProgress(current, total, progressCallback);
            return result;
        }).toList();
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
