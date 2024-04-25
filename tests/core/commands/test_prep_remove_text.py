from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.enums import ItemPosition
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file, verify_command_result


def test_command_with_none_arguments():
    from core.exceptions import PassedArgumentIsNone
    from core.commands.prep_remove_text import RemoveTextPrepareCommand
    test_command = RemoveTextPrepareCommand()
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    from core.commands.prep_remove_text import RemoveTextPrepareCommand
    test_command = RemoveTextPrepareCommand()

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_remove_text import RemoveTextPrepareCommand
    test_command = RemoveTextPrepareCommand()

    with pytest.raises(TypeError):
        test_command.execute("string", None)


def test_command_call_callback():
    from core.commands.prep_remove_text import RemoveTextPrepareCommand
    test_command = RemoveTextPrepareCommand()

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


@pytest.mark.parametrize("position, text_to_remove, file_name, exp_name, has_changes", [
    (ItemPosition.BEGIN, "TEXT_TO_REMOVE", "file_name", "file_name", False),
    (ItemPosition.BEGIN, "", "file_name", "file_name", False),
    (ItemPosition.BEGIN, "     ", "file_name", "file_name", False),
    (ItemPosition.BEGIN, "     ", "     file_name", "file_name", True),
    (ItemPosition.BEGIN, "TEXT_TO_REMOVE", "TEXT_TO_REMOVEfile_name", "file_name", True),

    (ItemPosition.END, "TEXT_TO_REMOVE", "file_name", "file_name", False),
    (ItemPosition.END, "", "file_name", "file_name", False),
    (ItemPosition.END, "     ", "file_name", "file_name", False),
    (ItemPosition.END, "     ", "file_name     ", "file_name", True),
    (ItemPosition.END, "TEXT_TO_REMOVE", "file_nameTEXT_TO_REMOVE", "file_name", True),

])
def test_extension_change_commands(position: ItemPosition, text_to_remove: str, file_name: str, exp_name: str,
                                   has_changes: bool):
    from core.commands.prep_remove_text import RemoveTextPrepareCommand
    test_command = RemoveTextPrepareCommand(position=position, text=text_to_remove)

    verify_command_result(test_command, file_name_origin=file_name, file_name_expected=exp_name,
                          is_updated_name=has_changes)
