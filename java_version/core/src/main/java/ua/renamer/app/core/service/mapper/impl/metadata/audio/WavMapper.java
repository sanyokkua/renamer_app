package ua.renamer.app.core.service.mapper.impl.metadata.audio;

import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.wav.WavDirectory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.helper.DateTimeOperations;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class WavMapper extends FileToMetadataMapper {

    private final DateTimeOperations dateTimeOperations;

    @Inject
    public WavMapper(FilesOperations filesOperations, DateTimeOperations dateTimeOperations) {
        super(filesOperations);
        this.dateTimeOperations = dateTimeOperations;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(FileType.Wav.getAllExtensions());
    }

    @Override
    public FileInformationMetadata process(File input) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            var dir = Optional.ofNullable(metadata.getFirstDirectoryOfType(WavDirectory.class));

            var artist = dir.map(directory -> directory.getString(WavDirectory.TAG_ARTIST));
            var title = dir.map(directory -> directory.getString(WavDirectory.TAG_TITLE));
            var date = dir.map(directory -> directory.getString(WavDirectory.TAG_DATE_CREATED));

            var artistValue = artist.orElse(null);
            var titleValue = title.orElse(null);
            var dateValue = date.map(dateTimeOperations::parseDateTimeString).orElse(null);

            return FileInformationMetadata.builder()
                                          .creationDate(dateValue)
                                          .audioArtistName(artistValue)
                                          .audioSongName(titleValue)
                                          .build();
        } catch (ImageProcessingException | IOException e) {
            log.warn("Failed to create Metadata", e);
            return FileInformationMetadata.builder().build();
        }
    }

}
