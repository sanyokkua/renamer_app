package ua.renamer.app.metadata.util;

import org.jspecify.annotations.Nullable;
import ua.renamer.app.api.enums.DateFormat;
import ua.renamer.app.api.enums.DateTimeFormat;
import ua.renamer.app.api.enums.TimeFormat;
import ua.renamer.app.api.interfaces.DateTimeUtils;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

/**
 * Adapts the {@link ua.renamer.app.utils.datetime.DateTimeUtils} static helpers to the
 * {@link DateTimeUtils} interface required by the metadata module.
 */
public class DateTimeConverter implements DateTimeUtils {

    @Override
    public ZoneId getSystemZoneId() {
        return ZoneId.systemDefault();
    }

    @Override
    public LocalDateTime toLocalDateTime(FileTime fileTime) {
        return ua.renamer.app.utils.datetime.DateTimeUtils.toLocalDateTime(fileTime);
    }

    @Override
    public LocalDateTime getMinimalDateTime() {
        return ua.renamer.app.utils.datetime.DateTimeUtils.getMinimalDateTime();
    }

    @Override
    public LocalDateTime parseDateTimeString(String dateTimeString) {
        return ua.renamer.app.utils.datetime.DateTimeUtils.parseDateTimeString(dateTimeString);
    }

    @Override
    public LocalDateTime parseDateTimeString(String dateTimeString, String offset) {
        return ua.renamer.app.utils.datetime.DateTimeUtils.parseDateTimeString(dateTimeString, offset);
    }

    @Override
    public Optional<LocalDateTime> parseDateTimeStringWithZoneInfo(String dateTimeString) {
        return ua.renamer.app.utils.datetime.DateTimeUtils.parseDateTimeStringWithZoneInfo(dateTimeString);
    }

    @Override
    public Optional<LocalDateTime> parseDateTimeStringWithZoneOffset(String dateTimeString, String offset) {
        return ua.renamer.app.utils.datetime.DateTimeUtils.parseDateTimeStringWithZoneOffset(dateTimeString, offset);
    }

    @Override
    public Optional<LocalDateTime> parseDateOnly(String dateTimeString) {
        return ua.renamer.app.utils.datetime.DateTimeUtils.parseDateOnly(dateTimeString);
    }

    @Override
    public Optional<LocalDateTime> parseYearAndMonth(String dateTimeString) {
        return ua.renamer.app.utils.datetime.DateTimeUtils.parseYearAndMonth(dateTimeString);
    }

    @Override
    public String formatLocalDateTime(LocalDateTime localDateTime) {
        return ua.renamer.app.utils.datetime.DateTimeUtils.formatLocalDateTime(localDateTime);
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
        return ua.renamer.app.utils.datetime.DateTimeUtils.findMinOrNull(values);
    }
}
