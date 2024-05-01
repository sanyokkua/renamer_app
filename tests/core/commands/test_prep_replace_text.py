import pytest

import tests.core.commands.test_base_command_tests as base
from core.enums import ItemPositionExtended
from tests.core.commands.test_commons import verify_command_result


def test_command_with_none_arguments():
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand

    test_command = ReplaceTextPrepareCommand()

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand

    test_command = ReplaceTextPrepareCommand()

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand

    test_command = ReplaceTextPrepareCommand()

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand

    test_command = ReplaceTextPrepareCommand()

    base.command_calls_callback_in_each_stage(test_command)


@pytest.mark.parametrize(
    "position, text_to_replace, new_value, file_name, exp_name, has_changes",
    [
        (
            ItemPositionExtended.BEGIN,
            "text_to_replace",
            "new_value",
            "file_name",
            "file_name",
            False,
        ),
        (ItemPositionExtended.BEGIN, "", "new_value", "file_name", "file_name", False),
        (ItemPositionExtended.BEGIN, "", "", "file_name", "file_name", False),
        (
            ItemPositionExtended.END,
            "text_to_replace",
            "new_value",
            "file_name",
            "file_name",
            False,
        ),
        (ItemPositionExtended.END, "", "new_value", "file_name", "file_name", False),
        (ItemPositionExtended.END, "", "", "file_name", "file_name", False),
        (
            ItemPositionExtended.EVERYWHERE,
            "text_to_replace",
            "new_value",
            "file_name",
            "file_name",
            False,
        ),
        (
            ItemPositionExtended.EVERYWHERE,
            "",
            "new_value",
            "file_name",
            "file_name",
            False,
        ),
        (ItemPositionExtended.EVERYWHERE, "", "", "file_name", "file_name", False),
        (
            ItemPositionExtended.BEGIN,
            "to_replace",
            "new_value",
            "to_replace_file_name",
            "new_value_file_name",
            True,
        ),
        (
            ItemPositionExtended.END,
            "to_replace",
            "new_value",
            "file_name_to_replace",
            "file_name_new_value",
            True,
        ),
        (
            ItemPositionExtended.BEGIN,
            "AVA",
            "TT",
            "AVA_fileAVA_nameAVA",
            "TT_fileAVA_nameAVA",
            True,
        ),
        (
            ItemPositionExtended.END,
            "AVA",
            "TT",
            "AVA_fileAVA_nameAVA",
            "AVA_fileAVA_nameTT",
            True,
        ),
        (
            ItemPositionExtended.EVERYWHERE,
            "AVA",
            "TT",
            "AVA_fileAVA_nameAVA",
            "TT_fileTT_nameTT",
            True,
        ),
        (
            ItemPositionExtended.EVERYWHERE,
            "AVA",
            "",
            "AVA_fileAVA_nameAVA",
            "_file_name",
            True,
        ),
    ],
)
def test_command_params_combinations(
    position: ItemPositionExtended,
    text_to_replace,
    new_value: str,
    file_name: str,
    exp_name: str,
    has_changes: bool,
):
    from core.commands.prep_replace_text import ReplaceTextPrepareCommand

    test_command = ReplaceTextPrepareCommand(position=position, text_to_replace=text_to_replace, new_value=new_value)

    verify_command_result(
        test_command,
        file_name_origin=file_name,
        file_name_expected=exp_name,
        is_updated_name=has_changes,
    )
