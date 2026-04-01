package ua.renamer.app.core.v2.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.RemoveTextConfig;
import ua.renamer.app.core.v2.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformer that removes text from the beginning or end of filenames.
 */
@Slf4j
public class RemoveTextTransformer implements FileTransformationService<RemoveTextConfig> {

    @Override
    public PreparedFileModel transform(FileModel input, RemoveTextConfig config) {
        if (config == null) {
            return buildErrorResult(input, "Transformer configuration must not be null");
        }
        // Check if file extraction failed - propagate as extraction error
        if (!input.isFile()) {
            log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            String newName = switch (config.getPosition()) {
                case BEGIN -> {
                    if (input.getName().startsWith(config.getTextToRemove())) {
                        yield input.getName().substring(config.getTextToRemove().length());
                    }
                    yield input.getName();
                }
                case END -> {
                    if (input.getName().endsWith(config.getTextToRemove())) {
                        yield input.getName().substring(0,
                                                        input.getName().length() - config.getTextToRemove().length());
                    }
                    yield input.getName();
                }
            };

            return PreparedFileModel.builder()
                                    .withOriginalFile(input)
                                    .withNewName(newName)
                                    .withNewExtension(input.getExtension())
                                    .withHasError(false)
                                    .withErrorMessage(null)
                                    .withTransformationMeta(buildMetadata(config))
                                    .build();

        } catch (Exception e) {
            log.error("Failed to remove text from file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to remove text: " + e.getMessage());
        }
    }

    private TransformationMetadata buildMetadata(RemoveTextConfig config) {
        return TransformationMetadata.builder()
                                     .withMode(TransformationMode.REMOVE_TEXT)
                                     .withAppliedAt(LocalDateTime.now())
                                     .withConfig(Map.of(
                                             "textToRemove", config.getTextToRemove(),
                                             "position", config.getPosition().name()
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
