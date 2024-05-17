package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.core.model.AppFile;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateTimeRenamePrepareCommand extends AppFileCommand {
    @Builder.Default
    private ItemPositionWithReplacement position = ItemPositionWithReplacement.BEGIN;
    @Builder.Default
    private DateFormat dateFormat = DateFormat.DO_NOT_USE_DATE;
    @Builder.Default
    private TimeFormat timeFormat = TimeFormat.DO_NOT_USE_TIME;
    @Builder.Default
    private DateTimeFormat dateTimeFormat = DateTimeFormat.DATE_TIME_TOGETHER;
    @Builder.Default
    private DateTimeSource dateTimeSource = DateTimeSource.FILE_CREATION_DATE;
    @Builder.Default
    private boolean useUppercase = false;
    @Builder.Default
    private String customDatetime = "";
    @Builder.Default
    private String separatorForNameAndDatetime = "";
    @Builder.Default
    private boolean useFallbackDates = false;
    @Builder.Default
    private float useFallbackDateTimestamp = 0;

    @Override
    public AppFile processItem(AppFile item) {
        return null;
    }
}
