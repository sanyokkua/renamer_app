package ua.renamer.app.core.service.transformation;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.ImageDimensionOptions;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.ImageDimensionsConfig;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.api.model.meta.category.VideoMeta;
import ua.renamer.app.core.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformer that adds image/video dimensions to filenames.
 */
@Slf4j
public class ImageDimensionsTransformer implements FileTransformationService<ImageDimensionsConfig> {

    @Override
    public PreparedFileModel transform(FileModel input, ImageDimensionsConfig config) {
        if (config == null) {
            return buildErrorResult(input, "Transformer configuration must not be null");
        }
        if (!input.isFile()) {
            if (!"application/x-directory".equals(input.getDetectedMimeType())) {
                return buildErrorResult(input, "File extraction failed");
            }
            log.debug("Skipping image dimensions for directory: {}", input.getAbsolutePath());
            return buildPassThroughResult(input);
        }

        try {
            // Extract dimensions from metadata (support both image and video)
            Integer width = null;
            Integer height = null;

            var metadataOpt = input.getMetadata();
            if (metadataOpt.isPresent()) {
                var metadata = metadataOpt.get();

                var imageMetaOpt = metadata.getImageMeta();
                if (imageMetaOpt.isPresent()) {
                    ImageMeta imageMeta = imageMetaOpt.get();
                    width = imageMeta.getWidth().orElse(null);
                    height = imageMeta.getHeight().orElse(null);
                } else {
                    var videoMetaOpt = metadata.getVideoMeta();
                    if (videoMetaOpt.isPresent()) {
                        VideoMeta videoMeta = videoMetaOpt.get();
                        width = videoMeta.getWidth().orElse(null);
                        height = videoMeta.getHeight().orElse(null);
                    }
                }
            }

            if (width == null && height == null) {
                return buildErrorResult(input, "No image/video metadata available");
            }

            // Build dimension string (e.g., "1920x1080")
            String dimensionStr = buildDimensionString(width, height, config);

            if (dimensionStr.isEmpty()) {
                return buildErrorResult(input, "Cannot build dimension string from configuration");
            }

            // Apply to filename
            String newName = switch (config.getPosition()) {
                case BEGIN -> dimensionStr + config.getNameSeparator() + input.getName();
                case END -> input.getName() + config.getNameSeparator() + dimensionStr;
                case REPLACE -> dimensionStr;
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
            log.error("Failed to add dimensions to file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to add dimensions: " + e.getMessage());
        }
    }

    private String buildDimensionString(Integer width, Integer height, ImageDimensionsConfig config) {
        StringBuilder sb = new StringBuilder();

        // Left side
        if (config.getLeftSide() == ImageDimensionOptions.WIDTH && width != null) {
            sb.append(width);
        } else if (config.getLeftSide() == ImageDimensionOptions.HEIGHT && height != null) {
            sb.append(height);
        }

        // Add separator if we have left side value
        if (!sb.isEmpty() && config.getRightSide() != ImageDimensionOptions.DO_NOT_USE) {
            sb.append(config.getSeparator() != null ? config.getSeparator() : "x");
        }

        // Right side
        if (config.getRightSide() == ImageDimensionOptions.WIDTH && width != null) {
            sb.append(width);
        } else if (config.getRightSide() == ImageDimensionOptions.HEIGHT && height != null) {
            sb.append(height);
        }

        return sb.toString();
    }

    private TransformationMetadata buildMetadata(ImageDimensionsConfig config) {
        return TransformationMetadata.builder()
                .withMode(TransformationMode.USE_IMAGE_DIMENSIONS)
                .withAppliedAt(LocalDateTime.now())
                .withConfig(Map.of(
                        "leftSide", config.getLeftSide().name(),
                        "rightSide", config.getRightSide().name(),
                        "separator", config.getSeparator() != null ? config.getSeparator() : "x",
                        "position", config.getPosition().name(),
                        "nameSeparator", config.getNameSeparator()
                ))
                .build();
    }

    private PreparedFileModel buildPassThroughResult(FileModel input) {
        return PreparedFileModel.builder()
                .withOriginalFile(input)
                .withNewName(input.getName())
                .withNewExtension(input.getExtension())
                .withHasError(false)
                .withErrorMessage(null)
                .withTransformationMeta(null)
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
