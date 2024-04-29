from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.enums import ItemPosition
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file, verify_command_result


def test_command_with_none_arguments():
    from core.exceptions import PassedArgumentIsNone
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand()
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand()

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand()

    with pytest.raises(TypeError):
        test_command.execute("string", None)


def test_command_call_callback():
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand()

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
    "position, number, separator, file_name, file_path, exp_name, should_be_updated",
    [
        (
                ItemPosition.BEGIN,
                0,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app",
                False,
        ),
        (
                ItemPosition.BEGIN,
                1,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                2,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "sources_config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                3,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "projects_sources_config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                4,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "home_projects_sources_config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                5,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "user_home_projects_sources_config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                6,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "root_user_home_projects_sources_config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                7,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "root_user_home_projects_sources_config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                8,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "root_user_home_projects_sources_config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                10000,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "root_user_home_projects_sources_config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                -1,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "config_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                -2,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "sources_config_app",
                True,
        ),
        (
                ItemPosition.END,
                0,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app",
                False,
        ),
        (
                ItemPosition.END,
                1,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_config",
                True,
        ),
        (
                ItemPosition.END,
                2,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_sources_config",
                True,
        ),
        (
                ItemPosition.END,
                3,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_projects_sources_config",
                True,
        ),
        (
                ItemPosition.END,
                4,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_home_projects_sources_config",
                True,
        ),
        (
                ItemPosition.END,
                5,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_user_home_projects_sources_config",
                True,
        ),
        (
                ItemPosition.END,
                6,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_root_user_home_projects_sources_config",
                True,
        ),
        (
                ItemPosition.END,
                7,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_root_user_home_projects_sources_config",
                True,
        ),
        (
                ItemPosition.END,
                8,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_root_user_home_projects_sources_config",
                True,
        ),
        (
                ItemPosition.END,
                10000,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_root_user_home_projects_sources_config",
                True,
        ),
        (
                ItemPosition.END,
                -1,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_config",
                True,
        ),
        (
                ItemPosition.END,
                -2,
                "_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "app_sources_config",
                True,
        ),
        (
                ItemPosition.BEGIN,
                2,
                " ",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "sources config app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                2,
                "_separator_",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "sources_separator_config_separator_app",
                True,
        ),
        (
                ItemPosition.BEGIN,
                2,
                "",
                "app",
                "/root/user/home/projects/sources/config/app.json",
                "sourcesconfigapp",
                True,
        ),
    ],
)
def test_command_params_combinations(
        position: ItemPosition,
        number: int,
        separator: str,
        file_name: str,
        file_path: str,
        exp_name: str,
        should_be_updated: bool,
):
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand(position, number, separator)

    verify_command_result(
        test_command,
        absolute_path=file_path,
        file_name_origin=file_name,
        file_name_expected=exp_name,
        file_ext=".json",
        is_updated_name=should_be_updated,
    )
