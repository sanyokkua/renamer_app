import pytest

import tests.core.commands.test_base_command_tests as base
from core.enums import TruncateOptions
from tests.core.commands.test_commons import verify_command_result


def test_command_with_none_arguments():
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand()

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand()

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand()

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand()

    base.command_calls_callback_in_each_stage(test_command)


@pytest.mark.parametrize(
    "option, number_of_symbols, file_name, exp_name, has_changes",
    [
        (TruncateOptions.TRUNCATE_EMPTY_SYMBOLS, 1000, "no_empty", "no_empty", False),
        (TruncateOptions.TRUNCATE_EMPTY_SYMBOLS, 100, "   no_empty", "no_empty", True),
        (
            TruncateOptions.TRUNCATE_EMPTY_SYMBOLS,
            10,
            "   no_empty    ",
            "no_empty",
            True,
        ),
        (TruncateOptions.TRUNCATE_EMPTY_SYMBOLS, 0, "no_empty    ", "no_empty", True),
        (TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 0, "no_empty", "no_empty", False),
        (TruncateOptions.REMOVE_SYMBOLS_FROM_END, 0, "no_empty", "no_empty", False),
        (TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 1, "no_empty", "o_empty", True),
        (TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 2, "no_empty", "_empty", True),
        (TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 5, "no_empty", "pty", True),
        (TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 8, "no_empty", "", True),
        (TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN, 1000, "no_empty", "", True),
        (TruncateOptions.REMOVE_SYMBOLS_FROM_END, 1, "no_empty", "no_empt", True),
        (TruncateOptions.REMOVE_SYMBOLS_FROM_END, 2, "no_empty", "no_emp", True),
        (TruncateOptions.REMOVE_SYMBOLS_FROM_END, 5, "no_empty", "no_", True),
        (TruncateOptions.REMOVE_SYMBOLS_FROM_END, 8, "no_empty", "", True),
        (TruncateOptions.REMOVE_SYMBOLS_FROM_END, 1000, "no_empty", "", True),
    ],
)
def test_command_params_combinations(option, number_of_symbols: int, file_name: str, exp_name: str, has_changes: bool):
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand(truncate_options=option, number_of_symbols=number_of_symbols)

    verify_command_result(
        test_command,
        file_name_origin=file_name,
        file_name_expected=exp_name,
        is_updated_name=has_changes,
    )
