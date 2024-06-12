package ua.renamer.app.core.service.mapper.impl;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.mapper.DataMapper;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;

import java.io.File;

/**
 * A mapper class for mapping File objects to FileInformation objects.
 * It implements the DataMapper interface.
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class FileToFileInformationMapper implements DataMapper<File, FileInformation> {

    private final FileToMetadataMapper fileToMetadataMapper;
    private final FilesOperations filesOperations;

    /**
     * Maps the given File input to a FileInformation object.
     * The mapping includes retrieving various attributes of the file and its metadata.
     *
     * @param file the input File to be mapped
     *
     * @return the mapped FileInformation object
     */
    @Override
    public FileInformation map(File file) {
        filesOperations.validateFileInstance(file);

        final var fileNameWithoutExtension = filesOperations.getFileNameWithoutExtension(file);
        final var fileAbsolutePath = filesOperations.getFileAbsolutePath(file);
        final var isFile = filesOperations.isFile(file);
        final var fileExtension = filesOperations.getFileExtension(file);
        final var fsCreationDate = filesOperations.getFileCreationTime(file);
        final var fsModificationDate = filesOperations.getFileModificationTime(file);
        final var fileSize = filesOperations.getFileSize(file);
        final var mime = filesOperations.getMimeType(file);
        final var extFromMime = filesOperations.getExtensionFromMimeType(mime);

        final var metadata = fileToMetadataMapper.map(file);
        final var creationDateTime = fsCreationDate.orElse(null);
        final var modificationDateTime = fsModificationDate.orElse(null);

        return FileInformation.builder()
                              .originalFile(file)
                              .fileAbsolutePath(fileAbsolutePath)
                              .isFile(isFile)
                              .fileName(fileNameWithoutExtension)
                              .newName(fileNameWithoutExtension) // Initial value is current file name
                              .fileExtension(fileExtension)
                              .newExtension(fileExtension) // Initial value is current file extension
                              .fileSize(fileSize)
                              .fsCreationDate(creationDateTime)
                              .fsModificationDate(modificationDateTime)
                              .detectedMimeType(mime)
                              .detectedExtension(extFromMime)
                              .metadata(metadata)
                              .build();
    }

}
