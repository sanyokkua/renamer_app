from datetime import datetime

import tests.core.commands.test_base_command_tests as base
from core.enums import SortSource
from core.models.app_file import AppFile
from tests.core.commands.test_commons import (
    build_app_file,
    check_that_only_new_name_changed,
)


def test_command_with_none_arguments():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand()

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand()

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand()

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand()

    base.command_calls_callback_in_each_stage(test_command)


def test_command_sort_source_file_name():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_NAME, start_number=0, step_value=1, padding=1)

    app_file_1: AppFile = build_app_file("file_1")
    app_file_2: AppFile = build_app_file("file_2")
    app_file_3: AppFile = build_app_file("file_3")
    app_file_5: AppFile = build_app_file("file_5")
    app_file_4: AppFile = build_app_file("file_4")

    files_list_random: list[AppFile] = [
        app_file_5,
        app_file_3,
        app_file_1,
        app_file_4,
        app_file_2,
    ]

    result = test_command.execute(files_list_random)
    assert result[0] == app_file_1
    assert result[1] == app_file_2
    assert result[2] == app_file_3
    assert result[3] == app_file_4
    assert result[4] == app_file_5

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_2, result[1])
    check_that_only_new_name_changed(app_file_3, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_2.next_name == "1"
    assert app_file_3.next_name == "2"
    assert app_file_4.next_name == "3"
    assert app_file_5.next_name == "4"


def test_command_sort_source_file_name_diff_start_number():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_NAME, start_number=5, step_value=1, padding=1)

    app_file_1: AppFile = build_app_file("file_1")
    app_file_2: AppFile = build_app_file("file_2")
    app_file_3: AppFile = build_app_file("file_3")
    app_file_5: AppFile = build_app_file("file_5")
    app_file_4: AppFile = build_app_file("file_4")

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_2, result[1])
    check_that_only_new_name_changed(app_file_3, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "5"
    assert app_file_2.next_name == "6"
    assert app_file_3.next_name == "7"
    assert app_file_4.next_name == "8"
    assert app_file_5.next_name == "9"


def test_command_sort_source_file_name_diff_step_value():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_NAME, start_number=0, step_value=2, padding=1)

    app_file_1: AppFile = build_app_file("file_1")
    app_file_2: AppFile = build_app_file("file_2")
    app_file_3: AppFile = build_app_file("file_3")
    app_file_4: AppFile = build_app_file("file_4")
    app_file_5: AppFile = build_app_file("file_5")

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_2, result[1])
    check_that_only_new_name_changed(app_file_3, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_2.next_name == "2"
    assert app_file_3.next_name == "4"
    assert app_file_4.next_name == "6"
    assert app_file_5.next_name == "8"


def test_command_sort_source_file_name_diff_padding_1():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_SIZE, start_number=1, step_value=1, padding=1)

    app_file_1: AppFile = build_app_file("file_1", file_size=1000)
    app_file_2: AppFile = build_app_file("file_2", file_size=1001)
    app_file_3: AppFile = build_app_file("file_3", file_size=1002)
    app_file_4: AppFile = build_app_file("file_4", file_size=1003)
    app_file_5: AppFile = build_app_file("file_5", file_size=1004)
    app_file_6: AppFile = build_app_file("file_6", file_size=1005)
    app_file_7: AppFile = build_app_file("file_7", file_size=1006)
    app_file_8: AppFile = build_app_file("file_8", file_size=1007)
    app_file_9: AppFile = build_app_file("file_9", file_size=1008)
    app_file_10: AppFile = build_app_file("file_10", file_size=1009)
    app_file_11: AppFile = build_app_file("file_11", file_size=1010)

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
        app_file_6,
        app_file_7,
        app_file_8,
        app_file_9,
        app_file_10,
        app_file_11,
    ]

    result = test_command.execute(files_list_random)

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_2, result[1])
    check_that_only_new_name_changed(app_file_3, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])
    check_that_only_new_name_changed(app_file_6, result[5])
    check_that_only_new_name_changed(app_file_7, result[6])
    check_that_only_new_name_changed(app_file_8, result[7])
    check_that_only_new_name_changed(app_file_9, result[8])
    check_that_only_new_name_changed(app_file_10, result[9])
    check_that_only_new_name_changed(app_file_11, result[10])

    assert app_file_1.next_name == "1"
    assert app_file_2.next_name == "2"
    assert app_file_3.next_name == "3"
    assert app_file_4.next_name == "4"
    assert app_file_5.next_name == "5"
    assert app_file_6.next_name == "6"
    assert app_file_7.next_name == "7"
    assert app_file_8.next_name == "8"
    assert app_file_9.next_name == "9"
    assert app_file_10.next_name == "10"
    assert app_file_11.next_name == "11"


