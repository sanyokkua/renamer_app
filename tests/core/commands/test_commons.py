from core.commons import PrepareCommand
from core.models.app_file import AppFile, Metadata


def build_app_file(
        file_name: str,
        file_ext: str = ".jpg",
        cr_timestamp: float = 10000,
        mod_timestamp: float = 10000,
        cc_timestamp: float = 10000,
        width: int = 1080,
        height: int = 720,
        absolute_path: str = "",
        file_size: int = 1000,
) -> AppFile:
    if len(absolute_path.strip()) == 0:
        absolute = f"absolute/file/path/{file_name}"
    else:
        absolute = absolute_path

    metadata = Metadata(
        _creation_date=cc_timestamp,
        _img_vid_width=width,
        _img_vid_height=height,
        _audio_artist_name="ArtistName",
        _audio_album_name="AlbumName",
        _audio_song_name="SongName",
        _audio_year=2000,
        _other_found_tag_values={},
    )

    return AppFile(
        _absolute_path=absolute,
        _is_folder=False,
        _file_extension=file_ext,
        _file_extension_new=file_ext,
        _file_name=file_name,
        _file_size=file_size,
        _next_name=file_name,
        _fs_creation_date=cr_timestamp,
        _fs_modification_date=mod_timestamp,
        _metadata=metadata,
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


def check_extension_after_command_applied(
        test_command: PrepareCommand, file_ext_origin: str, file_ext_expected: str
):
    built_app_file: AppFile = build_app_file("custom_name", file_ext_origin)

    result: list[AppFile] = test_command.execute([built_app_file])

    assert len(result) == 1
    assert built_app_file.file_extension_new == file_ext_expected

    assert built_app_file.absolute_path == result[0].absolute_path
    assert built_app_file.file_extension == result[0].file_extension
    assert built_app_file.file_name == result[0].file_name
    assert built_app_file.file_size == result[0].file_size
    assert built_app_file.fs_creation_date == result[0].fs_creation_date
    assert built_app_file.fs_modification_date == result[0].fs_modification_date
    assert built_app_file.is_folder == result[0].is_folder
    assert built_app_file.metadata == result[0].metadata
    assert built_app_file.next_name == result[0].file_name


def verify_command_result(
        test_command: PrepareCommand,
        file_name_origin: str,
        file_name_expected: str,
        file_ext: str = ".jpg",
        file_creation_time: float = 1000,
        img_width: int = 1080,
        img_height: int = 720,
        absolute_path: str = "",
        is_updated_name: bool = True,
):
    built_app_file: AppFile = build_app_file(
        file_name=file_name_origin,
        mod_timestamp=file_creation_time,
        width=img_width,
        height=img_height,
        absolute_path=absolute_path,
        file_ext=file_ext,
        cr_timestamp=file_creation_time,
        cc_timestamp=file_creation_time,
    )

    result: list[AppFile] = test_command.execute([built_app_file])

    assert len(result) == 1

    if is_updated_name:
        assert built_app_file.is_name_changed
        assert built_app_file.next_name != result[0].file_name

    assert built_app_file.next_name == file_name_expected
    assert built_app_file.absolute_path == result[0].absolute_path
    assert built_app_file.file_extension == result[0].file_extension
    assert built_app_file.file_name == result[0].file_name
    assert built_app_file.file_size == result[0].file_size
    assert built_app_file.fs_creation_date == result[0].fs_creation_date
    assert built_app_file.fs_modification_date == result[0].fs_modification_date
    assert built_app_file.is_folder == result[0].is_folder
    assert built_app_file.metadata == result[0].metadata
