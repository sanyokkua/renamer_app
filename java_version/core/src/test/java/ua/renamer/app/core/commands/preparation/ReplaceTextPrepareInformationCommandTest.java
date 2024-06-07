package ua.renamer.app.core.commands.preparation;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.abstracts.Command;
import ua.renamer.app.core.enums.ItemPositionExtended;
import ua.renamer.app.core.model.FileInformation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ua.renamer.app.core.commands.preparation.TestUtils.TEST_FILE_NAME;

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
                                                   .textToReplace(TEST_FILE_NAME)
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