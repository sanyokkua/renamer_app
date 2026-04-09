package ua.renamer.app.metadata.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.api.enums.DateFormat;
import ua.renamer.app.api.enums.DateTimeFormat;
import ua.renamer.app.api.enums.TimeFormat;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DateTimeConverterTest {

    private DateTimeConverter converter;

    static Stream<Arguments> provideZoneInfoFormats() {
        LocalDateTime expected = LocalDateTime.of(2024, 6, 8, 15, 30, 45);
        String monthName = expected.getMonth().getDisplayName(TextStyle.SHORT, Locale.US);
        String dayName = expected.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);

        return Stream.of(
                arguments(String.format("%s, 08 %s 2024 15:30:45 +0000", dayName, monthName), expected),
                arguments(String.format("%s %s 08 15:30:45 UTC 2024", dayName, monthName), expected),
                arguments("2024-06-08 15:30:45 +0000", expected),
                arguments("2024-06-08T15:30:45+0000", expected),
                arguments("2024-06-08T15:30:45+00:00", expected)
        );
    }

    // ============================================================================
    // A. System Zone ID Tests
    // ============================================================================

    static Stream<Arguments> provideOffsetFormats() {
        return Stream.of(
                arguments("2024:06:08 15:30:45", null, LocalDateTime.of(2024, 6, 8, 15, 30, 45)),
                arguments("2024:06:08 15:30", null, LocalDateTime.of(2024, 6, 8, 15, 30, 0)),
                arguments("2024-06-08 15:30:45", null, LocalDateTime.of(2024, 6, 8, 15, 30, 45)),
                arguments("2024-06-08 15:30", null, LocalDateTime.of(2024, 6, 8, 15, 30, 0)),
                arguments("2024.06.08 15:30:45", null, LocalDateTime.of(2024, 6, 8, 15, 30, 45)),
                arguments("2024.06.08 15:30", null, LocalDateTime.of(2024, 6, 8, 15, 30, 0)),
                arguments("2024-06-08T15:30:45", null, LocalDateTime.of(2024, 6, 8, 15, 30, 45)),
                arguments("2024-06-08T15:30", null, LocalDateTime.of(2024, 6, 8, 15, 30, 0)),
                arguments("2024-06-08 15:30:45", "+02:00", LocalDateTime.of(2024, 6, 8, 15, 30, 45)),
                arguments("2024-06-08 15:30:45", "-05:00", LocalDateTime.of(2024, 6, 8, 15, 30, 45))
        );
    }

    // ============================================================================
    // B. FileTime Conversion Tests
    // ============================================================================

    static Stream<Arguments> provideDateOnlyFormats() {
        return Stream.of(
                arguments("2024-06-08", LocalDateTime.of(2024, 6, 8, 0, 0)),
                arguments("20240608", LocalDateTime.of(2024, 6, 8, 0, 0))
        );
    }

    static Stream<Arguments> provideYearMonthFormats() {
        return Stream.of(
                arguments("2024-06", LocalDateTime.of(2024, 6, 1, 0, 0)),
                arguments("2024", LocalDateTime.of(2024, 1, 1, 0, 0))
        );
    }

    static Stream<Arguments> provideDateFormats() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 8, 15, 30, 45);
        return Stream.of(
                // Null handling
                arguments(null, DateFormat.DO_NOT_USE_DATE, ""),
                arguments(null, null, ""),
                arguments(null, DateFormat.YYYY_MM_DD_DASHED, ""),
                arguments(dt, DateFormat.DO_NOT_USE_DATE, ""),

                // YYYY formats
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, "20240608"),
                arguments(dt, DateFormat.YYYY_MM_DD_WHITE_SPACED, "2024 06 08"),
                arguments(dt, DateFormat.YYYY_MM_DD_UNDERSCORED, "2024_06_08"),
                arguments(dt, DateFormat.YYYY_MM_DD_DOTTED, "2024.06.08"),
                arguments(dt, DateFormat.YYYY_MM_DD_DASHED, "2024-06-08"),

                // YY formats
                arguments(dt, DateFormat.YY_MM_DD_TOGETHER, "240608"),
                arguments(dt, DateFormat.YY_MM_DD_WHITE_SPACED, "24 06 08"),
                arguments(dt, DateFormat.YY_MM_DD_UNDERSCORED, "24_06_08"),
                arguments(dt, DateFormat.YY_MM_DD_DOTTED, "24.06.08"),
                arguments(dt, DateFormat.YY_MM_DD_DASHED, "24-06-08"),

                // MM DD YYYY formats
                arguments(dt, DateFormat.MM_DD_YYYY_TOGETHER, "06082024"),
                arguments(dt, DateFormat.MM_DD_YYYY_WHITE_SPACED, "06 08 2024"),
                arguments(dt, DateFormat.MM_DD_YYYY_UNDERSCORED, "06_08_2024"),
                arguments(dt, DateFormat.MM_DD_YYYY_DOTTED, "06.08.2024"),
                arguments(dt, DateFormat.MM_DD_YYYY_DASHED, "06-08-2024"),

                // MM DD YY formats
                arguments(dt, DateFormat.MM_DD_YY_TOGETHER, "060824"),
                arguments(dt, DateFormat.MM_DD_YY_WHITE_SPACED, "06 08 24"),
                arguments(dt, DateFormat.MM_DD_YY_UNDERSCORED, "06_08_24"),
                arguments(dt, DateFormat.MM_DD_YY_DOTTED, "06.08.24"),
                arguments(dt, DateFormat.MM_DD_YY_DASHED, "06-08-24"),

                // DD MM YYYY formats
                arguments(dt, DateFormat.DD_MM_YYYY_TOGETHER, "08062024"),
                arguments(dt, DateFormat.DD_MM_YYYY_WHITE_SPACED, "08 06 2024"),
                arguments(dt, DateFormat.DD_MM_YYYY_UNDERSCORED, "08_06_2024"),
                arguments(dt, DateFormat.DD_MM_YYYY_DOTTED, "08.06.2024"),
                arguments(dt, DateFormat.DD_MM_YYYY_DASHED, "08-06-2024"),

                // DD MM YY formats
                arguments(dt, DateFormat.DD_MM_YY_TOGETHER, "080624"),
                arguments(dt, DateFormat.DD_MM_YY_WHITE_SPACED, "08 06 24"),
                arguments(dt, DateFormat.DD_MM_YY_UNDERSCORED, "08_06_24"),
                arguments(dt, DateFormat.DD_MM_YY_DOTTED, "08.06.24"),
                arguments(dt, DateFormat.DD_MM_YY_DASHED, "08-06-24")
        );
    }

    // ============================================================================
    // C. Minimal DateTime Tests
    // ============================================================================

    static Stream<Arguments> provideTimeFormats() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 8, 15, 30, 45);
        LocalDateTime dtAM = LocalDateTime.of(2024, 6, 8, 2, 30, 45);

        // Get locale-specific AM/PM symbols
        String pm = "PM";
        String am = "AM";

        return Stream.of(
                // Null handling
                arguments(null, TimeFormat.DO_NOT_USE_TIME, ""),
                arguments(null, null, ""),
                arguments(null, TimeFormat.HH_MM_24_DASHED, ""),
                arguments(dt, TimeFormat.DO_NOT_USE_TIME, ""),

                // 24-hour with seconds
                arguments(dt, TimeFormat.HH_MM_SS_24_TOGETHER, "153045"),
                arguments(dt, TimeFormat.HH_MM_SS_24_WHITE_SPACED, "15 30 45"),
                arguments(dt, TimeFormat.HH_MM_SS_24_UNDERSCORED, "15_30_45"),
                arguments(dt, TimeFormat.HH_MM_SS_24_DOTTED, "15.30.45"),
                arguments(dt, TimeFormat.HH_MM_SS_24_DASHED, "15-30-45"),

                // 24-hour without seconds
                arguments(dt, TimeFormat.HH_MM_24_TOGETHER, "1530"),
                arguments(dt, TimeFormat.HH_MM_24_WHITE_SPACED, "15 30"),
                arguments(dt, TimeFormat.HH_MM_24_UNDERSCORED, "15_30"),
                arguments(dt, TimeFormat.HH_MM_24_DOTTED, "15.30"),
                arguments(dt, TimeFormat.HH_MM_24_DASHED, "15-30"),

                // AM/PM format with seconds
                arguments(dt, TimeFormat.HH_MM_SS_AM_PM_TOGETHER, "033045" + pm),
                arguments(dt, TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED, "03 30 45 " + pm),
                arguments(dt, TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED, "03_30_45_" + pm),
                arguments(dt, TimeFormat.HH_MM_SS_AM_PM_DOTTED, "03.30.45." + pm),
                arguments(dt, TimeFormat.HH_MM_SS_AM_PM_DASHED, "03-30-45-" + pm),

                // AM/PM format without seconds
                arguments(dt, TimeFormat.HH_MM_AM_PM_TOGETHER, "0330" + pm),
                arguments(dt, TimeFormat.HH_MM_AM_PM_WHITE_SPACED, "03 30 " + pm),
                arguments(dt, TimeFormat.HH_MM_AM_PM_UNDERSCORED, "03_30_" + pm),
                arguments(dt, TimeFormat.HH_MM_AM_PM_DOTTED, "03.30." + pm),
                arguments(dt, TimeFormat.HH_MM_AM_PM_DASHED, "03-30-" + pm),

                // AM format
                arguments(dtAM, TimeFormat.HH_MM_AM_PM_DASHED, "02-30-" + am)
        );
    }

    // ============================================================================
    // D. DateTime String Parsing (Main Method) Tests
    // ============================================================================

    static Stream<Arguments> provideDateTimeFormats() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 8, 15, 30, 45);

        return Stream.of(
                arguments(null, DateFormat.DO_NOT_USE_DATE, TimeFormat.DO_NOT_USE_TIME,
                        DateTimeFormat.DATE_TIME_TOGETHER, ""),
                arguments(dt, DateFormat.DO_NOT_USE_DATE, TimeFormat.HH_MM_SS_24_TOGETHER,
                        DateTimeFormat.DATE_TIME_TOGETHER, "153045"),
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.DO_NOT_USE_TIME,
                        DateTimeFormat.DATE_TIME_TOGETHER, "20240608"),
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
                        DateTimeFormat.DATE_TIME_TOGETHER, "20240608153045"),
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
                        DateTimeFormat.DATE_TIME_WHITE_SPACED, "20240608 153045"),
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
                        DateTimeFormat.DATE_TIME_UNDERSCORED, "20240608_153045"),
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
                        DateTimeFormat.DATE_TIME_DOTTED, "20240608.153045"),
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
                        DateTimeFormat.DATE_TIME_DASHED, "20240608-153045"),
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
                        DateTimeFormat.REVERSE_DATE_TIME_TOGETHER, "15304520240608"),
                arguments(dt, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
                        DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970,
                        String.valueOf(dt.toInstant(ZoneOffset.UTC).toEpochMilli()))
        );
    }

    static Stream<Arguments> provideFindMinArgs() {
        LocalDateTime dt1 = LocalDateTime.of(2024, 5, 29, 17, 5, 10);
        LocalDateTime dt2 = LocalDateTime.of(2024, 5, 29, 17, 5, 11);
        LocalDateTime dt3 = LocalDateTime.of(2024, 5, 29, 17, 5, 9);
        LocalDateTime dt4 = LocalDateTime.of(2020, 5, 29, 17, 5, 10);

        return Stream.of(
                arguments(null, null),
                arguments(null, new LocalDateTime[0]),
                arguments(dt1, new LocalDateTime[]{dt1}),
                arguments(dt1, new LocalDateTime[]{dt1, dt2}),
                arguments(dt3, new LocalDateTime[]{dt1, dt3, dt2}),
                arguments(dt1, new LocalDateTime[]{dt1, null, dt2, null}),
                arguments(dt1, new LocalDateTime[]{dt1, null, null, null, null}),
                arguments(null, new LocalDateTime[]{null, null, null}),
                arguments(dt4, new LocalDateTime[]{dt1, dt2, dt4})
        );
    }

    @BeforeEach
    void setUp() {
        converter = new DateTimeConverter();
    }

    @Test
    void testGetSystemZoneId() {
        ZoneId zoneId = converter.getSystemZoneId();
        assertNotNull(zoneId);
        assertEquals(ZoneId.systemDefault(), zoneId);
    }

    // ============================================================================
    // E. DateTime Parsing with Zone Info Tests
    // ============================================================================

    @Test
    void testToLocalDateTime_WithValidFileTime() {
        Instant instant = Instant.parse("2024-06-08T15:30:45Z");
        FileTime fileTime = FileTime.from(instant);

        LocalDateTime result = converter.toLocalDateTime(fileTime);

        assertNotNull(result);
        assertEquals(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), result);
    }

    @Test
    void testToLocalDateTime_WithNullFileTime() {
        LocalDateTime result = converter.toLocalDateTime(null);
        assertNull(result);
    }

    @Test
    void testToLocalDateTime_WithEpochZero() {
        FileTime fileTime = FileTime.from(Instant.EPOCH);

        LocalDateTime result = converter.toLocalDateTime(fileTime);

        assertNotNull(result);
        assertEquals(LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()), result);
    }

    @Test
    void testGetMinimalDateTime() {
        LocalDateTime minimal = converter.getMinimalDateTime();

        assertNotNull(minimal);
        assertEquals(LocalDateTime.of(1900, 1, 1, 0, 0), minimal);
    }

    // ============================================================================
    // F. DateTime Parsing with Zone Offset Tests
    // ============================================================================

    @Test
    void testParseDateTimeString_WithNull() {
        LocalDateTime result = converter.parseDateTimeString(null);
        assertNull(result);
    }

    @Test
    void testParseDateTimeString_WithEmptyString() {
        LocalDateTime result = converter.parseDateTimeString("");
        assertNull(result);
    }

    @Test
    void testParseDateTimeString_WithBlankString() {
        LocalDateTime result = converter.parseDateTimeString("   ");
        assertNull(result);
    }

    @Test
    void testParseDateTimeString_WithWhitespace() {
        LocalDateTime result = converter.parseDateTimeString("  2024-06-08  ");

        assertNotNull(result);
        assertEquals(LocalDateTime.of(2024, 6, 8, 0, 0), result);
    }

    // ============================================================================
    // G. Date-Only Parsing Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideZoneInfoFormats")
    void testParseDateTimeStringWithZoneInfo(String input, LocalDateTime expected) {
        Optional<LocalDateTime> result = converter.parseDateTimeStringWithZoneInfo(input);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void testParseDateTimeStringWithZoneInfo_InvalidZone() {
        Optional<LocalDateTime> result = converter.parseDateTimeStringWithZoneInfo("invalid date string");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseDateTimeStringWithZoneInfo_DifferentLocales() {
        // Test with German locale
        Locale.setDefault(Locale.GERMAN);
        String input = "2024-06-08T15:30:45+02:00";
        Optional<LocalDateTime> result = converter.parseDateTimeStringWithZoneInfo(input);

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2024, 6, 8, 15, 30, 45), result.get());

        // Reset to US
        Locale.setDefault(Locale.US);
    }

    // ============================================================================
    // H. Year and Month Parsing Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideOffsetFormats")
    void testParseDateTimeStringWithZoneOffset(String input, String offset, LocalDateTime expected) {
        Optional<LocalDateTime> result = converter.parseDateTimeStringWithZoneOffset(input, offset);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void testParseDateTimeStringWithZoneOffset_InvalidOffset() {
        Optional<LocalDateTime> result = converter.parseDateTimeStringWithZoneOffset("2024-06-08 15:30:45", "invalid");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseDateTimeStringWithZoneOffset_NullDefaultsToUTC() {
        Optional<LocalDateTime> result = converter.parseDateTimeStringWithZoneOffset("2024-06-08 15:30:45", null);

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2024, 6, 8, 15, 30, 45), result.get());
    }

    @ParameterizedTest
    @MethodSource("provideDateOnlyFormats")
    void testParseDateOnly(String input, LocalDateTime expected) {
        Optional<LocalDateTime> result = converter.parseDateOnly(input);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    // ============================================================================
    // I. DateTime Formatting Tests
    // ============================================================================

    @Test
    void testParseDateOnly_InvalidFormat() {
        Optional<LocalDateTime> result = converter.parseDateOnly("2024/06/08");
        assertTrue(result.isEmpty());
    }

    // ============================================================================
    // J. Date Formatting Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideYearMonthFormats")
    void testParseYearAndMonth(String input, LocalDateTime expected) {
        Optional<LocalDateTime> result = converter.parseYearAndMonth(input);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void testParseYearAndMonth_InvalidYear() {
        Optional<LocalDateTime> result = converter.parseYearAndMonth("abcd");
        assertTrue(result.isEmpty());
    }

    // ============================================================================
    // K. Time Formatting Tests
    // ============================================================================

    @Test
    void testParseYearAndMonth_TooShort() {
        Optional<LocalDateTime> result = converter.parseYearAndMonth("123");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFormatLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 6, 8, 15, 30, 45);

        String result = converter.formatLocalDateTime(dateTime);

        assertNotNull(result);
        assertEquals("2024-06-08 15:30:45", result);
    }

    // ============================================================================
    // L. DateTime Combined Formatting Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideDateFormats")
    void testFormatDate(LocalDateTime dateTime, DateFormat format, String expected) {
        String result = converter.formatDate(dateTime, format);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("provideTimeFormats")
    void testFormatTime(LocalDateTime dateTime, TimeFormat format, String expected) {
        Locale.setDefault(Locale.US); // Ensure consistent AM/PM symbols

        String result = converter.formatTime(dateTime, format);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    // ============================================================================
    // M. Find Minimum DateTime Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideDateTimeFormats")
    void testFormatDateTime(LocalDateTime dt, DateFormat dateFormat, TimeFormat timeFormat,
                            DateTimeFormat dateTimeFormat, String expected) {
        String result = converter.formatDateTime(dt, dateFormat, timeFormat, dateTimeFormat);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("provideFindMinArgs")
    void testFindMinOrNull(LocalDateTime expected, LocalDateTime[] values) {
        LocalDateTime result = converter.findMinOrNull(values);
        assertEquals(expected, result);
    }
}
