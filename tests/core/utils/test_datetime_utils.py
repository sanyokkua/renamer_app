from datetime import datetime

import pytest

from core.enums import DateFormat
from core.enums import TimeFormat, DateTimeFormat
from core.utils.datetime_utils import make_date_string
from core.utils.datetime_utils import make_datetime_string
from core.utils.datetime_utils import make_time_string


@pytest.mark.parametrize(
    "date_format, expected_date_string",
    [
        (DateFormat.DO_NOT_USE_DATE, ""),
        (DateFormat.YYYY_MM_DD_TOGETHER, "20051223"),
        (DateFormat.YYYY_MM_DD_WHITE_SPACED, "2005 12 23"),
        (DateFormat.YYYY_MM_DD_UNDERSCORED, "2005_12_23"),
        (DateFormat.YYYY_MM_DD_DOTTED, "2005.12.23"),
        (DateFormat.YYYY_MM_DD_DASHED, "2005-12-23"),
        (DateFormat.YY_MM_DD_TOGETHER, "051223"),
        (DateFormat.YY_MM_DD_WHITE_SPACED, "05 12 23"),
        (DateFormat.YY_MM_DD_UNDERSCORED, "05_12_23"),
        (DateFormat.YY_MM_DD_DOTTED, "05.12.23"),
        (DateFormat.YY_MM_DD_DASHED, "05-12-23"),
        (DateFormat.MM_DD_YYYY_TOGETHER, "12232005"),
        (DateFormat.MM_DD_YYYY_WHITE_SPACED, "12 23 2005"),
        (DateFormat.MM_DD_YYYY_UNDERSCORED, "12_23_2005"),
        (DateFormat.MM_DD_YYYY_DOTTED, "12.23.2005"),
        (DateFormat.MM_DD_YYYY_DASHED, "12-23-2005"),
        (DateFormat.MM_DD_YY_TOGETHER, "122305"),
        (DateFormat.MM_DD_YY_WHITE_SPACED, "12 23 05"),
        (DateFormat.MM_DD_YY_UNDERSCORED, "12_23_05"),
        (DateFormat.MM_DD_YY_DOTTED, "12.23.05"),
        (DateFormat.MM_DD_YY_DASHED, "12-23-05"),
        (DateFormat.DD_MM_YYYY_TOGETHER, "23122005"),
        (DateFormat.DD_MM_YYYY_WHITE_SPACED, "23 12 2005"),
        (DateFormat.DD_MM_YYYY_UNDERSCORED, "23_12_2005"),
        (DateFormat.DD_MM_YYYY_DOTTED, "23.12.2005"),
        (DateFormat.DD_MM_YYYY_DASHED, "23-12-2005"),
        (DateFormat.DD_MM_YY_TOGETHER, "231205"),
        (DateFormat.DD_MM_YY_WHITE_SPACED, "23 12 05"),
        (DateFormat.DD_MM_YY_UNDERSCORED, "23_12_05"),
        (DateFormat.DD_MM_YY_DOTTED, "23.12.05"),
        (DateFormat.DD_MM_YY_DASHED, "23-12-05"),
    ],
)
def test_make_date_string(date_format: DateFormat, expected_date_string: str):
    # Prepare
    datetime_to_use = datetime(2005, 12, 23, 13, 59, 10)
    timestamp_to_use = datetime_to_use.timestamp()

    # Execute
    result = make_date_string(date_format, timestamp_to_use)

    # Verify
    assert result == expected_date_string


