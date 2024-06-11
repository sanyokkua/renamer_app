package ua.renamer.app.core.enums;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * An enumeration representing different time formats.
 * Each enum constant provides an example string of the format.
 */
public enum TimeFormat implements EnumWithExample {
    DO_NOT_USE_TIME("", ""),
    HH_MM_SS_24_TOGETHER("HHMMSS (24 hours)", "HHmmss"),
    HH_MM_SS_24_WHITE_SPACED("HH MM SS (24 hours)", "HH mm ss"),
    HH_MM_SS_24_UNDERSCORED("HH_MM_SS (24 hours)", "HH_mm_ss"),
    HH_MM_SS_24_DOTTED("HH.MM.SS (24 hours)", "HH.mm.ss"),
    HH_MM_SS_24_DASHED("HH-MM-SS (24 hours)", "HH-mm-ss"),
    HH_MM_24_TOGETHER("HHMM (24 hours)", "HHmm"),
    HH_MM_24_WHITE_SPACED("HH MM (24 hours)", "HH mm"),
    HH_MM_24_UNDERSCORED("HH_MM (24 hours)", "HH_mm"),
    HH_MM_24_DOTTED("HH.MM (24 hours)", "HH.mm"),
    HH_MM_24_DASHED("HH-MM (24 hours)", "HH-mm"),
    HH_MM_SS_AM_PM_TOGETHER("HHMMSS (AM/PM, 12 hours)", "hhmmssa"),
    HH_MM_SS_AM_PM_WHITE_SPACED("HH MM SS (AM/PM, 12 hours)", "hh mm ss a"),
    HH_MM_SS_AM_PM_UNDERSCORED("HH_MM_SS (AM/PM, 12 hours)", "hh_mm_ss_a"),
    HH_MM_SS_AM_PM_DOTTED("HH.MM.SS (AM/PM, 12 hours)", "hh.mm.ss.a"),
    HH_MM_SS_AM_PM_DASHED("HH-MM-SS (AM/PM, 12 hours)", "hh-mm-ss-a"),
    HH_MM_AM_PM_TOGETHER("HHMM (AM/PM, 12 hours)", "hhmma"),
    HH_MM_AM_PM_WHITE_SPACED("HH MM (AM/PM, 12 hours)", "hh mm a"),
    HH_MM_AM_PM_UNDERSCORED("HH_MM (AM/PM, 12 hours)", "hh_mm_a"),
    HH_MM_AM_PM_DOTTED("HH.MM (AM/PM, 12 hours)", "hh.mm.a"),
    HH_MM_AM_PM_DASHED("HH-MM (AM/PM, 12 hours)", "hh-mm-a");

    private final String example;
    private final DateTimeFormatter formatter;

    TimeFormat(String example, String pattern) {
        this.example = example;
        this.formatter = pattern.isEmpty() ? null : DateTimeFormatter.ofPattern(pattern, Locale.getDefault());
    }

    /**
     * Gets an example string of the time format.
     *
     * @return the example string.
     */
    @Override
    public String getExampleString() {
        return this.example;
    }

    /**
     * Gets the DateTimeFormatter for the time format.
     *
     * @return the DateTimeFormatter.
     */
    public Optional<DateTimeFormatter> getFormatter() {
        return Optional.ofNullable(this.formatter);
    }
}
