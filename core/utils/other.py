from enum import Enum
from pathlib import Path
from typing import Type

from core.exceptions import PassedArgumentIsNone, FileNotFoundException, PathStringIsEmpty
from core.models.app_file import AppFile, Metadata
from core.utils.io_utils import get_exif_creation_time, get_image_dimensions, get_exif_tags


def build_metadata_from_path(file_path: Path | None) -> Metadata | None:
    if file_path is None:
        raise PassedArgumentIsNone()
    assert isinstance(file_path, Path)

    if not file_path.exists():
        raise FileNotFoundException(path=str(file_path.absolute()))

    creation_date = get_exif_creation_time(file_path)
    width, height = get_image_dimensions(file_path)
    tags = get_exif_tags(file_path)

    return Metadata(
        _creation_date=creation_date,
        _img_vid_width=width,
        _img_vid_height=height,
        _audio_artist_name=None,
        _audio_album_name=None,
        _audio_song_name=None,
        _audio_year=None,
        _other_found_tag_values=tags
    )


def build_app_file_from_path_str(path: str | None) -> AppFile:
    if path is None:
        raise PassedArgumentIsNone()

    assert isinstance(path, str)

    if len(path.strip()) == 0:
        raise PathStringIsEmpty()

    current_file_path: Path = Path(path.strip())
    if not current_file_path.exists():
        raise FileNotFoundException(path=str(current_file_path.absolute()))

    file_path_str: str = str(current_file_path.absolute())
    is_folder = current_file_path.is_dir()
    file_ext: str = current_file_path.suffix
    file_name: str = current_file_path.name.removesuffix(file_ext)
    file_size: int = current_file_path.stat().st_size
    file_new_name: str = file_name
    file_creation_date: float = current_file_path.stat().st_birthtime
    file_modification_date: float = current_file_path.stat().st_mtime
    file_metadata: Metadata | None = build_metadata_from_path(current_file_path)

    return AppFile(
        _absolute_path=file_path_str,
        _is_folder=is_folder,
        _file_extension=file_ext,
        _file_name=file_name,
        _file_size=file_size,
        _next_name=file_new_name,
        _fs_creation_date=file_creation_date,
        _fs_modification_date=file_modification_date,
        _metadata=file_metadata,
    )


def get_enum_item_by_value[T: Enum](enum_class: Type[T], enum_value: object) -> T:
    for member in enum_class.__members__.values():
        if member.value == enum_value:
            return member
    raise ValueError(f"No matching enum value found for {enum_value}")
