package ua.renamer.app.core.service.mapper.impl.metadata.audio;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;

import java.io.File;
import java.util.Set;

@Slf4j
public class Mp3Mapper extends FileToMetadataMapper {

    @Inject
    public Mp3Mapper(FilesOperations filesOperations) {
        super(filesOperations);
    }

    @Override
    protected Set<String> getSupportedExtensions() {
        return Set.of("mp3");
    }

    @Override
    public FileInformationMetadata process(File input) {
        return FileInformationMetadata.builder().creationDate(null).audioArtistName(null).audioSongName(null).build();
    }

}