@pytest.mark.parametrize(
    "time_format, expected_time_string",
    [
        (TimeFormat.DO_NOT_USE_TIME, ""),
        (TimeFormat.HH_MM_SS_24_TOGETHER, "235909"),
        (TimeFormat.HH_MM_SS_24_WHITE_SPACED, "23 59 09"),
        (TimeFormat.HH_MM_SS_24_UNDERSCORED, "23_59_09"),
        (TimeFormat.HH_MM_SS_24_DOTTED, "23.59.09"),
        (TimeFormat.HH_MM_SS_24_DASHED, "23-59-09"),
        (TimeFormat.HH_MM_24_TOGETHER, "2359"),
        (TimeFormat.HH_MM_24_WHITE_SPACED, "23 59"),
        (TimeFormat.HH_MM_24_UNDERSCORED, "23_59"),
        (TimeFormat.HH_MM_24_DOTTED, "23.59"),
        (TimeFormat.HH_MM_24_DASHED, "23-59"),
        (TimeFormat.HH_MM_SS_AM_PM_TOGETHER, "115909pm"),
        (TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED, "11 59 09 pm"),
        (TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED, "11_59_09_pm"),
        (TimeFormat.HH_MM_SS_AM_PM_DOTTED, "11.59.09.pm"),
        (TimeFormat.HH_MM_SS_AM_PM_DASHED, "11-59-09-pm"),
        (TimeFormat.HH_MM_AM_PM_TOGETHER, "1159pm"),
        (TimeFormat.HH_MM_AM_PM_WHITE_SPACED, "11 59 pm"),
        (TimeFormat.HH_MM_AM_PM_UNDERSCORED, "11_59_pm"),
        (TimeFormat.HH_MM_AM_PM_DOTTED, "11.59.pm"),
        (TimeFormat.HH_MM_AM_PM_DASHED, "11-59-pm"),
    ],
)
def test_make_time_string(time_format, expected_time_string):
    # Prepare
    datetime_to_use = datetime(2008, 11, 22, 23, 59, 9)
    timestamp_to_use = datetime_to_use.timestamp()

    # Execute
    result = make_time_string(time_format, timestamp_to_use)

    # Verify
    assert result == expected_time_string


dt_to_use = datetime(1990, 12, 5, 11, 33, 22)
timestamp = dt_to_use.timestamp()


@pytest.mark.parametrize(
    "date_time_format, date_format, time_format, expected_output",
    [
        (
            DateTimeFormat.DATE_TIME_TOGETHER,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "19901205113322",
        ),
        (
            DateTimeFormat.DATE_TIME_WHITE_SPACED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "19901205 113322",
        ),
        (
            DateTimeFormat.DATE_TIME_UNDERSCORED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "19901205_113322",
        ),
        (
            DateTimeFormat.DATE_TIME_DOTTED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "19901205.113322",
        ),
        (
            DateTimeFormat.DATE_TIME_DASHED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "19901205-113322",
        ),
        (
            DateTimeFormat.REVERSE_DATE_TIME_TOGETHER,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "11332219901205",
        ),
        (
            DateTimeFormat.REVERSE_DATE_TIME_WHITE_SPACED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322 19901205",
        ),
        (
            DateTimeFormat.REVERSE_DATE_TIME_UNDERSCORED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322_19901205",
        ),
        (
            DateTimeFormat.REVERSE_DATE_TIME_DOTTED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322.19901205",
        ),
        (
            DateTimeFormat.REVERSE_DATE_TIME_DASHED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322-19901205",
        ),
        (
            DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            f"{timestamp}",
        ),
        (
            DateTimeFormat.DATE_TIME_TOGETHER,
            DateFormat.DO_NOT_USE_DATE,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322",
        ),
        (
            DateTimeFormat.DATE_TIME_WHITE_SPACED,
            DateFormat.DO_NOT_USE_DATE,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322",
        ),
        (
            DateTimeFormat.DATE_TIME_UNDERSCORED,
            DateFormat.DO_NOT_USE_DATE,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322",
        ),
        (
            DateTimeFormat.DATE_TIME_DOTTED,
            DateFormat.DO_NOT_USE_DATE,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322",
        ),
        (
            DateTimeFormat.DATE_TIME_DASHED,
            DateFormat.DO_NOT_USE_DATE,
            TimeFormat.HH_MM_SS_24_TOGETHER,
            "113322",
        ),
        (
            DateTimeFormat.DATE_TIME_TOGETHER,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.DO_NOT_USE_TIME,
            "19901205",
        ),
        (
            DateTimeFormat.DATE_TIME_WHITE_SPACED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.DO_NOT_USE_TIME,
            "19901205",
        ),
        (
            DateTimeFormat.DATE_TIME_UNDERSCORED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.DO_NOT_USE_TIME,
            "19901205",
        ),
        (
            DateTimeFormat.DATE_TIME_DOTTED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.DO_NOT_USE_TIME,
            "19901205",
        ),
        (
            DateTimeFormat.DATE_TIME_DASHED,
            DateFormat.YYYY_MM_DD_TOGETHER,
            TimeFormat.DO_NOT_USE_TIME,
            "19901205",
        ),
    ],
)
def test_make_datetime_string(
    date_time_format: DateTimeFormat,
    date_format: DateFormat,
    time_format: TimeFormat,
    expected_output: str,
):
    assert (
        make_datetime_string(date_time_format, date_format, time_format, timestamp)
        == expected_output
    )
