package ua.renamer.app.core.v2.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.AddTextConfig;
import ua.renamer.app.core.v2.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformer that adds text to the beginning or end of filenames.
 */
@Slf4j
public class AddTextTransformer implements FileTransformationService<AddTextConfig> {

    @Override
    public PreparedFileModel transform(FileModel input, AddTextConfig config) {
        // Check if file extraction failed - propagate as extraction error
        if (!input.isFile()) {
            log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            String newName = switch (config.getPosition()) {
                case BEGIN -> config.getTextToAdd() + input.getName();
                case END -> input.getName() + config.getTextToAdd();
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
            log.error("Failed to add text to file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to add text: " + e.getMessage());
        }
    }

    private TransformationMetadata buildMetadata(AddTextConfig config) {
        return TransformationMetadata.builder()
                                     .withMode(TransformationMode.ADD_TEXT)
                                     .withAppliedAt(LocalDateTime.now())
                                     .withConfig(Map.of(
                                             "textToAdd", config.getTextToAdd(),
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
