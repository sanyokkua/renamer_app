from datetime import datetime
from os import rename
from pathlib import Path
from typing import Union, Optional

import PIL
from PIL import Image
from PIL.ExifTags import TAGS
from PIL.Image import Exif, ExifTags
from exifread import process_file
from exifread.classes import IfdTag
from mutagen import flac, mp3, ogg, apev2, wavpack, aiff, mp4
from pillow_heif import register_heif_opener, register_avif_opener

from core.exceptions import PassedArgumentIsNone, FileNotFoundException
from core.models.app_file import AppFile

register_heif_opener()
register_avif_opener()

PIL_REG_EXT: dict[str, str] = Image.registered_extensions()
PIL_SUPPORTED_EXT: set[str] = {
    ext.lower() for ext, file in PIL_REG_EXT.items() if file in Image.OPEN
}
EXIF_READ_SUPPORTED_EXT: set[str] = {".tiff", ".jpg", ".jpeg", ".png", ".webp", ".heic"}

TAG_EXIF_IMAGE_DATE_TIME = "Image DateTime"
TAG_EXIF_DATE_TIME_ORIGINAL = "EXIF DateTimeOriginal"
TAG_EXIF_DATE_TIME_DIGITIZED = "EXIF DateTimeDigitized"

MUTAGEN_SUPPORTED_EXT: set[str] = {
    ".flac",
    ".mp3",
    ".ogg",
    ".ape",
    ".m4a",
    ".wv",
    ".aiff",
}


def validate_path(file_path: Path) -> None:
    if file_path is None:
        raise PassedArgumentIsNone("Passed file path is None")
    assert isinstance(file_path, Path)

    if not file_path.exists():
        raise FileNotFoundException(path=str(file_path.absolute()))


def is_ext_supported(file_path: Path | None, set_of_ext: set[str]) -> bool:
    validate_path(file_path)

    ext: str = file_path.suffix.lower()  # Extension with period
    return ext in set_of_ext


def chose_correct_date(
    date_time: float | None,
    date_time_original: float | None,
    date_time_digitized: float | None,
) -> float | None:
    # Here will be chosen best option of the provided dates.
    # In 99% cases, date_time_original is representing real datetime for the image and should be chosen
    # But if date_time_original is absent, date_time or date_time_digitized should be chosen.
    # Here is the logic to choose the earliest date

    # If all dates are absent, return None
    if date_time_original is None and date_time is None and date_time_digitized is None:
        return None

    # If all dates are present, choose the earliest one
    all_dates = [date_time_original, date_time, date_time_digitized]

    if all(date is not None for date in all_dates):
        return min(date_time_original, date_time, date_time_digitized)

    # Otherwise, choose the earliest non-None date
    dates = [date for date in all_dates if date is not None]
    return min(dates) if dates else None


def parse_date_time(date_time_str: Optional[str]) -> Union[float, int, None]:
    print(f"Will be a try to parse following date/time value: {date_time_str}")
    if date_time_str is None:
        return None

    formats = ["%Y-%m-%dT%H:%M:%S%z", "%Y:%m:%d %H:%M:%S"]

    for format_str in formats:
        try:
            print(f"Trying to parse {date_time_str} with format: {format_str}")
            parsed_datetime = datetime.strptime(date_time_str, format_str).timestamp()
            print(f"Date/time parsed, new value is {parsed_datetime}")
            return parsed_datetime
        except ValueError:
            pass

    print("There were no date/time values parsed, None will be returned")
    return None


def get_creation_datetime_by_exifread(file_path: Path) -> float | None:
    # Expected valid path, with existing file or folder and supported by exifread.
    # Should be verified before passing to the function
    def get_value(tag: IfdTag) -> str | None:
        # Helper function for extracting required field from tag
        return tag.printable if tag is not None else None

    try:
        with open(file_path, "rb") as opened_file:
            exif_by_exifread = process_file(opened_file)
            print(exif_by_exifread)

            tag_date_time_original: IfdTag | None = exif_by_exifread.get(
                TAG_EXIF_DATE_TIME_ORIGINAL
            )
            tag_img_date_time: IfdTag | None = exif_by_exifread.get(
                TAG_EXIF_IMAGE_DATE_TIME
            )
            tag_date_time_digit: IfdTag | None = exif_by_exifread.get(
                TAG_EXIF_DATE_TIME_DIGITIZED
            )

            parsed_datetime_original = parse_date_time(
                get_value(tag_date_time_original)
            )
            parsed_datetime = parse_date_time(get_value(tag_img_date_time))
            parsed_datetime_digitized = parse_date_time(get_value(tag_date_time_digit))

            return chose_correct_date(
                parsed_datetime, parsed_datetime_original, parsed_datetime_digitized
            )
    except Exception as ex:
        # Any Exception or error during work with file and extraction of file is OK in this case,
        # so None should be returned
        print(ex)
        return None


