package ua.renamer.app.core.service.command.impl.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.enums.SortSource;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.ProgressCallback;
import ua.renamer.app.core.service.command.Command;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static ua.renamer.app.core.TestUtilities.TEST_FILE_EXTENSION;
import static ua.renamer.app.core.TestUtilities.TEST_IS_FILE;

class SequencePrepareInformationCommandTest extends BaseRenamePreparationCommandTest {

    static Stream<Arguments> provideCommandArguments() {
        return Stream.of(arguments(SortSource.FILE_NAME, 0, 1, 0), arguments(SortSource.FILE_PATH, 0, 1, 0), arguments(SortSource.FILE_SIZE, 0, 1, 0), arguments(SortSource.FILE_CREATION_DATETIME, 0, 1, 0), arguments(SortSource.FILE_MODIFICATION_DATETIME, 0, 1, 0), arguments(SortSource.FILE_CONTENT_CREATION_DATETIME, 0, 1, 0), arguments(SortSource.IMAGE_WIDTH, 0, 1, 0), arguments(SortSource.IMAGE_HEIGHT, 0, 1, 0), arguments(SortSource.FILE_NAME, 1, 1, 0), arguments(SortSource.FILE_NAME, 1, 1, 1), arguments(SortSource.FILE_NAME, 1, 10, 1), arguments(SortSource.FILE_NAME, -1, 10, 1), arguments(SortSource.FILE_NAME, 1, 10, 10));
    }

    @Test
    @Override
    void defaultCommandCreation_ShouldSetDefaultValues() {
        var command = SequencePrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(SortSource.FILE_NAME, command.getSortSource());
        assertEquals(0, command.getStartNumber());
        assertEquals(1, command.getStepValue());
        assertEquals(0, command.getPadding());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return SequencePrepareInformationCommand.builder().build();
    }

    @Override
    boolean nameShouldBeChanged() {
        return true;
    }

    @Override
    boolean extensionShouldBeChanged() {
        return false;
    }

    @ParameterizedTest
    @MethodSource("provideCommandArguments")
    void commandExecution_ShoulSortFilesAndRenameThemWithCorrectOrder(SortSource sortSource, int startNumber, int stepValue, int padding) {
        // @formatter:off
        var files = createFilesFrom1to4();
        var file1 = files[0];
        var file2 = files[1];
        var file3 = files[2];
        var file4 = files[3];

        var filesList = new ArrayList<FileInformation>();
        filesList.add(file4);
        filesList.add(file3);
        filesList.add(file2);
        filesList.add(file1);

        var mockCallback = mock(ProgressCallback.class);

        var command = SequencePrepareInformationCommand.builder()
                                                       .sortSource(sortSource)
                                                       .startNumber(startNumber)
                                                       .stepValue(stepValue)
                                                       .padding(padding)
                                                       .build();

        var result = command.execute(filesList, mockCallback);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        verify(mockCallback, times(1)).updateProgress(0, 4);
        verify(mockCallback, times(1)).updateProgress(1, 4);
        verify(mockCallback, times(1)).updateProgress(2, 4);
        verify(mockCallback, times(1)).updateProgress(3, 4);
        verify(mockCallback, times(1)).updateProgress(4, 4);
        verify(mockCallback, times(1)).updateProgress(0, 4);

        assertEquals(file1, result.get(0));
        assertEquals(file2, result.get(1));
        assertEquals(file3, result.get(2));
        assertEquals(file4, result.get(3));

        assertTrue(result.get(0).getNewName().length() >= padding);
        assertTrue(result.get(1).getNewName().length() >= padding);
        assertTrue(result.get(2).getNewName().length() >= padding);
        assertTrue(result.get(3).getNewName().length() >= padding);

        var file1Number = startNumber;
        var file2Number = file1Number + stepValue;
        var file3Number = file2Number + stepValue;
        var file4Number = file3Number + stepValue;

        var file1Name = buildPadding(file1Number, padding);
        var file2Name = buildPadding(file2Number, padding);
        var file3Name = buildPadding(file3Number, padding);
        var file4Name = buildPadding(file4Number, padding);

        assertEquals(file1Name, result.get(0).getNewName());
        assertEquals(file2Name, result.get(1).getNewName());
        assertEquals(file3Name, result.get(2).getNewName());
        assertEquals(file4Name, result.get(3).getNewName());
        // @formatter:on
    }

    static FileInformation[] createFilesFrom1to4() {
        var file1 = createFileInfo("fileName1", "/file/path/1/fileName1", 1000L, LocalDateTime.of(2000, 1, 1, 1, 0), LocalDateTime.of(2000, 1, 1, 1, 0), LocalDateTime.of(2000, 1, 1, 1, 0), 1000, 2000);
        var file2 = createFileInfo("fileName2", "/file/path/2/fileName2", 1001L, LocalDateTime.of(2001, 1, 1, 1, 0), LocalDateTime.of(2001, 1, 1, 1, 0), LocalDateTime.of(2001, 1, 1, 1, 0), 1001, 2001);
        var file3 = createFileInfo("fileName3", "/file/path/3/fileName3", 1002L, LocalDateTime.of(2002, 1, 1, 1, 0), LocalDateTime.of(2002, 1, 1, 1, 0), LocalDateTime.of(2002, 1, 1, 1, 0), 1002, 2002);
        var file4 = createFileInfo("fileName4", "/file/path/4/fileName4", 1003L, LocalDateTime.of(2003, 1, 1, 1, 0), LocalDateTime.of(2003, 1, 1, 1, 0), LocalDateTime.of(2003, 1, 1, 1, 0), 1003, 2003);
        return new FileInformation[]{file1, file2, file3, file4};
    }

    static String buildPadding(int originalNumber, int padding) {
        StringBuilder stringNumber = new StringBuilder(String.valueOf(originalNumber));

        if (stringNumber.length() >= padding) {
            return stringNumber.toString();
        }

        while (stringNumber.length() < padding) {
            stringNumber.insert(0, "0");
        }

        return stringNumber.toString();
    }

    static FileInformation createFileInfo(String fileName, String fileAbsolutePath, long fileSize, LocalDateTime fc, LocalDateTime fm, LocalDateTime cc, int width, int height) {
        var metadata = FileInformationMetadata.builder()
                                              .creationDate(cc)
                                              .imgVidWidth(width)
                                              .imgVidHeight(height)
                                              .build();
        return FileInformation.builder()
                              .originalFile(new File(fileAbsolutePath))
                              .fileAbsolutePath(fileAbsolutePath)
                              .isFile(TEST_IS_FILE)
                              .fileName(fileName)
                              .newName(fileName)
                              .fileExtension(TEST_FILE_EXTENSION)
                              .newExtension(TEST_FILE_EXTENSION)
                              .fileSize(fileSize)
                              .fsCreationDate(fc)
                              .fsModificationDate(fm)
                              .metadata(metadata)
                              .build();
    }

}