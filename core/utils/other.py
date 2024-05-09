import logging
from pathlib import Path

from core.exceptions import (
    FileNotFoundException,
    PassedArgumentIsNone,
    PathStringIsEmpty,
)
from core.models.app_file import AppFile, Metadata
from core.utils.io_utils import (
    get_metadata_audio,
    get_metadata_creation_time,
    get_metadata_dimensions,
    get_metadata_tags,
)

log: logging.Logger = logging.getLogger(__name__)


# TODO: Add unit tests


def build_metadata_from_path(file_path: Path | None) -> Metadata | None:
    """
    Builds metadata from a given file path.

    Args:
        file_path (Path | None): The path to the file.

    Returns:
        Metadata | None: The metadata extracted from the file, or None if the file path is None.

    Raises:
        PassedArgumentIsNone: If the file path is None.
        FileNotFoundException: If the file specified by the file path does not exist.

    Example:
        > file_path = Path("example.jpg")
        > build_metadata_from_path(file_path)
        Metadata(creation_date=1617744061.0, img_vid_width=1920, img_vid_height=1080,
                 audio_artist_name=None, audio_album_name=None, audio_song_name=None,
                 audio_year=None, other_found_tag_values={'Model': 'Canon EOS 5D Mark IV', ...})
    """
    if file_path is None:
        raise PassedArgumentIsNone()
    assert isinstance(file_path, Path)

    if not file_path.exists():
        raise FileNotFoundException(path=str(file_path.absolute()))

    creation_date = get_metadata_creation_time(file_path)
    width, height = get_metadata_dimensions(file_path)
    tags = get_metadata_tags(file_path)
    a_artist, a_album, a_song, a_year = get_metadata_audio(file_path)

    return Metadata(
        _creation_date=creation_date,
        _img_vid_width=width,
        _img_vid_height=height,
        _audio_artist_name=a_artist,
        _audio_album_name=a_album,
        _audio_song_name=a_song,
        _audio_year=a_year,
        _other_found_tag_values=tags,
    )


def build_app_file_from_path_str(path: str | None) -> AppFile:
    """
    Builds an AppFile object from a given file path string.

    Args:
        path (str | None): The path to the file.

    Returns:
        AppFile: The AppFile object created from the file path.

    Raises:
        PassedArgumentIsNone: If the path is None.
        PathStringIsEmpty: If the path string is empty after stripping.
        FileNotFoundException: If the file specified by the path does not exist.

    Example:
        > path = "example.jpg"
        > build_app_file_from_path_str(path)
        AppFile(absolute_path='/path/to/example.jpg', is_folder=False, file_extension='.jpg',
                file_extension_new='.jpg', file_name='example', file_size=1024,
                next_name='example', fs_creation_date=1617744061.0, fs_modification_date=1617744061.0,
                metadata=Metadata(creation_date=1617744061.0, img_vid_width=1920, img_vid_height=1080,
                                   audio_artist_name=None, audio_album_name=None, audio_song_name=None,
                                   audio_year=None, other_found_tag_values={'Model': 'Canon EOS 5D Mark IV', ...}))
    """
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
    file_ext: str = current_file_path.suffix  # has leading period, ex: ".jpg"
    file_ext_new: str = current_file_path.suffix
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
        _file_extension_new=file_ext_new,
        _file_name=file_name,
        _file_size=file_size,
        _next_name=file_new_name,
        _fs_creation_date=file_creation_date,
        _fs_modification_date=file_modification_date,
        _metadata=file_metadata,
    )
