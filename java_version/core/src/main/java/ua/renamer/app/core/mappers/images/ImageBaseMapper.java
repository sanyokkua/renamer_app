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
import ua.renamer.app.core.utils.DateTimeUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static ua.renamer.app.core.utils.Utils.findMinOrNull;

@Slf4j
public abstract class ImageBaseMapper extends FileToMetadataMapper {

    private List<ExifDirectoryBase> extractExifDirectories(File input) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            if (metadata == null) {
                return List.of();
            }

            return Stream.of(metadata.getFirstDirectoryOfType(ExifImageDirectory.class),
                             metadata.getFirstDirectoryOfType(ExifIFD0Directory.class),
                             metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class)
                            )
                         .filter(Objects::nonNull)
                         .toList();
        } catch (Exception e) {
            log.warn("Failed to load EXIF directories", e);
            return List.of();
        }
    }

    private Optional<LocalDateTime> findDateTimeInDirectories(File input, int dateTimeTag, int offsetTag) {
        return extractExifDirectories(input).stream()
                                            .map(dir -> new Pair<>(dir.getString(dateTimeTag),
                                                                   dir.getString(offsetTag)
                                            ))
                                            .filter(pair -> pair.first() != null)
                                            .map(pair -> DateTimeUtils.parseDateTimeString(pair.first(), pair.second()))
                                            .filter(Objects::nonNull)
                                            .min(LocalDateTime::compareTo);
    }

    private Optional<Integer> findIntegerInDirectories(File input, int tag) {
        return extractExifDirectories(input).stream()
                                            .map(dir -> dir.getInteger(tag))
                                            .filter(Objects::nonNull)
                                            .findFirst();
    }

    protected LocalDateTime extractCreationDateTimeFromExif(File input) {
        var originalDateTime = findDateTimeInDirectories(input,
                                                         ExifDirectoryBase.TAG_DATETIME_ORIGINAL,
                                                         ExifDirectoryBase.TAG_TIME_ZONE_ORIGINAL
                                                        );
        var imageDateTime = findDateTimeInDirectories(input,
                                                      ExifDirectoryBase.TAG_DATETIME,
                                                      ExifDirectoryBase.TAG_TIME_ZONE
                                                     );
        var digitizedDateTime = findDateTimeInDirectories(input,
                                                          ExifDirectoryBase.TAG_DATETIME_DIGITIZED,
                                                          ExifDirectoryBase.TAG_TIME_ZONE_DIGITIZED
                                                         );

        var earliestDateTime = findMinOrNull(originalDateTime.orElse(null),
                                             imageDateTime.orElse(null),
                                             digitizedDateTime.orElse(null)
                                            );
        log.debug("Extracted creationDateTime: {}", earliestDateTime);
        return earliestDateTime;
    }

    private Integer extractWidthFromExif(File input) {
        return Stream.of(findIntegerInDirectories(input, ExifDirectoryBase.TAG_IMAGE_WIDTH),
                         findIntegerInDirectories(input, ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH)
                        )
                     .flatMap(Optional::stream)
                     .findFirst()
                     .orElse(null);
    }

    private Integer extractHeightFromExif(File input) {
        return Stream.of(findIntegerInDirectories(input, ExifDirectoryBase.TAG_IMAGE_HEIGHT),
                         findIntegerInDirectories(input, ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT)
                        )
                     .flatMap(Optional::stream)
                     .findFirst()
                     .orElse(null);
    }

    protected abstract Class<? extends Directory> getBaseDirectoryClass();

    protected abstract Integer getBaseWidthTag();

    protected abstract Integer getBaseHeightTag();

    private Optional<Integer> extractBaseValue(File input, Integer tag) {
        if (input == null || tag == null) {
            return Optional.empty();
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            var directory = Optional.ofNullable(metadata.getFirstDirectoryOfType(getBaseDirectoryClass()));
            return directory.map(value -> value.getInteger(tag));
        } catch (ImageProcessingException | IOException e) {
            log.warn("Failed to extract base value", e);
            return Optional.empty();
        }
    }

    protected Integer extractWidth(File input) {
        return extractBaseValue(input, getBaseWidthTag()).orElseGet(() -> extractWidthFromExif(input));
    }

    protected Integer extractHeight(File input) {
        return extractBaseValue(input, getBaseHeightTag()).orElseGet(() -> extractHeightFromExif(input));
    }

    @Override
    public FileInformationMetadata process(File input) {
        var height = extractHeight(input);
        var width = extractWidth(input);
        var creationDate = extractCreationDateTimeFromExif(input);

        if (width == null || height == null) {
            throw new IllegalArgumentException("Failed to load image metadata. img_name: " + input.getName());
        }

        return FileInformationMetadata.builder()
                                      .creationDate(creationDate)
                                      .imgVidWidth(width)
                                      .imgVidHeight(height)
                                      .build();
    }

    private record Pair<F, S>(F first, S second) {}

}
