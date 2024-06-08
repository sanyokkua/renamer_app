package ua.renamer.app.core.service.command.preparation;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.enums.ItemPosition;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.command.impl.preparation.ParentFoldersPrepareInformationCommand;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParentFoldersPrepareInformationCommandTest extends BaseCommandTest {

    @Test
    @Override
    void testCommandDefaultCreation() {
        var command = ParentFoldersPrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(ItemPosition.BEGIN, command.getPosition());
        assertEquals(1, command.getNumberOfParents());
        assertEquals("", command.getSeparator());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return ParentFoldersPrepareInformationCommand.builder().build();
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