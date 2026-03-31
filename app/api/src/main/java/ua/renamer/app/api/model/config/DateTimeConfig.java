package ua.renamer.app.api.model.config;

import lombok.Builder;
import lombok.Value;
import ua.renamer.app.api.enums.DateFormat;
import ua.renamer.app.api.enums.DateTimeFormat;
import ua.renamer.app.api.enums.DateTimeSource;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;
import ua.renamer.app.api.enums.TimeFormat;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Configuration for adding datetime to filenames.
 */
@Value
@Builder(setterPrefix = "with")
public class DateTimeConfig implements TransformationConfig {
    /** Source of the datetime (FILE_CREATION, CONTENT_CREATION, etc.). */
    DateTimeSource source;

    /** Date format to use. */
    DateFormat dateFormat;

    /** Time format to use. */
    TimeFormat timeFormat;

    /** Combined date-time format to use. */
    DateTimeFormat dateTimeFormat;

    /** Position where to add datetime (BEGIN, END, or REPLACE). */
    ItemPositionWithReplacement position;

    /** Custom datetime value (when source is CUSTOM_DATE). */
    LocalDateTime customDateTime;

    /** Separator between datetime and filename. */
    String separator;

    public Optional<LocalDateTime> getCustomDateTime() {
        return Optional.ofNullable(customDateTime);
    }
}
