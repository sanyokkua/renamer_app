from datetime import datetime

from core.enums import DateFormat, TimeFormat, DateTimeFormat

# Mapping of date formats to their respective format strings
DATE_FORMAT_MAPPING: dict[DateFormat, str] = {
    DateFormat.DO_NOT_USE_DATE: "",
    DateFormat.YYYY_MM_DD_TOGETHER: "%Y%m%d",
    DateFormat.YYYY_MM_DD_WHITE_SPACED: "%Y %m %d",
    DateFormat.YYYY_MM_DD_UNDERSCORED: "%Y_%m_%d",
    DateFormat.YYYY_MM_DD_DOTTED: "%Y.%m.%d",
    DateFormat.YYYY_MM_DD_DASHED: "%Y-%m-%d",
    DateFormat.YY_MM_DD_TOGETHER: "%y%m%d",
    DateFormat.YY_MM_DD_WHITE_SPACED: "%y %m %d",
    DateFormat.YY_MM_DD_UNDERSCORED: "%y_%m_%d",
    DateFormat.YY_MM_DD_DOTTED: "%y.%m.%d",
    DateFormat.YY_MM_DD_DASHED: "%y-%m-%d",
    DateFormat.MM_DD_YYYY_TOGETHER: "%m%d%Y",
    DateFormat.MM_DD_YYYY_WHITE_SPACED: "%m %d %Y",
    DateFormat.MM_DD_YYYY_UNDERSCORED: "%m_%d_%Y",
    DateFormat.MM_DD_YYYY_DOTTED: "%m.%d.%Y",
    DateFormat.MM_DD_YYYY_DASHED: "%m-%d-%Y",
    DateFormat.MM_DD_YY_TOGETHER: "%m%d%y",
    DateFormat.MM_DD_YY_WHITE_SPACED: "%m %d %y",
    DateFormat.MM_DD_YY_UNDERSCORED: "%m_%d_%y",
    DateFormat.MM_DD_YY_DOTTED: "%m.%d.%y",
    DateFormat.MM_DD_YY_DASHED: "%m-%d-%y",
    DateFormat.DD_MM_YYYY_TOGETHER: "%d%m%Y",
    DateFormat.DD_MM_YYYY_WHITE_SPACED: "%d %m %Y",
    DateFormat.DD_MM_YYYY_UNDERSCORED: "%d_%m_%Y",
    DateFormat.DD_MM_YYYY_DOTTED: "%d.%m.%Y",
    DateFormat.DD_MM_YYYY_DASHED: "%d-%m-%Y",
    DateFormat.DD_MM_YY_TOGETHER: "%d%m%y",
    DateFormat.DD_MM_YY_WHITE_SPACED: "%d %m %y",
    DateFormat.DD_MM_YY_UNDERSCORED: "%d_%m_%y",
    DateFormat.DD_MM_YY_DOTTED: "%d.%m.%y",
    DateFormat.DD_MM_YY_DASHED: "%d-%m-%y",
}

# Mapping of time formats to their respective format strings
TIME_FORMAT_MAPPING: dict[TimeFormat, str] = {
    TimeFormat.DO_NOT_USE_TIME: "",
    TimeFormat.HH_MM_SS_24_TOGETHER: "%H%M%S",
    TimeFormat.HH_MM_SS_24_WHITE_SPACED: "%H %M %S",
    TimeFormat.HH_MM_SS_24_UNDERSCORED: "%H_%M_%S",
    TimeFormat.HH_MM_SS_24_DOTTED: "%H.%M.%S",
    TimeFormat.HH_MM_SS_24_DASHED: "%H-%M-%S",
    TimeFormat.HH_MM_24_TOGETHER: "%H%M",
    TimeFormat.HH_MM_24_WHITE_SPACED: "%H %M",
    TimeFormat.HH_MM_24_UNDERSCORED: "%H_%M",
    TimeFormat.HH_MM_24_DOTTED: "%H.%M",
    TimeFormat.HH_MM_24_DASHED: "%H-%M",
    TimeFormat.HH_MM_SS_AM_PM_TOGETHER: "%I%M%S%p",
    TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED: "%I %M %S %p",
    TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED: "%I_%M_%S_%p",
    TimeFormat.HH_MM_SS_AM_PM_DOTTED: "%I.%M.%S.%p",
    TimeFormat.HH_MM_SS_AM_PM_DASHED: "%I-%M-%S-%p",
    TimeFormat.HH_MM_AM_PM_TOGETHER: "%I%M%p",
    TimeFormat.HH_MM_AM_PM_WHITE_SPACED: "%I %M %p",
    TimeFormat.HH_MM_AM_PM_UNDERSCORED: "%I_%M_%p",
    TimeFormat.HH_MM_AM_PM_DOTTED: "%I.%M.%p",
    TimeFormat.HH_MM_AM_PM_DASHED: "%I-%M-%p",
}


