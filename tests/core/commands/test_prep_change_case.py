from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.commands.prep_change_case import ChangeCasePreparePrepareCommand
from core.enums import TextCaseOptions
from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file, verify_command_result


def test_command_with_none_arguments():
    test_command = ChangeCasePreparePrepareCommand(
        capitalize=False, text_case=TextCaseOptions.TITLE_CASE
    )
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    test_command = ChangeCasePreparePrepareCommand(
        capitalize=False, text_case=TextCaseOptions.TITLE_CASE
    )

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    test_command = ChangeCasePreparePrepareCommand(
        capitalize=False, text_case=TextCaseOptions.TITLE_CASE
    )

    with pytest.raises(TypeError):
        test_command.execute("string", None)


def test_command_call_callback():
    test_command = ChangeCasePreparePrepareCommand(
        capitalize=False, text_case=TextCaseOptions.TITLE_CASE
    )

    mock_function = MagicMock()

    file1 = build_app_file("file_name_1")
    file2 = build_app_file("file_name_2", ".png")

    files = [file1, file2]

    result: list[AppFile] = test_command.execute(files, mock_function)

    assert len(result) == 2

    assert mock_function.call_count == 4
    mock_function.assert_has_calls(
        [
            mock.call(0, 2, 0),
            mock.call(0, 2, 1),
            mock.call(0, 2, 2),
            mock.call(0, 100, 0),
        ]
    )


@pytest.mark.parametrize(
    "text_case, capitalize, file_name, exp_file_name",
    [
        (TextCaseOptions.CAMEL_CASE, False, "this_is-a_file name", "thisIsAFileName"),
        (TextCaseOptions.PASCAL_CASE, False, "this_is-a_file name", "ThisIsAFileName"),
        (
                TextCaseOptions.SNAKE_CASE,
                False,
                "this_is-a_file name",
                "this_is_a_file_name",
        ),
        (
                TextCaseOptions.SNAKE_CASE_SCREAMING,
                False,
                "this_is-a_file name",
                "THIS_IS_A_FILE_NAME",
        ),
        (
                TextCaseOptions.KEBAB_CASE,
                False,
                "this_is-a_file name",
                "this-is-a-file-name",
        ),
        (
                TextCaseOptions.UPPERCASE,
                False,
                "this_is-a_file name",
                "THIS_IS-A_FILE NAME",
        ),
        (
                TextCaseOptions.LOWERCASE,
                False,
                "This_IS-A_file Name",
                "this_is-a_file name",
        ),
        (
                TextCaseOptions.TITLE_CASE,
                False,
                "this_is-a_file name",
                "This Is A File Name",
        ),
        (TextCaseOptions.LOWERCASE, True, "This_IS-A_file Name", "This_is-a_file name"),
        (TextCaseOptions.PASCAL_CASE, True, "this_is-a_file name", "ThisIsAFileName"),
    ],
)
def test_command_params_combinations(
        text_case: TextCaseOptions, capitalize: bool, file_name: str, exp_file_name: str
):
    test_command = ChangeCasePreparePrepareCommand(
        capitalize=capitalize, text_case=text_case
    )

    verify_command_result(
        test_command, file_name_origin=file_name, file_name_expected=exp_file_name
    )