def test_command_sort_source_file_name_diff_padding_2():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_SIZE, start_number=1, step_value=1, padding=2)

    app_file_1: AppFile = build_app_file("file_1", file_size=1000)
    app_file_2: AppFile = build_app_file("file_2", file_size=1001)
    app_file_3: AppFile = build_app_file("file_3", file_size=1002)
    app_file_4: AppFile = build_app_file("file_4", file_size=1003)
    app_file_5: AppFile = build_app_file("file_5", file_size=1004)
    app_file_6: AppFile = build_app_file("file_6", file_size=1005)
    app_file_7: AppFile = build_app_file("file_7", file_size=1006)
    app_file_8: AppFile = build_app_file("file_8", file_size=1007)
    app_file_9: AppFile = build_app_file("file_9", file_size=1008)
    app_file_10: AppFile = build_app_file("file_10", file_size=1009)
    app_file_11: AppFile = build_app_file("file_11", file_size=1010)

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
        app_file_6,
        app_file_7,
        app_file_8,
        app_file_9,
        app_file_10,
        app_file_11,
    ]

    result = test_command.execute(files_list_random)

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_2, result[1])
    check_that_only_new_name_changed(app_file_3, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])
    check_that_only_new_name_changed(app_file_6, result[5])
    check_that_only_new_name_changed(app_file_7, result[6])
    check_that_only_new_name_changed(app_file_8, result[7])
    check_that_only_new_name_changed(app_file_9, result[8])
    check_that_only_new_name_changed(app_file_10, result[9])
    check_that_only_new_name_changed(app_file_11, result[10])

    assert app_file_1.next_name == "01"
    assert app_file_2.next_name == "02"
    assert app_file_3.next_name == "03"
    assert app_file_4.next_name == "04"
    assert app_file_5.next_name == "05"
    assert app_file_6.next_name == "06"
    assert app_file_7.next_name == "07"
    assert app_file_8.next_name == "08"
    assert app_file_9.next_name == "09"
    assert app_file_10.next_name == "10"
    assert app_file_11.next_name == "11"


def test_command_sort_source_file_name_diff_padding_4():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_SIZE, start_number=1, step_value=1, padding=4)

    app_file_1: AppFile = build_app_file("file_1", file_size=1000)
    app_file_2: AppFile = build_app_file("file_2", file_size=1001)
    app_file_3: AppFile = build_app_file("file_3", file_size=1002)
    app_file_4: AppFile = build_app_file("file_4", file_size=1003)
    app_file_5: AppFile = build_app_file("file_5", file_size=1004)
    app_file_6: AppFile = build_app_file("file_6", file_size=1005)
    app_file_7: AppFile = build_app_file("file_7", file_size=1006)
    app_file_8: AppFile = build_app_file("file_8", file_size=1007)
    app_file_9: AppFile = build_app_file("file_9", file_size=1008)
    app_file_10: AppFile = build_app_file("file_10", file_size=1009)
    app_file_11: AppFile = build_app_file("file_11", file_size=1010)

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
        app_file_6,
        app_file_7,
        app_file_8,
        app_file_9,
        app_file_10,
        app_file_11,
    ]

    result = test_command.execute(files_list_random)

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_2, result[1])
    check_that_only_new_name_changed(app_file_3, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])
    check_that_only_new_name_changed(app_file_6, result[5])
    check_that_only_new_name_changed(app_file_7, result[6])
    check_that_only_new_name_changed(app_file_8, result[7])
    check_that_only_new_name_changed(app_file_9, result[8])
    check_that_only_new_name_changed(app_file_10, result[9])
    check_that_only_new_name_changed(app_file_11, result[10])

    assert app_file_1.next_name == "0001"
    assert app_file_2.next_name == "0002"
    assert app_file_3.next_name == "0003"
    assert app_file_4.next_name == "0004"
    assert app_file_5.next_name == "0005"
    assert app_file_6.next_name == "0006"
    assert app_file_7.next_name == "0007"
    assert app_file_8.next_name == "0008"
    assert app_file_9.next_name == "0009"
    assert app_file_10.next_name == "0010"
    assert app_file_11.next_name == "0011"


