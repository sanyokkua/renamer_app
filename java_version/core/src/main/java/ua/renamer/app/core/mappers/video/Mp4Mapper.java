package ua.renamer.app.core.mappers.video;

import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.mp4.media.Mp4MediaDirectory;
import com.drew.metadata.mp4.media.Mp4VideoDirectory;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.abstracts.FileToMetadataMapper;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class Mp4Mapper extends FileToMetadataMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Mp4.getAllExtensions());
    }

    @Override
    public FileInformationMetadata process(File input) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            var dir = metadata.getFirstDirectoryOfType(Mp4VideoDirectory.class);
            var height = dir.getInteger(Mp4VideoDirectory.TAG_HEIGHT);
            var width = dir.getInteger(Mp4VideoDirectory.TAG_WIDTH);

            var mediaDir = metadata.getFirstDirectoryOfType(Mp4MediaDirectory.class);
            var dateTimeOriginal = Optional.ofNullable(mediaDir.getDate(Mp4MediaDirectory.TAG_CREATION_TIME));

            var timeValue = dateTimeOriginal.map(Date::getTime).orElse(null);

            return FileInformationMetadata.builder()
                                          .creationDate(timeValue)
                                          .imgVidWidth(width)
                                          .imgVidHeight(height)
                                          .build();
        } catch (ImageProcessingException | IOException e) {
            log.warn("Failed to create Metadata", e);
            return FileInformationMetadata.builder().build();
        }
    }

}
