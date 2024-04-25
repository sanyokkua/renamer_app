from enum import IntEnum


class AppModes(IntEnum):
    """
    An enumeration of application modes.

    Attributes:
        ADD_CUSTOM_TEXT (int): Mode to add custom text.
        CHANGE_CASE (int): Mode to change text case.
        USE_DATETIME (int): Mode to use datetime.
        USE_IMAGE_DIMENSIONS (int): Mode to use image dimensions.
        USE_PARENT_FOLDER_NAME (int): Mode to use parent folder name.
        REMOVE_CUSTOM_TEXT (int): Mode to remove custom text.
        REPLACE_CUSTOM_TEXT (int): Mode to replace custom text.
        ADD_SEQUENCE (int): Mode to add a sequence.
        TRUNCATE_FILE_NAME (int): Mode to truncate file names.
        CHANGE_EXTENSION (int): Mode to change file extensions.
    """
    ADD_CUSTOM_TEXT = 0
    CHANGE_CASE = 1
    USE_DATETIME = 2
    USE_IMAGE_DIMENSIONS = 3
    USE_PARENT_FOLDER_NAME = 4
    REMOVE_CUSTOM_TEXT = 5
    REPLACE_CUSTOM_TEXT = 6
    ADD_SEQUENCE = 7
    TRUNCATE_FILE_NAME = 8
    CHANGE_EXTENSION = 9


class ItemPosition(IntEnum):
    """
    An enumeration of item positions.

    Attributes:
        BEGIN (int): Item position at the beginning.
        END (int): Item position at the end.
    """
    BEGIN = 0
    END = 1


class ItemPositionExtended(IntEnum):
    """
    An enumeration of extended item positions.

    Attributes:
        BEGIN (int): Item position at the beginning.
        END (int): Item position at the end.
        EVERYWHERE (int): Item position everywhere.
    """
    BEGIN = 0
    END = 1
    EVERYWHERE = 2


class ItemPositionWithReplacement(IntEnum):
    """
    An enumeration of item positions with replacement.

    Attributes:
        BEGIN (int): Item position at the beginning.
        END (int): Item position at the end.
        REPLACE (int): Item replacement position.
    """
    BEGIN = 0
    END = 1
    REPLACE = 2


class SortSource(IntEnum):
    """
    An enumeration of sorting sources.

    Attributes:
        FILE_NAME (int): Sort by file name.
        FILE_PATH (int): Sort by file path.
        FILE_SIZE (int): Sort by file size.
        FILE_CREATION_DATETIME (int): Sort by file creation date and time.
        FILE_MODIFICATION_DATETIME (int): Sort by file modification date and time.
        FILE_CONTENT_CREATION_DATETIME (int): Sort by file content creation date and time.
        IMAGE_WIDTH (int): Sort by image width.
        IMAGE_HEIGHT (int): Sort by image height.
    """
    FILE_NAME = 0
    FILE_PATH = 1
    FILE_SIZE = 2
    FILE_CREATION_DATETIME = 3
    FILE_MODIFICATION_DATETIME = 4
    FILE_CONTENT_CREATION_DATETIME = 5
    IMAGE_WIDTH = 6
    IMAGE_HEIGHT = 7


class TextCaseOptions(IntEnum):
    """
    An enumeration of text case options.

    Attributes:
        CAMEL_CASE (int): Convert to camel case.
        PASCAL_CASE (int): Convert to pascal case.
        SNAKE_CASE (int): Convert to snake case.
        SNAKE_CASE_SCREAMING (int): Convert to screaming snake case.
        KEBAB_CASE (int): Convert to kebab case.
        UPPERCASE (int): Convert to uppercase.
        LOWERCASE (int): Convert to lowercase.
        TITLE_CASE (int): Convert to title case.
    """
    CAMEL_CASE = 0
    PASCAL_CASE = 1
    SNAKE_CASE = 2
    SNAKE_CASE_SCREAMING = 3
    KEBAB_CASE = 4
    UPPERCASE = 5
    LOWERCASE = 6
    TITLE_CASE = 7


