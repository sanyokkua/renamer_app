package ua.renamer.app.core.service.command.impl.preparation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
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

class ImageDimensionsPrepareInformationCommandTest extends BaseRenamePreparationCommandTest {

    static Stream<Arguments> provideCommandArguments() {
        // @formatter:off
        return Stream.of(
                arguments("", "", null, null, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.DO_NOT_USE,ImageDimensionOptions.DO_NOT_USE, "x", "_"),
                arguments("Name", "Name", null, null, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.DO_NOT_USE,ImageDimensionOptions.DO_NOT_USE, "x", "_"),
                arguments("Name", "Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.DO_NOT_USE,ImageDimensionOptions.DO_NOT_USE, "x", "_"),
                arguments("Name", "Name", null, null, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.HEIGHT, "x", "_"),
                arguments("Name", "1920_Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.DO_NOT_USE, "x", "_"),
                arguments("Name", "1920_Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.DO_NOT_USE,ImageDimensionOptions.WIDTH, "x", "_"),
                arguments("Name", "1080_Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.HEIGHT,ImageDimensionOptions.DO_NOT_USE, "x", "_"),
                arguments("Name", "1080_Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.DO_NOT_USE,ImageDimensionOptions.HEIGHT, "x", "_"),
                arguments("Name", "Name", null, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.DO_NOT_USE, "x", "_"),
                arguments("Name", "Name", null, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.DO_NOT_USE,ImageDimensionOptions.WIDTH, "x", "_"),
                arguments("Name", "Name", 1920, null, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.HEIGHT,ImageDimensionOptions.DO_NOT_USE, "x", "_"),
                arguments("Name", "Name", 1920, null, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.DO_NOT_USE,ImageDimensionOptions.HEIGHT, "x", "_"),
                arguments("Name", "1920x1080_Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.HEIGHT, "x", "_"),
                arguments("Name", "1080x1920_Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.HEIGHT,ImageDimensionOptions.WIDTH, "x", "_"),
                arguments("Name", "1920x1920_Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.WIDTH, "x", "_"),
                arguments("Name", "1080x1080_Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.HEIGHT,ImageDimensionOptions.HEIGHT, "x", "_"),
                arguments("Name", "1920x1080Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.HEIGHT, "x", ""),
                arguments("Name", "19201080Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.HEIGHT, "", ""),
                arguments("Name", "1920-1080Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.HEIGHT, "-", ""),
                arguments("Name", "1920-1080 Name", 1920, 1080, ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH,ImageDimensionOptions.HEIGHT, "-", " "),
                arguments("Name", "Name_1920x1080", 1920, 1080, ItemPositionWithReplacement.END, ImageDimensionOptions.WIDTH,ImageDimensionOptions.HEIGHT, "x", "_"),
                arguments("Name", "1920x1080", 1920, 1080, ItemPositionWithReplacement.REPLACE, ImageDimensionOptions.WIDTH,ImageDimensionOptions.HEIGHT, "x", "_")
                        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideCommandArguments")
    void commandExecution_ShouldChangeFileNameToImageDimensions(String originalName, String expectedName, Integer width, Integer height, ItemPositionWithReplacement position, ImageDimensionOptions left, ImageDimensionOptions right, String dimSep, String nameSep) {
        // Prepare Test Data
        var fileInfoMeta = FileInformationMetadata.builder().imgVidWidth(width).imgVidHeight(height).build();
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
                ImageDimensionsPrepareInformationCommand.builder()
                                                        .position(position)
                                                        .leftSide(left)
                                                        .rightSide(right)
                                                        .dimensionSeparator(dimSep)
                                                        .nameSeparator(nameSep)
                                                        .build();
        // @formatter:on

        testCommandWithItemChanged(command, inputList, expectedName, TEST_FILE_EXTENSION);
    }

    @Test
    @Override
    void defaultCommandCreation_ShouldSetDefaultValues() {
        var command = ImageDimensionsPrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(ItemPositionWithReplacement.BEGIN, command.getPosition());
        assertEquals(ImageDimensionOptions.DO_NOT_USE, command.getLeftSide());
        assertEquals(ImageDimensionOptions.DO_NOT_USE, command.getRightSide());
        assertEquals("x", command.getDimensionSeparator());
        assertEquals("", command.getNameSeparator());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return ImageDimensionsPrepareInformationCommand.builder()
                                                       .leftSide(ImageDimensionOptions.WIDTH)
                                                       .rightSide(ImageDimensionOptions.HEIGHT)
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