def get_creation_datetime_by_pil(file_path: Path) -> float | None:
    # Expected valid path, with existing file or folder and supported by exifread.
    # Should be verified before passing to the function
    try:
        with Image.open(file_path) as image:
            base_exif_data: Exif | None = image.getexif()
            ifd_exif_data: dict | None = (
                base_exif_data.get_ifd(ExifTags.IFD.Exif) if base_exif_data else None
            )

            image_datetime_str = None
            image_datetime_original_str = None
            image_datetime_digitized_str = None

            if base_exif_data is not None and ExifTags.Base.DateTime in base_exif_data:
                image_datetime_str = base_exif_data[ExifTags.Base.DateTime]

            if (
                ifd_exif_data is not None
                and ExifTags.Base.DateTimeOriginal in ifd_exif_data
            ):
                image_datetime_original_str = ifd_exif_data[
                    ExifTags.Base.DateTimeOriginal
                ]

            if (
                ifd_exif_data is not None
                and ExifTags.Base.DateTimeDigitized in ifd_exif_data
            ):
                image_datetime_digitized_str = ifd_exif_data[
                    ExifTags.Base.DateTimeDigitized
                ]

            parsed_datetime_original = parse_date_time(image_datetime_original_str)
            parsed_datetime = parse_date_time(image_datetime_str)
            parsed_datetime_digitized = parse_date_time(image_datetime_digitized_str)

            return chose_correct_date(
                parsed_datetime, parsed_datetime_original, parsed_datetime_digitized
            )
    except Exception as ex:
        # Any Exception or error during work with file and extraction of file is OK in this case,
        # so None should be returned
        print(ex)
        return None


def get_metadata_creation_time(file_path: Path) -> float | None:
    validate_path(file_path)

    try:
        # To be sure that exif creation date will be found was decided to use 2 different libs for extracting data
        exifread_creation_val: float | None = None
        pil_creation_val: float | None = None

        if is_ext_supported(file_path, EXIF_READ_SUPPORTED_EXT):
            exifread_creation_val = get_creation_datetime_by_exifread(file_path)

        if is_ext_supported(file_path, PIL_SUPPORTED_EXT):
            pil_creation_val = get_creation_datetime_by_pil(file_path)

        if exifread_creation_val is None and pil_creation_val is None:
            return None

        return min(
            [
                date
                for date in [exifread_creation_val, pil_creation_val]
                if date is not None
            ]
        )

    except Exception:
        return None


def get_metadata_tags(file_path: Path) -> dict[str, any]:
    validate_path(file_path)
    metadata: dict[str, any] = {}

    if is_ext_supported(file_path, PIL_SUPPORTED_EXT):
        try:
            with open(file_path, "rb") as opened_file:
                exif_by_exifread = process_file(opened_file)

                for key, value in exif_by_exifread.items():
                    metadata[key] = str(value)
        except Exception:
            pass

    if is_ext_supported(file_path, PIL_SUPPORTED_EXT):
        try:
            with Image.open(file_path) as image:
                base_exif_data: Exif | None = image.getexif()
                ifd_exif_data: dict | None = (
                    base_exif_data.get_ifd(ExifTags.IFD.Exif)
                    if base_exif_data
                    else None
                )

                if base_exif_data is not None:
                    for tag, value in base_exif_data.items():
                        tag_name = TAGS.get(tag, tag)
                        metadata[str(tag_name)] = value
                if ifd_exif_data is not None:
                    for tag, value in ifd_exif_data.items():
                        tag_name = TAGS.get(tag, tag)
                        metadata[str(tag_name)] = value
        except FileNotFoundError | PIL.UnidentifiedImageError | ValueError | TypeError:
            pass

    return metadata


def get_metadata_dimensions(file_path: Path) -> tuple[int | None, int | None]:
    validate_path(file_path)

    if is_ext_supported(file_path, PIL_SUPPORTED_EXT):
        try:
            with Image.open(file_path) as image:
                width, height = image.size
                return width, height
        except FileNotFoundError | PIL.UnidentifiedImageError | ValueError | TypeError:
            pass

    return None, None


