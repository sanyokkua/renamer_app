from datetime import datetime
from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.enums import ItemPositionWithReplacement, DateFormat, TimeFormat, DateTimeFormat, DateTimeSource
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file, verify_command_result

DT_FOR_TESTS = datetime(2008, 11, 22, 23, 59, 9)
TS_TEST = DT_FOR_TESTS.timestamp()
DT_CURRENT = datetime.now()
TS_NOW = DT_CURRENT.timestamp()


def test_command_with_none_arguments():
    from core.exceptions import PassedArgumentIsNone
    from core.commands.prep_date_time import DateTimeRenamePrepareCommand
    test_command = DateTimeRenamePrepareCommand(
        position=ItemPositionWithReplacement.REPLACE,
        date_format=DateFormat.YYYY_MM_DD_TOGETHER,
        time_format=TimeFormat.HH_MM_SS_24_TOGETHER,
        datetime_format=DateTimeFormat.DATE_TIME_UNDERSCORED,
        datetime_source=DateTimeSource.CONTENT_CREATION_DATE,
        use_uppercase=True,
        custom_datetime="",
    )
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    from core.commands.prep_date_time import DateTimeRenamePrepareCommand
    test_command = DateTimeRenamePrepareCommand(
        position=ItemPositionWithReplacement.REPLACE,
        date_format=DateFormat.YYYY_MM_DD_TOGETHER,
        time_format=TimeFormat.HH_MM_SS_24_TOGETHER,
        datetime_format=DateTimeFormat.DATE_TIME_UNDERSCORED,
        datetime_source=DateTimeSource.CONTENT_CREATION_DATE,
        use_uppercase=True,
        custom_datetime="",
    )

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_date_time import DateTimeRenamePrepareCommand
    test_command = DateTimeRenamePrepareCommand(
        position=ItemPositionWithReplacement.REPLACE,
        date_format=DateFormat.YYYY_MM_DD_TOGETHER,
        time_format=TimeFormat.HH_MM_SS_24_TOGETHER,
        datetime_format=DateTimeFormat.DATE_TIME_UNDERSCORED,
        datetime_source=DateTimeSource.CONTENT_CREATION_DATE,
        use_uppercase=True,
        custom_datetime="",
    )

    with pytest.raises(TypeError):
        test_command.execute("string", None)


def test_command_call_callback():
    from core.commands.prep_date_time import DateTimeRenamePrepareCommand
    test_command = DateTimeRenamePrepareCommand(
        position=ItemPositionWithReplacement.REPLACE,
        date_format=DateFormat.YYYY_MM_DD_TOGETHER,
        time_format=TimeFormat.HH_MM_SS_24_TOGETHER,
        datetime_format=DateTimeFormat.DATE_TIME_UNDERSCORED,
        datetime_source=DateTimeSource.CONTENT_CREATION_DATE,
        use_uppercase=True,
        custom_datetime="",
    )

    mock_function = MagicMock()

    file1 = build_app_file("file_name_1")
    file2 = build_app_file("file_name_2", ".png")

    files = [file1, file2]

    result: list[AppFile] = test_command.execute(files, mock_function)

    assert len(result) == 2

    assert mock_function.call_count == 4
    mock_function.assert_has_calls([mock.call(0, 2, 0),
                                    mock.call(0, 2, 1),
                                    mock.call(0, 2, 2),
                                    mock.call(0, 100, 0)])


# TODO: Create tests

