package ua.renamer.app.core.service.mapper.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.file.impl.FilesOperations;
import ua.renamer.app.core.service.mapper.FileToMetadataMapper;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileToFileInformationMapperTest {

    @Mock
    private FileToMetadataMapper fileToMetadataMapper;
    @Mock
    private FilesOperations filesOperations;
    @Mock
    private File file;

    @Test
    void testMappingOfFileToFileInformation() {
        final var fileName = "FileName";
        final var fileAbsolutePath = "/root/FileName.zip";
        final var isFile = true;
        final var fileExt = ".zip";
        final var localDateTime = LocalDateTime.now();
        final var fileSize = 1000L;
        final var metadata = FileInformationMetadata.builder().build();

        when(filesOperations.getFileNameWithoutExtension(file)).thenReturn(fileName);
        when(filesOperations.getFileAbsolutePath(file)).thenReturn(fileAbsolutePath);
        when(filesOperations.isFile(file)).thenReturn(isFile);
        when(filesOperations.getFileExtension(file)).thenReturn(fileExt);
        when(filesOperations.getFileCreationTime(file)).thenReturn(Optional.of(localDateTime));
        when(filesOperations.getFileModificationTime(file)).thenReturn(Optional.of(localDateTime));
        when(filesOperations.getFileSize(file)).thenReturn(fileSize);
        when(filesOperations.getMimeType(file)).thenReturn("");
        when(filesOperations.getExtensionsFromMimeType(anyString())).thenReturn(Set.of());
        when(fileToMetadataMapper.map(file)).thenReturn(metadata);

        var mapper = new FileToFileInformationMapper(fileToMetadataMapper, filesOperations);

        var result = mapper.map(file);

        assertNotNull(result);
        assertEquals(fileName, result.getFileName());
        assertEquals(fileName, result.getNewName());
        assertEquals(fileAbsolutePath, result.getFileAbsolutePath());
        assertEquals(isFile, result.isFile());
        assertEquals(fileExt, result.getFileExtension());
        assertEquals(fileExt, result.getNewExtension());

        var fsCreationDate = result.getFsCreationDate();
        assertTrue(fsCreationDate.isPresent());
        assertEquals(localDateTime, fsCreationDate.get());

        var fsModificationDate = result.getFsModificationDate();
        assertTrue(fsModificationDate.isPresent());
        assertEquals(localDateTime, fsModificationDate.get());

        assertEquals(fileSize, result.getFileSize());

        var resMetadata = result.getMetadata();
        assertTrue(resMetadata.isPresent());
        assertEquals(metadata, resMetadata.get());
    }

}