class TruncateOptions(IntEnum):
    """
    An enumeration of truncation options.

    Attributes:
        REMOVE_SYMBOLS_IN_BEGIN (int): Remove symbols in the beginning.
        REMOVE_SYMBOLS_FROM_END (int): Remove symbols from the end.
        TRUNCATE_EMPTY_SYMBOLS (int): Truncate empty symbols.
    """
    REMOVE_SYMBOLS_IN_BEGIN = 0
    REMOVE_SYMBOLS_FROM_END = 1
    TRUNCATE_EMPTY_SYMBOLS = 2


class DateFormat(IntEnum):
    """
    An enumeration of date format options.

    Attributes:
        DO_NOT_USE_DATE (int): Indicates that the date should not be used.
        YYYY_MM_DD_TOGETHER (int): Year-month-day format without any separators.
        YYYY_MM_DD_WHITE_SPACED (int): Year-month-day format separated by white spaces.
        YYYY_MM_DD_UNDERSCORED (int): Year-month-day format separated by underscores.
        YYYY_MM_DD_DOTTED (int): Year-month-day format separated by dots.
        YYYY_MM_DD_DASHED (int): Year-month-day format separated by dashes.
        YY_MM_DD_TOGETHER (int): Year-month-day format with abbreviated year without separators.
        YY_MM_DD_WHITE_SPACED (int): Year-month-day format with abbreviated year separated by white spaces.
        YY_MM_DD_UNDERSCORED (int): Year-month-day format with abbreviated year separated by underscores.
        YY_MM_DD_DOTTED (int): Year-month-day format with abbreviated year separated by dots.
        YY_MM_DD_DASHED (int): Year-month-day format with abbreviated year separated by dashes.
        MM_DD_YYYY_TOGETHER (int): Month-day-year format without any separators.
        MM_DD_YYYY_WHITE_SPACED (int): Month-day-year format separated by white spaces.
        MM_DD_YYYY_UNDERSCORED (int): Month-day-year format separated by underscores.
        MM_DD_YYYY_DOTTED (int): Month-day-year format separated by dots.
        MM_DD_YYYY_DASHED (int): Month-day-year format separated by dashes.
        MM_DD_YY_TOGETHER (int): Month-day-year format with abbreviated year without separators.
        MM_DD_YY_WHITE_SPACED (int): Month-day-year format with abbreviated year separated by white spaces.
        MM_DD_YY_UNDERSCORED (int): Month-day-year format with abbreviated year separated by underscores.
        MM_DD_YY_DOTTED (int): Month-day-year format with abbreviated year separated by dots.
        MM_DD_YY_DASHED (int): Month-day-year format with abbreviated year separated by dashes.
        DD_MM_YYYY_TOGETHER (int): Day-month-year format without any separators.
        DD_MM_YYYY_WHITE_SPACED (int): Day-month-year format separated by white spaces.
        DD_MM_YYYY_UNDERSCORED (int): Day-month-year format separated by underscores.
        DD_MM_YYYY_DOTTED (int): Day-month-year format separated by dots.
        DD_MM_YYYY_DASHED (int): Day-month-year format separated by dashes.
        DD_MM_YY_TOGETHER (int): Day-month-year format with abbreviated year without separators.
        DD_MM_YY_WHITE_SPACED (int): Day-month-year format with abbreviated year separated by white spaces.
        DD_MM_YY_UNDERSCORED (int): Day-month-year format with abbreviated year separated by underscores.
        DD_MM_YY_DOTTED (int): Day-month-year format with abbreviated year separated by dots.
        DD_MM_YY_DASHED (int): Day-month-year format with abbreviated year separated by dashes.
    """
    DO_NOT_USE_DATE = 0
    YYYY_MM_DD_TOGETHER = 1
    YYYY_MM_DD_WHITE_SPACED = 2
    YYYY_MM_DD_UNDERSCORED = 3
    YYYY_MM_DD_DOTTED = 4
    YYYY_MM_DD_DASHED = 5
    YY_MM_DD_TOGETHER = 6
    YY_MM_DD_WHITE_SPACED = 7
    YY_MM_DD_UNDERSCORED = 8
    YY_MM_DD_DOTTED = 9
    YY_MM_DD_DASHED = 10
    MM_DD_YYYY_TOGETHER = 11
    MM_DD_YYYY_WHITE_SPACED = 12
    MM_DD_YYYY_UNDERSCORED = 13
    MM_DD_YYYY_DOTTED = 14
    MM_DD_YYYY_DASHED = 15
    MM_DD_YY_TOGETHER = 16
    MM_DD_YY_WHITE_SPACED = 17
    MM_DD_YY_UNDERSCORED = 18
    MM_DD_YY_DOTTED = 19
    MM_DD_YY_DASHED = 20
    DD_MM_YYYY_TOGETHER = 21
    DD_MM_YYYY_WHITE_SPACED = 22
    DD_MM_YYYY_UNDERSCORED = 23
    DD_MM_YYYY_DOTTED = 24
    DD_MM_YYYY_DASHED = 25
    DD_MM_YY_TOGETHER = 26
    DD_MM_YY_WHITE_SPACED = 27
    DD_MM_YY_UNDERSCORED = 28
    DD_MM_YY_DOTTED = 29
    DD_MM_YY_DASHED = 30


