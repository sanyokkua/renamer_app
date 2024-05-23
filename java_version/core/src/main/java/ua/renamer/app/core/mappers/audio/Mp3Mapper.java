package ua.renamer.app.core.mappers.audio;

import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.abstracts.FileToMetadataMapper;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.util.Set;

@Slf4j
public class Mp3Mapper extends FileToMetadataMapper {

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of("mp3");
    }

    @Override
    public FileInformationMetadata process(File input) {
        return FileInformationMetadata.builder()
                                      .creationDate(null)
                                      .audioArtistName(null)
                                      .audioSongName(null)
                                      .build();
    }

}