def make_date_string(date_format: DateFormat, timestamp: float) -> str:
    """
    Generate a formatted date string based on the given date format and timestamp.

    Args:
        date_format (DateFormat): The desired date format.
        timestamp (float): The timestamp in seconds since the epoch.

    Returns:
        str: The formatted date string.
    """
    date_format_pattern = DATE_FORMAT_MAPPING.get(date_format)
    dt = datetime.fromtimestamp(timestamp)

    formatted_string = dt.strftime(date_format_pattern)
    print(f"Pattern: {date_format_pattern}, Result: {formatted_string}")
    return formatted_string


def make_time_string(time_format: TimeFormat, timestamp: float) -> str:
    """
    Generate a formatted time string based on the given time format and timestamp.

    Args:
        time_format (TimeFormat): The desired time format.
        timestamp (float): The timestamp in seconds since the epoch.

    Returns:
        str: The formatted time string.
    """
    date_format_pattern = TIME_FORMAT_MAPPING.get(time_format)
    dt = datetime.fromtimestamp(timestamp)

    formatted_string = dt.strftime(date_format_pattern)
    print(f"Pattern: {date_format_pattern}, Result: {formatted_string}")
    return formatted_string.lower()


def make_datetime_string(
    date_time_format: DateTimeFormat,
    date_format: DateFormat,
    time_format: TimeFormat,
    timestamp: float,
) -> str:
    """
    Generate a formatted date-time string based on the given date-time format,
    date format, time format, and timestamp.

    Args:
        date_time_format (DateTimeFormat): The desired date-time format.
        date_format (DateFormat): The desired date format.
        time_format (TimeFormat): The desired time format.
        timestamp (float): The timestamp in seconds since the epoch.

    Returns:
        str: The formatted date-time string.
    """
    date_str: str = make_date_string(date_format, timestamp)
    time_str: str = make_time_string(time_format, timestamp)

    if (
        date_format == DateFormat.DO_NOT_USE_DATE
        and time_format != TimeFormat.DO_NOT_USE_TIME
    ):
        return time_str
    elif (
        time_format == TimeFormat.DO_NOT_USE_TIME
        and date_format != DateFormat.DO_NOT_USE_DATE
    ):
        return date_str

    match date_time_format:
        case DateTimeFormat.DATE_TIME_TOGETHER:
            return f"{date_str}{time_str}"
        case DateTimeFormat.DATE_TIME_WHITE_SPACED:
            return f"{date_str} {time_str}"
        case DateTimeFormat.DATE_TIME_UNDERSCORED:
            return f"{date_str}_{time_str}"
        case DateTimeFormat.DATE_TIME_DOTTED:
            return f"{date_str}.{time_str}"
        case DateTimeFormat.DATE_TIME_DASHED:
            return f"{date_str}-{time_str}"
        case DateTimeFormat.REVERSE_DATE_TIME_TOGETHER:
            return f"{time_str}{date_str}"
        case DateTimeFormat.REVERSE_DATE_TIME_WHITE_SPACED:
            return f"{time_str} {date_str}"
        case DateTimeFormat.REVERSE_DATE_TIME_UNDERSCORED:
            return f"{time_str}_{date_str}"
        case DateTimeFormat.REVERSE_DATE_TIME_DOTTED:
            return f"{time_str}.{date_str}"
        case DateTimeFormat.REVERSE_DATE_TIME_DASHED:
            return f"{time_str}-{date_str}"
        case DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970:
            return f"{timestamp}"
