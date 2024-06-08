package ua.renamer.app.core.service.command.preparation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseCommandTest {

    abstract void testCommandDefaultCreation();
    abstract Command<List<FileInformation>, List<FileInformation>> getCommand();
    abstract boolean nameShouldBeChanged();
    abstract boolean extensionShouldBeChanged();

    @Test
    void testCommandIsNotChangingNotEditableFields() {
        Command<List<FileInformation>, List<FileInformation>> command = getCommand();

        var fileInfoMeta = FileInformationMetadata.builder()
                                                  .creationDate(TestUtils.TEST_DEFAULT_TIME)
                                                  .imgVidWidth(TestUtils.TEST_IMG_VID_WIDTH)
                                                  .imgVidHeight(TestUtils.TEST_IMG_VID_HEIGHT)
                                                  .audioArtistName(TestUtils.TEST_ARTIST_NAME)
                                                  .audioAlbumName(TestUtils.TEST_ALBUM_NAME)
                                                  .audioSongName(TestUtils.TEST_SONG_NAME)
                                                  .audioYear(TestUtils.TEST_AUDIO_YEAR)
                                                  .build();
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File(TestUtils.TEST_DEFAULT_FILE_PATH))
                                      .fileAbsolutePath(TestUtils.TEST_ABSOLUTE_PATH)
                                      .isFile(TestUtils.TEST_IS_FILE)
                                      .fileName(TestUtils.TEST_FILE_NAME)
                                      .newName(TestUtils.TEST_FILE_NAME)
                                      .fileExtension(TestUtils.TEST_FILE_EXTENSION)
                                      .newExtension(TestUtils.TEST_FILE_EXTENSION)
                                      .fileSize(TestUtils.TEST_FILE_SIZE)
                                      .fsCreationDate(TestUtils.TEST_DEFAULT_TIME)
                                      .fsModificationDate(TestUtils.TEST_DEFAULT_TIME)
                                      .metadata(fileInfoMeta)
                                      .build();

        var input = new ArrayList<FileInformation>();
        input.add(fileInfo);
        var result = command.execute(input, (currentValue, maxValue) -> {});

        assertNotNull(result);
        assertEquals(1, result.size());

        FileInformation updated = result.getFirst();
        assertNotNull(updated);

        Assertions.assertEquals(TestUtils.TEST_ABSOLUTE_PATH, updated.getFileAbsolutePath());
        Assertions.assertEquals(TestUtils.TEST_IS_FILE, updated.isFile());
        Assertions.assertEquals(TestUtils.TEST_FILE_NAME, updated.getFileName());
        Assertions.assertEquals(TestUtils.TEST_FILE_EXTENSION, updated.getFileExtension());
        Assertions.assertEquals(TestUtils.TEST_FILE_SIZE, updated.getFileSize());
        Assertions.assertEquals(TestUtils.TEST_DEFAULT_TIME, updated.getFsCreationDate().orElse(null));
        Assertions.assertEquals(TestUtils.TEST_DEFAULT_TIME, updated.getFsModificationDate().orElse(null));

        var metadataOptional = updated.getMetadata();

        assertNotNull(metadataOptional);
        assertTrue(metadataOptional.isPresent());

        var metadata = metadataOptional.get();

        assertTrue(metadata.getCreationDate().isPresent());
        Assertions.assertEquals(TestUtils.TEST_DEFAULT_TIME, metadata.getCreationDate().get());

        assertTrue(metadata.getImgVidWidth().isPresent());
        Assertions.assertEquals(TestUtils.TEST_IMG_VID_WIDTH, metadata.getImgVidWidth().get());

        assertTrue(metadata.getImgVidHeight().isPresent());
        Assertions.assertEquals(TestUtils.TEST_IMG_VID_HEIGHT, metadata.getImgVidHeight().get());

        assertTrue(metadata.getAudioArtistName().isPresent());
        Assertions.assertEquals(TestUtils.TEST_ARTIST_NAME, metadata.getAudioArtistName().get());

        assertTrue(metadata.getAudioAlbumName().isPresent());
        Assertions.assertEquals(TestUtils.TEST_ALBUM_NAME, metadata.getAudioAlbumName().get());

        assertTrue(metadata.getAudioSongName().isPresent());
        Assertions.assertEquals(TestUtils.TEST_SONG_NAME, metadata.getAudioSongName().get());

        assertTrue(metadata.getAudioYear().isPresent());
        Assertions.assertEquals(TestUtils.TEST_AUDIO_YEAR, metadata.getAudioYear().get());

        if (nameShouldBeChanged()) {
            Assertions.assertNotEquals(TestUtils.TEST_FILE_NAME, updated.getNewName());
        } else {
            Assertions.assertEquals(TestUtils.TEST_FILE_NAME, updated.getNewName());
        }

        if (extensionShouldBeChanged()) {
            Assertions.assertNotEquals(TestUtils.TEST_FILE_EXTENSION, updated.getNewExtension());
        } else {
            Assertions.assertEquals(TestUtils.TEST_FILE_EXTENSION, updated.getNewExtension());
        }
    }

    void testCommandWithItemChanged(
            Command<List<FileInformation>, List<FileInformation>> command,
            List<FileInformation> inputList,
            String expectedName,
            String expectedExt) {
        assertNotNull(command);
        assertEquals(1, inputList.size());

        // Get Result
        var result = command.execute(inputList, (currentValue, maxValue) -> {});

        // Do the checks
        assertNotNull(result);
        assertEquals(1, result.size());

        FileInformation first = result.getFirst();
        assertNotNull(first);
        assertEquals(expectedName, first.getNewName());
        assertEquals(expectedExt, first.getNewExtension());
    }

}