class TimeFormat(IntEnum):
    """
    An enumeration of time format options.

    Attributes:
        DO_NOT_USE_TIME (int): Indicates that the time should not be used.
        HH_MM_SS_24_TOGETHER (int): Hour-minute-second format in 24-hour clock without separators.
        HH_MM_SS_24_WHITE_SPACED (int): Hour-minute-second format in 24-hour clock separated by white spaces.
        HH_MM_SS_24_UNDERSCORED (int): Hour-minute-second format in 24-hour clock separated by underscores.
        HH_MM_SS_24_DOTTED (int): Hour-minute-second format in 24-hour clock separated by dots.
        HH_MM_SS_24_DASHED (int): Hour-minute-second format in 24-hour clock separated by dashes.
        HH_MM_24_TOGETHER (int): Hour-minute format in 24-hour clock without separators.
        HH_MM_24_WHITE_SPACED (int): Hour-minute format in 24-hour clock separated by white spaces.
        HH_MM_24_UNDERSCORED (int): Hour-minute format in 24-hour clock separated by underscores.
        HH_MM_24_DOTTED (int): Hour-minute format in 24-hour clock separated by dots.
        HH_MM_24_DASHED (int): Hour-minute format in 24-hour clock separated by dashes.
        HH_MM_SS_AM_PM_TOGETHER (int): Hour-minute-second format with AM/PM indicator without separators.
        HH_MM_SS_AM_PM_WHITE_SPACED (int): Hour-minute-second format with AM/PM indicator separated by white spaces.
        HH_MM_SS_AM_PM_UNDERSCORED (int): Hour-minute-second format with AM/PM indicator separated by underscores.
        HH_MM_SS_AM_PM_DOTTED (int): Hour-minute-second format with AM/PM indicator separated by dots.
        HH_MM_SS_AM_PM_DASHED (int): Hour-minute-second format with AM/PM indicator separated by dashes.
        HH_MM_AM_PM_TOGETHER (int): Hour-minute format with AM/PM indicator without separators.
        HH_MM_AM_PM_WHITE_SPACED (int): Hour-minute format with AM/PM indicator separated by white spaces.
        HH_MM_AM_PM_UNDERSCORED (int): Hour-minute format with AM/PM indicator separated by underscores.
        HH_MM_AM_PM_DOTTED (int): Hour-minute format with AM/PM indicator separated by dots.
        HH_MM_AM_PM_DASHED (int): Hour-minute format with AM/PM indicator separated by dashes.
    """
    DO_NOT_USE_TIME = 0
    HH_MM_SS_24_TOGETHER = 1
    HH_MM_SS_24_WHITE_SPACED = 2
    HH_MM_SS_24_UNDERSCORED = 3
    HH_MM_SS_24_DOTTED = 4
    HH_MM_SS_24_DASHED = 5
    HH_MM_24_TOGETHER = 6
    HH_MM_24_WHITE_SPACED = 7
    HH_MM_24_UNDERSCORED = 8
    HH_MM_24_DOTTED = 9
    HH_MM_24_DASHED = 10
    HH_MM_SS_AM_PM_TOGETHER = 11
    HH_MM_SS_AM_PM_WHITE_SPACED = 12
    HH_MM_SS_AM_PM_UNDERSCORED = 13
    HH_MM_SS_AM_PM_DOTTED = 14
    HH_MM_SS_AM_PM_DASHED = 15
    HH_MM_AM_PM_TOGETHER = 16
    HH_MM_AM_PM_WHITE_SPACED = 17
    HH_MM_AM_PM_UNDERSCORED = 18
    HH_MM_AM_PM_DOTTED = 19
    HH_MM_AM_PM_DASHED = 20


