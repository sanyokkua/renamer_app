package ua.renamer.app.core.v2.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.model.FileModel;
import ua.renamer.app.core.v2.model.PreparedFileModel;
import ua.renamer.app.core.v2.model.TransformationMetadata;
import ua.renamer.app.core.v2.model.TransformationMode;
import ua.renamer.app.core.v2.model.config.TruncateConfig;
import ua.renamer.app.core.v2.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformer that truncates filenames by removing characters.
 */
@Slf4j
public class TruncateTransformer implements FileTransformationService<TruncateConfig> {

    @Override
    public PreparedFileModel transform(FileModel input, TruncateConfig config) {
        // Check if file extraction failed - propagate as extraction error
        if (!input.isFile()) {
            log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            final boolean isNameWithinLimit = input.getName().length() <= config.getNumberOfSymbols();
            String newName = switch (config.getTruncateOption()) {
                case REMOVE_SYMBOLS_IN_BEGIN -> {
                    if (isNameWithinLimit) {
                        yield "";  // Entire name removed
                    }
                    yield input.getName().substring(config.getNumberOfSymbols());
                }
                case REMOVE_SYMBOLS_FROM_END -> {
                    if (isNameWithinLimit) {
                        yield "";  // Entire name removed
                    }
                    yield input.getName().substring(0,
                                                    input.getName().length() - config.getNumberOfSymbols());
                }
                case TRUNCATE_EMPTY_SYMBOLS -> input.getName().trim();
            };

            if (newName.isEmpty()) {
                return buildErrorResult(input, "Truncation resulted in empty filename");
            }

            return PreparedFileModel.builder()
                                    .withOriginalFile(input)
                                    .withNewName(newName)
                                    .withNewExtension(input.getExtension())
                                    .withHasError(false)
                                    .withErrorMessage(null)
                                    .withTransformationMeta(buildMetadata(config))
                                    .build();

        } catch (Exception e) {
            log.error("Failed to truncate file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to truncate: " + e.getMessage());
        }
    }

    private TransformationMetadata buildMetadata(TruncateConfig config) {
        return TransformationMetadata.builder()
                                     .withMode(TransformationMode.TRUNCATE_FILE_NAME)
                                     .withAppliedAt(LocalDateTime.now())
                                     .withConfig(Map.of(
                                             "numberOfSymbols", config.getNumberOfSymbols(),
                                             "truncateOption", config.getTruncateOption().name()
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
