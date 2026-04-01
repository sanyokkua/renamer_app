package ua.renamer.app.utils.datetime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.nio.file.attribute.FileTime;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class providing date-time parsing and formatting methods.
 * All methods are static, pure functions without side effects.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

    private static final LocalDateTime MINIMAL = LocalDateTime.of(1900, 1, 1, 0, 0);

    /**
     * Common locales for date parsing.
     * Includes English, US, EU (German, French, Croatian, Czech, Polish), Ukrainian, and Russian.
     * This optimized list reduces parsing attempts from ~7,000 to ~150 per call.
     */
    private static final List<Locale> COMMON_LOCALES = List.of(
            Locale.ENGLISH,
            Locale.US,
            Locale.UK,
            Locale.GERMAN,
            Locale.GERMANY,
            Locale.FRENCH,
            Locale.FRANCE,
            Locale.of("hr", "HR"),  // Croatian
            Locale.of("cs", "CZ"),  // Czech
            Locale.of("pl", "PL"),  // Polish
            Locale.of("uk", "UA"),  // Ukrainian
            Locale.of("ru", "RU")   // Russian
    );

    /**
     * Converts a {@link FileTime} to a {@link LocalDateTime} using the system default time zone.
     *
     * @param fileTime the file time to convert
     * @return the converted LocalDateTime, or null if fileTime is null
     */
    public static LocalDateTime toLocalDateTime(FileTime fileTime) {
        if (fileTime == null) {
            return null;
        }

        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Returns a hardcoded minimum date (1900-01-01 00:00:00).
     *
     * @return the minimal LocalDateTime
     */
    public static LocalDateTime getMinimalDateTime() {
        return MINIMAL;
    }

    /**
     * Parses a date-time string to a LocalDateTime object using various formats.
     *
     * @param dateTimeString The date-time string to parse.
     * @return The parsed LocalDateTime, or null if the string could not be parsed.
     */
    public static LocalDateTime parseDateTimeString(String dateTimeString) {
        return parseDateTimeString(dateTimeString, null);
    }

    /**
     * Parses a date-time string to a LocalDateTime object using various formats, with an optional offset.
     * Tries parsing strategies in order: with zone info, with offset, date only, year/month only.
     *
     * @param dateTimeString The date-time string to parse.
     * @param offset         The optional offset to apply (e.g., "+02:00").
     * @return The parsed LocalDateTime, or null if the string could not be parsed.
     */
    public static LocalDateTime parseDateTimeString(String dateTimeString, String offset) {
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
        return parsedYearAndMonth.orElse(null);
    }

    /**
     * Tries to parse the date-time string using formats that include zone information.
     *
     * @param dateTimeString The date-time string to parse.
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    public static Optional<LocalDateTime> parseDateTimeStringWithZoneInfo(String dateTimeString) {
        var formatsWithZoneInfo = List.of(
                "EEE, dd MMM yyyy HH:mm:ss Z",
                "EEE, dd MMM yyyy HH:mm:ss z",
                "EEE, dd MMM yyyy HH:mm:ss ZZZZ",
                "EEE, dd MMM yyyy HH:mm:ss zzzz",
                "EEE, dd MMM yyyy HH:mm:ss SSSZ",
                "E MMM dd HH:mm:ss z yyyy",
                "yyyy-MM-dd HH:mm:ss Z",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ssXXX"
        );

        for (String format : formatsWithZoneInfo) {
            for (Locale currentLocale : COMMON_LOCALES) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, currentLocale);
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, formatter);
                    return Optional.ofNullable(zonedDateTime.toLocalDateTime());
                } catch (DateTimeParseException e) {
                    // Ignore and continue trying other formats/locales
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Tries to parse the date-time string using formats that do not include zone information,
     * applying the provided offset.
     *
     * @param dateTimeString The date-time string to parse.
     * @param offset         The offset to apply (e.g., "+02:00"). If null, UTC is used.
     * @return An Optional containing the parsed LocalDateTime if successful, or an empty Optional otherwise.
     */
    public static Optional<LocalDateTime> parseDateTimeStringWithZoneOffset(String dateTimeString, String offset) {
        ZoneOffset zoneOffset;
        try {
            zoneOffset = offset == null ? ZoneOffset.UTC : ZoneOffset.of(offset);
        } catch (DateTimeException e) {
            return Optional.empty();
        }

        var formatsWithoutZoneInfo = List.of(
                "yyyy:MM:dd HH:mm:ss",
                "yyyy:MM:dd HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy.MM.dd HH:mm:ss",
                "yyyy.MM.dd HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "EEE, dd MMM yyyy HH:mm:ss"
        );

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
     * Tries to parse the date-time string as a date only (yyyy-MM-dd or yyyyMMdd).
     *
     * @param dateTimeString The date-time string to parse.
     * @return An Optional containing the parsed LocalDateTime (at midnight) if successful,
     * or an empty Optional otherwise.
     */
    public static Optional<LocalDateTime> parseDateOnly(String dateTimeString) {
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
     * Tries to parse the date-time string as a year and month only (yyyy-MM) or year only (yyyy).
     *
     * @param dateTimeString The date-time string to parse.
     * @return An Optional containing the parsed LocalDateTime (at day 1, midnight) if successful,
     * or an empty Optional otherwise.
     */
    public static Optional<LocalDateTime> parseYearAndMonth(String dateTimeString) {
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
                return Optional.of(LocalDateTime.of(LocalDate.of(year, 1, 1), LocalTime.of(0, 0)));
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
     * @return The formatted date-time string.
     */
    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    /**
     * Finds the minimum LocalDateTime value from an array of LocalDateTime objects.
     * If the input array is null or empty, the method returns null. Null values in the array are ignored.
     *
     * @param values an array of LocalDateTime objects to search
     * @return the minimum LocalDateTime value, or null if the input array is null, empty, or contains only null values
     */
    public static @Nullable LocalDateTime findMinOrNull(@Nullable LocalDateTime... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        return Stream.of(values).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
    }

}
