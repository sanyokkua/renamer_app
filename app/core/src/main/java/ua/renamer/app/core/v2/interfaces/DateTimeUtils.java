package ua.renamer.app.core.v2.interfaces;

import org.jspecify.annotations.Nullable;
import ua.renamer.app.core.enums.DateFormat;
import ua.renamer.app.core.enums.DateTimeFormat;
import ua.renamer.app.core.enums.TimeFormat;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

public interface DateTimeUtils {
    ZoneId getSystemZoneId();

    LocalDateTime toLocalDateTime(FileTime fileTime);

    LocalDateTime getMinimalDateTime();

    /**
     * Parses a date-time string to a LocalDateTime object using various formats.
     *
     * @param dateTimeString The date-time string to parse.
     * @return The parsed LocalDateTime, or null if the string could not be parsed.
     */
    LocalDateTime parseDateTimeString(String dateTimeString);

    /**
     * Parses a date-time string to a LocalDateTime object using various formats, with an optional offset.
     *
     * @param dateTimeString The date-time string to parse.
     * @param offset         The optional offset to apply.
     * @return The parsed LocalDateTime, or null if the string could not be parsed.
     */
    LocalDateTime parseDateTimeString(String dateTimeString, String offset);

    /**
     * Tries to parse the date-time string using formats that include zone information.
     *
     * @param dateTimeString The date-time string to parse.
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    Optional<LocalDateTime> parseDateTimeStringWithZoneInfo(String dateTimeString);

    /**
     * Tries to parse the date-time string using formats that do not include zone information, applying the provided offset.
     *
     * @param dateTimeString The date-time string to parse.
     * @param offset         The offset to apply.
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    Optional<LocalDateTime> parseDateTimeStringWithZoneOffset(String dateTimeString, String offset);

    /**
     * Tries to parse the date-time string as a date only.
     *
     * @param dateTimeString The date-time string to parse.
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    Optional<LocalDateTime> parseDateOnly(String dateTimeString);

    /**
     * Tries to parse the date-time string as a year and month only.
     *
     * @param dateTimeString The date-time string to parse.
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    Optional<LocalDateTime> parseYearAndMonth(String dateTimeString);

    /**
     * Formats a LocalDateTime object to a string in the format "yyyy-MM-dd HH:mm:ss".
     *
     * @param localDateTime The LocalDateTime to format.
     * @return The formatted date-time string.
     */
    String formatLocalDateTime(LocalDateTime localDateTime);

    /**
     * Formats a LocalDateTime object according to specified date and time formats.
     *
     * @param localDateTime  The LocalDateTime to format.
     * @param dateFormat     The DateFormat to apply.
     * @param timeFormat     The TimeFormat to apply.
     * @param dateTimeFormat The DateTimeFormat to apply.
     * @return The formatted date-time string.
     */
    String formatDateTime(LocalDateTime localDateTime, DateFormat dateFormat, TimeFormat timeFormat, DateTimeFormat dateTimeFormat);

    /**
     * Formats a LocalDateTime object according to specified date format.
     *
     * @param localDateTime The LocalDateTime to format.
     * @param format        The DateFormat to apply.
     * @return The formatted date string.
     */
    String formatDate(LocalDateTime localDateTime, DateFormat format);

    /**
     * Formats a LocalDateTime object according to specified time format.
     *
     * @param localDateTime The LocalDateTime to format.
     * @param format        The TimeFormat to apply.
     * @return The formatted time string.
     */
    String formatTime(LocalDateTime localDateTime, TimeFormat format);

    /**
     * Finds the minimum LocalDateTime value from an array of LocalDateTime objects.
     * If the input array is null or empty, the method returns null. Null values in the array are ignored.
     *
     * @param values an array of LocalDateTime objects to search
     * @return the minimum LocalDateTime value, or null if the input array is null, empty, or contains only null values
     */
    @Nullable LocalDateTime findMinOrNull(@Nullable LocalDateTime... values);

}
