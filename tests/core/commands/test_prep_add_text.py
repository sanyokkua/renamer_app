import tests.core.commands.test_base_command_tests as base
from core.enums import ItemPosition
from core.models.app_file import AppFile
from tests.core.commands.test_commons import (
    build_app_file,
    check_that_only_new_name_changed,
)


def test_command_with_none_arguments():
    from core.commands.prep_add_text import AddTextPrepareCommand

    test_command = AddTextPrepareCommand(text="", position=ItemPosition.BEGIN)

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.prep_add_text import AddTextPrepareCommand

    test_command = AddTextPrepareCommand(text="", position=ItemPosition.BEGIN)

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_add_text import AddTextPrepareCommand

    test_command = AddTextPrepareCommand(text="", position=ItemPosition.BEGIN)

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.prep_add_text import AddTextPrepareCommand

    test_command = AddTextPrepareCommand(text="", position=ItemPosition.BEGIN)

    base.command_calls_callback_in_each_stage(test_command)


def test_command_add_text_in_begin():
    from core.commands.prep_add_text import AddTextPrepareCommand

    text_to_add = "TEST_TEXT"
    test_command = AddTextPrepareCommand(text=text_to_add, position=ItemPosition.BEGIN)

    file1 = build_app_file("file_name_1")
    file2 = build_app_file("file_name_2", ".png")

    files = [file1, file2]

    result: list[AppFile] = test_command.execute(files)

    assert len(result) == 2

    res_file1: AppFile = result[0]
    res_file2: AppFile = result[1]
    check_that_only_new_name_changed(file1, res_file1)
    assert res_file1.next_name == f"{text_to_add}{file1.file_name}"

    check_that_only_new_name_changed(file2, res_file2)
    assert res_file2.next_name == f"{text_to_add}{file2.file_name}"


def test_command_add_text_in_end():
    from core.commands.prep_add_text import AddTextPrepareCommand

    text_to_add = "TEST_TEXT_2"
    test_command = AddTextPrepareCommand(text=text_to_add, position=ItemPosition.END)

    file1 = build_app_file("file_name_1")
    file2 = build_app_file("file_name_2", ".png")

    files = [file1, file2]

    result: list[AppFile] = test_command.execute(files)

    assert len(result) == 2

    res_file1: AppFile = result[0]
    res_file2: AppFile = result[1]
    check_that_only_new_name_changed(file1, res_file1)
    assert res_file1.next_name == f"{file1.file_name}{text_to_add}"

    check_that_only_new_name_changed(file2, res_file2)
    assert res_file2.next_name == f"{file2.file_name}{text_to_add}"
