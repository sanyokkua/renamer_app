package ua.renamer.app.core.service.command.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.enums.TextCaseOptions;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.command.impl.preparation.ChangeCasePreparePrepareInformationCommand;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ua.renamer.app.core.service.command.preparation.TestUtils.*;

class ChangeCasePreparePrepareInformationCommandTest extends BaseCommandTest {

    static Stream<Arguments> commandArguments() {
        return Stream.of(
                arguments("", "", "", "", true, TextCaseOptions.CAMEL_CASE, false),
                arguments("", "", "", "", false, TextCaseOptions.CAMEL_CASE, false),
                arguments("", "", "", "", false, TextCaseOptions.CAMEL_CASE, true),

                arguments("this_is-a_file name",
                          "thisIsAFileName",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.CAMEL_CASE,
                          false
                         ),
                arguments("this_is-a_file name",
                          "ThisIsAFileName",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.PASCAL_CASE,
                          false
                         ),
                arguments("this_is-a_file name",
                          "this_is_a_file_name",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.SNAKE_CASE,
                          false
                         ),
                arguments("this_is-a_file name",
                          "THIS_IS_A_FILE_NAME",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.SNAKE_CASE_SCREAMING,
                          false
                         ),
                arguments("this_is-a_file name",
                          "this-is-a-file-name",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.KEBAB_CASE,
                          false
                         ),
                arguments("this_is-a_file name",
                          "THIS_IS-A_FILE NAME",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.UPPERCASE,
                          false
                         ),
                arguments("this_is-a_file name",
                          "this_is-a_file name",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.LOWERCASE,
                          false
                         ),
                arguments("this_is-a_file name",
                          "This Is A File Name",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.TITLE_CASE,
                          false
                         ),

                arguments("fileNameOriginal",
                          "FileNameOriginal",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.CAMEL_CASE,
                          true
                         ),
                arguments("file Name Original",
                          "File_name_original",
                          ".jpg",
                          ".jpg",
                          false,
                          TextCaseOptions.SNAKE_CASE,
                          true
                         )
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
            TextCaseOptions textCaseOptions,
            boolean capitilize) {
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
                ChangeCasePreparePrepareInformationCommand.builder()
                                                          .textCase(textCaseOptions)
                                                          .capitalize(capitilize)
                                                          .build();

        testCommandWithItemChanged(command, inputList, expectedName, expectedExt);
    }

    @Test
    @Override
    void testCommandDefaultCreation() {
        var command = ChangeCasePreparePrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(TextCaseOptions.CAMEL_CASE, command.getTextCase());
        assertFalse(command.isCapitalize());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return ChangeCasePreparePrepareInformationCommand.builder().build();
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