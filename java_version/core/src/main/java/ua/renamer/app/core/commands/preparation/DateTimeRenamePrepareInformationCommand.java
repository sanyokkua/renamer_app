package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.core.model.FileInformation;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateTimeRenamePrepareInformationCommand extends FileInformationCommand {

    @Builder.Default
    private ItemPositionWithReplacement dateTimePositionInTheName = ItemPositionWithReplacement.BEGIN;
    @Builder.Default
    private DateFormat dateFormat = DateFormat.DO_NOT_USE_DATE;
    @Builder.Default
    private TimeFormat timeFormat = TimeFormat.DO_NOT_USE_TIME;
    @Builder.Default
    private DateTimeFormat dateTimeFormat = DateTimeFormat.DATE_TIME_TOGETHER;
    @Builder.Default
    private DateTimeSource dateTimeSource = DateTimeSource.FILE_CREATION_DATE;
    @Builder.Default
    private boolean useUppercaseForAmPm = false;
    @Builder.Default
    private LocalDateTime customDateTime = LocalDateTime.now();
    @Builder.Default
    private String dateTimeAndNameSeparator = "";
    @Builder.Default
    private boolean useFallbackDateTime = false;
    @Builder.Default
    private boolean useCustomDateTimeAsFallback = false;

    @Override
    public FileInformation processItem(FileInformation item) {
        return null;
    }

}
