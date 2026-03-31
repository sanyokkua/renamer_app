package ua.renamer.app.metadata.extractor.strategy.format.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifImageDirectory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import ua.renamer.app.metadata.extractor.strategy.format.ExtractionResult;
import ua.renamer.app.metadata.extractor.strategy.format.MetadataCommons;
import ua.renamer.app.metadata.interfaces.DateTimeUtils;
import ua.renamer.app.metadata.interfaces.FileMetadataExtractor;
import ua.renamer.app.metadata.model.meta.FileMeta;
import ua.renamer.app.metadata.model.meta.category.ImageMeta;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
public abstract class BaseImageMetadataExtractor implements FileMetadataExtractor {

    protected final DateTimeUtils dateTimeUtils;

    @Override
    public FileMeta extract(File file, String mimeType) {
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (IOException e) {
            log.warn("File I/O error processing image: {}", file.getAbsolutePath(), e);
            return FileMeta.withError(e);
        } catch (ImageProcessingException e) {
            log.warn("Image processing failed for: {}", file.getName(), e);
            return FileMeta.withError(e);
        }

        var exifDirectories = extractAvailableExifDirectories(metadata);
        List<String> errors = new ArrayList<>();

        // Extract metadata with error tracking
        var width = extractWidthSafely(metadata, exifDirectories);
        var height = extractHeightSafely(metadata, exifDirectories);
        var creationDate = extractDateTimeSafely(exifDirectories);

        // Collect any extraction errors
        collectError(width, "extracting image width", errors);
        collectError(height, "extracting image height", errors);
        collectError(creationDate, "extracting image creation date", errors);

        // Build image metadata
        var imgMeta = ImageMeta.builder()
                               .withWidth(width.value())
                               .withHeight(height.value())
                               .withContentCreationDate(creationDate.value())
                               .build();

        // Extract all metadata tags as a map
        var metadataMap = MetadataCommons.buildMetadataMap(metadata);

        return FileMeta.builder().withImage(imgMeta).withErrors(errors).withMetaInfo(metadataMap).build();
    }

    private void collectError(ExtractionResult<?> result, String operation, List<String> errors) {
        if (result.hasError()) {
            log.debug("Error when {}: {}", operation, result.errorMessage());
            errors.add(result.errorMessage());
        }
    }

    protected ExtractionResult<Integer> extractWidthSafely(Metadata metadata, List<ExifDirectoryBase> directories) {
        try {
            var width = extractWidth(metadata, directories);
            return ExtractionResult.success(width);
        } catch (RuntimeException e) {
            log.debug("Unexpected error extracting width", e);
            return ExtractionResult.failure("Failed to extract width: " + e.getMessage());
        }
    }

    protected ExtractionResult<Integer> extractHeightSafely(Metadata metadata, List<ExifDirectoryBase> directories) {
        try {
            var height = extractHeight(metadata, directories);
            return ExtractionResult.success(height);
        } catch (RuntimeException e) {
            log.debug("Unexpected error extracting height", e);
            return ExtractionResult.failure("Failed to extract height: " + e.getMessage());
        }
    }

    protected ExtractionResult<LocalDateTime> extractDateTimeSafely(List<ExifDirectoryBase> directories) {
        try {
            var dateTime = extractCreationDateTimeFromExif(directories);
            return ExtractionResult.success(dateTime);
        } catch (RuntimeException e) {
            log.debug("Unexpected error extracting creation date", e);
            return ExtractionResult.failure("Failed to extract creation date: " + e.getMessage());
        }
    }

    @Nullable
    protected Integer extractWidth(Metadata metadata, List<ExifDirectoryBase> directories) {
        return extractIntFromBaseDirectory(metadata, getBaseWidthTag()).or(() -> extractWidthFromExif(directories))
                                                                       .orElse(null);
    }

