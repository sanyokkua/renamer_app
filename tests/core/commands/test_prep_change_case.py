import pytest

import tests.core.commands.test_base_command_tests as base
from core.commands.prep_change_case import ChangeCasePreparePrepareCommand
from core.enums import TextCaseOptions
from tests.core.commands.test_commons import verify_command_result


def test_command_with_none_arguments():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    test_command = ChangeCasePreparePrepareCommand(capitalize=False, text_case=TextCaseOptions.TITLE_CASE)

    base.command_calls_callback_in_each_stage(test_command)


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
def test_command_params_combinations(text_case: TextCaseOptions, capitalize: bool, file_name: str, exp_file_name: str):
    test_command = ChangeCasePreparePrepareCommand(capitalize=capitalize, text_case=text_case)

    verify_command_result(test_command, file_name_origin=file_name, file_name_expected=exp_file_name)
