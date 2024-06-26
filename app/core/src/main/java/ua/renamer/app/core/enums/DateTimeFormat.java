package ua.renamer.app.core.enums;

import java.util.Objects;
import java.util.Optional;

/**
 * An enumeration representing different date-time formats.
 * Each enum constant provides an example string of the format.
 */
public enum DateTimeFormat implements EnumWithExample {
    DATE_TIME_TOGETHER("DateTime", "%1$s%2$s"),
    DATE_TIME_WHITE_SPACED("Date Time", "%1$s %2$s"),
    DATE_TIME_UNDERSCORED("Date_Time", "%1$s_%2$s"),
    DATE_TIME_DOTTED("Date.Time", "%1$s.%2$s"),
    DATE_TIME_DASHED("Date-Time", "%1$s-%2$s"),
    REVERSE_DATE_TIME_TOGETHER("TimeDate", "%2$s%1$s"),
    REVERSE_DATE_TIME_WHITE_SPACED("Time Date", "%2$s %1$s"),
    REVERSE_DATE_TIME_UNDERSCORED("Time_Date", "%2$s_%1$s"),
    REVERSE_DATE_TIME_DOTTED("Time.Date", "%2$s.%1$s"),
    REVERSE_DATE_TIME_DASHED("Time-Date", "%2$s-%1$s"),
    NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970("181888128183818381", "");

    private final String example;
    private final String formatString;

    DateTimeFormat(String example, String formatString) {
        this.example = example;
        this.formatString = Objects.isNull(formatString) || formatString.isBlank() ? null : formatString;
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

    /**
     * Gets a format String of the date-time format.
     *
     * @return the formatString.
     */
    public Optional<String> getFormatString() {
        return Optional.ofNullable(this.formatString);
    }
}
