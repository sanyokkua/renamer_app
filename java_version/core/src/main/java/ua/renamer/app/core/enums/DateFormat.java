package ua.renamer.app.core.enums;

import ua.renamer.app.core.abstracts.EnumWithExample;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * An enumeration representing different date formats.
 * Each enum constant provides an example string of the format.
 */
public enum DateFormat implements EnumWithExample {
    DO_NOT_USE_DATE("", ""),
    YYYY_MM_DD_TOGETHER("YYYYMMDD", "yyyyMMdd"),
    YYYY_MM_DD_WHITE_SPACED("YYYY MM DD", "yyyy MM dd"),
    YYYY_MM_DD_UNDERSCORED("YYYY_MM_DD", "yyyy_MM_dd"),
    YYYY_MM_DD_DOTTED("YYYY.MM.DD", "yyyy.MM.dd"),
    YYYY_MM_DD_DASHED("YYYY-MM-DD", "yyyy-MM-dd"),
    YY_MM_DD_TOGETHER("YYMMDD", "yyMMdd"),
    YY_MM_DD_WHITE_SPACED("YY MM DD", "yy MM dd"),
    YY_MM_DD_UNDERSCORED("YY_MM_DD", "yy_MM_dd"),
    YY_MM_DD_DOTTED("YY.MM.DD", "yy.MM.dd"),
    YY_MM_DD_DASHED("YY-MM-DD", "yy-MM-dd"),
    MM_DD_YYYY_TOGETHER("MMDDYYYY", "MMddyyyy"),
    MM_DD_YYYY_WHITE_SPACED("MM DD YYYY", "MM dd yyyy"),
    MM_DD_YYYY_UNDERSCORED("MM_DD_YYYY", "MM_dd_yyyy"),
    MM_DD_YYYY_DOTTED("MM.DD.YYYY", "MM.dd.yyyy"),
    MM_DD_YYYY_DASHED("MM-DD-YYYY", "MM-dd-yyyy"),
    MM_DD_YY_TOGETHER("MMDDYY", "MMddyy"),
    MM_DD_YY_WHITE_SPACED("MM DD YY", "MM dd yy"),
    MM_DD_YY_UNDERSCORED("MM_DD_YY", "MM_dd_yy"),
    MM_DD_YY_DOTTED("MM.DD.YY", "MM.dd.yy"),
    MM_DD_YY_DASHED("MM-DD-YY", "MM-dd-yy"),
    DD_MM_YYYY_TOGETHER("DDMMYYYY", "ddMMyyyy"),
    DD_MM_YYYY_WHITE_SPACED("DD MM YYYY", "dd MM yyyy"),
    DD_MM_YYYY_UNDERSCORED("DD_MM_YYYY", "dd_MM_yyyy"),
    DD_MM_YYYY_DOTTED("MM.DD.MM.YYYY", "MM.dd.MM.yyyy"),
    DD_MM_YYYY_DASHED("MM-DD-MM-YYYY", "MM-dd-MM-yyyy"),
    DD_MM_YY_TOGETHER("DDMMYY", "ddMMyy"),
    DD_MM_YY_WHITE_SPACED("DD MM YY", "dd MM yy"),
    DD_MM_YY_UNDERSCORED("DD_MM_YY", "dd_MM_yy"),
    DD_MM_YY_DOTTED("DD.MM.YY", "dd.MM.yy"),
    DD_MM_YY_DASHED("DD-MM-YY", "dd-MM-yy");

    private final String example;
    private final DateTimeFormatter formatter;

    DateFormat(String example, String pattern) {
        this.example = example;
        this.formatter = pattern.isEmpty() ? null : DateTimeFormatter.ofPattern(pattern);
    }

    /**
     * Gets an example string of the date format.
     *
     * @return the example string.
     */
    @Override
    public String getExampleString() {
        return this.example;
    }

    /**
     * Gets the DateTimeFormatter for the date format.
     *
     * @return the DateTimeFormatter.
     */
    public Optional<DateTimeFormatter> getFormatter() {
        return Optional.ofNullable(this.formatter);
    }
}
