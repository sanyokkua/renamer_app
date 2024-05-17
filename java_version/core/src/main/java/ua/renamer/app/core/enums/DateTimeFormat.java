package ua.renamer.app.core.enums;

import ua.renamer.app.core.abstracts.EnumWithExample;

/**
 * An enumeration representing different date-time formats.
 * Each enum constant provides an example string of the format.
 */
public enum DateTimeFormat implements EnumWithExample {
    DATE_TIME_TOGETHER("DateTime"),
    DATE_TIME_WHITE_SPACED("Date Time"),
    DATE_TIME_UNDERSCORED("Date_Time"),
    DATE_TIME_DOTTED("Date.Time"),
    DATE_TIME_DASHED("Date-Time"),
    REVERSE_DATE_TIME_TOGETHER("TimeDate"),
    REVERSE_DATE_TIME_WHITE_SPACED("Time Date"),
    REVERSE_DATE_TIME_UNDERSCORED("Time_Date"),
    REVERSE_DATE_TIME_DOTTED("Time.Date"),
    REVERSE_DATE_TIME_DASHED("Time-Date"),
    NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970("181888128183818381");

    private final String example;

    DateTimeFormat(String example) {
        this.example = example;
    }

    /**
     * Gets an example string of the date-time format.
     *
     * @return the example string.
     */
    @Override
    public String getExampleString() {
        return this.example;
    }
}
