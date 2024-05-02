import tempfile
from datetime import datetime
from pathlib import Path

import pytest

from core.exceptions import PassedArgumentIsNone, FileNotFoundException
from core.utils.io_utils import (
    get_parent_folders,
    validate_path,
    is_ext_supported,
    chose_correct_date_from_metadata,
    parse_date_time,
)


def test_empty_path():
    assert get_parent_folders("") == []


def test_none_path():
    assert get_parent_folders(None) == []


def test_single_folder():
    folder_path = "/folder"
    expected_result = []
    assert get_parent_folders(folder_path) == expected_result


def test_multiple_slashes():
    folder_path = "/user/documents//projects/"
    expected_result = ["user", "documents"]
    assert get_parent_folders(folder_path) == expected_result


def test_file_path():
    file_path = "/user/documents/projects/file.txt"
    expected_result = ["user", "documents", "projects"]
    assert get_parent_folders(file_path) == expected_result


def test_folder_path():
    folder_path = "/user/documents/code_folder"
    expected_result = ["user", "documents"]
    assert get_parent_folders(folder_path) == expected_result


def test_root_path():
    root_path = "/"
    expected_result = []
    assert get_parent_folders(root_path) == expected_result


def test_root_path_win():
    root_path = "c:"
    expected_result = []
    assert get_parent_folders(root_path) == expected_result


def test_windows_path():
    windows_path = r"C:\Users\Documents\Projects\File.txt"
    expected_result = ["Users", "Documents", "Projects"]
    assert get_parent_folders(windows_path) == expected_result


def test_windows_path_folder():
    windows_path = r"C:\Users\Documents\Projects\File_Folder\last_folder_to_not_include"
    expected_result = ["Users", "Documents", "Projects", "File_Folder"]
    assert get_parent_folders(windows_path) == expected_result


def test_validate_path_raises_exception_on_none():
    with pytest.raises(PassedArgumentIsNone):
        validate_path(None)


def test_validate_path_raises_exception_on_incorrect_types():
    with pytest.raises(AssertionError):
        validate_path(1000)
    with pytest.raises(AssertionError):
        validate_path("STRING")


def test_validate_path_raises_exception_on_file_not_found():
    mock_path = Path("NOT_EXIST")

    with pytest.raises(FileNotFoundException):
        validate_path(mock_path)


def test_is_ext_supported_returns_true_for_supported_ext():
    temp_dir = tempfile.gettempdir()

    test_path_0 = Path(tempfile.NamedTemporaryFile(suffix=".jpg", dir=temp_dir, delete=False).name)
    test_path_1 = Path(tempfile.NamedTemporaryFile(suffix=".PNG", dir=temp_dir, delete=False).name)
    test_path_2 = Path(tempfile.NamedTemporaryFile(suffix=".heic", dir=temp_dir, delete=False).name)
    test_path_3 = Path(tempfile.NamedTemporaryFile(suffix=".Gif", dir=temp_dir, delete=False).name)

    supported_extensions = [".jpg", ".png", ".heic", ".gif", ".mov", ".mp3", ".hh"]

    assert is_ext_supported(test_path_0, supported_extensions)
    assert is_ext_supported(test_path_1, supported_extensions)
    assert is_ext_supported(test_path_2, supported_extensions)
    assert is_ext_supported(test_path_3, supported_extensions)


def test_is_ext_supported_returns_false_for_not_supported_ext():
    temp_dir = tempfile.gettempdir()

    test_path_0 = Path(tempfile.NamedTemporaryFile(suffix=".cpp", dir=temp_dir, delete=False).name)
    test_path_1 = Path(tempfile.NamedTemporaryFile(suffix=".TxT", dir=temp_dir, delete=False).name)
    test_path_2 = Path(tempfile.NamedTemporaryFile(suffix=".py", dir=temp_dir, delete=False).name)
    test_path_3 = Path(tempfile.NamedTemporaryFile(suffix=".JAVA", dir=temp_dir, delete=False).name)

    supported_extensions = [".jpg", ".png", ".heic", ".gif", ".mov", ".mp3", ".hh"]

    assert not is_ext_supported(test_path_0, supported_extensions)
    assert not is_ext_supported(test_path_1, supported_extensions)
    assert not is_ext_supported(test_path_2, supported_extensions)
    assert not is_ext_supported(test_path_3, supported_extensions)


def test_chose_correct_date_from_metadata_if_all_none():
    date = chose_correct_date_from_metadata(None, None, None)

    assert date is None


