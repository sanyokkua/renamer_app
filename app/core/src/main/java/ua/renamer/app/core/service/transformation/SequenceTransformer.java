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
                if (!input.isFile()) {
                    log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
                    results.add(buildErrorResult(input, "File extraction failed"));
                } else {
                    validFiles.add(input);
                }
            }

            // Step 2: Sort valid files by criteria
            List<FileModel> sorted = sortBySource(validFiles, config);

            // Step 3: Apply sequence numbers (must be sequential!)
            AtomicInteger counter = new AtomicInteger(config.getStartNumber());

            // Use sequential stream to ensure counter increments in order
            List<PreparedFileModel> sequencedResults = sorted.stream()  // Sequential stream!
                    .map(input -> {
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
                    })
                    .toList();

            // Step 4: Combine error results and sequenced results
            results.addAll(sequencedResults);
            return results;

        } catch (Exception e) {
            log.error("Failed to sort files for sequence", e);
            // If sorting fails, return all as errors
            return inputs.stream()
                    .map(input -> buildErrorResult(input, "Failed to sort: " + e.getMessage()))
                    .toList();
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
                .withMode(TransformationMode.ADD_SEQUENCE)
                .withAppliedAt(LocalDateTime.now())
                .withConfig(Map.of(
                        "startNumber", config.getStartNumber(),
                        "stepValue", config.getStepValue(),
                        "padding", config.getPadding(),
                        "sortSource", config.getSortSource().name()
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
