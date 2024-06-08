package ua.renamer.app.core.service.command.preparation;

import org.junit.jupiter.api.Test;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.service.command.Command;
import ua.renamer.app.core.service.command.impl.preparation.DateTimeRenamePrepareInformationCommand;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeRenamePrepareInformationCommandTest extends BaseCommandTest {

    @Test
    @Override
    void testCommandDefaultCreation() {
        var command = DateTimeRenamePrepareInformationCommand.builder().build();

        assertNotNull(command);
        assertEquals(ItemPositionWithReplacement.BEGIN, command.getDateTimePositionInTheName());
        assertEquals(DateFormat.DO_NOT_USE_DATE, command.getDateFormat());
        assertEquals(TimeFormat.DO_NOT_USE_TIME, command.getTimeFormat());
        assertEquals(DateTimeFormat.DATE_TIME_TOGETHER, command.getDateTimeFormat());
        assertEquals(DateTimeSource.FILE_CREATION_DATE, command.getDateTimeSource());
        assertEquals("", command.getDateTimeAndNameSeparator());

        assertFalse(command.isUseUppercaseForAmPm());
        assertFalse(command.isUseFallbackDateTime());
        assertFalse(command.isUseCustomDateTimeAsFallback());

        var customDateTime = command.getCustomDateTime();
        assertNotNull(customDateTime);

        var currentDateTime = LocalDateTime.now();
        assertEquals(currentDateTime.getYear(), customDateTime.getYear());
        assertEquals(currentDateTime.getMonth(), customDateTime.getMonth());
        assertEquals(currentDateTime.getDayOfMonth(), customDateTime.getDayOfMonth());
        assertEquals(currentDateTime.getHour(), customDateTime.getHour());
        assertEquals(currentDateTime.getMinute(), customDateTime.getMinute());

    }

    @Override
    Command<List<FileInformation>, List<FileInformation>> getCommand() {
        return DateTimeRenamePrepareInformationCommand.builder()
                                                      .dateTimeSource(DateTimeSource.CURRENT_DATE)
                                                      .dateTimeFormat(DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970)
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