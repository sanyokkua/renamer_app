package ua.renamer.app.core.mappers.video;

import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.avi.AviDirectory;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.abstracts.FileToMetadataMapper;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class AviMapper extends FileToMetadataMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.Avi.getAllExtensions());
    }

    @Override
    public FileInformationMetadata process(File input) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            var dir = metadata.getFirstDirectoryOfType(AviDirectory.class);

            var dateTimeOriginal = Optional.ofNullable(dir.getDate(AviDirectory.TAG_DATETIME_ORIGINAL));
            var height = dir.getInteger(AviDirectory.TAG_HEIGHT);
            var width = dir.getInteger(AviDirectory.TAG_WIDTH);
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
