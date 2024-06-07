package ua.renamer.app.core.commands.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.abstracts.Command;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ua.renamer.app.core.commands.preparation.TestUtils.*;

class ExtensionChangePrepareInformationCommandTest extends BaseCommandTest {

    static Stream<Arguments> commandArguments() {
        return Stream.of(
                arguments("", "", "", "", true, ""),
                arguments("name", "name", "", "", true, ""),
                arguments("   ", "   ", "", "", true, ""),
                arguments("fileName", "fileName", "ext", ".jpg", true, "jpg"),
                arguments("fileName", "fileName", "ext", ".heif", true, ".heif"),
                arguments("fileName", "fileName", "ext", "", true, ""),
                arguments("fileName", "fileName", "", "", false, ".ext"),
                arguments("fileName", "fileName", "", ".ext", true, ".ext")
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
            String newExtension) {
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
                ExtensionChangePrepareInformationCommand.builder()
                                                        .newExtension(newExtension)
                                                        .build();

        testCommandWithItemChanged(command, inputList, expectedName, expectedExt);
    }

    @Test
    @Override
    void testCommandDefaultCreation() {
        var command = ExtensionChangePrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals("", command.getNewExtension());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return ExtensionChangePrepareInformationCommand.builder()
                                                       .newExtension("heic")
                                                       .build();
    }

    @Override
    boolean nameShouldBeChanged() {
        return false;
    }

    @Override
    boolean extensionShouldBeChanged() {
        return true;
    }

}