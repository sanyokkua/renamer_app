package ua.renamer.app.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.enums.DateFormat;
import ua.renamer.app.core.enums.DateTimeFormat;
import ua.renamer.app.core.enums.TimeFormat;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

    public static LocalDateTime parseDateTimeString(String dateTimeString) {
        return parseDateTimeString(dateTimeString, null);
    }

    public static LocalDateTime parseDateTimeString(String dateTimeString, String offset) {
        if (Objects.isNull(dateTimeString) || dateTimeString.isEmpty()) {
            return null;
        }

        var formatsWithZoneInfo = List.of(
                "EEE, dd MMM yyyy HH:mm:ss Z",
                "E MMM dd HH:mm:ss z yyyy",
                "yyyy-MM-dd HH:mm:ss Z",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ssXXX"
                                         );

        // Try parsing with zone info formats first
        for (String format : formatsWithZoneInfo) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, formatter);
                return zonedDateTime.toLocalDateTime();
            } catch (DateTimeParseException e) {
                // Ignore and try next format
            }
        }

        ZoneOffset zoneOffset;
        try {
            zoneOffset = offset == null ? ZoneOffset.UTC : ZoneOffset.of(offset);
        } catch (DateTimeException e) {
            log.warn("Invalid offset provided: {}", offset);
            return null;
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
                "yyyy-MM-dd",
                "yyyy-MM",
                "yyyyMMdd",
                "yyyy",
                "EEE, dd MMM yyyy HH:mm:ss"
                                            );

        // Try parsing without zone info formats next
        for (String format : formatsWithoutZoneInfo) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
                return localDateTime.atOffset(zoneOffset).toLocalDateTime();
            } catch (DateTimeParseException e) {
                // Ignore and try next format
            }
        }

        log.warn("Date format not supported: {}", dateTimeString);
        return null;
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    public static String formatDate(LocalDateTime localDateTime, DateFormat format) {
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

    public static String formatTime(LocalDateTime localDateTime, TimeFormat format) {
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

    public static String formatDateTime(
            LocalDateTime localDateTime,
            DateFormat dateFormat,
            TimeFormat timeFormat,
            DateTimeFormat dateTimeFormat) {
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

}
