package ua.renamer.app.core.v2.util;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import ua.renamer.app.api.enums.DateFormat;
import ua.renamer.app.api.enums.DateTimeFormat;
import ua.renamer.app.api.enums.TimeFormat;
import ua.renamer.app.api.interfaces.DateTimeUtils;

import java.nio.file.attribute.FileTime;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Test-only implementation of DateTimeUtils backed by java.time.
 * Used in core tests instead of the real DateTimeConverter (which lives in metadata).
 */
@Slf4j
public class TestDateTimeUtils implements DateTimeUtils {

    @Override
    public ZoneId getSystemZoneId() {
        return ZoneId.systemDefault();
    }

    @Override
    public LocalDateTime toLocalDateTime(FileTime fileTime) {
        if (fileTime == null) {
            return getMinimalDateTime();
        }
        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }

    @Override
    public LocalDateTime getMinimalDateTime() {
        return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    }

    @Override
    public LocalDateTime parseDateTimeString(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return getMinimalDateTime();
        }
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));
        } catch (Exception e) {
            log.debug("Failed to parse date-time string '{}', returning minimal date-time. Exception: {}", dateTimeString, e.getMessage());
            return getMinimalDateTime();
        }
    }

    @Override
    public LocalDateTime parseDateTimeString(String dateTimeString, String offset) {
        return parseDateTimeString(dateTimeString);
    }

    @Override
    public Optional<LocalDateTime> parseDateTimeStringWithZoneInfo(String dateTimeString) {
        try {
            return Optional.of(parseDateTimeString(dateTimeString));
        } catch (Exception e) {
            log.debug("Failed to parse date-time string '{}' with zone info, returning empty. Exception: {}", dateTimeString, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<LocalDateTime> parseDateTimeStringWithZoneOffset(String dateTimeString, String offset) {
        return parseDateTimeStringWithZoneInfo(dateTimeString);
    }

    @Override
    public Optional<LocalDateTime> parseDateOnly(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return Optional.empty();
        }
        try {
            LocalDate date = LocalDate.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy:MM:dd"));
            return Optional.of(date.atStartOfDay());
        } catch (Exception e) {
            log.debug("Failed to parse date-only string '{}', returning empty. Exception: {}", dateTimeString, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<LocalDateTime> parseYearAndMonth(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return Optional.empty();
        }
        try {
            YearMonth ym = YearMonth.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy:MM"));
            return Optional.of(ym.atDay(1).atStartOfDay());
        } catch (Exception e) {
            log.debug("Failed to parse year/month string '{}', returning empty. Exception: {}", dateTimeString, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String formatLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String formatDateTime(LocalDateTime localDateTime, DateFormat dateFormat, TimeFormat timeFormat, DateTimeFormat dateTimeFormat) {
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

        if (dateFormatted.isEmpty()) {
            return timeFormatted;
        }

        if (timeFormatted.isEmpty()) {
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
        return enumFormatter.get().format(localDateTime);
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
        return enumFormatter.get().format(localDateTime);
    }

    @Override
    public @Nullable LocalDateTime findMinOrNull(@Nullable LocalDateTime... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }
}