@pytest.mark.parametrize("position, df, tf, dtf, dts, up, custom, f_name, n_name, ts", [
    # datetime(2008, 11, 22, 23, 59, 9), 20081122_235909
    (ItemPositionWithReplacement.BEGIN, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_235909TEST",
     TS_TEST),
    (ItemPositionWithReplacement.END, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "TEST20081122_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_235909",
     TS_TEST),

    # datetime(2008, 11, 22, 23, 59, 9), 20081122_235909
    (ItemPositionWithReplacement.REPLACE, DateFormat.DO_NOT_USE_DATE, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "235909", TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_WHITE_SPACED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "2008 11 22_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_UNDERSCORED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "2008_11_22_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_DOTTED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "2008.11.22_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_DASHED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "2008-11-22_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "081122_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YY_MM_DD_WHITE_SPACED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "08 11 22_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YY_MM_DD_UNDERSCORED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "08_11_22_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YY_MM_DD_DOTTED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "08.11.22_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YY_MM_DD_DASHED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "08-11-22_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YYYY_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11222008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YYYY_WHITE_SPACED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11 22 2008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YYYY_UNDERSCORED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11_22_2008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YYYY_DOTTED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11.22.2008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YYYY_DASHED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11-22-2008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YY_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "112208_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YY_WHITE_SPACED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11 22 08_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YY_UNDERSCORED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11_22_08_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YY_DOTTED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11.22.08_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.MM_DD_YY_DASHED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "11-22-08_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YYYY_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22112008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YYYY_WHITE_SPACED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22 11 2008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YYYY_UNDERSCORED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22_11_2008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YYYY_DOTTED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22.11.2008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YYYY_DASHED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22-11-2008_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YY_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "221108_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YY_WHITE_SPACED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22 11 08_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YY_UNDERSCORED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22_11_08_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YY_DOTTED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22.11.08_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.DD_MM_YY_DASHED, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "22-11-08_235909",
     TS_TEST),
    # datetime(2008, 11, 22, 23, 59, 9), 20081122_235909
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.DO_NOT_USE_TIME,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122", TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_WHITE_SPACED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_23 59 09",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_UNDERSCORED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_23_59_09",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_DOTTED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_23.59.09",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_DASHED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_23-59-09",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_2359",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_24_WHITE_SPACED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_23 59",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_24_UNDERSCORED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_23_59",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_24_DOTTED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_23.59",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_24_DASHED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_23-59",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_AM_PM_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_115909PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_11 59 09 PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_11_59_09_PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_AM_PM_DOTTED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_11.59.09.PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_AM_PM_DASHED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_11-59-09-PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_AM_PM_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_1159PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_AM_PM_WHITE_SPACED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_11 59 PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_AM_PM_UNDERSCORED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_11_59_PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_AM_PM_DOTTED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_11.59.PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_AM_PM_DASHED,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_11-59-PM",
     TS_TEST),
    # datetime(2008, 11, 22, 23, 59, 9), 20081122_235909
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_TOGETHER, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122235909", TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_WHITE_SPACED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122 235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_DOTTED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122.235909", TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_DASHED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122-235909", TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.REVERSE_DATE_TIME_TOGETHER, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "23590920081122",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.REVERSE_DATE_TIME_WHITE_SPACED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST",
     "235909 20081122", TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.REVERSE_DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST",
     "235909_20081122", TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.REVERSE_DATE_TIME_DOTTED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "235909.20081122",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.REVERSE_DATE_TIME_DASHED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "235909-20081122",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST",
     f"{TS_TEST}", TS_TEST),
    # datetime(2008, 11, 22, 23, 59, 9), 20081122_235909
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_MODIFICATION_DATE, True, "", "TEST", "20081122_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.CONTENT_CREATION_DATE, True, "", "TEST", "20081122_235909",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_24_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.CUSTOM_DATE, True, "20150501_220301", "TEST",
     "20150501_220301", TS_TEST),
    # datetime(2008, 11, 22, 23, 59, 9), 20081122_235909
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_AM_PM_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, True, "", "TEST", "20081122_115909PM",
     TS_TEST),
    (ItemPositionWithReplacement.REPLACE, DateFormat.YYYY_MM_DD_TOGETHER, TimeFormat.HH_MM_SS_AM_PM_TOGETHER,
     DateTimeFormat.DATE_TIME_UNDERSCORED, DateTimeSource.FILE_CREATION_DATE, False, "2008112211_59_PM", "TEST",
     "20081122_115909pm", TS_TEST),

])
def test_command_params_combinations(position: ItemPositionWithReplacement,
                                     df: DateFormat,
                                     tf: TimeFormat,
                                     dtf: DateTimeFormat,
                                     dts: DateTimeSource,
                                     up: bool,
                                     custom: str,
                                     f_name: str,
                                     n_name: str,
                                     ts: float):
    # Prepare
    from core.commands.prep_date_time import DateTimeRenamePrepareCommand
    test_command = DateTimeRenamePrepareCommand(
        position=position,
        date_format=df,
        time_format=tf,
        datetime_format=dtf,
        datetime_source=dts,
        use_uppercase=up,
        custom_datetime=custom,
    )

    # Verify
    verify_command_result(test_command, file_name_origin=f_name, file_name_expected=n_name, file_creation_time=ts)
