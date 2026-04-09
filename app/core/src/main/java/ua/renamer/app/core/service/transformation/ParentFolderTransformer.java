package ua.renamer.app.core.service.transformation;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.ParentFolderConfig;
import ua.renamer.app.core.service.FileTransformationService;

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
        if (config == null) {
            return buildErrorResult(input, "Transformer configuration must not be null");
        }
        if (!input.isFile() && !"application/x-directory".equals(input.getDetectedMimeType())) {
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            List<String> parentNames = getParentNames(input, config);

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

    private static @NonNull List<String> getParentNames(FileModel input, ParentFolderConfig config) {
        Path filePath = input.getFile().toPath();
        List<String> parentNames = new ArrayList<>();

        // Collect parent folder names
        Path parent = filePath.getParent();
        for (int i = 0; i < config.getNumberOfParentFolders() && parent != null; i++) {
            Path fileNamePath = parent.getFileName();
            if (fileNamePath != null) {
                parentNames.add(fileNamePath.toString());
            }
            parent = parent.getParent();
        }
        return parentNames;
    }

    private TransformationMetadata buildMetadata(ParentFolderConfig config) {
        return TransformationMetadata.builder()
                .withMode(TransformationMode.ADD_FOLDER_NAME)
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
