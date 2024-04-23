from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file


def test_command_with_none_arguments():
    from core.exceptions import PassedArgumentIsNone
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand
    test_command = ReplaceTextPrepareCommand()
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand
    test_command = ReplaceTextPrepareCommand()

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand
    test_command = ReplaceTextPrepareCommand()

    with pytest.raises(TypeError):
        test_command.execute("string", None)


def test_command_call_callback():
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand
    test_command = ReplaceTextPrepareCommand()

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
