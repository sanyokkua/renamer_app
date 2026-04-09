package ua.renamer.app.api.interfaces;

import org.jspecify.annotations.Nullable;
import ua.renamer.app.api.enums.DateFormat;
import ua.renamer.app.api.enums.DateTimeFormat;
import ua.renamer.app.api.enums.TimeFormat;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Port interface for datetime parsing, formatting, and conversion operations.
 * Implementations live in the infrastructure (metadata) module.
 */
public interface DateTimeUtils {

    /**
     * Returns the system default time zone.
     *
     * @return the system zone ID; never null
     */
    ZoneId getSystemZoneId();

    /**
     * Converts a {@link FileTime} to a {@link LocalDateTime}.
     *
     * @param fileTime the file time; may be null
     * @return the converted local date-time; never null (returns minimal fallback for null input)
     */
    LocalDateTime toLocalDateTime(FileTime fileTime);

    /**
     * Returns a sentinel minimal date-time value used as a fallback.
     *
     * @return the minimal date-time; never null
     */
    LocalDateTime getMinimalDateTime();

    /**
     * Parses a date-time string to a LocalDateTime using various formats.
     *
     * @param dateTimeString the date-time string to parse; must not be null
     * @return the parsed LocalDateTime, or a minimal fallback if parsing fails
     */
    LocalDateTime parseDateTimeString(String dateTimeString);

    /**
     * Parses a date-time string to a LocalDateTime using various formats, with an optional timezone offset.
     *
     * @param dateTimeString the date-time string to parse; must not be null
     * @param offset         the timezone offset string; must not be null
     * @return the parsed LocalDateTime, or a minimal fallback if parsing fails
     */
    LocalDateTime parseDateTimeString(String dateTimeString, String offset);

    /**
     * Tries to parse the date-time string using formats that include zone information.
     *
     * @param dateTimeString the date-time string to parse; must not be null
     * @return an Optional containing the parsed LocalDateTime, or empty if parsing fails
     */
    Optional<LocalDateTime> parseDateTimeStringWithZoneInfo(String dateTimeString);

    /**
     * Tries to parse the date-time string applying the provided timezone offset.
     *
     * @param dateTimeString the date-time string to parse; must not be null
     * @param offset         the offset to apply; must not be null
     * @return an Optional containing the parsed LocalDateTime, or empty if parsing fails
     */
    Optional<LocalDateTime> parseDateTimeStringWithZoneOffset(String dateTimeString, String offset);

    /**
     * Tries to parse the date-time string as a date-only value.
     *
     * @param dateTimeString the date-time string to parse; must not be null
     * @return an Optional containing the parsed LocalDateTime, or empty if parsing fails
     */
    Optional<LocalDateTime> parseDateOnly(String dateTimeString);

    /**
     * Tries to parse the date-time string as a year-and-month-only value.
     *
     * @param dateTimeString the date-time string to parse; must not be null
     * @return an Optional containing the parsed LocalDateTime, or empty if parsing fails
     */
    Optional<LocalDateTime> parseYearAndMonth(String dateTimeString);

    /**
     * Formats a LocalDateTime to a string in the format {@code yyyy-MM-dd HH:mm:ss}.
     *
     * @param localDateTime the date-time to format; must not be null
     * @return the formatted string; never null
     */
    String formatLocalDateTime(LocalDateTime localDateTime);

    /**
     * Formats a LocalDateTime according to specified date, time, and combined formats.
     *
     * @param localDateTime  the date-time to format; may be null (returns empty string)
     * @param dateFormat     the date format to apply; must not be null
     * @param timeFormat     the time format to apply; must not be null
     * @param dateTimeFormat the combined format to apply; must not be null
     * @return the formatted string; never null; empty string if localDateTime is null or both parts are empty
     */
    String formatDateTime(LocalDateTime localDateTime, DateFormat dateFormat, TimeFormat timeFormat, DateTimeFormat dateTimeFormat);

    /**
     * Formats a LocalDateTime using the specified date format only.
     *
     * @param localDateTime the date-time to format; may be null (returns empty string)
     * @param format        the date format to apply; must not be null
     * @return the formatted date string; never null
     */
    String formatDate(LocalDateTime localDateTime, DateFormat format);

    /**
     * Formats a LocalDateTime using the specified time format only.
     *
     * @param localDateTime the date-time to format; may be null (returns empty string)
     * @param format        the time format to apply; must not be null
     * @return the formatted time string; never null
     */
    String formatTime(LocalDateTime localDateTime, TimeFormat format);

    /**
     * Finds the minimum LocalDateTime value from an array of values.
     * Null values within the array are ignored.
     *
     * @param values an array of LocalDateTime objects; may be null or empty
     * @return the minimum value, or null if the input is null, empty, or contains only nulls
     */
    @Nullable LocalDateTime findMinOrNull(@Nullable LocalDateTime... values);
}