def test_command_sort_source_file_path():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_PATH)

    app_file_1: AppFile = build_app_file(file_name="file_1", absolute_path="/photos/file_1.jpg")
    app_file_3: AppFile = build_app_file(file_name="file_3", absolute_path="/user/file_3.jpg")
    app_file_2: AppFile = build_app_file(file_name="file_2", absolute_path="/user/photos/file_2.jpg")
    app_file_4: AppFile = build_app_file(file_name="file_4", absolute_path="/user/photos/file_4.jpg")
    app_file_5: AppFile = build_app_file(file_name="file_5", absolute_path="/user/photos/file_5.jpg")

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)
    assert result[0] == app_file_1
    assert result[1] == app_file_3
    assert result[2] == app_file_2
    assert result[3] == app_file_4
    assert result[4] == app_file_5

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_3, result[1])
    check_that_only_new_name_changed(app_file_2, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_3.next_name == "1"
    assert app_file_2.next_name == "2"
    assert app_file_4.next_name == "3"
    assert app_file_5.next_name == "4"


def test_command_sort_source_file_size():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_SIZE)

    app_file_1: AppFile = build_app_file(file_name="file_1", file_size=1000)
    app_file_3: AppFile = build_app_file(file_name="file_3", file_size=1001)
    app_file_2: AppFile = build_app_file(file_name="file_2", file_size=1002)
    app_file_4: AppFile = build_app_file(file_name="file_4", file_size=1003)
    app_file_5: AppFile = build_app_file(file_name="file_5", file_size=1004)

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)
    assert result[0] == app_file_1
    assert result[1] == app_file_3
    assert result[2] == app_file_2
    assert result[3] == app_file_4
    assert result[4] == app_file_5

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_3, result[1])
    check_that_only_new_name_changed(app_file_2, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_3.next_name == "1"
    assert app_file_2.next_name == "2"
    assert app_file_4.next_name == "3"
    assert app_file_5.next_name == "4"


def test_command_sort_source_file_creation_datetime():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_CREATION_DATETIME)
    now: datetime = datetime.now()
    now_timestamp: float = now.timestamp()

    app_file_1: AppFile = build_app_file(file_name="file_1", cr_timestamp=None)
    app_file_3: AppFile = build_app_file(file_name="file_3", cr_timestamp=(now_timestamp + 10))
    app_file_2: AppFile = build_app_file(file_name="file_2", cr_timestamp=(now_timestamp + 15))
    app_file_4: AppFile = build_app_file(file_name="file_4", cr_timestamp=(now_timestamp + 20))
    app_file_5: AppFile = build_app_file(file_name="file_5", cr_timestamp=(now_timestamp + 25))

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)
    assert result[0] == app_file_1
    assert result[1] == app_file_3
    assert result[2] == app_file_2
    assert result[3] == app_file_4
    assert result[4] == app_file_5

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_3, result[1])
    check_that_only_new_name_changed(app_file_2, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_3.next_name == "1"
    assert app_file_2.next_name == "2"
    assert app_file_4.next_name == "3"
    assert app_file_5.next_name == "4"


def test_command_sort_source_file_modification_datetime():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_MODIFICATION_DATETIME)
    now: datetime = datetime.now()
    now_timestamp: float = now.timestamp()

    app_file_1: AppFile = build_app_file(file_name="file_1", mod_timestamp=None)
    app_file_3: AppFile = build_app_file(file_name="file_3", mod_timestamp=(now_timestamp + 10))
    app_file_2: AppFile = build_app_file(file_name="file_2", mod_timestamp=(now_timestamp + 15))
    app_file_4: AppFile = build_app_file(file_name="file_4", mod_timestamp=(now_timestamp + 20))
    app_file_5: AppFile = build_app_file(file_name="file_5", mod_timestamp=(now_timestamp + 25))

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)
    assert result[0] == app_file_1
    assert result[1] == app_file_3
    assert result[2] == app_file_2
    assert result[3] == app_file_4
    assert result[4] == app_file_5

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_3, result[1])
    check_that_only_new_name_changed(app_file_2, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_3.next_name == "1"
    assert app_file_2.next_name == "2"
    assert app_file_4.next_name == "3"
    assert app_file_5.next_name == "4"


def test_command_sort_source_content_creation_datetime():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.FILE_CONTENT_CREATION_DATETIME)
    now: datetime = datetime.now()
    now_timestamp: float = now.timestamp()

    app_file_1: AppFile = build_app_file(file_name="file_1", cc_timestamp=None)
    app_file_3: AppFile = build_app_file(file_name="file_3", cc_timestamp=(now_timestamp + 10))
    app_file_2: AppFile = build_app_file(file_name="file_2", cc_timestamp=(now_timestamp + 15))
    app_file_4: AppFile = build_app_file(file_name="file_4", cc_timestamp=(now_timestamp + 20))
    app_file_5: AppFile = build_app_file(file_name="file_5", cc_timestamp=(now_timestamp + 25))

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)
    assert result[0] == app_file_1
    assert result[1] == app_file_3
    assert result[2] == app_file_2
    assert result[3] == app_file_4
    assert result[4] == app_file_5

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_3, result[1])
    check_that_only_new_name_changed(app_file_2, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_3.next_name == "1"
    assert app_file_2.next_name == "2"
    assert app_file_4.next_name == "3"
    assert app_file_5.next_name == "4"


def test_command_sort_source_img_width():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.IMAGE_WIDTH)

    app_file_1: AppFile = build_app_file(file_name="file_1", width=None)
    app_file_3: AppFile = build_app_file(file_name="file_3", width=1001)
    app_file_2: AppFile = build_app_file(file_name="file_2", width=1002)
    app_file_4: AppFile = build_app_file(file_name="file_4", width=1003)
    app_file_5: AppFile = build_app_file(file_name="file_5", width=1004)

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)
    assert result[0] == app_file_1
    assert result[1] == app_file_3
    assert result[2] == app_file_2
    assert result[3] == app_file_4
    assert result[4] == app_file_5

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_3, result[1])
    check_that_only_new_name_changed(app_file_2, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_3.next_name == "1"
    assert app_file_2.next_name == "2"
    assert app_file_4.next_name == "3"
    assert app_file_5.next_name == "4"


def test_command_sort_source_img_height():
    from core.commands.prep_seq_gen import SequencePrepareCommand

    test_command = SequencePrepareCommand(sort_source=SortSource.IMAGE_HEIGHT)

    app_file_1: AppFile = build_app_file(file_name="file_1", height=None)
    app_file_3: AppFile = build_app_file(file_name="file_3", height=1001)
    app_file_2: AppFile = build_app_file(file_name="file_2", height=1002)
    app_file_4: AppFile = build_app_file(file_name="file_4", height=1003)
    app_file_5: AppFile = build_app_file(file_name="file_5", height=1004)

    files_list_random: list[AppFile] = [
        app_file_1,
        app_file_2,
        app_file_3,
        app_file_4,
        app_file_5,
    ]

    result = test_command.execute(files_list_random)
    assert result[0] == app_file_1
    assert result[1] == app_file_3
    assert result[2] == app_file_2
    assert result[3] == app_file_4
    assert result[4] == app_file_5

    check_that_only_new_name_changed(app_file_1, result[0])
    check_that_only_new_name_changed(app_file_3, result[1])
    check_that_only_new_name_changed(app_file_2, result[2])
    check_that_only_new_name_changed(app_file_4, result[3])
    check_that_only_new_name_changed(app_file_5, result[4])

    assert app_file_1.next_name == "0"
    assert app_file_3.next_name == "1"
    assert app_file_2.next_name == "2"
    assert app_file_4.next_name == "3"
    assert app_file_5.next_name == "4"
