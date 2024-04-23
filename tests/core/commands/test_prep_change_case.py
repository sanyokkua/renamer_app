from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.commands.prep_change_case import ChangeCasePreparePrepareCommand
from core.enums import TextCaseOptions
from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file, \
    check_name_after_command_applied


def test_command_with_none_arguments():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)

    with pytest.raises(TypeError):
        test_command.execute("string", None)


def test_command_call_callback():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)

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


def test_command_change_to_camel_case():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.CAMEL_CASE)

    file_name_origin: str = "this_is-a_file name"
    file_name_expected: str = "thisIsAFileName"
    check_name_after_command_applied(test_command, file_name_origin, file_name_expected)


def test_command_change_to_pascal_case():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.PASCAL_CASE)

    file_name_origin: str = "this_is-a_file name"
    file_name_expected: str = "ThisIsAFileName"
    check_name_after_command_applied(test_command, file_name_origin, file_name_expected)


def test_command_change_to_snake_case():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.SNAKE_CASE)

    file_name_origin: str = "this_is-a_file name"
    file_name_expected: str = "this_is_a_file_name"
    check_name_after_command_applied(test_command, file_name_origin, file_name_expected)


def test_command_change_to_snake_screaming_case():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.SNAKE_CASE_SCREAMING)

    file_name_origin: str = "this_is-a_file name"
    file_name_expected: str = "THIS_IS_A_FILE_NAME"
    check_name_after_command_applied(test_command, file_name_origin, file_name_expected)


def test_command_change_to_kebab_case():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.KEBAB_CASE)

    file_name_origin: str = "this_is-a_file name"
    file_name_expected: str = "this-is-a-file-name"
    check_name_after_command_applied(test_command, file_name_origin, file_name_expected)


def test_command_change_to_upper_case():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.UPPERCASE)

    file_name_origin: str = "this_is-a_file name"
    file_name_expected: str = "THIS_IS-A_FILE NAME"
    check_name_after_command_applied(test_command, file_name_origin, file_name_expected)


def test_command_change_to_lower_case():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.LOWERCASE)

    file_name_origin: str = "This_IS-A_file Name"
    file_name_expected: str = "this_is-a_file name"
    check_name_after_command_applied(test_command, file_name_origin, file_name_expected)


def test_command_change_to_title_case():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)

    file_name_origin: str = "this_is-a_file name"
    file_name_expected: str = "This Is A File Name"
    check_name_after_command_applied(test_command, file_name_origin, file_name_expected)
