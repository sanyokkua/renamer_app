package ua.renamer.app.core.mappers.images;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifImageDirectory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.abstracts.FileToMetadataMapper;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static ua.renamer.app.core.utils.Utils.findMinOrNull;

@Slf4j
public abstract class CommonImageMapper extends FileToMetadataMapper {

    private List<ExifDirectoryBase> getExifDirectories(File input) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            if (Objects.isNull(metadata)) {
                return List.of();
            }

            ExifImageDirectory exifImageDirectory = metadata.getFirstDirectoryOfType(ExifImageDirectory.class);
            ExifIFD0Directory exifIfd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            ExifSubIFDDirectory exifSubIfdDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            return Stream.of(exifImageDirectory, exifIfd0Directory, exifSubIfdDirectory)
                         .filter(Objects::nonNull)
                         .toList();
        } catch (Exception e) {
            log.warn("Failed to load exif directories", e);
            return List.of();
        }
    }

    private Optional<Long> findDateInSubDirectories(File input, int tag) {
        var result = getExifDirectories(input).stream()
                                              .map(dir -> dir.getDate(tag))
                                              .filter(Objects::nonNull)
                                              .map(Date::getTime)
                                              .min(Long::compareTo)
                                              .orElse(null);
        // TODO: think about processing zone info
        return Optional.ofNullable(result);
    }

    private Optional<Integer> findIntegerInSubDirectories(File input, int tag) {
        var result = getExifDirectories(input).stream()
                                              .map(dir -> dir.getInteger(tag))
                                              .filter(Objects::nonNull)
                                              .findFirst()
                                              .orElse(null);

        return Optional.ofNullable(result);
    }

    protected Long getContentCreationDateTimeFromExif(File input) {
        var exifDateTimeOriginal = findDateInSubDirectories(input, ExifDirectoryBase.TAG_DATETIME_ORIGINAL);
        var exifImageDateTime = findDateInSubDirectories(input, ExifDirectoryBase.TAG_DATETIME);
        var exifDateTimeDigitized = findDateInSubDirectories(input, ExifDirectoryBase.TAG_DATETIME_DIGITIZED);

        var dateTimeValue = findMinOrNull(exifDateTimeOriginal.orElse(null),
                                          exifImageDateTime.orElse(null),
                                          exifDateTimeDigitized.orElse(null)
                                         );

        log.debug("dateTimeValue: {}", dateTimeValue);
        return dateTimeValue;
    }

    private Integer getWidthFromExif(File input) {
        var val1 = findIntegerInSubDirectories(input, ExifDirectoryBase.TAG_IMAGE_WIDTH).orElse(null);
        var val2 = findIntegerInSubDirectories(input, ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH).orElse(null);

        return Stream.of(val1, val2).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private Integer getHeightFromExif(File input) {
        var val1 = findIntegerInSubDirectories(input, ExifDirectoryBase.TAG_IMAGE_HEIGHT).orElse(null);
        var val2 = findIntegerInSubDirectories(input, ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT).orElse(null);

        return Stream.of(val1, val2).filter(Objects::nonNull).findFirst().orElse(null);
    }

    protected abstract Class<? extends Directory> getBaseDirectory();
    protected abstract Integer getWidthTag();
    protected abstract Integer getHeightTag();

    private Optional<Integer> getBaseWidth(File input) {
        Class<? extends Directory> baseDirectory = getBaseDirectory();
        Integer widthTag = getWidthTag();

        return getBaseValue(input, baseDirectory, widthTag);
    }

    private Optional<Integer> getBaseHeight(File input) {
        Class<? extends Directory> baseDirectory = getBaseDirectory();
        Integer heightTag = getHeightTag();

        return getBaseValue(input, baseDirectory, heightTag);
    }

    private Optional<Integer> getBaseValue(File input, Class<? extends Directory> baseDirectory, Integer tag) {
        if (Objects.isNull(input) || Objects.isNull(baseDirectory) || Objects.isNull(tag)) {
            return Optional.empty();
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            var dir = metadata.getFirstDirectoryOfType(baseDirectory);
            return Optional.ofNullable(dir.getInteger(tag));

        } catch (ImageProcessingException | IOException e) {
            log.warn("Failed to getBaseValue", e);
            return Optional.empty();
        }
    }

    protected Integer getWidth(File input) {
        var customWidth = getBaseWidth(input);
        return customWidth.orElseGet(() -> getWidthFromExif(input));
    }

    protected Integer getHeight(File input) {
        var customWidth = getBaseHeight(input);
        return customWidth.orElseGet(() -> getHeightFromExif(input));
    }

    @Override
    public FileInformationMetadata process(File input) {
        var height = getHeight(input);
        var width = getWidth(input);
        var dateTime = getContentCreationDateTimeFromExif(input);

        return FileInformationMetadata.builder()
                                      .creationDate(dateTime)
                                      .imgVidWidth(width)
                                      .imgVidHeight(height)
                                      .build();
    }

}
