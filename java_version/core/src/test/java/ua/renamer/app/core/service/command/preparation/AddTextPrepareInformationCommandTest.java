package ua.renamer.app.core.service.command.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.command.impl.preparation.AddTextPrepareInformationCommand;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ua.renamer.app.core.service.command.preparation.TestUtils.*;

class AddTextPrepareInformationCommandTest extends BaseCommandTest {

    static Stream<Arguments> commandArguments() {
        return Stream.of(
                arguments("", "", "", "", true, ItemPosition.BEGIN, ""),
                arguments("name", "name", "", "", true, ItemPosition.BEGIN, ""),
                arguments("   ", "   ", "", "", true, ItemPosition.BEGIN, ""),
                arguments("name", "name", "ext", "ext", true, ItemPosition.BEGIN, ""),
                arguments("name", "name", "ext", "ext", false, ItemPosition.BEGIN, ""),
                arguments("name", "new_name", "ext", "ext", true, ItemPosition.BEGIN, "new_"),
                arguments("name", "  name", "ext", "ext", true, ItemPosition.BEGIN, "  "),
                arguments("name", "name", "ext", "ext", true, ItemPosition.END, ""),
                arguments("name", "nametest", "ext", "ext", true, ItemPosition.END, "test"),
                arguments("name", "name ", "ext", "ext", true, ItemPosition.END, " "),
                arguments("name", "name_test", "ext", "ext", true, ItemPosition.END, "_test"),
                arguments("name", "namesomething", "", "", true, ItemPosition.END, "something")
                        );
    }

    @ParameterizedTest
    @MethodSource("commandArguments")
    void testCommandExecution(
            String originalName,
            String expectedName,
            String originalExt,
            String expectedExt,
            boolean isFile,
            ItemPosition position,
            String textToAdd) {
        // Prepare Test Data
        var fileInfoMeta = FileInformationMetadata.builder().build();
        var fileInfo = FileInformation.builder()
                                      .originalFile(new File(TEST_DEFAULT_FILE_PATH))
                                      .fileAbsolutePath(TEST_ABSOLUTE_PATH)
                                      .isFile(isFile)
                                      .fileName(originalName)
                                      .newName(originalName)
                                      .fileExtension(originalExt)
                                      .newExtension(originalExt)
                                      .fileSize(TEST_FILE_SIZE)
                                      .fsCreationDate(TEST_DEFAULT_TIME)
                                      .fsModificationDate(TEST_DEFAULT_TIME)
                                      .metadata(fileInfoMeta)
                                      .build();

        List<FileInformation> inputList = List.of(fileInfo);

        // Build the command for test
        Command<List<FileInformation>, List<FileInformation>> command =
                AddTextPrepareInformationCommand.builder()
                                                .position(position)
                                                .text(textToAdd)
                                                .build();

        testCommandWithItemChanged(command, inputList, expectedName, expectedExt);
    }

    @Test
    @Override
    void testCommandDefaultCreation() {
        var command = AddTextPrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(ItemPosition.BEGIN, command.getPosition());
        assertEquals("", command.getText());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return AddTextPrepareInformationCommand.builder().text("Addition").build();
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