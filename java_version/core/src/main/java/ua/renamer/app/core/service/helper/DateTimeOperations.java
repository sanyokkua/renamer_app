package ua.renamer.app.core.service.helper;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.DateFormat;
import ua.renamer.app.core.enums.DateTimeFormat;
import ua.renamer.app.core.enums.TimeFormat;
import ua.renamer.app.core.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class for date and time operations, such as parsing and formatting.
 */
@Slf4j
@NoArgsConstructor
public class DateTimeOperations {

    /**
     * Parses a date-time string to a LocalDateTime object using various formats.
     *
     * @param dateTimeString The date-time string to parse.
     *
     * @return The parsed LocalDateTime, or null if the string could not be parsed.
     */
    public LocalDateTime parseDateTimeString(String dateTimeString) {
        return parseDateTimeString(dateTimeString, null);
    }

    /**
     * Parses a date-time string to a LocalDateTime object using various formats, with an optional offset.
     *
     * @param dateTimeString The date-time string to parse.
     * @param offset         The optional offset to apply.
     *
     * @return The parsed LocalDateTime, or null if the string could not be parsed.
     */
    public LocalDateTime parseDateTimeString(String dateTimeString, String offset) {
        if (Objects.isNull(dateTimeString) || dateTimeString.isBlank()) {
            return null;
        }

        var preparedDate = dateTimeString.trim();

        var parsedWithZoneInfo = parseDateTimeStringWithZoneInfo(preparedDate);
        if (parsedWithZoneInfo.isPresent()) {
            return parsedWithZoneInfo.get();
        }

        var parsedWithOffset = parseDateTimeStringWithZoneOffset(preparedDate, offset);
        if (parsedWithOffset.isPresent()) {
            return parsedWithOffset.get();
        }

        var parsedDateOnly = parseDateOnly(preparedDate);
        if (parsedDateOnly.isPresent()) {
            return parsedDateOnly.get();
        }

        var parsedYearAndMonth = parseYearAndMonth(preparedDate);
        if (parsedYearAndMonth.isPresent()) {
            return parsedYearAndMonth.get();
        }

        log.warn("Date format not supported: {}", preparedDate);
        return null;
    }

