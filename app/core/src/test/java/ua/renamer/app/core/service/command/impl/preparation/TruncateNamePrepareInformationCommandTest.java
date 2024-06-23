package ua.renamer.app.core.service.command.impl.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.enums.TruncateOptions;
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

class TruncateNamePrepareInformationCommandTest extends BaseRenamePreparationCommandTest {

    static Stream<Arguments> provideCommandArguments() {
        return Stream.of(arguments("", "", TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 0), arguments("Name", "Name", TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 0), arguments("Name", "Name", TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, Integer.MIN_VALUE), arguments("Name", "", TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, Integer.MAX_VALUE), arguments("Name", "ame", TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 1), arguments("Name", "me", TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 2), arguments("Name", "e", TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 3), arguments("Name", "", TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 4), arguments("Name", "Nam", TruncateOptions.REMOVE_SYMBOLS_FROM_END, 1), arguments("Name", "Na", TruncateOptions.REMOVE_SYMBOLS_FROM_END, 2), arguments("Name", "N", TruncateOptions.REMOVE_SYMBOLS_FROM_END, 3), arguments("Name", "", TruncateOptions.REMOVE_SYMBOLS_FROM_END, 4), arguments("Name", "Name", TruncateOptions.TRUNCATE_EMPTY_SYMBOLS, Integer.MAX_VALUE), arguments("   Name", "Name", TruncateOptions.TRUNCATE_EMPTY_SYMBOLS, Integer.MAX_VALUE), arguments("   Name    ", "Name", TruncateOptions.TRUNCATE_EMPTY_SYMBOLS, Integer.MAX_VALUE), arguments("Name    ", "Name", TruncateOptions.TRUNCATE_EMPTY_SYMBOLS, Integer.MAX_VALUE));
    }

    @ParameterizedTest
    @MethodSource("provideCommandArguments")
    void commandExecution_ShouldTruncateEmptySymbolsOrRemoveAmountOfSymbols(String originalName, String expectedName, TruncateOptions truncateOptions, int numberOfSymbols) {
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
        Command<List<FileInformation>, List<FileInformation>> command = TruncateNamePrepareInformationCommand.builder()
                                                                                                             .truncateOptions(truncateOptions)
                                                                                                             .numberOfSymbols(numberOfSymbols)
                                                                                                             .build();

        testCommandWithItemChanged(command, inputList, expectedName, TEST_FILE_EXTENSION);
    }

    @Test
    @Override
    void defaultCommandCreation_ShouldSetDefaultValues() {
        var command = TruncateNamePrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, command.getTruncateOptions());
        assertEquals(0, command.getNumberOfSymbols());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return TruncateNamePrepareInformationCommand.builder().numberOfSymbols(2).build();
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