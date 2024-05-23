package ua.renamer.app.core.mappers.video;

import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.abstracts.FileToMetadataMapper;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static ua.renamer.app.core.utils.Utils.findMinOrNull;

@Slf4j
public class QuickTimeMapper extends FileToMetadataMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of(FileType.QuickTime.getAllExtensions());
    }

    @Override
    public FileInformationMetadata process(File input) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(input);
            var dir = metadata.getFirstDirectoryOfType(QuickTimeDirectory.class);
            var dateTimeOriginal = Optional.ofNullable(dir.getDate(QuickTimeDirectory.TAG_CREATION_TIME));

            var dirVid = metadata.getFirstDirectoryOfType(QuickTimeVideoDirectory.class);
            var height = dirVid.getInteger(QuickTimeVideoDirectory.TAG_HEIGHT);
            var width = dirVid.getInteger(QuickTimeVideoDirectory.TAG_WIDTH);

            var dirInfo = metadata.getFirstDirectoryOfType(QuickTimeMetadataDirectory.class);
            var creationTime = Optional.ofNullable(dirInfo.getDate(QuickTimeDirectory.TAG_CREATION_TIME));
            var modificationTime = Optional.ofNullable(dirInfo.getDate(QuickTimeDirectory.TAG_MODIFICATION_TIME));

            var dateTimeOrigVal = dateTimeOriginal.map(Date::getTime).orElse(null);
            var creationTimeVal = creationTime.map(Date::getTime).orElse(null);
            var modificationTimeVal = modificationTime.map(Date::getTime).orElse(null);

            var timeValue = findMinOrNull(dateTimeOrigVal, creationTimeVal, modificationTimeVal);

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
