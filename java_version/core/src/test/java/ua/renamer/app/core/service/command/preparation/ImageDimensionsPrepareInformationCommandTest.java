package ua.renamer.app.core.service.command.preparation;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.enums.ImageDimensionOptions;
import ua.renamer.app.core.enums.ItemPositionWithReplacement;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.command.impl.preparation.ImageDimensionsPrepareInformationCommand;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageDimensionsPrepareInformationCommandTest extends BaseCommandTest {

    @Test
    @Override
    void testCommandDefaultCreation() {
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