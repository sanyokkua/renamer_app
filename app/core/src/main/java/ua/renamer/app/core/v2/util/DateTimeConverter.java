package ua.renamer.app.core.v2.util;

import org.jspecify.annotations.Nullable;
import ua.renamer.app.core.enums.DateFormat;
import ua.renamer.app.core.enums.DateTimeFormat;
import ua.renamer.app.core.enums.TimeFormat;
import ua.renamer.app.core.v2.interfaces.DateTimeUtils;

import java.nio.file.attribute.FileTime;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Stream;

public class DateTimeConverter implements DateTimeUtils {
    private static final LocalDateTime MINIMAL = LocalDateTime.of(1900, 1, 1, 0, 0);

    @Override
    public ZoneId getSystemZoneId() {
        return ZoneId.systemDefault();
    }

    @Override
    public LocalDateTime toLocalDateTime(FileTime fileTime) {
        if (fileTime == null) {
            return null;
        }

        return LocalDateTime.ofInstant(fileTime.toInstant(), getSystemZoneId());
    }

    public static LocalDateTime toLocalDateTimeStatic(FileTime fileTime) {
        if (fileTime == null) {
            return null;
        }

        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }

    @Override
    public LocalDateTime getMinimalDateTime() {
        return MINIMAL;
    }

    @Override
    public LocalDateTime parseDateTimeString(String dateTimeString) {
        return parseDateTimeString(dateTimeString, null);
    }

    @Override
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

        return null;
    }

    @Override
    public Optional<LocalDateTime> parseDateTimeStringWithZoneInfo(String dateTimeString) {
        var formatsWithZoneInfo = List.of("EEE, dd MMM yyyy HH:mm:ss Z",
                                          "EEE, dd MMM yyyy HH:mm:ss Z",
                                          "EEE, dd MMM yyyy HH:mm:ss z",
                                          "EEE, dd MMM yyyy HH:mm:ss ZZZZ",
                                          "EEE, dd MMM yyyy HH:mm:ss zzzz",
                                          "EEE, dd MMM yyyy HH:mm:ss SSSZ",
                                          "E MMM dd HH:mm:ss z yyyy",
                                          "yyyy-MM-dd HH:mm:ss Z",
                                          "yyyy-MM-dd'T'HH:mm:ssZ",
                                          "yyyy-MM-dd'T'HH:mm:ssXXX");

        // Try parsing with zone info formats first
        Locale[] availableLocales = Locale.getAvailableLocales();
        Set<Locale> locales = new LinkedHashSet<>();
        locales.add(Locale.ENGLISH); // Exif usually uses ENGLISH, US, FRENCH, GERMAN locales
        locales.add(Locale.UK);
        locales.add(Locale.US);
        locales.add(Locale.CANADA);
        locales.add(Locale.FRANCE);
        locales.add(Locale.FRENCH);
        locales.add(Locale.GERMAN);
        locales.add(Locale.GERMANY);
        locales.add(Locale.getDefault());
        locales.addAll(Arrays.stream(availableLocales).toList());

        for (String format : formatsWithZoneInfo) {
            for (Locale currentLocale : locales) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, currentLocale);
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString, formatter);
                    return Optional.ofNullable(zonedDateTime.toLocalDateTime());
                } catch (DateTimeParseException e) {
                    // Ignore and return empty optional
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<LocalDateTime> parseDateTimeStringWithZoneOffset(String dateTimeString, String offset) {
        ZoneOffset zoneOffset;
        try {
            zoneOffset = offset == null ? ZoneOffset.UTC : ZoneOffset.of(offset);
        } catch (DateTimeException e) {
            return Optional.empty();
        }

        var formatsWithoutZoneInfo = List.of("yyyy:MM:dd HH:mm:ss",
                                             "yyyy:MM:dd HH:mm",
                                             "yyyy-MM-dd HH:mm:ss",
                                             "yyyy-MM-dd HH:mm",
                                             "yyyy.MM.dd HH:mm:ss",
                                             "yyyy.MM.dd HH:mm",
                                             "yyyy-MM-dd'T'HH:mm:ss",
                                             "yyyy-MM-dd'T'HH:mm",
                                             "EEE, dd MMM yyyy HH:mm:ss");

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

    @Override
    public Optional<LocalDateTime> parseDateOnly(String dateTimeString) {
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

    @Override
    public Optional<LocalDateTime> parseYearAndMonth(String dateTimeString) {
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

    @Override
    public String formatLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    @Override
    public String formatDateTime(LocalDateTime localDateTime, DateFormat dateFormat, TimeFormat timeFormat,
                                 DateTimeFormat dateTimeFormat) {
        if (Objects.isNull(localDateTime)) {
            return "";
        }

        if (DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970.equals(dateTimeFormat)) {
            Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
            return "%d".formatted(instant.toEpochMilli());
        }

        final String dateFormatted = formatDate(localDateTime, dateFormat);
        final String timeFormatted = formatTime(localDateTime, timeFormat);

        if (dateFormatted.isEmpty() && timeFormatted.isEmpty()) {
            return "";
        }

        if (dateFormatted.isEmpty() && !timeFormatted.isEmpty()) {
            return timeFormatted;
        }

        if (timeFormatted.isEmpty() && !dateFormatted.isEmpty()) {
            return dateFormatted;
        }

        Optional<String> formatString = dateTimeFormat.getFormatString();

        return formatString.map(fs -> fs.formatted(dateFormatted, timeFormatted)).orElse("");
    }

    @Override
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

    @Override
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

    @Override
    public @Nullable LocalDateTime findMinOrNull(@Nullable LocalDateTime... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        return Stream.of(values).filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
    }
}
