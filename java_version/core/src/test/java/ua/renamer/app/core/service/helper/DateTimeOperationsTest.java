package ua.renamer.app.core.service.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.TestUtilities;
import ua.renamer.app.core.enums.DateFormat;
import ua.renamer.app.core.enums.DateTimeFormat;
import ua.renamer.app.core.enums.TimeFormat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DateTimeOperationsTest {

    private final DateTimeOperations operations = new DateTimeOperations();

    static Stream<Arguments> provideArgumentsForFindMinOrNull() {
        var date20240529170510 = LocalDateTime.of(2024, 5, 29, 17, 5, 10);

        var date20240529170511 = LocalDateTime.of(2024, 5, 29, 17, 5, 11);
        var date20240529170509 = LocalDateTime.of(2024, 5, 29, 17, 5, 9);
        var date20200529170510 = LocalDateTime.of(2020, 5, 29, 17, 5, 10);
        var date20240529170512 = LocalDateTime.of(2024, 5, 29, 17, 5, 12);
        var date20240529170513 = LocalDateTime.of(2024, 5, 29, 17, 5, 13);
        var date20240529170514 = LocalDateTime.of(2024, 5, 29, 17, 5, 14);
        // @formatter:off
        return Stream.of(arguments(null, null),
                         arguments(null, new LocalDateTime[0]),
                         arguments(date20240529170510, new LocalDateTime[]{date20240529170510}),
                         arguments(date20240529170510, new LocalDateTime[]{date20240529170510, date20240529170511}),
                         arguments(date20240529170509, new LocalDateTime[]{date20240529170510, date20240529170509, date20240529170511}),
                         arguments(date20240529170510, new LocalDateTime[]{date20240529170510, date20240529170511, date20240529170512, date20240529170513, date20240529170514}),
                         arguments(date20240529170510, new LocalDateTime[]{date20240529170510, null, date20240529170512, null, date20240529170514}),
                         arguments(date20240529170510, new LocalDateTime[]{date20240529170510, null, date20240529170512, null, null}),
                         arguments(date20240529170510, new LocalDateTime[]{date20240529170510, null, null, null, null}),
                         arguments(date20200529170510, new LocalDateTime[]{date20240529170510, date20240529170511, date20240529170512, date20240529170513, date20240529170514, date20240529170510, date20200529170510})
                        );
        // @formatter:on
    }

    static Stream<Arguments> provideArgumentsForParseDateTimeString() {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 6, 8, 15, 30, 45);
        var monthName = localDateTime.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        var dayName = localDateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());

        // @formatter:off
        return Stream.of(
                arguments(null, null, null),
                arguments("%s, 08 %s 2024 15:30:45 +0300".formatted(dayName, monthName), LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("%s %s 08 15:30:45 EEST 2024".formatted(dayName, monthName), LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("2024-06-08 15:30:45 +0300", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("2024-06-08T15:30:45+0300", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("2024-06-08T15:30:45+03:00", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("2024:06:08 15:30:45", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("2024:06:08 15:30", LocalDateTime.of(2024, 6, 8, 15, 30, 0), null),
                arguments("2024-06-08 15:30:45", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("2024-06-08 15:30", LocalDateTime.of(2024, 6, 8, 15, 30), null),
                arguments("2024.06.08 15:30:45", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("2024.06.08 15:30", LocalDateTime.of(2024, 6, 8, 15, 30), null),
                arguments("2024-06-08T15:30:45", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("2024-06-08T15:30", LocalDateTime.of(2024, 6, 8, 15, 30), null),
                arguments("2024-06-08", LocalDateTime.of(2024, 6, 8, 0, 0), null),
                arguments("2024-06", LocalDateTime.of(2024, 6, 1, 0, 0), null),
                arguments("20240608", LocalDateTime.of(2024, 6, 8, 0, 0, 0), null),
                arguments("2024", LocalDateTime.of(2024, 1, 1, 0, 0), null),
                arguments("", null, null),
                arguments("aaaa", null, null),
                arguments("1", null, null),
                arguments("12", null, null),
                arguments("123", null, null),
                arguments("12345", null, null),
                arguments("123456", null, null),
                arguments("bbbbbbb", null, null),
                arguments("12345678", null, null),
                arguments("123456789", null, null),
                arguments("%s, 08 %s 2024 15:30:45".formatted(dayName, monthName), LocalDateTime.of(2024, 6, 8, 15, 30, 45), null)
                        );
        // @formatter:on
    }

    static Stream<Arguments> provideArgumentsForFormatDateArguments() {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 6, 8, 15, 30, 45);
        return Stream.of(arguments(null, DateFormat.DO_NOT_USE_DATE, ""), arguments(null, null, ""), arguments(null, DateFormat.YYYY_MM_DD_DASHED, ""), arguments(localDateTime, DateFormat.DO_NOT_USE_DATE, ""), arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, "20240608"), arguments(localDateTime, DateFormat.YYYY_MM_DD_WHITE_SPACED, "2024 06 08"), arguments(localDateTime, DateFormat.YYYY_MM_DD_UNDERSCORED, "2024_06_08"), arguments(localDateTime, DateFormat.YYYY_MM_DD_DOTTED, "2024.06.08"), arguments(localDateTime, DateFormat.YYYY_MM_DD_DASHED, "2024-06-08"), arguments(localDateTime, DateFormat.YY_MM_DD_TOGETHER, "240608"), arguments(localDateTime, DateFormat.YY_MM_DD_WHITE_SPACED, "24 06 08"), arguments(localDateTime, DateFormat.YY_MM_DD_UNDERSCORED, "24_06_08"), arguments(localDateTime, DateFormat.YY_MM_DD_DOTTED, "24.06.08"), arguments(localDateTime, DateFormat.YY_MM_DD_DASHED, "24-06-08"), arguments(localDateTime, DateFormat.MM_DD_YYYY_TOGETHER, "06082024"), arguments(localDateTime, DateFormat.MM_DD_YYYY_WHITE_SPACED, "06 08 2024"), arguments(localDateTime, DateFormat.MM_DD_YYYY_UNDERSCORED, "06_08_2024"), arguments(localDateTime, DateFormat.MM_DD_YYYY_DOTTED, "06.08.2024"), arguments(localDateTime, DateFormat.MM_DD_YYYY_DASHED, "06-08-2024"), arguments(localDateTime, DateFormat.MM_DD_YY_TOGETHER, "060824"), arguments(localDateTime, DateFormat.MM_DD_YY_WHITE_SPACED, "06 08 24"), arguments(localDateTime, DateFormat.MM_DD_YY_UNDERSCORED, "06_08_24"), arguments(localDateTime, DateFormat.MM_DD_YY_DOTTED, "06.08.24"), arguments(localDateTime, DateFormat.MM_DD_YY_DASHED, "06-08-24"), arguments(localDateTime, DateFormat.DD_MM_YYYY_TOGETHER, "08062024"), arguments(localDateTime, DateFormat.DD_MM_YYYY_WHITE_SPACED, "08 06 2024"), arguments(localDateTime, DateFormat.DD_MM_YYYY_UNDERSCORED, "08_06_2024"), arguments(localDateTime, DateFormat.DD_MM_YYYY_DOTTED, "08.06.2024"), arguments(localDateTime, DateFormat.DD_MM_YYYY_DASHED, "08-06-2024"), arguments(localDateTime, DateFormat.DD_MM_YY_TOGETHER, "080624"), arguments(localDateTime, DateFormat.DD_MM_YY_WHITE_SPACED, "08 06 24"), arguments(localDateTime, DateFormat.DD_MM_YY_UNDERSCORED, "08_06_24"), arguments(localDateTime, DateFormat.DD_MM_YY_DOTTED, "08.06.24"), arguments(localDateTime, DateFormat.DD_MM_YY_DASHED, "08-06-24"));
    }

    static Stream<Arguments> provideArgumentsForFormatTimeArguments() {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 6, 8, 15, 30, 45);
        var res = TestUtilities.getLocaleAmPm();
        var amLocaleSymbols = res.AM();
        var pmLocaleSymbols = res.PM();
        return Stream.of(arguments(null, TimeFormat.DO_NOT_USE_TIME, ""), arguments(null, null, ""), arguments(null, TimeFormat.HH_MM_24_DASHED, ""), arguments(localDateTime, TimeFormat.HH_MM_SS_24_TOGETHER, "153045"), arguments(localDateTime, TimeFormat.HH_MM_SS_24_WHITE_SPACED, "15 30 45"), arguments(localDateTime, TimeFormat.HH_MM_SS_24_UNDERSCORED, "15_30_45"), arguments(localDateTime, TimeFormat.HH_MM_SS_24_DOTTED, "15.30.45"), arguments(localDateTime, TimeFormat.HH_MM_SS_24_DASHED, "15-30-45"), arguments(localDateTime, TimeFormat.HH_MM_24_TOGETHER, "1530"), arguments(localDateTime, TimeFormat.HH_MM_24_WHITE_SPACED, "15 30"), arguments(localDateTime, TimeFormat.HH_MM_24_UNDERSCORED, "15_30"), arguments(localDateTime, TimeFormat.HH_MM_24_DOTTED, "15.30"), arguments(localDateTime, TimeFormat.HH_MM_24_DASHED, "15-30"), arguments(localDateTime, TimeFormat.HH_MM_SS_AM_PM_TOGETHER, "033045" + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED, "03 30 45 " + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED, "03_30_45_" + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_SS_AM_PM_DOTTED, "03.30.45." + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_SS_AM_PM_DASHED, "03-30-45-" + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_AM_PM_TOGETHER, "0330" + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_AM_PM_WHITE_SPACED, "03 30 " + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_AM_PM_UNDERSCORED, "03_30_" + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_AM_PM_DOTTED, "03.30." + pmLocaleSymbols), arguments(localDateTime, TimeFormat.HH_MM_AM_PM_DASHED, "03-30-" + pmLocaleSymbols), arguments(localDateTime.withHour(2), TimeFormat.HH_MM_AM_PM_DASHED, "02-30-" + amLocaleSymbols));
    }

    static Stream<Arguments> provideArgumentsForFormatDateTimeArguments() {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 6, 8, 15, 30, 45);
        // @formatter:off
        return Stream.of(
                arguments(null, DateFormat.DO_NOT_USE_DATE, TimeFormat.DO_NOT_USE_TIME, DateTimeFormat.DATE_TIME_TOGETHER, ""),
                arguments(localDateTime, DateFormat.DO_NOT_USE_DATE, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_TOGETHER, "153045"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.DO_NOT_USE_TIME, DateTimeFormat.DATE_TIME_TOGETHER, "20240608"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_TOGETHER, "20240608153045"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_WHITE_SPACED, "20240608 153045"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_UNDERSCORED, "20240608_153045"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_DOTTED, "20240608.153045"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.DATE_TIME_DASHED, "20240608-153045"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.REVERSE_DATE_TIME_TOGETHER, "15304520240608"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.REVERSE_DATE_TIME_WHITE_SPACED, "153045 20240608"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.REVERSE_DATE_TIME_UNDERSCORED, "153045_20240608"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.REVERSE_DATE_TIME_DOTTED, "153045.20240608"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.REVERSE_DATE_TIME_DASHED, "153045-20240608"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970, String.valueOf(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli())),
                arguments(localDateTime, DateFormat.DO_NOT_USE_DATE, TimeFormat.DO_NOT_USE_TIME, DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970, String.valueOf(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli())),
                arguments(localDateTime, DateFormat.DO_NOT_USE_DATE, TimeFormat.HH_MM_SS_24_TOGETHER, DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970, String.valueOf(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli())),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.DO_NOT_USE_TIME, DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970, String.valueOf(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()))
                        );
        // @formatter:on
    }

    @Test
    void testParseDateTimeStringWithNullOffset() {
        var result = operations.parseDateTimeString("2022-12-01 13:11:44 +0200");

        assertNotNull(result);
        assertEquals(LocalDateTime.of(2022, 12, 1, 13, 11, 44), result);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForParseDateTimeString")
    void testParseDateTimeString(String dateTimeString, LocalDateTime expected, String offset) {
        var result = operations.parseDateTimeString(dateTimeString, offset);

        assertEquals(expected, result);
    }

    @Test
    void testFormatLocalDateTime() {
        Locale.setDefault(Locale.US);
        var result = operations.formatLocalDateTime(LocalDateTime.of(2024, 12, 9, 9, 59));

        assertNotNull(result);
        assertEquals("2024-12-09 09:59:00", result);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForFormatDateArguments")
    void testFormatDate(LocalDateTime localDateTime, DateFormat dateFormat, String expected) {
        var result = operations.formatDate(localDateTime, dateFormat);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForFormatTimeArguments")
    void testFormatTime(LocalDateTime localDateTime, TimeFormat timeFormat, String expected) {
        var result = operations.formatTime(localDateTime, timeFormat);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForFormatDateTimeArguments")
    void testFormatDateTime(LocalDateTime localDateTime, DateFormat dateFormat, TimeFormat timeFormat, DateTimeFormat dateTimeFormat, String expected) {
        var result = operations.formatDateTime(localDateTime, dateFormat, timeFormat, dateTimeFormat);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForFindMinOrNull")
    void testFindMinOrNull(LocalDateTime expected, LocalDateTime[] actual) {
        var result = operations.findMinOrNull(actual);
        assertEquals(expected, result);
    }

}