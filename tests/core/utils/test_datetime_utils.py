from datetime import datetime

from core.enums import DateFormat, TimeFormat, DateTimeFormat
from core.utils.datetime_utils import make_date_string, make_time_string, make_datetime_string


def test_make_date_string():
    datetime_to_use = datetime(2005, 12, 23, 13, 59, 10)
    timestamp_to_use = datetime_to_use.timestamp()

    assert make_date_string(DateFormat.DO_NOT_USE_DATE, timestamp_to_use) == ""
    assert make_date_string(DateFormat.YYYY_MM_DD_TOGETHER, timestamp_to_use) == "20051223"
    assert make_date_string(DateFormat.YYYY_MM_DD_WHITE_SPACED, timestamp_to_use) == "2005 12 23"
    assert make_date_string(DateFormat.YYYY_MM_DD_UNDERSCORED, timestamp_to_use) == "2005_12_23"
    assert make_date_string(DateFormat.YYYY_MM_DD_DOTTED, timestamp_to_use) == "2005.12.23"
    assert make_date_string(DateFormat.YYYY_MM_DD_DASHED, timestamp_to_use) == "2005-12-23"

    assert make_date_string(DateFormat.YY_MM_DD_TOGETHER, timestamp_to_use) == "051223"
    assert make_date_string(DateFormat.YY_MM_DD_WHITE_SPACED, timestamp_to_use) == "05 12 23"
    assert make_date_string(DateFormat.YY_MM_DD_UNDERSCORED, timestamp_to_use) == "05_12_23"
    assert make_date_string(DateFormat.YY_MM_DD_DOTTED, timestamp_to_use) == "05.12.23"
    assert make_date_string(DateFormat.YY_MM_DD_DASHED, timestamp_to_use) == "05-12-23"

    assert make_date_string(DateFormat.MM_DD_YYYY_TOGETHER, timestamp_to_use) == "12232005"
    assert make_date_string(DateFormat.MM_DD_YYYY_WHITE_SPACED, timestamp_to_use) == "12 23 2005"
    assert make_date_string(DateFormat.MM_DD_YYYY_UNDERSCORED, timestamp_to_use) == "12_23_2005"
    assert make_date_string(DateFormat.MM_DD_YYYY_DOTTED, timestamp_to_use) == "12.23.2005"
    assert make_date_string(DateFormat.MM_DD_YYYY_DASHED, timestamp_to_use) == "12-23-2005"

    assert make_date_string(DateFormat.MM_DD_YY_TOGETHER, timestamp_to_use) == "122305"
    assert make_date_string(DateFormat.MM_DD_YY_WHITE_SPACED, timestamp_to_use) == "12 23 05"
    assert make_date_string(DateFormat.MM_DD_YY_UNDERSCORED, timestamp_to_use) == "12_23_05"
    assert make_date_string(DateFormat.MM_DD_YY_DOTTED, timestamp_to_use) == "12.23.05"
    assert make_date_string(DateFormat.MM_DD_YY_DASHED, timestamp_to_use) == "12-23-05"

    assert make_date_string(DateFormat.DD_MM_YYYY_TOGETHER, timestamp_to_use) == "23122005"
    assert make_date_string(DateFormat.DD_MM_YYYY_WHITE_SPACED, timestamp_to_use) == "23 12 2005"
    assert make_date_string(DateFormat.DD_MM_YYYY_UNDERSCORED, timestamp_to_use) == "23_12_2005"
    assert make_date_string(DateFormat.DD_MM_YYYY_DOTTED, timestamp_to_use) == "23.12.2005"
    assert make_date_string(DateFormat.DD_MM_YYYY_DASHED, timestamp_to_use) == "23-12-2005"

    assert make_date_string(DateFormat.DD_MM_YY_TOGETHER, timestamp_to_use) == "231205"
    assert make_date_string(DateFormat.DD_MM_YY_WHITE_SPACED, timestamp_to_use) == "23 12 05"
    assert make_date_string(DateFormat.DD_MM_YY_UNDERSCORED, timestamp_to_use) == "23_12_05"
    assert make_date_string(DateFormat.DD_MM_YY_DOTTED, timestamp_to_use) == "23.12.05"
    assert make_date_string(DateFormat.DD_MM_YY_DASHED, timestamp_to_use) == "23-12-05"