class DateTimeFormat(IntEnum):
    """
    An enumeration of date and time format options.

    Attributes:
        DATE_TIME_TOGETHER (int): Date and time together without separators.
        DATE_TIME_WHITE_SPACED (int): Date and time separated by white spaces.
        DATE_TIME_UNDERSCORED (int): Date and time separated by underscores.
        DATE_TIME_DOTTED (int): Date and time separated by dots.
        DATE_TIME_DASHED (int): Date and time separated by dashes.
        REVERSE_DATE_TIME_TOGETHER (int): Date and time together in reverse order without separators.
        REVERSE_DATE_TIME_WHITE_SPACED (int): Date and time in reverse order separated by white spaces.
        REVERSE_DATE_TIME_UNDERSCORED (int): Date and time in reverse order separated by underscores.
        REVERSE_DATE_TIME_DOTTED (int): Date and time in reverse order separated by dots.
        REVERSE_DATE_TIME_DASHED (int): Date and time in reverse order separated by dashes.
        NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970 (int): Number of seconds since January 1, 1970.
    """
    DATE_TIME_TOGETHER = 0
    DATE_TIME_WHITE_SPACED = 1
    DATE_TIME_UNDERSCORED = 2
    DATE_TIME_DOTTED = 3
    DATE_TIME_DASHED = 4
    REVERSE_DATE_TIME_TOGETHER = 5
    REVERSE_DATE_TIME_WHITE_SPACED = 6
    REVERSE_DATE_TIME_UNDERSCORED = 7
    REVERSE_DATE_TIME_DOTTED = 8
    REVERSE_DATE_TIME_DASHED = 9
    NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970 = 10


class DateTimeSource(IntEnum):
    """
    An enumeration of sources for obtaining datetime information.

    Attributes:
        FILE_CREATION_DATE (int): Date and time of file creation.
        FILE_MODIFICATION_DATE (int): Date and time of file modification.
        CONTENT_CREATION_DATE (int): Date and time of content creation.
        CURRENT_DATE (int): Current date and time.
        CUSTOM_DATE (int): Custom date and time.
    """
    FILE_CREATION_DATE = 0
    FILE_MODIFICATION_DATE = 1
    CONTENT_CREATION_DATE = 2
    CURRENT_DATE = 3
    CUSTOM_DATE = 4


class ImageDimensionOptions(IntEnum):
    """
    An enumeration of options for handling image dimensions.

    Attributes:
        DO_NOT_USE (int): Do not use image dimensions.
        WIDTH (int): Use image width.
        HEIGHT (int): Use image height.
    """
    DO_NOT_USE = 0
    WIDTH = 1
    HEIGHT = 2
