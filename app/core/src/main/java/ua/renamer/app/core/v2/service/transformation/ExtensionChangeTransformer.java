package ua.renamer.app.core.v2.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.model.FileModel;
import ua.renamer.app.core.v2.model.PreparedFileModel;
import ua.renamer.app.core.v2.model.TransformationMetadata;
import ua.renamer.app.core.v2.model.TransformationMode;
import ua.renamer.app.core.v2.model.config.ExtensionChangeConfig;
import ua.renamer.app.core.v2.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformer that changes file extensions.
 */
@Slf4j
public class ExtensionChangeTransformer implements FileTransformationService<ExtensionChangeConfig> {

    @Override
    public PreparedFileModel transform(FileModel input, ExtensionChangeConfig config) {
        // Check if file extraction failed - propagate as extraction error
        if (!input.isFile()) {
            log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            String newExtension = config.getNewExtension().trim();

            // Remove leading dot if present
            if (newExtension.startsWith(".")) {
                newExtension = newExtension.substring(1);
            }

            if (newExtension.isEmpty()) {
                return buildErrorResult(input, "New extension cannot be empty");
            }

            return PreparedFileModel.builder()
                                    .withOriginalFile(input)
                                    .withNewName(input.getName())
                                    .withNewExtension(newExtension)
                                    .withHasError(false)
                                    .withErrorMessage(null)
                                    .withTransformationMeta(buildMetadata(config))
                                    .build();

        } catch (Exception e) {
            log.error("Failed to change extension of file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to change extension: " + e.getMessage());
        }
    }

    private TransformationMetadata buildMetadata(ExtensionChangeConfig config) {
        return TransformationMetadata.builder()
                                     .withMode(TransformationMode.CHANGE_EXTENSION)
                                     .withAppliedAt(LocalDateTime.now())
                                     .withConfig(Map.of(
                                             "newExtension", config.getNewExtension()
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
