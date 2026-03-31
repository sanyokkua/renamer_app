package ua.renamer.app.metadata.extractor.strategy.format.video;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import ua.renamer.app.metadata.extractor.strategy.format.ExtractionResult;
import ua.renamer.app.metadata.extractor.strategy.format.MetadataCommons;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;
import ua.renamer.app.metadata.interfaces.FileMetadataExtractor;
import ua.renamer.app.metadata.model.meta.FileMeta;
import ua.renamer.app.metadata.model.meta.category.VideoMeta;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public abstract class BaseVideoMetadataExtractor implements FileMetadataExtractor {

    protected final DateTimeUtils dateTimeUtils;

    @Override
    public FileMeta extract(File file, String mimeType) {
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (IOException e) {
            log.warn("File I/O error processing video: {}", file.getAbsolutePath(), e);
            return FileMeta.withError(e);
        } catch (ImageProcessingException e) {
            log.warn("Video processing failed for: {}", file.getName(), e);
            return FileMeta.withError(e);
        }

        var directories = extractAvailableDirectories(metadata);
        List<String> errors = new ArrayList<>();

        // Extract metadata with error tracking
        var creationDate = extractDateTimeSafely(directories);
        var width = extractWidthSafely(directories);
        var height = extractHeightSafely(directories);
        var duration = extractDurationSafely(directories);

        // Collect any extraction errors
        collectError(creationDate, "extracting video creation date", errors);
        collectError(width, "extracting video width", errors);
        collectError(height, "extracting video height", errors);
        collectError(duration, "extracting video duration", errors);

        // Build video metadata
        var videoMeta = VideoMeta.builder()
                                 .withContentCreationDate(creationDate.value())
                                 .withWidth(width.value())
                                 .withHeight(height.value())
                                 .withDuration(duration.value())
                                 .build();

        // Extract all metadata tags as a map
        var metadataMap = MetadataCommons.buildMetadataMap(metadata);

        return FileMeta.builder().withVideo(videoMeta).withErrors(errors).withMetaInfo(metadataMap).build();
    }

    private void collectError(ExtractionResult<?> result, String operation, List<String> errors) {
        if (result.hasError()) {
            log.debug("Error when {}: {}", operation, result.errorMessage());
            errors.add(result.errorMessage());
        }
    }

    protected ExtractionResult<LocalDateTime> extractDateTimeSafely(List<? extends Directory> directories) {
        try {
            var dateTime = extractCreationDateTime(directories);
            return ExtractionResult.success(dateTime);
        } catch (RuntimeException e) {
            log.debug("Unexpected error extracting creation date", e);
            return ExtractionResult.failure("Failed to extract creation date: " + e.getMessage());
        }
    }

    protected ExtractionResult<Integer> extractWidthSafely(List<? extends Directory> directories) {
        try {
            var width = extractVideoWidth(directories);
            return ExtractionResult.success(width);
        } catch (RuntimeException e) {
            log.debug("Unexpected error extracting width", e);
            return ExtractionResult.failure("Failed to extract width: " + e.getMessage());
        }
    }

    protected ExtractionResult<Integer> extractHeightSafely(List<? extends Directory> directories) {
        try {
            var height = extractVideoHeight(directories);
            return ExtractionResult.success(height);
        } catch (RuntimeException e) {
            log.debug("Unexpected error extracting height", e);
            return ExtractionResult.failure("Failed to extract height: " + e.getMessage());
        }
    }

    protected ExtractionResult<Integer> extractDurationSafely(List<? extends Directory> directories) {
        try {
            var duration = extractDuration(directories);
            return ExtractionResult.success(duration);
        } catch (RuntimeException e) {
            log.debug("Unexpected error extracting duration", e);
            return ExtractionResult.failure("Failed to extract duration: " + e.getMessage());
        }
    }

    // Abstract methods for subclass customization
    protected abstract List<Class<? extends Directory>> getAvailableDirectories();

    protected abstract List<Integer> getContentCreationTags();

    protected abstract List<Integer> getVideoWidthTags();

    protected abstract List<Integer> getVideoHeightTags();

    protected abstract List<Integer> getDurationTags();

    private List<? extends Directory> extractAvailableDirectories(Metadata metadata) {
        return getAvailableDirectories().stream()
                                        .map(metadata::getFirstDirectoryOfType)
                                        .filter(Objects::nonNull)
                                        .toList();
    }

    @Nullable
    protected LocalDateTime extractCreationDateTime(List<? extends Directory> directories) {
        var tags = getContentCreationTags();
        var result = findStringValues(directories, tags);

        return result.stream()
                     .map(dateTimeUtils::parseDateTimeString)
                     .filter(Objects::nonNull)
                     .min(LocalDateTime::compareTo)
                     .orElse(null);
    }

    @Nullable
    protected Integer extractVideoWidth(List<? extends Directory> directories) {
        var tags = getVideoWidthTags();
        var result = findIntegerValues(directories, tags);

        return result.stream().filter(Objects::nonNull).min(Integer::compareTo).orElse(null);
    }

    @Nullable
    protected Integer extractVideoHeight(List<? extends Directory> directories) {
        var tags = getVideoHeightTags();
        var result = findIntegerValues(directories, tags);

        return result.stream().filter(Objects::nonNull).min(Integer::compareTo).orElse(null);
    }

    @Nullable
    protected Integer extractDuration(List<? extends Directory> directories) {
        var tags = getDurationTags();
        var result = findIntegerValues(directories, tags);

        // Duration might be in milliseconds, convert to seconds if > 10000 (arbitrary threshold)
        return result.stream()
                     .filter(Objects::nonNull)
                     .map(d -> d > 10000 ? d / 1000 : d)
                     .min(Integer::compareTo)
                     .orElse(null);
    }

    private List<String> findStringValues(List<? extends Directory> directories, List<Integer> tags) {
        return tags.stream()
                   .filter(Objects::nonNull)
                   .flatMap(tag -> directories.stream()
                                              .filter(Objects::nonNull)
                                              .map(dir -> dir.getString(tag))
                                              .filter(Objects::nonNull))
                   .toList();
    }

    private List<Integer> findIntegerValues(List<? extends Directory> directories, List<Integer> tags) {
        return tags.stream()
                   .filter(Objects::nonNull)
                   .flatMap(tag -> directories.stream()
                                              .filter(Objects::nonNull)
                                              .map(dir -> dir.getInteger(tag))
                                              .filter(Objects::nonNull))
                   .toList();
    }
}
