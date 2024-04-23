from core.commons import PrepareCommand
from core.models.app_file import AppFile


def build_app_file(file_name: str, file_ext: str = ".jpg") -> AppFile:
    return AppFile(
        _absolute_path=f"absolute/file/path/{file_name}",
        _is_folder=False,
        _file_extension=file_ext,
        _file_name=file_name,
        _file_size=1000,
        _next_name=file_name,
        _fs_creation_date=10000,
        _fs_modification_date=10000,
        _metadata=None,
    )


def check_that_only_new_name_changed(original_file: AppFile, updated_file: AppFile):
    assert updated_file.absolute_path == original_file.absolute_path
    assert updated_file.file_extension == original_file.file_extension
    assert updated_file.file_name == original_file.file_name
    assert updated_file.file_size == original_file.file_size
    assert updated_file.fs_creation_date == original_file.fs_creation_date
    assert updated_file.fs_modification_date == original_file.fs_modification_date
    assert updated_file.is_folder == original_file.is_folder
    assert updated_file.is_name_changed
    assert updated_file.metadata == original_file.metadata
    assert updated_file.next_name != original_file.file_name


def check_name_after_command_applied(test_command: PrepareCommand, file_name_origin: str, file_name_expected: str):
    built_app_file: AppFile = build_app_file(file_name_origin)

    result: list[AppFile] = test_command.execute([built_app_file])

    assert len(result) == 1
    check_that_only_new_name_changed(built_app_file, result[0])
    assert built_app_file.next_name == file_name_expected