def get_metadata_audio(
    file_path: Path,
) -> tuple[str | None, str | None, str | None, str | None]:
    validate_path(file_path)

    if not is_ext_supported(file_path, MUTAGEN_SUPPORTED_EXT):
        return None, None, None, None

    file_ext: str = file_path.suffix.lower()

    audio_artist_name: str | None = None
    audio_album_name: str | None = None
    audio_song_name: str | None = None
    audio_year: str | None = None

    match file_ext:
        case ".flac":
            metadata: flac.FLAC = flac.FLAC(file_path)
            audio_artist_name = metadata.tags.get("artist", [""])[0]
            audio_album_name = metadata.tags.get("album", [""])[0]
            audio_song_name = metadata.tags.get("title", [""])[0]
            audio_year = metadata.tags.get("date", [""])[0]
        case ".mp3":
            metadata: mp3.MP3 = mp3.MP3(file_path)
            audio_artist_name = metadata.get("TPE1", [""])[0]
            audio_album_name = metadata.get("TALB", [""])[0]
            audio_song_name = metadata.get("TIT2", [""])[0]
            audio_year = metadata.get("TDRC", [""])[0]
        case ".ogg":
            metadata: ogg.OggFileType = ogg.OggFileType(file_path)
            audio_artist_name = metadata.tags.get("artist", [""])[0]
            audio_album_name = metadata.tags.get("album", [""])[0]
            audio_song_name = metadata.tags.get("title", [""])[0]
            audio_year = metadata.tags.get("date", [""])[0]
        case ".ape":
            metadata: apev2.APEv2 = apev2.APEv2(file_path)
            audio_artist_name = metadata.get("artist", [""])[0]
            audio_album_name = metadata.get("album", [""])[0]
            audio_song_name = metadata.get("title", [""])[0]
            audio_year = metadata.get("year", [""])[0]
        case ".m4a":
            metadata: mp4.MP4 = mp4.MP4(file_path)
            audio_artist_name = metadata.tags.get("\xa9ART", [""])[0]
            audio_album_name = metadata.tags.get("\xa9alb", [""])[0]
            audio_song_name = metadata.tags.get("\xa9nam", [""])[0]
            audio_year = metadata.tags.get("\xa9day", [""])[0]
        case ".wv":
            metadata: wavpack.WavPack = wavpack.WavPack(file_path)
            audio_artist_name = metadata.tags.get("artist", [""])[0]
            audio_album_name = metadata.tags.get("album", [""])[0]
            audio_song_name = metadata.tags.get("title", [""])[0]
            audio_year = metadata.tags.get("year", [""])[0]
        case ".aiff":
            metadata: aiff.AIFF = aiff.AIFF(file_path)
            audio_artist_name = metadata.tags.get("artist", [""])[0]
            audio_album_name = metadata.tags.get("album", [""])[0]
            audio_song_name = metadata.tags.get("title", [""])[0]
            audio_year = metadata.tags.get("year", [""])[0]
        case _:
            return None, None, None, None

    return audio_artist_name, audio_album_name, audio_song_name, audio_year


def rename_file(app_file: AppFile | None) -> None:
    if app_file is None:
        return

    if not isinstance(app_file, AppFile):
        return

    if not app_file.is_name_changed:
        return

    if not app_file.is_valid():
        return

    file_absolute_path: str = app_file.absolute_path
    print(f"Original File Name: {file_absolute_path}")

    new_file_path = file_absolute_path.replace(app_file.file_name, app_file.next_name)
    if (
        len(app_file.file_extension) > 0
        and len(app_file.file_extension_new.strip()) == 0
    ):
        new_file_path = new_file_path.replace(app_file.file_extension, "")
    else:
        new_file_path = new_file_path.replace(
            app_file.file_extension, app_file.file_extension_new
        )

    print(f"New File Name: {new_file_path}")
    rename(app_file.absolute_path, new_file_path)


def get_parent_folders(file_path: str) -> list[str]:
    if file_path is None or len(file_path.strip()) == 0:
        return []

    file_path = file_path.replace("\\", "/")
    file_path = file_path.replace("//", "/")
    file_path = file_path.removesuffix("/")
    split_path_items = file_path.split("/")

    # Exclude the root element (drive letter for windows or empty string for unix)
    # and exclude last item (filename or directory)
    return split_path_items[1 : len(split_path_items) - 1]
