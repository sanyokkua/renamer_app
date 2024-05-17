package ua.renamer.app.core.enums;

import ua.renamer.app.core.abstracts.EnumWithExample;

/**
 * An enumeration representing different time formats.
 * Each enum constant provides an example string of the format.
 */
public enum TimeFormat implements EnumWithExample {
    DO_NOT_USE_TIME(""),
    HH_MM_SS_24_TOGETHER("HHMMSS  (24 hours)"),
    HH_MM_SS_24_WHITE_SPACED("HH MM SS (24 hours)"),
    HH_MM_SS_24_UNDERSCORED("HH_MM_SS (24 hours)"),
    HH_MM_SS_24_DOTTED("HH.MM.SS (24 hours)"),
    HH_MM_SS_24_DASHED("HH-MM-SS (24 hours)"),
    HH_MM_24_TOGETHER("HHMM (24 hours)"),
    HH_MM_24_WHITE_SPACED("HH MM (24 hours)"),
    HH_MM_24_UNDERSCORED("HH_MM (24 hours)"),
    HH_MM_24_DOTTED("HH.MM (24 hours)"),
    HH_MM_24_DASHED("HH-MM (24 hours)"),
    HH_MM_SS_AM_PM_TOGETHER("HHMMSS  (AM/PM, 12 hours)"),
    HH_MM_SS_AM_PM_WHITE_SPACED("HH MM SS (AM/PM, 12 hours)"),
    HH_MM_SS_AM_PM_UNDERSCORED("HH_MM_SS (AM/PM, 12 hours)"),
    HH_MM_SS_AM_PM_DOTTED("HH.MM.SS (AM/PM, 12 hours)"),
    HH_MM_SS_AM_PM_DASHED("HH-MM-SS (AM/PM, 12 hours)"),
    HH_MM_AM_PM_TOGETHER("HHMM (AM/PM, 12 hours)"),
    HH_MM_AM_PM_WHITE_SPACED("HH MM (AM/PM, 12 hours)"),
    HH_MM_AM_PM_UNDERSCORED("HH_MM (AM/PM, 12 hours)"),
    HH_MM_AM_PM_DOTTED("HH.MM (AM/PM, 12 hours)"),
    HH_MM_AM_PM_DASHED("HH-MM (AM/PM, 12 hours)");

    private final String example;

    TimeFormat(String example) {
        this.example = example;
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
}
