import pytest

import tests.core.commands.test_base_command_tests as base
from core.enums import ItemPosition
from tests.core.commands.test_commons import verify_command_result


def test_command_with_none_arguments():
    from core.commands.prep_remove_text import RemoveTextPrepareCommand

    test_command = RemoveTextPrepareCommand()

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.prep_remove_text import RemoveTextPrepareCommand

    test_command = RemoveTextPrepareCommand()

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_remove_text import RemoveTextPrepareCommand

    test_command = RemoveTextPrepareCommand()

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.prep_remove_text import RemoveTextPrepareCommand

    test_command = RemoveTextPrepareCommand()

    base.command_calls_callback_in_each_stage(test_command)


@pytest.mark.parametrize(
    "position, text_to_remove, file_name, exp_name, has_changes",
    [
        (ItemPosition.BEGIN, "TEXT_TO_REMOVE", "file_name", "file_name", False),
        (ItemPosition.BEGIN, "", "file_name", "file_name", False),
        (ItemPosition.BEGIN, "     ", "file_name", "file_name", False),
        (ItemPosition.BEGIN, "     ", "     file_name", "file_name", True),
        (
            ItemPosition.BEGIN,
            "TEXT_TO_REMOVE",
            "TEXT_TO_REMOVEfile_name",
            "file_name",
            True,
        ),
        (ItemPosition.END, "TEXT_TO_REMOVE", "file_name", "file_name", False),
        (ItemPosition.END, "", "file_name", "file_name", False),
        (ItemPosition.END, "     ", "file_name", "file_name", False),
        (ItemPosition.END, "     ", "file_name     ", "file_name", True),
        (
            ItemPosition.END,
            "TEXT_TO_REMOVE",
            "file_nameTEXT_TO_REMOVE",
            "file_name",
            True,
        ),
    ],
)
def test_command_params_combinations(
    position: ItemPosition,
    text_to_remove: str,
    file_name: str,
    exp_name: str,
    has_changes: bool,
):
    from core.commands.prep_remove_text import RemoveTextPrepareCommand

    test_command = RemoveTextPrepareCommand(position=position, text=text_to_remove)

    verify_command_result(
        test_command,
        file_name_origin=file_name,
        file_name_expected=exp_name,
        is_updated_name=has_changes,
    )
