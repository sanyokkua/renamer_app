package ua.renamer.app.core.commands.preparation;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.abstracts.Command;
import ua.renamer.app.core.enums.TruncateOptions;
import ua.renamer.app.core.model.FileInformation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TruncateNamePrepareInformationCommandTest extends BaseCommandTest {

    @Test
    @Override
    void testCommandDefaultCreation() {
        var command = TruncateNamePrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, command.getTruncateOptions());
        assertEquals(0, command.getNumberOfSymbols());
    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return TruncateNamePrepareInformationCommand.builder()
                                                    .numberOfSymbols(2)
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