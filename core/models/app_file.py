from dataclasses import dataclass
from typing import Optional


@dataclass
class Metadata:
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
        return self._file_name != self._next_name or self._file_extension != self._file_extension_new

    def is_valid(self) -> tuple[bool, str]:
        if len(self.next_name.strip()) == 0:
            return False, "Name is empty"

        return True, ""

    def reset(self):
        self.next_name = self.file_name
        self.file_extension_new = self.file_extension
