package ua.renamer.app.core.service.transformation;

import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.TimeFormat;
import ua.renamer.app.api.interfaces.DateTimeUtils;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.DateTimeConfig;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.api.model.meta.category.VideoMeta;
import ua.renamer.app.core.service.FileTransformationService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Transformer that adds datetime to filenames from various sources.
 * Uses V2 DateTimeConverter for formatting.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DateTimeTransformer implements FileTransformationService<DateTimeConfig> {

    private static final List<TimeFormat> AM_PM_FORMATS = List.of(
            TimeFormat.HH_MM_SS_AM_PM_TOGETHER,
            TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED,
            TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED,
            TimeFormat.HH_MM_SS_AM_PM_DOTTED,
            TimeFormat.HH_MM_SS_AM_PM_DASHED,
            TimeFormat.HH_MM_AM_PM_TOGETHER,
            TimeFormat.HH_MM_AM_PM_WHITE_SPACED,
            TimeFormat.HH_MM_AM_PM_UNDERSCORED,
            TimeFormat.HH_MM_AM_PM_DOTTED,
            TimeFormat.HH_MM_AM_PM_DASHED
    );

    private final DateTimeUtils dateTimeConverter;

    @Override
    public PreparedFileModel transform(FileModel input, DateTimeConfig config) {
        if (config == null) {
            return buildErrorResult(input, "Transformer configuration must not be null");
        }
        if (!input.isFile() && !"application/x-directory".equals(input.getDetectedMimeType())) {
            return buildErrorResult(input, "File extraction failed");
        }

        try {
            // Extract datetime based on source
            LocalDateTime dateTime = extractDateTime(input, config);

            if (dateTime == null) {
                if (!input.isFile()) {
                    log.debug("Skipping datetime for directory (source unavailable): {}", input.getAbsolutePath());
                    return buildPassThroughResult(input);
                }
                return buildErrorResult(input, "No datetime available for source: " + config.getSource());
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

            if (isAmPmFormat(config.getTimeFormat())) {
                formattedDateTime = config.isUseUppercaseForAmPm()
                        ? formattedDateTime.toUpperCase(java.util.Locale.ROOT)
                        : formattedDateTime.toLowerCase(java.util.Locale.ROOT);
            }

            // Apply to filename based on position
            String newName = switch (config.getPosition()) {
                case BEGIN -> formattedDateTime + config.getSeparator() + input.getName();
                case END -> input.getName() + config.getSeparator() + formattedDateTime;
                case REPLACE -> formattedDateTime;
            };

            // Apply to extension if requested; otherwise preserve original
            String newExtension = config.isApplyToExtension()
                    ? switch (config.getPosition()) {
                case BEGIN -> formattedDateTime + config.getSeparator() + input.getExtension();
                case END -> input.getExtension() + config.getSeparator() + formattedDateTime;
                case REPLACE -> formattedDateTime;
            }
                    : input.getExtension();

            return PreparedFileModel.builder()
                    .withOriginalFile(input)
                    .withNewName(newName)
                    .withNewExtension(newExtension)
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
        LocalDateTime dateTime = switch (config.getSource()) {
            case FILE_CREATION_DATE -> input.getCreationDate().orElse(null);
            case FILE_MODIFICATION_DATE -> input.getModificationDate().orElse(null);
            case CONTENT_CREATION_DATE -> extractContentCreationDate(input);
            case CURRENT_DATE -> LocalDateTime.now();
            case CUSTOM_DATE -> config.getCustomDateTime().orElse(null);
        };

        if (dateTime == null && config.isUseFallbackDateTime()) {
            if (config.isUseCustomDateTimeAsFallback() && config.getCustomDateTime().orElse(null) != null) {
                dateTime = config.getCustomDateTime().orElse(null); // Custom date as guaranteed fallback
            } else {
                LocalDateTime creation = input.getCreationDate().orElse(null);
                LocalDateTime modification = input.getModificationDate().orElse(null);
                LocalDateTime contentCreation = extractContentCreationDate(input);
                dateTime = Stream.of(creation, modification, contentCreation)
                        .filter(Objects::nonNull)
                        .min(Comparator.naturalOrder())
                        .orElse(null);
            }
        }

        return dateTime;
    }

    private LocalDateTime extractContentCreationDate(FileModel input) {
        return input.getMetadata()
                .flatMap(meta -> meta.getImageMeta()
                        .flatMap(ImageMeta::getContentCreationDate)
                        .or(() -> meta.getVideoMeta()
                                .flatMap(VideoMeta::getContentCreationDate)))
                .orElse(null);
    }

    private boolean isAmPmFormat(TimeFormat timeFormat) {
        return AM_PM_FORMATS.contains(timeFormat);
    }

    private TransformationMetadata buildMetadata(DateTimeConfig config) {
        return TransformationMetadata.builder()
                .withMode(TransformationMode.ADD_DATETIME)
                .withAppliedAt(LocalDateTime.now())
                .withConfig(Map.of(
                        "source", config.getSource().name(),
                        "dateFormat", config.getDateFormat().name(),
                        "timeFormat", config.getTimeFormat().name(),
                        "dateTimeFormat", config.getDateTimeFormat() != null
                                ? config.getDateTimeFormat().name() : "null",
                        "position", config.getPosition().name(),
                        "separator", config.getSeparator() != null ? config.getSeparator() : ""
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
