import pytest

import tests.core.commands.test_base_command_tests as base
from core.commands.prep_ext_change import ExtensionChangePrepareCommand
from tests.core.commands.test_commons import check_extension_after_command_applied


def test_command_with_none_arguments():
    from core.commands.prep_ext_change import ExtensionChangePrepareCommand

    test_command = ExtensionChangePrepareCommand(new_extension="")

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.prep_ext_change import ExtensionChangePrepareCommand

    test_command = ExtensionChangePrepareCommand(new_extension="")

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_ext_change import ExtensionChangePrepareCommand

    test_command = ExtensionChangePrepareCommand(new_extension="")

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.prep_ext_change import ExtensionChangePrepareCommand

    test_command = ExtensionChangePrepareCommand(new_extension="")

    base.command_calls_callback_in_each_stage(test_command)


@pytest.mark.parametrize(
    "user_ext, file_ext_orig, file_ext_new",
    [
        (".new_ext", ".jpg", ".new_ext"),
        (".new_ext", ".png", ".new_ext"),
        (".new_ext", "", ".new_ext"),
        ("", ".jpg", ""),
        ("", ".png", ""),
        ("", "", ""),
        ("ext", ".jpg", ".ext"),
        ("ext", ".png", ".ext"),
        ("ext", "", ".ext"),
    ],
)
def test_command_params_combinations(user_ext: str, file_ext_orig: str, file_ext_new: str):
    test_command = ExtensionChangePrepareCommand(new_extension=user_ext)
    check_extension_after_command_applied(test_command, file_ext_orig, file_ext_new)