def test_make_time_string():
    datetime_to_use = datetime(2008, 11, 22, 23, 59, 9)
    timestamp_to_use = datetime_to_use.timestamp()
    assert make_time_string(TimeFormat.DO_NOT_USE_TIME, timestamp_to_use) == ""
    assert make_time_string(TimeFormat.HH_MM_SS_24_TOGETHER, timestamp_to_use) == "235909"
    assert make_time_string(TimeFormat.HH_MM_SS_24_WHITE_SPACED, timestamp_to_use) == "23 59 09"
    assert make_time_string(TimeFormat.HH_MM_SS_24_UNDERSCORED, timestamp_to_use) == "23_59_09"
    assert make_time_string(TimeFormat.HH_MM_SS_24_DOTTED, timestamp_to_use) == "23.59.09"
    assert make_time_string(TimeFormat.HH_MM_SS_24_DASHED, timestamp_to_use) == "23-59-09"

    assert make_time_string(TimeFormat.HH_MM_24_TOGETHER, timestamp_to_use) == "2359"
    assert make_time_string(TimeFormat.HH_MM_24_WHITE_SPACED, timestamp_to_use) == "23 59"
    assert make_time_string(TimeFormat.HH_MM_24_UNDERSCORED, timestamp_to_use) == "23_59"
    assert make_time_string(TimeFormat.HH_MM_24_DOTTED, timestamp_to_use) == "23.59"
    assert make_time_string(TimeFormat.HH_MM_24_DASHED, timestamp_to_use) == "23-59"

    assert make_time_string(TimeFormat.HH_MM_SS_AM_PM_TOGETHER, timestamp_to_use).lower() == "115909pm"
    assert make_time_string(TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED, timestamp_to_use).lower() == "11 59 09 pm"
    assert make_time_string(TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED, timestamp_to_use).lower() == "11_59_09_pm"
    assert make_time_string(TimeFormat.HH_MM_SS_AM_PM_DOTTED, timestamp_to_use).lower() == "11.59.09.pm"
    assert make_time_string(TimeFormat.HH_MM_SS_AM_PM_DASHED, timestamp_to_use).lower() == "11-59-09-pm"

    assert make_time_string(TimeFormat.HH_MM_AM_PM_TOGETHER, timestamp_to_use).lower() == "1159pm"
    assert make_time_string(TimeFormat.HH_MM_AM_PM_WHITE_SPACED, timestamp_to_use).lower() == "11 59 pm"
    assert make_time_string(TimeFormat.HH_MM_AM_PM_UNDERSCORED, timestamp_to_use).lower() == "11_59_pm"
    assert make_time_string(TimeFormat.HH_MM_AM_PM_DOTTED, timestamp_to_use).lower() == "11.59.pm"
    assert make_time_string(TimeFormat.HH_MM_AM_PM_DASHED, timestamp_to_use).lower() == "11-59-pm"


def test_make_datetime_string():
    datetime_to_use = datetime(1990, 12, 5, 11, 33, 22)
    ts = datetime_to_use.timestamp()
    df = DateFormat.YYYY_MM_DD_TOGETHER
    tf = TimeFormat.HH_MM_SS_24_TOGETHER

    assert make_datetime_string(DateTimeFormat.DATE_TIME_TOGETHER, df, tf, ts) == "19901205113322"
    assert make_datetime_string(DateTimeFormat.DATE_TIME_WHITE_SPACED, df, tf, ts) == "19901205 113322"
    assert make_datetime_string(DateTimeFormat.DATE_TIME_UNDERSCORED, df, tf, ts) == "19901205_113322"
    assert make_datetime_string(DateTimeFormat.DATE_TIME_DOTTED, df, tf, ts) == "19901205.113322"
    assert make_datetime_string(DateTimeFormat.DATE_TIME_DASHED, df, tf, ts) == "19901205-113322"

    assert make_datetime_string(DateTimeFormat.REVERSE_DATE_TIME_TOGETHER, df, tf, ts) == "11332219901205"
    assert make_datetime_string(DateTimeFormat.REVERSE_DATE_TIME_WHITE_SPACED, df, tf, ts) == "113322 19901205"
    assert make_datetime_string(DateTimeFormat.REVERSE_DATE_TIME_UNDERSCORED, df, tf, ts) == "113322_19901205"
    assert make_datetime_string(DateTimeFormat.REVERSE_DATE_TIME_DOTTED, df, tf, ts) == "113322.19901205"
    assert make_datetime_string(DateTimeFormat.REVERSE_DATE_TIME_DASHED, df, tf, ts) == "113322-19901205"

    assert make_datetime_string(DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970, df, tf, ts) == f"{ts}"
