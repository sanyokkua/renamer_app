package ua.renamer.app.core.service.command.preparation;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.command.impl.preparation.ReplaceTextPrepareInformationCommand;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReplaceTextPrepareInformationCommandTest extends BaseCommandTest {

    @Test
    @Override
    void testCommandDefaultCreation() {
        var command = ReplaceTextPrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(ItemPositionExtended.BEGIN, command.getPosition());
        assertEquals("", command.getTextToReplace());
        assertEquals("", command.getNewValueToAdd());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return ReplaceTextPrepareInformationCommand.builder()
                                                   .textToReplace(TestUtils.TEST_FILE_NAME)
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