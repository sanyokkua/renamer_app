from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.models.app_file import AppFile

mock_object_inst = AppFile(
    _absolute_path="",
    _is_folder=False,
    _file_extension=".file_ext",
    _file_extension_new=".file_ext",
    _file_name="file_name",
    _file_size=1000,
    _next_name="file_new_name",
    _fs_creation_date=100000,
    _fs_modification_date=100000,
    _metadata=None,
)


def build_app_file_from_path_str_mock(path: str | None):
    return mock_object_inst


def test_command_call_imported_function(monkeypatch):
    monkeypatch.setattr(
        "core.utils.other.build_app_file_from_path_str",
        build_app_file_from_path_str_mock,
    )

    from core.commands.map_url_to_app_file import MapUrlToAppFileCommand

    test_command = MapUrlToAppFileCommand()
    result = test_command.execute(["file_path1", "file_path2"], None)

    assert result == [mock_object_inst, mock_object_inst]


def test_command_call_callback(monkeypatch):
    monkeypatch.setattr(
        "core.utils.other.build_app_file_from_path_str",
        build_app_file_from_path_str_mock,
    )
    from core.commands.map_url_to_app_file import MapUrlToAppFileCommand

    mock_function = MagicMock()
    test_command = MapUrlToAppFileCommand()

    result = test_command.execute(["file_path1", "file_path2"], mock_function)

    assert result == [mock_object_inst, mock_object_inst]
    assert mock_function.call_count == 4
    mock_function.assert_has_calls(
        [
            mock.call(0, 2),
            mock.call(1, 2),
            mock.call(2, 2),
            mock.call(0, 100),
        ]
    )


def test_command_with_none_arguments():
    from core.exceptions import PassedArgumentIsNone
    from core.commands.map_url_to_app_file import MapUrlToAppFileCommand

    test_command = MapUrlToAppFileCommand()
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    from core.commands.map_url_to_app_file import MapUrlToAppFileCommand

    test_command = MapUrlToAppFileCommand()

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    from core.commands.map_url_to_app_file import MapUrlToAppFileCommand

    test_command = MapUrlToAppFileCommand()

    with pytest.raises(TypeError):
        test_command.execute("string", None)
