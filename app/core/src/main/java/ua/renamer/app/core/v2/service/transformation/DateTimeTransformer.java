package ua.renamer.app.core.v2.service.transformation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.model.FileModel;
import ua.renamer.app.core.v2.model.PreparedFileModel;
import ua.renamer.app.core.v2.model.TransformationMetadata;
import ua.renamer.app.core.v2.model.TransformationMode;
import ua.renamer.app.core.v2.model.config.DateTimeConfig;
import ua.renamer.app.core.v2.model.meta.category.ImageMeta;
import ua.renamer.app.core.v2.model.meta.category.VideoMeta;
import ua.renamer.app.core.v2.service.FileTransformationService;
import ua.renamer.app.core.v2.util.DateTimeConverter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transformer that adds datetime to filenames from various sources.
 * Uses V2 DateTimeConverter for formatting.
 */
@Slf4j
@RequiredArgsConstructor
public class DateTimeTransformer implements FileTransformationService<DateTimeConfig> {

    private final DateTimeConverter dateTimeConverter;

    @Override
    public PreparedFileModel transform(FileModel input, DateTimeConfig config) {
        // Check if file extraction failed - propagate as extraction error
        if (!input.isFile()) {
            log.debug("Propagating extraction error for: {}", input.getAbsolutePath());
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            // Extract datetime based on source
            LocalDateTime dateTime = extractDateTime(input, config);

            if (dateTime == null) {
                return buildErrorResult(input,
                                        "No datetime available for source: " + config.getSource());
            }

            // Format datetime using DateTimeConverter
            String formattedDateTime = dateTimeConverter.formatDateTime(
                    dateTime,
                    config.getDateFormat(),
                    config.getTimeFormat(),
                    config.getDateTimeFormat()
            );

            if (formattedDateTime == null || formattedDateTime.isEmpty()) {
                return buildErrorResult(input, "Failed to format datetime");
            }

            // Apply to filename based on position
            String newName = switch (config.getPosition()) {
                case BEGIN -> formattedDateTime + config.getSeparator() + input.getName();
                case END -> input.getName() + config.getSeparator() + formattedDateTime;
                case REPLACE -> formattedDateTime;
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
            log.error("Failed to add datetime to file: {}", input.getName(), e);
            return buildErrorResult(input, "Failed to add datetime: " + e.getMessage());
        }
    }

    private LocalDateTime extractDateTime(FileModel input, DateTimeConfig config) {
        return switch (config.getSource()) {
            case FILE_CREATION_DATE -> input.getCreationDate().orElse(null);
            case FILE_MODIFICATION_DATE -> input.getModificationDate().orElse(null);
            case CONTENT_CREATION_DATE -> {
                var metadataOpt = input.getMetadata();
                if (metadataOpt.isPresent()) {
                    var metadata = metadataOpt.get();
                    var imageMetaOpt = metadata.getImageMeta();
                    if (imageMetaOpt.isPresent()) {
                        yield imageMetaOpt.flatMap(ImageMeta::getContentCreationDate).orElse(null);
                    }
                    var videoMetaOpt = metadata.getVideoMeta();
                    if (videoMetaOpt.isPresent()) {
                        yield videoMetaOpt.flatMap(VideoMeta::getContentCreationDate).orElse(null);
                    }
                }
                yield null;
            }
            case CURRENT_DATE -> LocalDateTime.now();
            case CUSTOM_DATE -> config.getCustomDateTime().orElse(null);
        };
    }

    private TransformationMetadata buildMetadata(DateTimeConfig config) {
        return TransformationMetadata.builder()
                                     .withMode(TransformationMode.USE_DATETIME)
                                     .withAppliedAt(LocalDateTime.now())
                                     .withConfig(Map.of(
                                             "source", config.getSource().name(),
                                             "dateFormat", config.getDateFormat().name(),
                                             "timeFormat", config.getTimeFormat().name(),
                                             "dateTimeFormat", config.getDateTimeFormat().name(),
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
