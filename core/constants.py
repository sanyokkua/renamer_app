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
    ISO_8601_FORMAT = "YYYY-MM-DD HH:MM:SS"
    US_FORMAT = "MM/DD/YYYY HH:MM:SS AM/PM"
    EUROPEAN_FORMAT = "DD/MM/YYYY HH:MM:SS"
    NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970 = "Number of seconds since January 1, 1970"


class DateTimePlacing(Enum):
    REPLACE = 0,
    ADD_TO_BEGIN = 1,
    ADD_TO_END = 2
