from dataclasses import dataclass
from typing import Optional


@dataclass
class Metadata:
    """
    Represents metadata associated with a file, such as creation date, dimensions, and audio information.

    Attributes:
        _creation_date (Optional[float]): The creation date of the file.
        _img_vid_width (Optional[int]): The width of the image or video.
        _img_vid_height (Optional[int]): The height of the image or video.
        _audio_artist_name (Optional[str]): The artist name for audio files.
        _audio_album_name (Optional[str]): The album name for audio files.
        _audio_song_name (Optional[str]): The song name for audio files.
        _audio_year (Optional[int]): The year of the audio file.
        _other_found_tag_values (Optional[dict[str, str]]): Other found tag values associated with the file.

    """

    _creation_date: Optional[float] = None
    _img_vid_width: Optional[int] = None
    _img_vid_height: Optional[int] = None
    _audio_artist_name: Optional[str] = None
    _audio_album_name: Optional[str] = None
    _audio_song_name: Optional[str] = None
    _audio_year: Optional[int] = None
    _other_found_tag_values: Optional[dict[str, str]] = None

    @property
    def creation_date(self) -> Optional[float]:
        return self._creation_date

    @property
    def img_vid_width(self) -> Optional[int]:
        return self._img_vid_width

    @property
    def img_vid_height(self) -> Optional[int]:
        return self._img_vid_height

    @property
    def audio_artist_name(self) -> Optional[str]:
        return self._audio_artist_name

    @property
    def audio_album_name(self) -> Optional[str]:
        return self._audio_album_name

    @property
    def audio_song_name(self) -> Optional[str]:
        return self._audio_song_name

    @property
    def audio_year(self) -> Optional[int]:
        return self._audio_year

    @property
    def other_found_tag_values(self) -> dict[str, str]:
        return self._other_found_tag_values


@dataclass
class AppFile:
    """
    Represents a file in the application.

    Attributes:
        _absolute_path (str): The absolute path to the file.
        _is_folder (bool): Indicates whether the file is a folder or not.
        _file_name (str): The name of the file.
        _file_extension (str): The file extension.
        _file_extension_new (str): The new file extension.
        _file_size (int): The size of the file.
        _next_name (str): The next name of the file.
        _fs_creation_date (float): The creation date of the file in the file system.
        _fs_modification_date (float): The modification date of the file in the file system.
        _metadata (Optional[Metadata]): Metadata associated with the file.

    """

    _absolute_path: str
    _is_folder: bool
    _file_name: str
    _file_extension: str
    _file_extension_new: str
    _file_size: int
    _next_name: str
    _fs_creation_date: float
    _fs_modification_date: float
    _metadata: Optional[Metadata] = None

    @property
    def absolute_path(self) -> str:
        return self._absolute_path

    @property
    def is_folder(self) -> bool:
        return self._is_folder

    @property
    def file_name(self) -> str:
        return self._file_name

    @property
    def file_extension(self) -> str:
        return self._file_extension

    @property
    def file_extension_new(self) -> str:
        return self._file_extension_new

    @file_extension_new.setter
    def file_extension_new(self, value: str):
        self._file_extension_new = value

    @property
    def file_size(self) -> int:
        return self._file_size

    @property
    def next_name(self) -> str:
        return self._next_name

    @next_name.setter
    def next_name(self, value: str):
        self._next_name = value

    @property
    def fs_creation_date(self) -> float:
        return self._fs_creation_date

    @property
    def fs_modification_date(self) -> float:
        return self._fs_modification_date

    @property
    def metadata(self) -> Optional[Metadata]:
        return self._metadata

    @property
    def is_name_changed(self) -> bool:
        """
        bool: Checks if the name or extension of the file has been changed.

        Returns:
            bool: True if the name or extension has been changed, False otherwise.
        """
        return self._file_name != self._next_name or self._file_extension != self._file_extension_new

    def is_valid(self) -> tuple[bool, str]:
        """
        Checks if the file is valid.

        Returns:
            tuple[bool, str]: A tuple indicating whether the file is valid and an optional error message.
        """
        if len(self.next_name.strip()) == 0:
            return False, "Name is empty"

        return True, ""

    def reset(self):
        """Resets the file name and extension to their original values."""
        self.next_name = self.file_name
        self.file_extension_new = self.file_extension
