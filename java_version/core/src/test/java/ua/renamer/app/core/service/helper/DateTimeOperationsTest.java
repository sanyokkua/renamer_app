package ua.renamer.app.core.service.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.enums.DateFormat;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DateTimeOperationsTest {

    private final DateTimeOperations operations = new DateTimeOperations();

    static Stream<Arguments> findMinOrNullArguments() {
        var date20240529170510 = LocalDateTime.of(2024, 5, 29, 17, 5, 10);

        var date20240529170511 = LocalDateTime.of(2024, 5, 29, 17, 5, 11);
        var date20240529170509 = LocalDateTime.of(2024, 5, 29, 17, 5, 9);
        var date20200529170510 = LocalDateTime.of(2020, 5, 29, 17, 5, 10);
        var date20240529170512 = LocalDateTime.of(2024, 5, 29, 17, 5, 12);
        var date20240529170513 = LocalDateTime.of(2024, 5, 29, 17, 5, 13);
        var date20240529170514 = LocalDateTime.of(2024, 5, 29, 17, 5, 14);
        return Stream.of(
                arguments(null, null),
                arguments(null, new LocalDateTime[0]),
                arguments(date20240529170510, new LocalDateTime[]{date20240529170510}),
                arguments(date20240529170510, new LocalDateTime[]{date20240529170510, date20240529170511}),

                arguments(date20240529170509,
                          new LocalDateTime[]{date20240529170510, date20240529170509, date20240529170511}
                         ),

                arguments(date20240529170510,
                          new LocalDateTime[]{
                                  date20240529170510,
                                  date20240529170511,
                                  date20240529170512,
                                  date20240529170513,
                                  date20240529170514
                          }
                         ),
                arguments(date20240529170510,
                          new LocalDateTime[]{date20240529170510, null, date20240529170512, null, date20240529170514}
                         ),
                arguments(date20240529170510,
                          new LocalDateTime[]{date20240529170510, null, date20240529170512, null, null}
                         ),
                arguments(date20240529170510, new LocalDateTime[]{date20240529170510, null, null, null, null}),
                arguments(date20200529170510,
                          new LocalDateTime[]{
                                  date20240529170510,
                                  date20240529170511,
                                  date20240529170512,
                                  date20240529170513,
                                  date20240529170514,
                                  date20240529170510,
                                  date20200529170510
                          }
                         )
                        );
    }

    static Stream<Arguments> testParseDateTimeStringArguments() {
        return Stream.of(
                arguments(null, null, null),
                arguments("Sat, 08 Jun 2024 15:30:45 +0300", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
                arguments("Sat Jun 08 15:30:45 EEST 2024", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null),
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
                arguments("Sat, 08 Jun 2024 15:30:45", LocalDateTime.of(2024, 6, 8, 15, 30, 45), null)
                        );
    }

    static Stream<Arguments> testFormatDateArguments() {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 6, 8, 15, 30, 45);
        return Stream.of(
                arguments(null, DateFormat.DO_NOT_USE_DATE, ""),
                arguments(localDateTime, DateFormat.DO_NOT_USE_DATE, ""),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_TOGETHER, "20240608"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_WHITE_SPACED, "2024 06 08"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_UNDERSCORED, "2024_06_08"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_DOTTED, "2024.06.08"),
                arguments(localDateTime, DateFormat.YYYY_MM_DD_DASHED, "2024-06-08"),
                arguments(localDateTime, DateFormat.YY_MM_DD_TOGETHER, "240608"),
                arguments(localDateTime, DateFormat.YY_MM_DD_WHITE_SPACED, "24 06 08"),
                arguments(localDateTime, DateFormat.YY_MM_DD_UNDERSCORED, "24_06_08"),
                arguments(localDateTime, DateFormat.YY_MM_DD_DOTTED, "24.06.08"),
                arguments(localDateTime, DateFormat.YY_MM_DD_DASHED, "24-06-08"),
                arguments(localDateTime, DateFormat.MM_DD_YYYY_TOGETHER, "06082024"),
                arguments(localDateTime, DateFormat.MM_DD_YYYY_WHITE_SPACED, "06 08 2024"),
                arguments(localDateTime, DateFormat.MM_DD_YYYY_UNDERSCORED, "06_08_2024"),
                arguments(localDateTime, DateFormat.MM_DD_YYYY_DOTTED, "06.08.2024"),
                arguments(localDateTime, DateFormat.MM_DD_YYYY_DASHED, "06-08-2024"),
                arguments(localDateTime, DateFormat.MM_DD_YY_TOGETHER, "060824"),
                arguments(localDateTime, DateFormat.MM_DD_YY_WHITE_SPACED, "06 08 24"),
                arguments(localDateTime, DateFormat.MM_DD_YY_UNDERSCORED, "06_08_24"),
                arguments(localDateTime, DateFormat.MM_DD_YY_DOTTED, "06.08.24"),
                arguments(localDateTime, DateFormat.MM_DD_YY_DASHED, "06-08-24"),
                arguments(localDateTime, DateFormat.DD_MM_YYYY_TOGETHER, "08062024"),
                arguments(localDateTime, DateFormat.DD_MM_YYYY_WHITE_SPACED, "08 06 2024"),
                arguments(localDateTime, DateFormat.DD_MM_YYYY_UNDERSCORED, "08_06_2024"),
                arguments(localDateTime, DateFormat.DD_MM_YYYY_DOTTED, "08.06.2024"),
                arguments(localDateTime, DateFormat.DD_MM_YYYY_DASHED, "08-06-2024"),
                arguments(localDateTime, DateFormat.DD_MM_YY_TOGETHER, "080624"),
                arguments(localDateTime, DateFormat.DD_MM_YY_WHITE_SPACED, "08 06 24"),
                arguments(localDateTime, DateFormat.DD_MM_YY_UNDERSCORED, "08_06_24"),
                arguments(localDateTime, DateFormat.DD_MM_YY_DOTTED, "08.06.24"),
                arguments(localDateTime, DateFormat.DD_MM_YY_DASHED, "08-06-24")
                        );
    }

    @Test
    void testParseDateTimeStringWithNullOffset() {
        var result = operations.parseDateTimeString("2022-12-01 13:11:44 +0200");

        assertNotNull(result);
        assertEquals(LocalDateTime.of(2022, 12, 1, 13, 11, 44), result);
    }

    @ParameterizedTest
    @MethodSource("testParseDateTimeStringArguments")
    void testParseDateTimeString(String dateTimeString, LocalDateTime expected, String offset) {
        Locale.setDefault(Locale.US);
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
    @MethodSource("testFormatDateArguments")
    void testFormatDate(LocalDateTime localDateTime, DateFormat dateFormat, String expected) {
        Locale.setDefault(Locale.US);
        var result = operations.formatDate(localDateTime, dateFormat);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void formatTime() {
        // TODO:
    }

    @Test
    void formatDateTime() {
        // TODO:
    }

    @ParameterizedTest
    @MethodSource("findMinOrNullArguments")
    void testFindMinOrNull(LocalDateTime expected, LocalDateTime[] actual) {
        var result = operations.findMinOrNull(actual);
        assertEquals(expected, result);
    }

}