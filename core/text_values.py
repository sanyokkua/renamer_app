from core.enums import (
    ItemPosition,
    AppModes,
    ItemPositionExtended,
    ItemPositionWithReplacement,
    SortSource,
    TextCaseOptions,
    TruncateOptions,
    ImageDimensionOptions,
    DateFormat,
    DateTimeFormat,
    DateTimeSource,
    TimeFormat,
)

APP_MODE_LABEL_VALUE: str = "Select mode"
POSITION_OF_TEXT_LABEL_VALUE: str = "Position of text"

APP_MODE_TEXT: dict[AppModes, str] = {
    AppModes.ADD_CUSTOM_TEXT: "Add custom text to existing file name",
    AppModes.CHANGE_CASE: "Change file name case",
    AppModes.USE_DATETIME: "Use file DateTime information",
    AppModes.USE_IMAGE_DIMENSIONS: "Use Image/Video dimensions",
    AppModes.USE_PARENT_FOLDER_NAME: "Use parent folders",
    AppModes.REMOVE_CUSTOM_TEXT: "Remove custom text from existing file name",
    AppModes.REPLACE_CUSTOM_TEXT: "Replace custom text in existing file name",
    AppModes.ADD_SEQUENCE: "Add sequence to the existing file name",
    AppModes.TRUNCATE_FILE_NAME: "Truncate existing file name",
    AppModes.CHANGE_EXTENSION: "Change extension",
}

ITEM_POSITION_TEXT: dict[ItemPosition, str] = {
    ItemPosition.BEGIN: "Begin",
    ItemPosition.END: "End",
}

ITEM_POSITION_EXTENDED_TEXT: dict[ItemPositionExtended, str] = {
    ItemPositionExtended.BEGIN: "Begin",
    ItemPositionExtended.END: "End",
    ItemPositionExtended.EVERYWHERE: "Everywhere",
}

ITEM_POSITION_WITH_REPLACEMENT_TEXT: dict[ItemPositionWithReplacement, str] = {
    ItemPositionWithReplacement.BEGIN: "Begin",
    ItemPositionWithReplacement.END: "End",
    ItemPositionWithReplacement.REPLACE: "Replace",
}

SORT_SOURCE_TEXT: dict[SortSource, str] = {
    SortSource.FILE_NAME: "File name",
    SortSource.FILE_PATH: "File path (absolute)",
    SortSource.FILE_SIZE: "File size",
    SortSource.FILE_CREATION_DATETIME: "File creation datetime",
    SortSource.FILE_MODIFICATION_DATETIME: "File modification datetime",
    SortSource.FILE_CONTENT_CREATION_DATETIME: "File content creation (exif, tiff, etc) datetime",
    SortSource.IMAGE_WIDTH: "Image width (only for images/video)",
    SortSource.IMAGE_HEIGHT: "Image height (only for images/video)",
}

TEXT_CASE_OPTIONS_TEXT: dict[TextCaseOptions, str] = {
    TextCaseOptions.CAMEL_CASE: "camelCaseText",
    TextCaseOptions.PASCAL_CASE: "PascalCaseText",
    TextCaseOptions.SNAKE_CASE: "snake_case_text",
    TextCaseOptions.SNAKE_CASE_SCREAMING: "SNAKE_CASE_TEXT",
    TextCaseOptions.KEBAB_CASE: "kebab-case-text",
    TextCaseOptions.UPPERCASE: "UPPERCASE TEXT",
    TextCaseOptions.LOWERCASE: "lowercase text",
    TextCaseOptions.TITLE_CASE: "Title Case Text",
}

TRUNCATE_OPTIONS_TEXT: dict[TruncateOptions, str] = {
    TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN: "Remove Symbols In Begin",
    TruncateOptions.REMOVE_SYMBOLS_FROM_END: "Remove Symbols From Begin",
    TruncateOptions.TRUNCATE_EMPTY_SYMBOLS: "Remove empty symbols from begin and end",
}

DATE_FORMAT_TEXT: dict[DateFormat, str] = {
    DateFormat.DO_NOT_USE_DATE: "Do not use date",
    DateFormat.YYYY_MM_DD_TOGETHER: "YYYYMMDD",
    DateFormat.YYYY_MM_DD_WHITE_SPACED: "YYYY MM DD",
    DateFormat.YYYY_MM_DD_UNDERSCORED: "YYYY_MM_DD",
    DateFormat.YYYY_MM_DD_DOTTED: "YYYY.MM.DD",
    DateFormat.YYYY_MM_DD_DASHED: "YYYY-MM-DD",
    DateFormat.YY_MM_DD_TOGETHER: "YYMMDD",
    DateFormat.YY_MM_DD_WHITE_SPACED: "YY MM DD",
    DateFormat.YY_MM_DD_UNDERSCORED: "YY_MM_DD",
    DateFormat.YY_MM_DD_DOTTED: "YY.MM.DD",
    DateFormat.YY_MM_DD_DASHED: "YY-MM-DD",
    DateFormat.MM_DD_YYYY_TOGETHER: "MMDDYYYY",
    DateFormat.MM_DD_YYYY_WHITE_SPACED: "MM DD YYYY",
    DateFormat.MM_DD_YYYY_UNDERSCORED: "MM_DD_YYYY",
    DateFormat.MM_DD_YYYY_DOTTED: "MM.DD.YYYY",
    DateFormat.MM_DD_YYYY_DASHED: "MM-DD-YYYY",
    DateFormat.MM_DD_YY_TOGETHER: "MMDDYY",
    DateFormat.MM_DD_YY_WHITE_SPACED: "MM DD YY",
    DateFormat.MM_DD_YY_UNDERSCORED: "MM_DD_YY",
    DateFormat.MM_DD_YY_DOTTED: "MM.DD.YY",
    DateFormat.MM_DD_YY_DASHED: "MM-DD-YY",
    DateFormat.DD_MM_YYYY_TOGETHER: "DDMMYYYY",
    DateFormat.DD_MM_YYYY_WHITE_SPACED: "DD MM YYYY",
    DateFormat.DD_MM_YYYY_UNDERSCORED: "DD_MM_YYYY",
    DateFormat.DD_MM_YYYY_DOTTED: "MM.DD.MM.YYYY",
    DateFormat.DD_MM_YYYY_DASHED: "MM-DD-MM-YYYY",
    DateFormat.DD_MM_YY_TOGETHER: "DDMMYY",
    DateFormat.DD_MM_YY_WHITE_SPACED: "DD MM YY",
    DateFormat.DD_MM_YY_UNDERSCORED: "DD_MM_YY",
    DateFormat.DD_MM_YY_DOTTED: "DD.MM.YY",
    DateFormat.DD_MM_YY_DASHED: "DD-MM-YY",
}