    @Nullable
    protected Integer extractHeight(Metadata metadata, List<ExifDirectoryBase> directories) {
        return extractIntFromBaseDirectory(metadata, getBaseHeightTag()).or(() -> extractHeightFromExif(directories))
                                                                        .orElse(null);
    }

    private Optional<Integer> extractIntFromBaseDirectory(Metadata metadata, Integer tag) {
        if (tag == null) {
            return Optional.empty();
        }

        Class<? extends Directory> baseClass = getBaseDirectoryClass();
        Directory directory = metadata.getFirstDirectoryOfType(baseClass);

        if (directory == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(directory.getInteger(tag));
    }

    // Abstract methods for subclass customization
    protected abstract Class<? extends Directory> getBaseDirectoryClass();

    protected abstract Integer getBaseWidthTag();

    protected abstract Integer getBaseHeightTag();

    private Optional<Integer> extractWidthFromExif(List<ExifDirectoryBase> directories) {
        var tagImageWidth = findIntegerInDirectories(directories, ExifDirectoryBase.TAG_IMAGE_WIDTH);
        var tagExifImageWidth = findIntegerInDirectories(directories, ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH);

        return tagImageWidth.or(() -> tagExifImageWidth);
    }

    private Optional<Integer> extractHeightFromExif(List<ExifDirectoryBase> directories) {
        var tagImageHeight = findIntegerInDirectories(directories, ExifDirectoryBase.TAG_IMAGE_HEIGHT);
        var tagExifImageHeight = findIntegerInDirectories(directories, ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT);

        return tagImageHeight.or(() -> tagExifImageHeight);
    }

    private Optional<Integer> findIntegerInDirectories(List<ExifDirectoryBase> directories, int tag) {
        return directories.stream().map(dir -> dir.getInteger(tag)).filter(Objects::nonNull).findFirst();
    }

    private List<ExifDirectoryBase> extractAvailableExifDirectories(Metadata metadata) {
        var exifImageDirectory = metadata.getFirstDirectoryOfType(ExifImageDirectory.class);
        var exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        var exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        return Stream.of(exifImageDirectory, exifIFD0Directory, exifSubIFDDirectory).filter(Objects::nonNull).toList();
    }

    @Nullable
    protected LocalDateTime extractCreationDateTimeFromExif(List<ExifDirectoryBase> directories) {
        var originalDateTime = findDateTimeInDirectories(directories,
                                                         ExifDirectoryBase.TAG_DATETIME_ORIGINAL,
                                                         ExifDirectoryBase.TAG_TIME_ZONE_ORIGINAL);

        var imageDateTime = findDateTimeInDirectories(directories,
                                                      ExifDirectoryBase.TAG_DATETIME,
                                                      ExifDirectoryBase.TAG_TIME_ZONE);

        var digitizedDateTime = findDateTimeInDirectories(directories,
                                                          ExifDirectoryBase.TAG_DATETIME_DIGITIZED,
                                                          ExifDirectoryBase.TAG_TIME_ZONE_DIGITIZED);

        return dateTimeUtils.findMinOrNull(originalDateTime.orElse(null),
                                           imageDateTime.orElse(null),
                                           digitizedDateTime.orElse(null));
    }

    private Optional<LocalDateTime> findDateTimeInDirectories(List<ExifDirectoryBase> directories, int dateTimeTag, int offsetTag) {

        return directories.stream()
                          .map(dir -> new ExifDateTime(dir.getString(dateTimeTag), dir.getString(offsetTag)))
                          .filter(dt -> dt.dateTime() != null)
                          .map(dt -> dateTimeUtils.parseDateTimeString(dt.dateTime(), dt.timeZone()))
                          .filter(Objects::nonNull)
                          .filter(dt -> dt.isAfter(dateTimeUtils.getMinimalDateTime()))
                          .min(LocalDateTime::compareTo);
    }

    /**
     * Represents an EXIF date-time value with optional timezone offset.
     */
    protected record ExifDateTime(String dateTime, @Nullable String timeZone) {
    }
}
