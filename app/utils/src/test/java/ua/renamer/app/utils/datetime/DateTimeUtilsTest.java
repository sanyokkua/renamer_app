package ua.renamer.app.utils.datetime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DateTimeUtilsTest {

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

    // ============================================================================
    // FileTime Conversion Tests
    // ============================================================================

    @Test
    void testToLocalDateTime_WithValidFileTime() {
        Instant instant = Instant.parse("2024-06-08T15:30:45Z");
        FileTime fileTime = FileTime.from(instant);

        LocalDateTime result = DateTimeUtils.toLocalDateTime(fileTime);

        assertNotNull(result);
        assertEquals(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), result);
    }

    @Test
    void testToLocalDateTime_WithNullFileTime() {
        LocalDateTime result = DateTimeUtils.toLocalDateTime(null);
        assertNull(result);
    }

    @Test
    void testToLocalDateTime_WithEpochZero() {
        FileTime fileTime = FileTime.from(Instant.EPOCH);

        LocalDateTime result = DateTimeUtils.toLocalDateTime(fileTime);

        assertNotNull(result);
        assertEquals(LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault()), result);
    }

    // ============================================================================
    // Minimal DateTime Tests
    // ============================================================================

    @Test
    void testGetMinimalDateTime() {
        LocalDateTime minimal = DateTimeUtils.getMinimalDateTime();

        assertNotNull(minimal);
        assertEquals(LocalDateTime.of(1900, 1, 1, 0, 0), minimal);
    }

    // ============================================================================
    // DateTime String Parsing Tests
    // ============================================================================

    @Test
    void testParseDateTimeString_WithNull() {
        LocalDateTime result = DateTimeUtils.parseDateTimeString(null);
        assertNull(result);
    }

    @Test
    void testParseDateTimeString_WithEmptyString() {
        LocalDateTime result = DateTimeUtils.parseDateTimeString("");
        assertNull(result);
    }

    @Test
    void testParseDateTimeString_WithBlankString() {
        LocalDateTime result = DateTimeUtils.parseDateTimeString("   ");
        assertNull(result);
    }

    @Test
    void testParseDateTimeString_WithWhitespace() {
        LocalDateTime result = DateTimeUtils.parseDateTimeString("  2024-06-08  ");

        assertNotNull(result);
        assertEquals(LocalDateTime.of(2024, 6, 8, 0, 0), result);
    }

    // ============================================================================
    // Zone Info Parsing Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideZoneInfoFormats")
    void testParseDateTimeStringWithZoneInfo(String input, LocalDateTime expected) {
        Optional<LocalDateTime> result = DateTimeUtils.parseDateTimeStringWithZoneInfo(input);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void testParseDateTimeStringWithZoneInfo_InvalidZone() {
        Optional<LocalDateTime> result = DateTimeUtils.parseDateTimeStringWithZoneInfo("invalid date string");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseDateTimeStringWithZoneInfo_DifferentLocales() {
        Locale.setDefault(Locale.GERMAN);
        String input = "2024-06-08T15:30:45+02:00";
        Optional<LocalDateTime> result = DateTimeUtils.parseDateTimeStringWithZoneInfo(input);

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2024, 6, 8, 15, 30, 45), result.get());

        Locale.setDefault(Locale.US);
    }

    // ============================================================================
    // Zone Offset Parsing Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideOffsetFormats")
    void testParseDateTimeStringWithZoneOffset(String input, String offset, LocalDateTime expected) {
        Optional<LocalDateTime> result = DateTimeUtils.parseDateTimeStringWithZoneOffset(input, offset);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void testParseDateTimeStringWithZoneOffset_InvalidOffset() {
        Optional<LocalDateTime> result = DateTimeUtils.parseDateTimeStringWithZoneOffset("2024-06-08 15:30:45", "invalid");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseDateTimeStringWithZoneOffset_NullDefaultsToUTC() {
        Optional<LocalDateTime> result = DateTimeUtils.parseDateTimeStringWithZoneOffset("2024-06-08 15:30:45", null);

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.of(2024, 6, 8, 15, 30, 45), result.get());
    }

    // ============================================================================
    // Date-Only Parsing Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideDateOnlyFormats")
    void testParseDateOnly(String input, LocalDateTime expected) {
        Optional<LocalDateTime> result = DateTimeUtils.parseDateOnly(input);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void testParseDateOnly_InvalidFormat() {
        Optional<LocalDateTime> result = DateTimeUtils.parseDateOnly("2024/06/08");
        assertTrue(result.isEmpty());
    }

    // ============================================================================
    // Year and Month Parsing Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideYearMonthFormats")
    void testParseYearAndMonth(String input, LocalDateTime expected) {
        Optional<LocalDateTime> result = DateTimeUtils.parseYearAndMonth(input);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void testParseYearAndMonth_InvalidYear() {
        Optional<LocalDateTime> result = DateTimeUtils.parseYearAndMonth("abcd");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseYearAndMonth_TooShort() {
        Optional<LocalDateTime> result = DateTimeUtils.parseYearAndMonth("123");
        assertTrue(result.isEmpty());
    }

    // ============================================================================
    // DateTime Formatting Tests
    // ============================================================================

    @Test
    void testFormatLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 6, 8, 15, 30, 45);

        String result = DateTimeUtils.formatLocalDateTime(dateTime);

        assertNotNull(result);
        assertEquals("2024-06-08 15:30:45", result);
    }

    // ============================================================================
    // Find Minimum DateTime Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("provideFindMinArgs")
    void testFindMinOrNull(LocalDateTime expected, LocalDateTime[] values) {
        LocalDateTime result = DateTimeUtils.findMinOrNull(values);
        assertEquals(expected, result);
    }

}
