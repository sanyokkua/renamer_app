from unittest import mock

import tests.core.commands.test_base_command_tests as base
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file


def test_command_with_none_arguments():
    from core.commands.fix_same_names import FixSameNamesCommand

    test_command = FixSameNamesCommand()

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.fix_same_names import FixSameNamesCommand

    test_command = FixSameNamesCommand()

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.fix_same_names import FixSameNamesCommand

    test_command = FixSameNamesCommand()

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.fix_same_names import FixSameNamesCommand

    test_command = FixSameNamesCommand()

    base.command_calls_callback_in_each_stage(
        test_command,
        number_of_calls=14,
        list_of_calls=[
            mock.call(0, 2),
            mock.call(1, 2),
            mock.call(2, 2),
            mock.call(0, 2),
            mock.call(0, 4),
            mock.call(0, 4),
            mock.call(1, 4),
            mock.call(2, 4),
            mock.call(3, 4),
            mock.call(0, 100),
            mock.call(0, 2),
            mock.call(1, 2),
            mock.call(2, 2),
            mock.call(0, 2),
        ],
    )


def test_command_will_not_change_unique_names():
    from core.commands.fix_same_names import FixSameNamesCommand

    test_command = FixSameNamesCommand()

    file1 = build_app_file("file_name_1")
    file2 = build_app_file("file_name_2")
    file3 = build_app_file("file_name_3")

    list_of_files = [file1, file2, file3]

    result: list[AppFile] = test_command.execute(list_of_files)

    assert len(list_of_files) == len(result)
    assert result[0].next_name == "file_name_1"
    assert result[1].next_name == "file_name_2"
    assert result[2].next_name == "file_name_3"


def test_command_will_change_non_unique_names():
    from core.commands.fix_same_names import FixSameNamesCommand

    test_command = FixSameNamesCommand()

    file1 = build_app_file("file_name_1")
    file2 = build_app_file("file_name_2")
    file3 = build_app_file("file_name_3")
    file4 = build_app_file("file_name_4")
    file5 = build_app_file("file_name_5")
    file6 = build_app_file("next_name")

    file2.next_name = "file_name_1"

    file4.next_name = "next_name"
    file4.file_extension_new = ".new"

    file5.next_name = "next_name"
    file5.file_extension_new = ".new"

    list_of_files = [file1, file2, file3, file4, file5, file6]

    result: list[AppFile] = test_command.execute(list_of_files)

    unique_names: set[str] = set()
    for item in result:
        name = f"{item.next_name}{item.file_extension_new}"
        unique_names.add(name)
        print(name)

    assert len(list_of_files) == len(result)
    assert len(list_of_files) == len(unique_names)
    # File1 file shouldn't be changed
    assert result[0].file_name == "file_name_1"
    assert result[0].next_name == "file_name_1"
    assert result[0].file_extension == ".jpg"
    assert result[0].file_extension_new == ".jpg"
    # File2
    assert result[1].file_name == "file_name_2"
    assert result[1].next_name == "file_name_1 (1)"
    assert result[1].file_extension == ".jpg"
    assert result[1].file_extension_new == ".jpg"
    # File3
    assert result[2].file_name == "file_name_3"
    assert result[2].next_name == "file_name_3"
    assert result[2].file_extension == ".jpg"
    assert result[2].file_extension_new == ".jpg"
    # File4
    assert result[3].file_name == "file_name_4"
    assert result[3].next_name == "next_name (1)"
    assert result[3].file_extension == ".jpg"
    assert result[3].file_extension_new == ".new"
    # File5
    assert result[4].file_name == "file_name_5"
    assert result[4].next_name == "next_name (2)"
    assert result[4].file_extension == ".jpg"
    assert result[4].file_extension_new == ".new"
    # File6 file shouldn't be changed
    assert result[5].file_name == "next_name"
    assert result[5].next_name == "next_name"
    assert result[5].file_extension == ".jpg"
    assert result[5].file_extension_new == ".jpg"