    /**
     * Tries to parse the date-time string using formats that include zone information.
     *
     * @param dateTimeString The date-time string to parse.
     *
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    private Optional<LocalDateTime> parseDateTimeStringWithZoneInfo(String dateTimeString) {
        var formatsWithZoneInfo = List.of("EEE, dd MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss z", "EEE, dd MMM yyyy HH:mm:ss ZZZZ", "EEE, dd MMM yyyy HH:mm:ss zzzz", "EEE, dd MMM yyyy HH:mm:ss SSSZ", "E MMM dd HH:mm:ss z yyyy", "yyyy-MM-dd HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ssXXX");

        // Try parsing with zone info formats first
        for (String format : formatsWithZoneInfo) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, formatter);
                return Optional.ofNullable(zonedDateTime.toLocalDateTime());
            } catch (DateTimeParseException e) {
                // Ignore and return empty optional
            }
        }
        return Optional.empty();
    }

    /**
     * Tries to parse the date-time string using formats that do not include zone information, applying the provided offset.
     *
     * @param dateTimeString The date-time string to parse.
     * @param offset         The offset to apply.
     *
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    private Optional<LocalDateTime> parseDateTimeStringWithZoneOffset(String dateTimeString, String offset) {

        ZoneOffset zoneOffset;
        try {
            zoneOffset = offset == null ? ZoneOffset.UTC : ZoneOffset.of(offset);
        } catch (DateTimeException e) {
            log.warn("Invalid offset provided: {}", offset);
            return Optional.empty();
        }

        var formatsWithoutZoneInfo = List.of("yyyy:MM:dd HH:mm:ss", "yyyy:MM:dd HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm", "EEE, dd MMM yyyy HH:mm:ss");

        // Try parsing without zone info formats next
        for (String format : formatsWithoutZoneInfo) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
                return Optional.ofNullable(localDateTime.atOffset(zoneOffset).toLocalDateTime());
            } catch (DateTimeParseException e) {
                // Ignore and try next format
            }
        }
        return Optional.empty();
    }

    /**
     * Tries to parse the date-time string as a date only.
     *
     * @param dateTimeString The date-time string to parse.
     *
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    private Optional<LocalDateTime> parseDateOnly(String dateTimeString) {
        var dateOnly = List.of("yyyy-MM-dd", "yyyyMMdd");

        for (String format : dateOnly) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate localDate = LocalDate.parse(dateTimeString, formatter);
                return Optional.of(LocalDateTime.of(localDate, LocalTime.of(0, 0)));
            } catch (DateTimeParseException e) {
                // Ignore and try next format
            }
        }
        return Optional.empty();
    }

    /**
     * Tries to parse the date-time string as a year and month only.
     *
     * @param dateTimeString The date-time string to parse.
     *
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    private Optional<LocalDateTime> parseYearAndMonth(String dateTimeString) {
        if (dateTimeString.length() == 7) {
            try {
                String[] strings = dateTimeString.split("-");
                var year = Integer.parseInt(strings[0]);
                var month = Integer.parseInt(strings[1]);
                return Optional.of(LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.of(0, 0)));
            } catch (NumberFormatException e) {
                // Ignore and try next format
            }
        }

        if (dateTimeString.length() == 4) {
            try {
                var year = Integer.parseInt(dateTimeString);
                return Optional.ofNullable(LocalDateTime.of(LocalDate.of(year, 1, 1), LocalTime.of(0, 0)));
            } catch (NumberFormatException e) {
                // Ignore and try next format
            }
        }

        return Optional.empty();
    }

    /**
     * Formats a LocalDateTime object to a string in the format "yyyy-MM-dd HH:mm:ss".
     *
     * @param localDateTime The LocalDateTime to format.
     *
     * @return The formatted date-time string.
     */
    public String formatLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    /**
     * Formats a LocalDateTime object according to specified date and time formats.
     *
     * @param localDateTime  The LocalDateTime to format.
     * @param dateFormat     The DateFormat to apply.
     * @param timeFormat     The TimeFormat to apply.
     * @param dateTimeFormat The DateTimeFormat to apply.
     *
     * @return The formatted date-time string.
     */
    public String formatDateTime(LocalDateTime localDateTime, DateFormat dateFormat, TimeFormat timeFormat, DateTimeFormat dateTimeFormat) {
        if (Objects.isNull(localDateTime)) {
            return "";
        }

        if (DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970.equals(dateTimeFormat)) {
            Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
            return "%d".formatted(instant.toEpochMilli()); // Check what is better seconds or millis. Probably millis should be better
        }

        final String dateFormatted = formatDate(localDateTime, dateFormat);
        final String timeFormatted = formatTime(localDateTime, timeFormat);

        if (StringUtils.isEmpty(dateFormatted) && StringUtils.isEmpty(timeFormatted)) {
            return "";
        }

        if (StringUtils.isEmpty(dateFormatted) && !StringUtils.isEmpty(timeFormatted)) {
            return timeFormatted;
        }

        if (StringUtils.isEmpty(timeFormatted) && !StringUtils.isEmpty(dateFormatted)) {
            return dateFormatted;
        }

        Optional<String> formatString = dateTimeFormat.getFormatString();

        return formatString.map(fs -> fs.formatted(dateFormatted, timeFormatted)).orElse("");
    }

    /**
     * Formats a LocalDateTime object according to specified date format.
     *
     * @param localDateTime The LocalDateTime to format.
     * @param format        The DateFormat to apply.
     *
     * @return The formatted date string.
     */
    public String formatDate(LocalDateTime localDateTime, DateFormat format) {
        if (Objects.isNull(format) || DateFormat.DO_NOT_USE_DATE.equals(format)) {
            return "";
        }

        if (Objects.isNull(localDateTime)) {
            return "";
        }

        var enumFormatter = format.getFormatter();
        if (enumFormatter.isEmpty()) {
            return "";
        }

        DateTimeFormatter formatter = enumFormatter.get();
        return formatter.format(localDateTime);
    }

    /**
     * Formats a LocalDateTime object according to specified time format.
     *
     * @param localDateTime The LocalDateTime to format.
     * @param format        The TimeFormat to apply.
     *
     * @return The formatted time string.
     */
    public String formatTime(LocalDateTime localDateTime, TimeFormat format) {
        if (Objects.isNull(format) || TimeFormat.DO_NOT_USE_TIME.equals(format)) {
            return "";
        }

        if (Objects.isNull(localDateTime)) {
            return "";
        }

        var enumFormatter = format.getFormatter();
        if (enumFormatter.isEmpty()) {
            return "";
        }

        DateTimeFormatter formatter = enumFormatter.get();
        return formatter.format(localDateTime);
    }

    /**
     * Finds the minimum LocalDateTime value from an array of LocalDateTime objects.
     * If the input array is null or empty, the method returns null. Null values in the array are ignored.
     *
     * @param values an array of LocalDateTime objects to search
     *
     * @return the minimum LocalDateTime value, or null if the input array is null, empty, or contains only null values
     */
    public LocalDateTime findMinOrNull(LocalDateTime... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        return Stream.of(values).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
    }

}
