package ua.renamer.app.core.service.mapper.impl.metadata.video;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public abstract class VideoBaseMapper extends FileToMetadataMapper {

    private final DateTimeOperations dateTimeOperations;

    protected VideoBaseMapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations);
        this.dateTimeOperations = dateTimeOperations;
    }

    @Override
    public FileInformationMetadata process(File input) {
        var directories = getDirectories(input);

        var dateTime = extractCreationDateTime(directories);
        var width = extractVideoWidth(directories);
        var height = extractVideoHeight(directories);

        if (width.isEmpty() || height.isEmpty()) {
            throw new IllegalArgumentException("Failed to load video metadata. vid_name: " + input.getName());
        }

        return FileInformationMetadata.builder()
                                      .creationDate(dateTime.orElse(null))
                                      .imgVidWidth(width.orElse(null))
                                      .imgVidHeight(height.orElse(null))
                                      .build();
    }

    private List<? extends Directory> getDirectories(File input) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            var directoryClasses = getAvailableDirectories();
            return directoryClasses.stream()
                                   .filter(Objects::nonNull)
                                   .map(metadata::getFirstDirectoryOfType)
                                   .filter(Objects::nonNull)
                                   .toList();
        } catch (ImageProcessingException | IOException e) {
            log.warn("Failed to create Metadata", e);
            return List.of();
        }
    }

    private Optional<LocalDateTime> extractCreationDateTime(List<? extends Directory> directories) {
        var tags = getContentCreationTags();
        var result = findStringValues(directories, tags);

        return result.stream()
                     .map(dateTimeOperations::parseDateTimeString)
                     .filter(Objects::nonNull)
                     .min(LocalDateTime::compareTo);
    }

    private Optional<Integer> extractVideoWidth(List<? extends Directory> directories) {
        var tags = getVideoWidthTags();
        var result = findIntegerValues(directories, tags);

        return result.stream().filter(Objects::nonNull).min(Integer::compareTo);
    }

    private Optional<Integer> extractVideoHeight(List<? extends Directory> directories) {
        var tags = getVideoHeightTags();
        var result = findIntegerValues(directories, tags);

        return result.stream().filter(Objects::nonNull).min(Integer::compareTo);
    }

    protected abstract List<Class<? extends Directory>> getAvailableDirectories();

    protected abstract List<Integer> getContentCreationTags();

    private static List<String> findStringValues(List<? extends Directory> directories, List<Integer> tags) {
        return tags.stream()
                   .filter(Objects::nonNull)
                   .flatMap(tag -> directories.stream()
                                              .filter(Objects::nonNull)
                                              .map(dir -> dir.getString(tag))
                                              .filter(Objects::nonNull))
                   .toList();
    }

    protected abstract List<Integer> getVideoWidthTags();

    private static List<Integer> findIntegerValues(List<? extends Directory> directories, List<Integer> tags) {
        return tags.stream()
                   .filter(Objects::nonNull)
                   .flatMap(tag -> directories.stream()
                                              .filter(Objects::nonNull)
                                              .map(dir -> dir.getInteger(tag))
                                              .filter(Objects::nonNull))
                   .toList();
    }

    protected abstract List<Integer> getVideoHeightTags();

}