def test_chose_correct_date_from_metadata_if_all_present_and_original_is_oldest():
    date_time = datetime(2008, 11, 22, 23, 52, 0)
    date_time_original = datetime(2008, 11, 22, 23, 50, 0)
    date_time_digitized = datetime(2008, 11, 22, 23, 51, 0)
    dt_timestamp = date_time.timestamp()
    dto_timestamp = date_time_original.timestamp()
    dtd_timestamp = date_time_digitized.timestamp()

    date = chose_correct_date_from_metadata(dt_timestamp, dto_timestamp, dtd_timestamp)

    assert date == dto_timestamp


def test_chose_correct_date_from_metadata_if_all_present_and_date_time_is_oldest():
    date_time = datetime(2008, 11, 22, 23, 49, 0)
    date_time_original = datetime(2008, 11, 22, 23, 50, 0)
    date_time_digitized = datetime(2008, 11, 22, 23, 51, 0)
    dt_timestamp = date_time.timestamp()
    dto_timestamp = date_time_original.timestamp()
    dtd_timestamp = date_time_digitized.timestamp()

    date = chose_correct_date_from_metadata(dt_timestamp, dto_timestamp, dtd_timestamp)

    assert date == dt_timestamp


def test_chose_correct_date_from_metadata_if_all_present_and_digitized_is_oldest():
    date_time = datetime(2008, 11, 22, 23, 49, 0)
    date_time_original = datetime(2008, 11, 22, 23, 50, 0)
    date_time_digitized = datetime(2000, 11, 22, 23, 51, 0)
    dt_timestamp = date_time.timestamp()
    dto_timestamp = date_time_original.timestamp()
    dtd_timestamp = date_time_digitized.timestamp()

    date = chose_correct_date_from_metadata(dt_timestamp, dto_timestamp, dtd_timestamp)

    assert date == dtd_timestamp


def test_chose_correct_date_from_metadata_if_1_is_none_chose_datetime():
    date_time = datetime(2008, 11, 22, 23, 49, 0)
    date_time_digitized = datetime(2008, 11, 22, 23, 51, 0)
    dt_timestamp = date_time.timestamp()
    dtd_timestamp = date_time_digitized.timestamp()

    date = chose_correct_date_from_metadata(dt_timestamp, None, dtd_timestamp)

    assert date == dt_timestamp


def test_chose_correct_date_from_metadata_if_1_is_none_chose_original():
    date_time_original = datetime(2008, 11, 22, 23, 50, 0)
    date_time_digitized = datetime(2008, 11, 22, 23, 51, 0)
    dto_timestamp = date_time_original.timestamp()
    dtd_timestamp = date_time_digitized.timestamp()

    date = chose_correct_date_from_metadata(None, dto_timestamp, dtd_timestamp)

    assert date == dto_timestamp


def test_chose_correct_date_from_metadata_if_1_is_none_chose_digitized():
    date_time = datetime(2008, 11, 22, 23, 49, 0)
    date_time_digitized = datetime(2007, 11, 22, 23, 51, 0)
    dt_timestamp = date_time.timestamp()
    dtd_timestamp = date_time_digitized.timestamp()

    date = chose_correct_date_from_metadata(dt_timestamp, None, dtd_timestamp)

    assert date == dtd_timestamp


def test_chose_correct_date_from_metadata_if_2_is_none_should_return_available_date():
    date_time = datetime(2008, 11, 22, 23, 49, 0)
    dt_timestamp = date_time.timestamp()

    assert dt_timestamp == chose_correct_date_from_metadata(dt_timestamp, None, None)
    assert dt_timestamp == chose_correct_date_from_metadata(None, dt_timestamp, None)
    assert dt_timestamp == chose_correct_date_from_metadata(None, None, dt_timestamp)


def test_parse_date_time_if_none_passed():
    assert parse_date_time(None) is None


def test_parse_date_time_for_all_date_formats():
    date_time = datetime(2024, 10, 29, 15, 00, 59)
    result_timestamp = date_time.timestamp()

    format_1 = date_time.strftime("%Y-%m-%dT%H:%M:%S")  # ISO 8601 format (YYYY-MM-DDTHH:MM:SS)
    format_2 = date_time.strftime("%a, %d %b %Y %H:%M:%S %z")  # RFC 2822 format (Day, DD Mon YYYY HH:MM:SS +ZZZZ)
    format_3 = date_time.strftime("%Y-%m-%d %H:%M:%S %z")  # YYYY-MM-DD HH:MM:SS ±HHMM
    format_4 = date_time.strftime("%Y-%m-%dT%H:%M:%S%z")
    format_5 = date_time.strftime("%Y:%m:%d %H:%M:%S")

    assert result_timestamp == parse_date_time(format_1)
    assert result_timestamp == parse_date_time(format_2)
    assert result_timestamp == parse_date_time(format_3)
    assert result_timestamp == parse_date_time(format_4)
    assert result_timestamp == parse_date_time(format_5)
    assert parse_date_time("NOT_SUPPORTED_FORMAT") is None
