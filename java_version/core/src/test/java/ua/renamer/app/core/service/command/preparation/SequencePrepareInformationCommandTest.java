package ua.renamer.app.core.service.command.preparation;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.enums.SortSource;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.command.impl.preparation.SequencePrepareInformationCommand;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SequencePrepareInformationCommandTest extends BaseCommandTest {

    @Test
    @Override
    void testCommandDefaultCreation() {
        var command = SequencePrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(SortSource.FILE_NAME, command.getSortSource());
        assertEquals(0, command.getStartNumber());
        assertEquals(1, command.getStepValue());
        assertEquals(0, command.getPadding());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return SequencePrepareInformationCommand.builder().build();
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