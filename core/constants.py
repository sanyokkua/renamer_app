from enum import Enum


class RenamingModes(Enum):
    RENAME_BY_PATTERN = "Rename By Pattern"
    RENAME_TO_DATE_TIME = "Rename To Date/Time"
    REPLACE_PREFIX_SUFFIX = "Replace Prefix and/or Suffix"


class DateTimeSource(Enum):
    FILE_CREATION_TIME = "File Creation Time"
    FILE_MODIFICATION_TIME = "File Modification Time"
    FILE_EXIF_CREATION_TIME = "File EXIF Creation Time"


class DateTimeFormats(Enum):
    YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_MILLIS = "2024_05_21_22_02_59_100"
    YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_MILLIS_AM_PM = "2024_05_21_10PM_02_59_100"


class DateTimePlacing(Enum):
    REPLACE = 0,
    ADD_TO_BEGIN = 1,
    ADD_TO_END = 2
