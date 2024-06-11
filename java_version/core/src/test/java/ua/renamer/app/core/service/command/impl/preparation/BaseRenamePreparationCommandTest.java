package ua.renamer.app.core.service.command.impl.preparation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ua.renamer.app.core.TestUtilities.*;

public abstract class BaseRenamePreparationCommandTest {

    abstract void defaultCommandCreation_ShouldSetDefaultValues();

    @Test
    void commandShouldNotChangeNonEditableFields() {
        Command<List<FileInformation>, List<FileInformation>> command = getCommand();

        var fileInfoMeta = FileInformationMetadata.builder()
                                                  .creationDate(TEST_DEFAULT_TIME)
                                                  .imgVidWidth(TEST_IMG_VID_WIDTH)
                                                  .imgVidHeight(TEST_IMG_VID_HEIGHT)
                                                  .audioArtistName(TEST_ARTIST_NAME)
                                                  .audioAlbumName(TEST_ALBUM_NAME)
                                                  .audioSongName(TEST_SONG_NAME)
                                                  .audioYear(TEST_AUDIO_YEAR)
                                                  .build();
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File(TEST_DEFAULT_FILE_PATH))
                                      .fileAbsolutePath(TEST_ABSOLUTE_PATH)
                                      .isFile(TEST_IS_FILE)
                                      .fileName(TEST_FILE_NAME)
                                      .newName(TEST_FILE_NAME)
                                      .fileExtension(TEST_FILE_EXTENSION)
                                      .newExtension(TEST_FILE_EXTENSION)
                                      .fileSize(TEST_FILE_SIZE)
                                      .fsCreationDate(TEST_DEFAULT_TIME)
                                      .fsModificationDate(TEST_DEFAULT_TIME)
                                      .metadata(fileInfoMeta)
                                      .build();

        var input = new ArrayList<FileInformation>();
        input.add(fileInfo);
        var result = command.execute(input, (currentValue, maxValue) -> {
        });

        assertNotNull(result);
        assertEquals(1, result.size());

        FileInformation updated = result.getFirst();
        assertNotNull(updated);

        Assertions.assertEquals(TEST_ABSOLUTE_PATH, updated.getFileAbsolutePath());
        Assertions.assertEquals(TEST_IS_FILE, updated.isFile());
        Assertions.assertEquals(TEST_FILE_NAME, updated.getFileName());
        Assertions.assertEquals(TEST_FILE_EXTENSION, updated.getFileExtension());
        Assertions.assertEquals(TEST_FILE_SIZE, updated.getFileSize());
        Assertions.assertEquals(TEST_DEFAULT_TIME, updated.getFsCreationDate().orElse(null));
        Assertions.assertEquals(TEST_DEFAULT_TIME, updated.getFsModificationDate().orElse(null));

        var metadataOptional = updated.getMetadata();

        assertNotNull(metadataOptional);
        assertTrue(metadataOptional.isPresent());

        var metadata = metadataOptional.get();

        assertTrue(metadata.getCreationDate().isPresent());
        Assertions.assertEquals(TEST_DEFAULT_TIME, metadata.getCreationDate().get());

        assertTrue(metadata.getImgVidWidth().isPresent());
        Assertions.assertEquals(TEST_IMG_VID_WIDTH, metadata.getImgVidWidth().get());

        assertTrue(metadata.getImgVidHeight().isPresent());
        Assertions.assertEquals(TEST_IMG_VID_HEIGHT, metadata.getImgVidHeight().get());

        assertTrue(metadata.getAudioArtistName().isPresent());
        Assertions.assertEquals(TEST_ARTIST_NAME, metadata.getAudioArtistName().get());

        assertTrue(metadata.getAudioAlbumName().isPresent());
        Assertions.assertEquals(TEST_ALBUM_NAME, metadata.getAudioAlbumName().get());

        assertTrue(metadata.getAudioSongName().isPresent());
        Assertions.assertEquals(TEST_SONG_NAME, metadata.getAudioSongName().get());

        assertTrue(metadata.getAudioYear().isPresent());
        Assertions.assertEquals(TEST_AUDIO_YEAR, metadata.getAudioYear().get());

        if (nameShouldBeChanged()) {
            Assertions.assertNotEquals(TEST_FILE_NAME, updated.getNewName());
        } else {
            Assertions.assertEquals(TEST_FILE_NAME, updated.getNewName());
        }

        if (extensionShouldBeChanged()) {
            Assertions.assertNotEquals(TEST_FILE_EXTENSION, updated.getNewExtension());
        } else {
            Assertions.assertEquals(TEST_FILE_EXTENSION, updated.getNewExtension());
        }
    }

    abstract Command<List<FileInformation>, List<FileInformation>> getCommand();

    abstract boolean nameShouldBeChanged();

    abstract boolean extensionShouldBeChanged();

    void testCommandWithItemChanged(Command<List<FileInformation>, List<FileInformation>> command, List<FileInformation> inputList, String expectedName, String expectedExt) {
        assertNotNull(command);
        assertEquals(1, inputList.size());

        // Get Result
        var result = command.execute(inputList, (currentValue, maxValue) -> {
        });

        // Do the checks
        assertNotNull(result);
        assertEquals(1, result.size());

        FileInformation first = result.getFirst();
        assertNotNull(first);
        assertEquals(expectedName, first.getNewName());
        assertEquals(expectedExt, first.getNewExtension());
    }

}
