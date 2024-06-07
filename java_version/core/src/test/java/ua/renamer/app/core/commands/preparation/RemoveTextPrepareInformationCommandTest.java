package ua.renamer.app.core.commands.preparation;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.abstracts.Command;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ua.renamer.app.core.commands.preparation.TestUtils.TEST_FILE_NAME;

class RemoveTextPrepareInformationCommandTest extends BaseCommandTest {

    @Test
    @Override
    void testCommandDefaultCreation() {
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