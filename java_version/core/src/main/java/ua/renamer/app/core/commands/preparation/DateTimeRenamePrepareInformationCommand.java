package ua.renamer.app.core.commands.preparation;

import lombok.*;
import ua.renamer.app.core.abstracts.FileInformationCommand;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.utils.DateTimeUtils;
import ua.renamer.app.core.utils.Utils;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeRenamePrepareInformationCommand extends FileInformationCommand {

    @NonNull
    @Builder.Default
    private final ItemPositionWithReplacement dateTimePositionInTheName = ItemPositionWithReplacement.BEGIN;
    @NonNull
    @Builder.Default
    private final DateFormat dateFormat = DateFormat.DO_NOT_USE_DATE;
    @NonNull
    @Builder.Default
    private final TimeFormat timeFormat = TimeFormat.DO_NOT_USE_TIME;
    @NonNull
    @Builder.Default
    private final DateTimeFormat dateTimeFormat = DateTimeFormat.DATE_TIME_TOGETHER;
    @NonNull
    @Builder.Default
    private final DateTimeSource dateTimeSource = DateTimeSource.FILE_CREATION_DATE;
    @NonNull
    @Builder.Default
    private final boolean useUppercaseForAmPm = false;
    @NonNull
    @Builder.Default
    private final LocalDateTime customDateTime = LocalDateTime.now();
    @NonNull
    @Builder.Default
    private final String dateTimeAndNameSeparator = "";
    @NonNull
    @Builder.Default
    private final boolean useFallbackDateTime = false;
    @NonNull
    @Builder.Default
    private final boolean useCustomDateTimeAsFallback = false;

    @Override
    public FileInformation processItem(FileInformation item) {
        var currentName = item.getFileName();
        var fileCreationDateTime = item.getFsCreationDate().orElse(null);
        var fileModificationDateTime = item.getFsModificationDate().orElse(null);
        var fileContentCreationDateTime = item.getMetadata()
                                              .flatMap(FileInformationMetadata::getCreationDate)
                                              .orElse(null);

        LocalDateTime localDateTime = null;
        switch (dateTimeSource) {
            case FILE_CREATION_DATE -> localDateTime = fileCreationDateTime;
            case FILE_MODIFICATION_DATE -> localDateTime = fileModificationDateTime;
            case CONTENT_CREATION_DATE -> localDateTime = fileContentCreationDateTime;
            case CURRENT_DATE -> localDateTime = LocalDateTime.now();
            case CUSTOM_DATE -> localDateTime = customDateTime;
        }

        if (useFallbackDateTime && Objects.isNull(localDateTime)) {
            if (useCustomDateTimeAsFallback) {
                localDateTime = customDateTime;
            } else {
                localDateTime = Utils.findMinOrNull(fileCreationDateTime,
                                                    fileModificationDateTime,
                                                    fileContentCreationDateTime
                                                   );
            }
        }

        if (Objects.isNull(localDateTime)) {
            return item;
        }

        var resultDateTime = DateTimeUtils.formatDateTime(localDateTime, dateFormat, timeFormat, dateTimeFormat);
        var newName = "";

        switch (dateTimePositionInTheName) {
            case REPLACE -> newName = resultDateTime;
            case BEGIN -> newName = resultDateTime + dateTimeAndNameSeparator + currentName;
            case END -> newName = currentName + dateTimeAndNameSeparator + resultDateTime;
        }

        item.setNewName(newName);

        return item;
    }

}
