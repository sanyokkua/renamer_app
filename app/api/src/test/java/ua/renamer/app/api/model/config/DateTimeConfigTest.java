package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.DateFormat;
import ua.renamer.app.api.enums.DateTimeSource;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;
import ua.renamer.app.api.enums.TimeFormat;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .build()
        );
    }

    @Test
    void givenNullSource_whenBuild_thenNullPointerException() {
        var builder = DateTimeConfig.builder()
            .withSource(null)
            .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
            .withTimeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
            .withPosition(ItemPositionWithReplacement.BEGIN);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullDateFormat_whenBuild_thenNullPointerException() {
        var builder = DateTimeConfig.builder()
            .withSource(DateTimeSource.FILE_MODIFICATION_DATE)
            .withDateFormat(null)
            .withTimeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
            .withPosition(ItemPositionWithReplacement.END);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullTimeFormat_whenBuild_thenNullPointerException() {
        var builder = DateTimeConfig.builder()
            .withSource(DateTimeSource.CURRENT_DATE)
            .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
            .withTimeFormat(null)
            .withPosition(ItemPositionWithReplacement.REPLACE);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullPosition_whenBuild_thenNullPointerException() {
        var builder = DateTimeConfig.builder()
            .withSource(DateTimeSource.CONTENT_CREATION_DATE)
            .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
            .withTimeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
            .withPosition(null);

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenSourceCustomDateAndNoCustomDateTime_whenBuild_thenIllegalArgumentException() {
        var builder = DateTimeConfig.builder()
            .withSource(DateTimeSource.CUSTOM_DATE)
            .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
            .withTimeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
            .withPosition(ItemPositionWithReplacement.BEGIN);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void givenSourceCustomDateWithCustomDateTime_whenBuild_thenSucceeds() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 6, 15, 10, 30, 0);

        assertDoesNotThrow(() ->
            DateTimeConfig.builder()
                .withSource(DateTimeSource.CUSTOM_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
                .withPosition(ItemPositionWithReplacement.END)
                .withCustomDateTime(fixedDateTime)
                .build()
        );
    }

    @Test
    void givenNonCustomSourceWithoutCustomDateTime_whenBuild_thenSucceeds() {
        assertDoesNotThrow(() ->
            DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_MODIFICATION_DATE)
                .withDateFormat(DateFormat.DD_MM_YYYY_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_24_DASHED)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .build()
        );
    }

    @Test
    void givenNullDateTimeFormatAndSeparator_whenBuild_thenSucceeds() {
        assertDoesNotThrow(() ->
            DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withDateTimeFormat(null)
                .withSeparator(null)
                .build()
        );
    }
}
