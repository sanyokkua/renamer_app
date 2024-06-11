package ua.renamer.app.core.service.command.impl.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.Command;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ua.renamer.app.core.TestUtilities.*;

class RemoveTextPrepareInformationCommandTest extends BaseRenamePreparationCommandTest {

    static Stream<Arguments> provideCommandArguments() {
        // @formatter:off
        return Stream.of(
                arguments("", "", ItemPosition.BEGIN, ""),
                arguments("Name", "Name", ItemPosition.BEGIN, ""),
                arguments("Prefix_Name_Suffix", "Name_Suffix", ItemPosition.BEGIN, "Prefix_"),
                arguments("Prefix_Name_Suffix", "_Name_Suffix", ItemPosition.BEGIN, "Prefix"),
                arguments("Prefix_Name_Suffix", "Suffix", ItemPosition.BEGIN, "Prefix_Name_"),
                arguments("Prefix_Name_Suffix", "Prefix_Name_Suffix", ItemPosition.END, "Prefix"),
                arguments("Prefix_Name_Suffix", "Prefix_Name_", ItemPosition.END, "Suffix"),
                arguments("Prefix_Name_Suffix", "Prefix_Name", ItemPosition.END, "_Suffix"),
                arguments("Prefix_Name_Suffix", "Prefix_Name_Suffix", ItemPosition.END, "NonExisting")
                        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideCommandArguments")
    void commandExecution_ShouldRemoveTextFromFileName(String originalName, String expectedName, ItemPosition position, String textToRemove) {
        // Prepare Test Data
        var fileInfoMeta = FileInformationMetadata.builder().build();
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File(TEST_DEFAULT_FILE_PATH))
                                      .fileAbsolutePath(TEST_ABSOLUTE_PATH)
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
                RemoveTextPrepareInformationCommand.builder()
                                                   .position(position)
                                                   .text(textToRemove)
                                                   .build();
        // @formatter:on

        testCommandWithItemChanged(command, inputList, expectedName, TEST_FILE_EXTENSION);
    }

    @Test
    @Override
    void defaultCommandCreation_ShouldSetDefaultValues() {
        var command = RemoveTextPrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(ItemPosition.BEGIN, command.getPosition());
        assertEquals("", command.getText());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return RemoveTextPrepareInformationCommand.builder().text(TEST_FILE_NAME).build();
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