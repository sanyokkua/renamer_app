from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.enums import TruncateOptions
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file, verify_command_result


def test_command_with_none_arguments():
    from core.exceptions import PassedArgumentIsNone
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand()
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand()

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand()

    with pytest.raises(TypeError):
        test_command.execute("string", None)


def test_command_call_callback():
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand()

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
def test_command_params_combinations(
    option, number_of_symbols: int, file_name: str, exp_name: str, has_changes: bool
):
    from core.commands.prep_truncate_text import TruncateNamePrepareCommand

    test_command = TruncateNamePrepareCommand(
        truncate_options=option, number_of_symbols=number_of_symbols
    )

    verify_command_result(
        test_command,
        file_name_origin=file_name,
        file_name_expected=exp_name,
        is_updated_name=has_changes,
    )
