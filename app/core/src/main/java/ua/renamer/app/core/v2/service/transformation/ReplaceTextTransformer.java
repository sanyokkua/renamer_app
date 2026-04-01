package ua.renamer.app.core.v2.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.ReplaceTextConfig;
import ua.renamer.app.core.v2.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformer that replaces text in filenames at specified positions.
 */
@Slf4j
public class ReplaceTextTransformer implements FileTransformationService<ReplaceTextConfig> {

    @Override
    public PreparedFileModel transform(FileModel input, ReplaceTextConfig config) {
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
                    if (input.getName().startsWith(config.getTextToReplace())) {
                        yield config.getReplacementText() +
                                input.getName().substring(config.getTextToReplace().length());
                    }
                    yield input.getName();
                }
                case END -> {
                    if (input.getName().endsWith(config.getTextToReplace())) {
                        yield input.getName().substring(0,
                                input.getName().length() - config.getTextToReplace().length()) +
                                config.getReplacementText();
                    }
                    yield input.getName();
                }
                case EVERYWHERE -> input.getName().replace(
                        config.getTextToReplace(),
                        config.getReplacementText()
                );
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
            log.error("Failed to replace text in file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to replace text: " + e.getMessage());
        }
    }

    private TransformationMetadata buildMetadata(ReplaceTextConfig config) {
        return TransformationMetadata.builder()
                .withMode(TransformationMode.REPLACE_TEXT)
                .withAppliedAt(LocalDateTime.now())
                .withConfig(Map.of(
                        "textToReplace", config.getTextToReplace(),
                        "replacementText", config.getReplacementText(),
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
