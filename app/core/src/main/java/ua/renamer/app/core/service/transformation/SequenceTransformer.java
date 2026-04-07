package ua.renamer.app.core.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.SequenceConfig;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.core.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Transformer that adds sequence numbers to filenames.
 * This transformer REQUIRES sequential execution to preserve counter order.
 */
@Slf4j
public class SequenceTransformer implements FileTransformationService<SequenceConfig> {

    @Override
    public boolean requiresSequentialExecution() {
        return true;  // MUST be sequential to preserve counter order
    }

    @Override
    public PreparedFileModel transform(FileModel input, SequenceConfig config) {
        throw new UnsupportedOperationException(
                "Sequence transformer must use transformBatch() method for proper sequential processing");
    }

    @Override
    public List<PreparedFileModel> transformBatch(List<FileModel> inputs, SequenceConfig config) {
        if (config == null) {
            return inputs.stream()
                    .map(input -> buildErrorResult(input, "Transformer configuration must not be null"))
                    .toList();
        }
        try {
            // Step 1: Filter out invalid files and create error results
            List<PreparedFileModel> results = new ArrayList<>();
            List<FileModel> validFiles = new ArrayList<>();

            for (FileModel input : inputs) {
                if (!input.isFile() && !"application/x-directory".equals(input.getDetectedMimeType())) {
                    results.add(buildErrorResult(input, "File extraction failed"));
                } else {
                    validFiles.add(input);
                }
            }

            // Step 2: Apply sequence numbers, either per-folder or globally
            List<PreparedFileModel> sequencedResults = new ArrayList<>();
            if (config.isPerFolderCounting()) {
                // Group by parent directory, preserving the order in which folders first appear
                Map<String, List<FileModel>> grouped = new LinkedHashMap<>();
                for (FileModel fm : validFiles) {
                    String parent = fm.getFile().getParent() != null ? fm.getFile().getParent() : "";
                    grouped.computeIfAbsent(parent, k -> new ArrayList<>()).add(fm);
                }
                // Apply an independent counter to each folder group
                for (List<FileModel> group : grouped.values()) {
                    List<FileModel> groupSorted = sortBySource(group, config);
                    AtomicInteger counter = new AtomicInteger(config.getStartNumber());
                    groupSorted.stream()
                            .map(fm -> applySequence(fm, counter, config))
                            .forEach(sequencedResults::add);
                }
            } else {
                // Flat counting: one global counter across all files
                List<FileModel> sorted = sortBySource(validFiles, config);
                AtomicInteger counter = new AtomicInteger(config.getStartNumber());
                sorted.stream()
                        .map(fm -> applySequence(fm, counter, config))
                        .forEach(sequencedResults::add);
            }

            // Step 3: Combine error results and sequenced results
            results.addAll(sequencedResults);
            return results;

        } catch (Exception e) {
            log.error("Failed to process files for sequence", e);
            return inputs.stream()
                    .map(input -> buildErrorResult(input, "Failed to process: " + e.getMessage()))
                    .toList();
        }
    }

    private PreparedFileModel applySequence(FileModel input, AtomicInteger counter, SequenceConfig config) {
        try {
            int num = counter.getAndAdd(config.getStepValue());
            String newName = formatSequenceNumber(num, config.getPadding());
            return PreparedFileModel.builder()
                    .withOriginalFile(input)
                    .withNewName(newName)
                    .withNewExtension(input.getExtension())
                    .withHasError(false)
                    .withErrorMessage(null)
                    .withTransformationMeta(buildMetadata(config))
                    .build();
        } catch (Exception e) {
            log.error("Failed to apply sequence to file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to apply sequence: " + e.getMessage());
        }
    }

    private List<FileModel> sortBySource(List<FileModel> models, SequenceConfig config) {
        List<FileModel> sorted = new ArrayList<>(models);

        switch (config.getSortSource()) {
            case FILE_NAME -> sorted.sort(Comparator.comparing(FileModel::getName));
            case FILE_PATH -> sorted.sort(Comparator.comparing(FileModel::getAbsolutePath));
            case FILE_SIZE -> sorted.sort(Comparator.comparing(FileModel::getFileSize));
            case FILE_CREATION_DATETIME -> sorted.sort(Comparator.comparing(
                    m -> m.getCreationDate().orElse(LocalDateTime.MIN)));
            case FILE_MODIFICATION_DATETIME -> sorted.sort(Comparator.comparing(
                    m -> m.getModificationDate().orElse(LocalDateTime.MIN)));
            case FILE_CONTENT_CREATION_DATETIME -> sorted.sort(Comparator.comparing(m -> m.getMetadata()
                    .flatMap(FileMeta::getImageMeta)
                    .flatMap(ImageMeta::getContentCreationDate)
                    .orElse(LocalDateTime.MIN)));
            case IMAGE_WIDTH -> sorted.sort(Comparator.comparing(m -> m.getMetadata()
                    .flatMap(FileMeta::getImageMeta)
                    .flatMap(ImageMeta::getWidth)
                    .orElse(0)));
            case IMAGE_HEIGHT -> sorted.sort(Comparator.comparing(m -> m.getMetadata()
                    .flatMap(FileMeta::getImageMeta)
                    .flatMap(ImageMeta::getHeight)
                    .orElse(0)));
        }

        return sorted;
    }

    private String formatSequenceNumber(int number, int padding) {
        if (padding <= 0) {
            return String.valueOf(number);  // No padding: raw number
        }
        String format = "%0" + padding + "d";
        return String.format(format, number);
    }

    private TransformationMetadata buildMetadata(SequenceConfig config) {
        return TransformationMetadata.builder()
                .withMode(TransformationMode.NUMBER_FILES)
                .withAppliedAt(LocalDateTime.now())
                .withConfig(Map.of(
                        "startNumber", config.getStartNumber(),
                        "stepValue", config.getStepValue(),
                        "padding", config.getPadding(),
                        "sortSource", config.getSortSource().name(),
                        "perFolderCounting", config.isPerFolderCounting()
                ))
                .build();
    }

    private PreparedFileModel buildErrorResult(FileModel input, String error) {
        return PreparedFileModel.builder()
                .withOriginalFile(input)
                .withNewName(input.getName())
                .withNewExtension(input.getExtension())
                .withHasError(true)
                .withErrorMessage(error)
                .withTransformationMeta(null)
                .build();
    }
}
