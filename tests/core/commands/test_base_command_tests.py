from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand
from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file


def command_validates_none_input(command: AppFileItemByItemListProcessingCommand):
    with pytest.raises(PassedArgumentIsNone):
        command.execute(None, None)


def command_returns_empty_array_on_empty_input(
    command: AppFileItemByItemListProcessingCommand,
):
    result = command.execute([], None)
    assert result == []


def command_validates_data_input_type(command: AppFileItemByItemListProcessingCommand):
    with pytest.raises(TypeError):
        command.execute("string", None)
    with pytest.raises(TypeError):
        command.execute(["string", "string2", "string3"], None)
    with pytest.raises(TypeError):
        command.execute([1, 2, 3], None)


def command_calls_callback_in_each_stage(
    command: AppFileItemByItemListProcessingCommand,
    number_of_calls: int = 12,
    list_of_calls: list | None = None,
):
    mock_function = MagicMock()

    file1 = build_app_file("file_name_1")
    file2 = build_app_file("file_name_2", ".png")

    files = [file1, file2]

    result: list[AppFile] = command.execute(files, mock_function)

    assert len(result) == 2

    assert mock_function.call_count == number_of_calls

    if list_of_calls is None:
        list_of_calls = [
            # pre-processing calls
            mock.call(0, 2),
            mock.call(1, 2),
            mock.call(2, 2),
            mock.call(0, 2),
            # processing calls
            mock.call(0, 2),
            mock.call(1, 2),
            mock.call(2, 2),
            mock.call(0, 2),
            # post-processing calls
            mock.call(0, 2),
            mock.call(1, 2),
            mock.call(2, 2),
            mock.call(0, 2),
        ]

    mock_function.assert_has_calls(list_of_calls)
