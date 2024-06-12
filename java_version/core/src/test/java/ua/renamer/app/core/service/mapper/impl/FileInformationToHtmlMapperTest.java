package ua.renamer.app.core.service.mapper.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.model.RenameModel;
import ua.renamer.app.core.service.command.impl.MapFileInformationToRenameModel;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileInformationToHtmlMapperTest {

    @Spy
    private DateTimeOperations dateTimeOperations;

    @Test
    void testMapFunctionThatCreatesHtmlStringFromFileInformation() {
        // This test is not final, it is more like a stub.
        // When final HTML mapper will be implemented it should be improved
        final var testTime = LocalDateTime.now();
        final var imgVidWidth = 640;
        final var imgVidHeight = 480;
        final var artName = "ArtName";
        final var albName = "AlbName";
        final var songName = "SongName";
        final var audioYear = 1993;
        final var absolutePath = "/root/tests/file.mp4";
        final var isFile = true;
        final var fileName = "file";
        final var fileExt = ".mp4";
        final var fileSize = 1024L;
        final var file = new File(absolutePath);

        FileInformationMetadata metadata = FileInformationMetadata.builder()
                                                                  .creationDate(testTime)
                                                                  .imgVidWidth(imgVidWidth)
                                                                  .imgVidHeight(imgVidHeight)
                                                                  .audioArtistName(artName)
                                                                  .audioAlbumName(albName)
                                                                  .audioSongName(songName)
                                                                  .audioYear(audioYear)
                                                                  .build();

        FileInformation fileInformation = FileInformation.builder()
                                                         .originalFile(file)
                                                         .fileAbsolutePath(absolutePath)
                                                         .isFile(isFile)
                                                         .fileName(fileName)
                                                         .fileExtension(fileExt)
                                                         .fileSize(fileSize)
                                                         .fsCreationDate(testTime)
                                                         .fsModificationDate(testTime)
                                                         .metadata(metadata)
                                                         .newName(fileName)
                                                         .newExtension(fileExt)
                                                         .build();
        MapFileInformationToRenameModel mapperToModel = new MapFileInformationToRenameModel();
        RenameModel model = mapperToModel.execute(List.of(fileInformation), null).get(0);

        var mapper = new FileInformationToHtmlMapper(dateTimeOperations, (value) -> value);
        var resultHtml = mapper.map(model);

        assertNotNull(resultHtml);
        assertFalse(resultHtml.isBlank());
        assertTrue(resultHtml.contains(fileName));
        assertTrue(resultHtml.contains(fileExt));
        assertTrue(resultHtml.contains(absolutePath));
    }

}