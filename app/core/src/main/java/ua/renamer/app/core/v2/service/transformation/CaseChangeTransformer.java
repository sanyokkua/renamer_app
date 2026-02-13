package ua.renamer.app.core.v2.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.model.FileModel;
import ua.renamer.app.core.v2.model.PreparedFileModel;
import ua.renamer.app.core.v2.model.TransformationMetadata;
import ua.renamer.app.core.v2.model.TransformationMode;
import ua.renamer.app.core.v2.model.config.CaseChangeConfig;
import ua.renamer.app.core.v2.service.FileTransformationService;
import ua.renamer.app.core.v2.util.StringUtilsV2;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformer that changes the case of filenames.
 * Uses existing StringUtils for case transformations.
 */
@Slf4j
public class CaseChangeTransformer implements FileTransformationService<CaseChangeConfig> {

    @Override
    public PreparedFileModel transform(FileModel input, CaseChangeConfig config) {
        // Check if file extraction failed - propagate as extraction error
        if (!input.isFile()) {
            log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            String newName = StringUtilsV2.toProvidedCase(input.getName(), config.getCaseOption());

            if (config.isCapitalizeFirstLetter() && !newName.isEmpty()) {
                newName = Character.toUpperCase(newName.charAt(0)) + newName.substring(1);
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
            log.error("Failed to change case of file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to change case: " + e.getMessage());
        }
    }

    private TransformationMetadata buildMetadata(CaseChangeConfig config) {
        return TransformationMetadata.builder()
                                     .withMode(TransformationMode.CHANGE_CASE)
                                     .withAppliedAt(LocalDateTime.now())
                                     .withConfig(Map.of(
                                             "caseOption", config.getCaseOption().name(),
                                             "capitalizeFirstLetter", config.isCapitalizeFirstLetter()
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
