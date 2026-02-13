package ua.renamer.app.core.v2.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.model.FileModel;
import ua.renamer.app.core.v2.model.PreparedFileModel;
import ua.renamer.app.core.v2.model.TransformationMetadata;
import ua.renamer.app.core.v2.model.TransformationMode;
import ua.renamer.app.core.v2.model.config.ParentFolderConfig;
import ua.renamer.app.core.v2.service.FileTransformationService;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Transformer that adds parent folder name(s) to filenames.
 */
@Slf4j
public class ParentFolderTransformer implements FileTransformationService<ParentFolderConfig> {

    @Override
    public PreparedFileModel transform(FileModel input, ParentFolderConfig config) {
        // Check if file extraction failed - propagate as extraction error
        if (!input.isFile()) {
            log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            Path filePath = input.getFile().toPath();
            List<String> parentNames = new ArrayList<>();

            // Collect parent folder names
            Path parent = filePath.getParent();
            for (int i = 0; i < config.getNumberOfParentFolders() && parent != null; i++) {
                if (parent.getFileName() != null) {
                    parentNames.add(parent.getFileName().toString());
                }
                parent = parent.getParent();
            }

            if (parentNames.isEmpty()) {
                return buildErrorResult(input, "No parent folders available");
            }

            // Reverse to get correct order (closest parent first)
            Collections.reverse(parentNames);
            String parentString = String.join(config.getSeparator(), parentNames);

            // Apply to filename
            String newName = switch (config.getPosition()) {
                case BEGIN -> parentString + config.getSeparator() + input.getName();
                case END -> input.getName() + config.getSeparator() + parentString;
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
            log.error("Failed to add parent folder to file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to add parent folder: " + e.getMessage());
        }
    }

    private TransformationMetadata buildMetadata(ParentFolderConfig config) {
        return TransformationMetadata.builder()
                                     .withMode(TransformationMode.USE_PARENT_FOLDER_NAME)
                                     .withAppliedAt(LocalDateTime.now())
                                     .withConfig(Map.of(
                                             "numberOfParentFolders", config.getNumberOfParentFolders(),
                                             "position", config.getPosition().name(),
                                             "separator", config.getSeparator()
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
