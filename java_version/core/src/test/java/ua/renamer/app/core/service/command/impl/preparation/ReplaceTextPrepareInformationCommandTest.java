package ua.renamer.app.core.service.command.impl.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.enums.ItemPositionExtended;
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

class ReplaceTextPrepareInformationCommandTest extends BaseRenamePreparationCommandTest {

    static Stream<Arguments> provideCommandArguments() {
        // @formatter:off
        return Stream.of(
                arguments("", "", ItemPositionExtended.BEGIN, "", ""),
                arguments("Name", "Name", ItemPositionExtended.BEGIN, "", ""),
                arguments("Prefix_Name_Suffix", "Name_Suffix", ItemPositionExtended.BEGIN, "Prefix_", ""),
                arguments("Prefix_Name_Suffix", "_Name_Suffix", ItemPositionExtended.BEGIN, "Prefix", ""),
                arguments("Prefix_Name_Suffix", "Suffix", ItemPositionExtended.BEGIN, "Prefix_Name_", ""),
                arguments("Prefix_Name_Suffix", "_Name_Suffix", ItemPositionExtended.END, "Prefix", ""),
                arguments("Prefix_Name_Suffix", "Prefix_Name_", ItemPositionExtended.END, "Suffix", ""),
                arguments("Prefix_Name_Suffix", "Prefix_Name", ItemPositionExtended.END, "_Suffix", ""),
                arguments("Prefix_Name_Suffix", "Prefix_Name_Suffix", ItemPositionExtended.END, "NonExisting", ""),
                arguments("Prefix_Name_Suffix", "AAA_Name_Suffix", ItemPositionExtended.BEGIN, "Prefix", "AAA"),
                arguments("Prefix_Name_Suffix", "Prefix_Name_AAA", ItemPositionExtended.BEGIN, "Suffix", "AAA"),
                arguments("Prefix_Name_Suffix", "Prefix_NAME_Suffix", ItemPositionExtended.END, "Name", "NAME"),
                arguments("PreREMfix_NaREMme_SufREMfix", "Prefix_Name_Suffix", ItemPositionExtended.EVERYWHERE, "REM", ""),
                arguments("PreREMfix_NaREMme_SufREMfix", "Pre123fix_Na123me_Suf123fix", ItemPositionExtended.EVERYWHERE, "REM", "123"),
                arguments("PreREMfix_NaREMme_SufREMfix", "PreREMfix_NaREMme_Suf123fix", ItemPositionExtended.END, "REM", "123"),
                arguments("PreREMfix_NaREMme_SufREMfix", "Pre123fix_NaREMme_SufREMfix", ItemPositionExtended.BEGIN, "REM", "123")
                        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideCommandArguments")
    void commandExecution_ShouldReplaceTextInFileName(String originalName, String expectedName, ItemPositionExtended position, String textToReplace, String textToAdd) {
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
                ReplaceTextPrepareInformationCommand.builder()
                                                    .position(position)
                                                    .textToReplace(textToReplace)
                                                    .newValueToAdd(textToAdd)
                                                    .build();
        // @formatter:on

        testCommandWithItemChanged(command, inputList, expectedName, TEST_FILE_EXTENSION);
    }

    @Test
    @Override
    void defaultCommandCreation_ShouldSetDefaultValues() {
        var command = ReplaceTextPrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(ItemPositionExtended.BEGIN, command.getPosition());
        assertEquals("", command.getTextToReplace());
        assertEquals("", command.getNewValueToAdd());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return ReplaceTextPrepareInformationCommand.builder().textToReplace(TEST_FILE_NAME)
                                                   .newValueToAdd("New")
                                                   .build();
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