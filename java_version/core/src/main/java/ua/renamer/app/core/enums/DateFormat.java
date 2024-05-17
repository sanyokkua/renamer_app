package ua.renamer.app.core.enums;

import ua.renamer.app.core.abstracts.EnumWithExample;

/**
 * An enumeration representing different date formats.
 * Each enum constant provides an example string of the format.
 */
public enum DateFormat implements EnumWithExample {
    DO_NOT_USE_DATE(""),
    YYYY_MM_DD_TOGETHER("YYYYMMDD"),
    YYYY_MM_DD_WHITE_SPACED("YYYY MM DD"),
    YYYY_MM_DD_UNDERSCORED("YYYY_MM_DD"),
    YYYY_MM_DD_DOTTED("YYYY.MM.DD"),
    YYYY_MM_DD_DASHED("YYYY-MM-DD"),
    YY_MM_DD_TOGETHER("YYMMDD"),
    YY_MM_DD_WHITE_SPACED("YY MM DD"),
    YY_MM_DD_UNDERSCORED("YY_MM_DD"),
    YY_MM_DD_DOTTED("YY.MM.DD"),
    YY_MM_DD_DASHED("YY-MM-DD"),
    MM_DD_YYYY_TOGETHER("MMDDYYYY"),
    MM_DD_YYYY_WHITE_SPACED("MM DD YYYY"),
    MM_DD_YYYY_UNDERSCORED("MM_DD_YYYY"),
    MM_DD_YYYY_DOTTED("MM.DD.YYYY"),
    MM_DD_YYYY_DASHED("MM-DD-YYYY"),
    MM_DD_YY_TOGETHER("MMDDYY"),
    MM_DD_YY_WHITE_SPACED("MM DD YY"),
    MM_DD_YY_UNDERSCORED("MM_DD_YY"),
    MM_DD_YY_DOTTED("MM.DD.YY"),
    MM_DD_YY_DASHED("MM-DD-YY"),
    DD_MM_YYYY_TOGETHER("DDMMYYYY"),
    DD_MM_YYYY_WHITE_SPACED("DD MM YYYY"),
    DD_MM_YYYY_UNDERSCORED("DD_MM_YYYY"),
    DD_MM_YYYY_DOTTED("MM.DD.MM.YYYY"),
    DD_MM_YYYY_DASHED("MM-DD-MM-YYYY"),
    DD_MM_YY_TOGETHER("DDMMYY"),
    DD_MM_YY_WHITE_SPACED("DD MM YY"),
    DD_MM_YY_UNDERSCORED("DD_MM_YY"),
    DD_MM_YY_DOTTED("DD.MM.YY"),
    DD_MM_YY_DASHED("DD-MM-YY");

    private final String example;

    DateFormat(String example) {
        this.example = example;
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
}
