package ua.renamer.app.core.service.command.impl.preparation;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ua.renamer.app.core.enums.*;
import ua.renamer.app.core.model.FileInformation;
import ua.renamer.app.core.model.FileInformationMetadata;
import ua.renamer.app.core.service.command.FileInformationCommand;
import ua.renamer.app.core.service.helper.DateTimeOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Command for renaming file names by adding date and time information within {@link FileInformation} objects.
 * This class extends {@link FileInformationCommand} and provides functionality to add formatted date and time
 * to the file names based on various options and sources.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class DateTimeRenamePrepareInformationCommand extends FileInformationCommand {

    /**
     * The operations to perform on date and time values.
     */
    @NonNull
    private final DateTimeOperations dateTimeOperations;

    /**
     * The position where the date and time should be added in the file name.
     */
    @NonNull
    @Builder.Default
    private final ItemPositionWithReplacement dateTimePositionInTheName = ItemPositionWithReplacement.BEGIN;
    /**
     * The date format to be used.
     */
    @NonNull
    @Builder.Default
    private final DateFormat dateFormat = DateFormat.DO_NOT_USE_DATE;
    /**
     * The time format to be used.
     */
    @NonNull
    @Builder.Default
    private final TimeFormat timeFormat = TimeFormat.DO_NOT_USE_TIME;
    /**
     * The combined date and time format to be used.
     */
    @NonNull
    @Builder.Default
    private final DateTimeFormat dateTimeFormat = DateTimeFormat.DATE_TIME_TOGETHER;
    /**
     * The source from which to retrieve the date and time.
     */
    @NonNull
    @Builder.Default
    private final DateTimeSource dateTimeSource = DateTimeSource.FILE_CREATION_DATE;
    /**
     * Flag indicating whether to use uppercase for AM/PM in time format.
     */
    @Builder.Default
    private final boolean useUppercaseForAmPm = false;
    /**
     * Custom date and time to be used when the source is set to CUSTOM_DATE.
     */
    @NonNull
    @Builder.Default
    private final LocalDateTime customDateTime = LocalDateTime.now();
    /**
     * Separator between the date/time and the original file name.
     */
    @NonNull
    @Builder.Default
    private final String dateTimeAndNameSeparator = "";
    /**
     * Flag indicating whether to use a fallback date and time if the primary source is null.
     */
    @Builder.Default
    private final boolean useFallbackDateTime = false;
    /**
     * Flag indicating whether to use custom date and time as the fallback.
     */
    @Builder.Default
    private final boolean useCustomDateTimeAsFallback = false;

    /**
     * Processes a {@link FileInformation} item by adding formatted date and time to its file name
     * based on the specified options and sources.
     *
     * @param item the {@link FileInformation} item to be processed.
     *
     * @return the processed {@link FileInformation} item with the new file name.
     */
    @Override
    public FileInformation processItem(FileInformation item) {
        if (DateFormat.DO_NOT_USE_DATE.equals(dateFormat) && TimeFormat.DO_NOT_USE_TIME.equals(timeFormat)) {
            return item;
        }

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
                localDateTime = dateTimeOperations.findMinOrNull(fileCreationDateTime,
                                                                 fileModificationDateTime,
                                                                 fileContentCreationDateTime);
            }
        }

        if (Objects.isNull(localDateTime)) {
            return item;
        }

        var resultDateTime = dateTimeOperations.formatDateTime(localDateTime, dateFormat, timeFormat, dateTimeFormat);
        var caseFormats = List.of(TimeFormat.HH_MM_SS_AM_PM_TOGETHER,
                                  TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED,
                                  TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED,
                                  TimeFormat.HH_MM_SS_AM_PM_DOTTED,
                                  TimeFormat.HH_MM_SS_AM_PM_DASHED,
                                  TimeFormat.HH_MM_AM_PM_TOGETHER,
                                  TimeFormat.HH_MM_AM_PM_WHITE_SPACED,
                                  TimeFormat.HH_MM_AM_PM_UNDERSCORED,
                                  TimeFormat.HH_MM_AM_PM_DOTTED,
                                  TimeFormat.HH_MM_AM_PM_DASHED);

        if (caseFormats.contains(timeFormat)) {
            if (useUppercaseForAmPm) {
                resultDateTime = resultDateTime.toUpperCase();
            } else {
                resultDateTime = resultDateTime.toLowerCase();
            }
        }

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
