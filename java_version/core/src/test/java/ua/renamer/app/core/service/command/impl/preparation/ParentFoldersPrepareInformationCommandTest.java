package ua.renamer.app.core.service.command.impl.preparation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.file.impl.FilesOperations;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static ua.renamer.app.core.TestUtilities.*;

@ExtendWith(MockitoExtension.class)
class ParentFoldersPrepareInformationCommandTest extends BaseRenamePreparationCommandTest {

    @Mock
    private FilesOperations filesOperations;
    private FilesOperations realFileOperations;

    static Stream<Arguments> provideCommandArguments() {
        // @formatter:off
        return Stream.of(
                arguments("", "", "/path/", ItemPosition.BEGIN, 0, "_"),
                arguments("Name", "Name", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.BEGIN, 0, "_"),
                arguments("Name", "Name", "", ItemPosition.BEGIN, 1, "_"),
                arguments("Name", "Name", "  ", ItemPosition.BEGIN, 1, "_"),
                arguments("Name", "ToFile_Name", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.BEGIN, 1, "_"),
                arguments("Name", "Path_ToFile_Name", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.BEGIN, 2, "_"),
                arguments("Name", "Absolute_Path_ToFile_Name", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.BEGIN, 3, "_"),
                arguments("Name", "Absolute_Path_ToFile_Name", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.BEGIN, 4, "_"),
                arguments("Name", "Absolute_Path_ToFile_Name", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.BEGIN, Integer.MAX_VALUE, "_"),
                arguments("Name", "Name_ToFile", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.END, 1, "_"),
                arguments("Name", "Name", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.END, -1, "_"),
                arguments("Name", "Name", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.END, Integer.MIN_VALUE, "_"),
                arguments("Name", "ToFilexName", "/Absolute/Path/ToFile/Name.jpg", ItemPosition.BEGIN, 1, "x")

                        );
        // @formatter:on
    }

    @BeforeEach
    void setUp() {
        realFileOperations = new FilesOperations(Files::readAttributes);
    }

    @ParameterizedTest
    @MethodSource("provideCommandArguments")
    void commandExecution_ShouldAddParentFoldersToTheName(String originalName, String expectedName, String absolutePath, ItemPosition position, int numberOfParents, String nameSep) {
        // Prepare Test Data
        var fileInfoMeta = FileInformationMetadata.builder().build();
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File(TEST_DEFAULT_FILE_PATH))
                                      .fileAbsolutePath(absolutePath)
                                      .isFile(TEST_IS_FILE)
                                      .fileName(originalName)
                                      .newName(originalName)
                                      .fileExtension(TEST_FILE_EXTENSION)
                                      .newExtension(TEST_FILE_EXTENSION)
                                      .fileSize(TEST_FILE_SIZE)
                                      .fsCreationDate(TEST_DEFAULT_TIME)
                                      .fsModificationDate(TEST_DEFAULT_TIME)
                                      .metadata(fileInfoMeta)
                                      .build();

        List<FileInformation> inputList = List.of(fileInfo);

        // Build the command for test
        // @formatter:off
        Command<List<FileInformation>, List<FileInformation>> command =
                ParentFoldersPrepareInformationCommand.builder()
                                                      .filesOperations(realFileOperations)
                                                      .position(position)
                                                      .numberOfParents(numberOfParents)
                                                      .separator(nameSep)
                                                      .build();
        // @formatter:on

        testCommandWithItemChanged(command, inputList, expectedName, TEST_FILE_EXTENSION);
    }

    @Test
    @Override
    void defaultCommandCreation_ShouldSetDefaultValues() {
        var command = ParentFoldersPrepareInformationCommand.builder().filesOperations(filesOperations).build();

        assertNotNull(command);
        assertEquals(ItemPosition.BEGIN, command.getPosition());
        assertEquals(1, command.getNumberOfParents());
        assertEquals("", command.getSeparator());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        when(filesOperations.getParentFolders(anyString())).thenReturn(List.of("parent"));
        return ParentFoldersPrepareInformationCommand.builder().filesOperations(filesOperations).build();
    }

    @Override
    boolean nameShouldBeChanged() {
        return true;
    }

    @Override
    boolean extensionShouldBeChanged() {
        return false;
    }

}