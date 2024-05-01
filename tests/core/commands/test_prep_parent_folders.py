import pytest

import tests.core.commands.test_base_command_tests as base
from core.enums import ItemPosition
from tests.core.commands.test_commons import verify_command_result


def test_command_with_none_arguments():
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand()

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand()

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand()

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.prep_parent_folders import ParentFoldersPrepareCommand

    test_command = ParentFoldersPrepareCommand()

    base.command_calls_callback_in_each_stage(test_command)


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