TIME_FORMAT_TEXT: dict[TimeFormat, str] = {
    TimeFormat.DO_NOT_USE_TIME: "Do not use time",
    TimeFormat.HH_MM_SS_24_TOGETHER: "HHMMSS  (24 hours)",
    TimeFormat.HH_MM_SS_24_WHITE_SPACED: "HH MM SS (24 hours)",
    TimeFormat.HH_MM_SS_24_UNDERSCORED: "HH_MM_SS (24 hours)",
    TimeFormat.HH_MM_SS_24_DOTTED: "HH.MM.SS (24 hours)",
    TimeFormat.HH_MM_SS_24_DASHED: "HH-MM-SS (24 hours)",
    TimeFormat.HH_MM_24_TOGETHER: "HHMM (24 hours)",
    TimeFormat.HH_MM_24_WHITE_SPACED: "HH MM (24 hours)",
    TimeFormat.HH_MM_24_UNDERSCORED: "HH_MM (24 hours)",
    TimeFormat.HH_MM_24_DOTTED: "HH.MM (24 hours)",
    TimeFormat.HH_MM_24_DASHED: "HH-MM (24 hours)",
    TimeFormat.HH_MM_SS_AM_PM_TOGETHER: "HHMMSS  (AM/PM, 12 hours)",
    TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED: "HH MM SS (AM/PM, 12 hours)",
    TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED: "HH_MM_SS (AM/PM, 12 hours)",
    TimeFormat.HH_MM_SS_AM_PM_DOTTED: "HH.MM.SS (AM/PM, 12 hours)",
    TimeFormat.HH_MM_SS_AM_PM_DASHED: "HH-MM-SS (AM/PM, 12 hours)",
    TimeFormat.HH_MM_AM_PM_TOGETHER: "HHMM (AM/PM, 12 hours)",
    TimeFormat.HH_MM_AM_PM_WHITE_SPACED: "HH MM (AM/PM, 12 hours)",
    TimeFormat.HH_MM_AM_PM_UNDERSCORED: "HH_MM (AM/PM, 12 hours)",
    TimeFormat.HH_MM_AM_PM_DOTTED: "HH.MM (AM/PM, 12 hours)",
    TimeFormat.HH_MM_AM_PM_DASHED: "HH-MM (AM/PM, 12 hours)",
}

DATE_TIME_FORMAT_TEXT: dict[DateTimeFormat, str] = {
    DateTimeFormat.DATE_TIME_TOGETHER: "DateTime",
    DateTimeFormat.DATE_TIME_WHITE_SPACED: "Date Time",
    DateTimeFormat.DATE_TIME_UNDERSCORED: "Date_Time",
    DateTimeFormat.DATE_TIME_DOTTED: "Date.Time",
    DateTimeFormat.DATE_TIME_DASHED: "Date-Time",
    DateTimeFormat.REVERSE_DATE_TIME_TOGETHER: "TimeDate",
    DateTimeFormat.REVERSE_DATE_TIME_WHITE_SPACED: "Time Date",
    DateTimeFormat.REVERSE_DATE_TIME_UNDERSCORED: "Time_Date",
    DateTimeFormat.REVERSE_DATE_TIME_DOTTED: "Time.Date",
    DateTimeFormat.REVERSE_DATE_TIME_DASHED: "Time-Date",
    DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970: "Number of seconds since January 1, 1970",
}

DATE_TIME_SOURCE_TEXT: dict[DateTimeSource, str] = {
    DateTimeSource.FILE_CREATION_DATE: "File creation date (File System)",
    DateTimeSource.FILE_MODIFICATION_DATE: "File modification date (File System)",
    DateTimeSource.CONTENT_CREATION_DATE: "Content Creation Date (Exif, TIFF, etc)",
    DateTimeSource.CURRENT_DATE: "Current Date",
    DateTimeSource.CUSTOM_DATE: "Custom Date",
}

IMAGE_DIMENSION_OPTIONS_TEXT: dict[ImageDimensionOptions, str] = {
    ImageDimensionOptions.DO_NOT_USE: "Do not use",
    ImageDimensionOptions.WIDTH: "Width",
    ImageDimensionOptions.HEIGHT: "Height",
}
