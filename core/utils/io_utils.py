import logging
from datetime import datetime
from os import rename
from pathlib import Path
from typing import Optional, Union

import PIL
from PIL import Image
from PIL.ExifTags import TAGS
from PIL.Image import Exif, ExifTags
from exifread import process_file
from exifread.classes import IfdTag
from mutagen import aiff, apev2, flac, mp3, mp4, ogg, wavpack
from pillow_heif import register_avif_opener, register_heif_opener

from core.exceptions import FileNotFoundException, PassedArgumentIsNone
from core.models.app_file import AppFile

log: logging.Logger = logging.getLogger(__name__)

register_heif_opener()
register_avif_opener()

PIL_REG_EXT: dict[str, str] = Image.registered_extensions()
PIL_SUPPORTED_EXT: set[str] = {ext.lower() for ext, file in PIL_REG_EXT.items() if file in Image.OPEN}
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


# TODO: Add unit tests


def validate_path(file_path: Path) -> None:
    """
    Validates whether the given file path exists and is not None.

    Parameters:
    - file_path (Path): A Path object representing the file path to be validated.

    Raises:
    - PassedArgumentIsNone: If the passed file path is None.
    - AssertionError: If the passed file_path is not an instance of Path.
    - FileNotFoundException: If the file specified by the file_path does not exist.

    Returns:
    - None: This function does not return anything.
    """
    if file_path is None:
        raise PassedArgumentIsNone("Passed file path is None")
    assert isinstance(file_path, Path)

    if not file_path.exists():
        raise FileNotFoundException(path=str(file_path.absolute()))


def is_ext_supported(file_path: Path | None, set_of_ext: set[str]) -> bool:
    """
    Checks if the extension of the given file path is supported.

    Parameters:
    - file_path (Path | None): A Path object representing the file path. If None, the function returns False.
    - set_of_ext (set[str]): A set containing supported file extensions, each as a string (e.g., {'.txt', '.csv'}).

    Returns:
    - bool: True if the extension of the file path is in the set_of_ext, False otherwise.

    Raises:
    - PassedArgumentIsNone: If the passed file_path is None.
    - AssertionError: If the file_path parameter is not an instance of Path.
    - FileNotFoundException: If the file specified by the file_path does not exist.
    """
    validate_path(file_path)

    ext: str = file_path.suffix.lower()  # Extension with period
    return ext in set_of_ext


def chose_correct_date_from_metadata(
    date_time: float | None,
    date_time_original: float | None,
    date_time_digitized: float | None,
) -> float | None:
    """
    Chooses the most appropriate date from the provided metadata timestamps.

    Parameters:
    - date_time (float | None): The timestamp representing the date and time.
    - date_time_original (float | None): The timestamp representing the original creation date and time.
    - date_time_digitized (float | None): The timestamp representing the digitization date and time.

    Returns:
    - float | None: The chosen timestamp, or None if all provided timestamps are None.

    Logic:
    - If all timestamps are None, returns None.
    - If all timestamps are present, chooses the earliest one.
    - Otherwise, chooses the earliest non-None timestamp.

    Note:
    - In most cases, date_time_original is chosen if present, as it typically represents the real creation datetime.
    - If date_time_original is absent, date_time or date_time_digitized is chosen.
    - If multiple timestamps are present, the earliest one is chosen.

    """
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
    """
    Parses the given date/time string into a timestamp (seconds since epoch).

    Parameters:
    - date_time_str (Optional[str]): A string representing a date and time in various formats.

    Returns:
    - Union[float, int, None]: A timestamp (seconds since epoch) if the parsing is successful,
      None if the input string is None or cannot be parsed.

    Logic:
    - Attempts to parse the input date/time string using a list of predefined format strings.
    - Iterates through the format strings until a successful parsing is achieved or all formats fail.
    - If parsing is successful, returns the corresponding timestamp.
    - If parsing fails for all formats, returns None.

    Example:
    ```python
    # Example usage:
    date_time_str = "2024-10-29T15:00:59"
    timestamp = parse_date_time(date_time_str)
    if timestamp is not None:
        print("Timestamp:", timestamp)
    else:
        print("Failed to parse the date/time string.")
    ```

    Supported Date/Time Formats (in order of priority):
    - "%Y-%m-%dT%H:%M:%S": ISO 8601 format with timezone
    - "%a, %d %b %Y %H:%M:%S %z": RFC 2822 format with timezone
    - "%a, %d %b %Y %H:%M:%S": RFC 2822 format without timezone
    - "%Y-%m-%d %H:%M:%S %z": Custom format with timezone
    - "%Y-%m-%d %H:%M:%S": Custom format without timezone
    - "%Y-%m-%dT%H:%M:%S%z": ISO 8601 format without space before timezone
    - "%Y-%m-%dT%H:%M:%S": ISO 8601 format without timezone
    - "%Y:%m:%d %H:%M:%S": Custom format with colon separator

    Note:
    - This function does not handle parsing of timezone offsets other than UTC.
    """
    log.debug(f"Will be a try to parse following date/time value: {date_time_str}")
    if date_time_str is None:
        return None

    formats = [
        "%Y-%m-%dT%H:%M:%S",
        "%a, %d %b %Y %H:%M:%S %z",
        "%a, %d %b %Y %H:%M:%S",
        "%Y-%m-%d %H:%M:%S %z",
        "%Y-%m-%d %H:%M:%S",
        "%Y-%m-%dT%H:%M:%S%z",
        "%Y-%m-%dT%H:%M:%S",
        "%Y:%m:%d %H:%M:%S",
    ]

    for format_str in formats:
        try:
            log.debug(f"Trying to parse {date_time_str} with format: {format_str}")
            parsed_datetime = datetime.strptime(date_time_str.strip(), format_str).timestamp()
            log.debug(f"Date/time parsed, new value is {parsed_datetime}")
            return parsed_datetime
        except ValueError as err:
            log.debug(f"Failed to parse date: {date_time_str} with format: {format_str}, {err}")

    log.debug("There were no date/time values parsed, None will be returned")
    return None


def get_creation_datetime_by_exifread(file_path: Path) -> float | None:
    """
    Retrieves the creation date and time of an image using exifread library.

    Args:
        file_path (Path): The path to the image file.

    Returns:
        float | None: The creation time of the image in seconds since the epoch,
        or None if the creation time cannot be determined.

    Raises:
        Any Exception: Any error during work with the file and extraction of metadata.
            This function handles any exception and returns None.

    Example:
        > file_path = Path("example.jpg")
        > get_creation_datetime_by_exifread(file_path)
        1617744061.0
    """

    # Expected valid path, with existing file or folder and supported by exifread.
    # Should be verified before passing to the function
    def get_value(tag: IfdTag) -> str | None:
        """
        Helper function for extracting required field from tag.

        Args:
            tag (IfdTag): The tag object.

        Returns:
            str | None: The printable value of the tag, or None if the tag is None.
        """
        # Helper function for extracting required field from tag
        return tag.printable if tag is not None else None

    try:
        with open(file_path, "rb") as opened_file:
            exif_by_exifread = process_file(opened_file)
            log.debug(exif_by_exifread)

            tag_date_time_original: IfdTag | None = exif_by_exifread.get(TAG_EXIF_DATE_TIME_ORIGINAL)
            tag_img_date_time: IfdTag | None = exif_by_exifread.get(TAG_EXIF_IMAGE_DATE_TIME)
            tag_date_time_digit: IfdTag | None = exif_by_exifread.get(TAG_EXIF_DATE_TIME_DIGITIZED)

            parsed_datetime_original = parse_date_time(get_value(tag_date_time_original))
            parsed_datetime = parse_date_time(get_value(tag_img_date_time))
            parsed_datetime_digitized = parse_date_time(get_value(tag_date_time_digit))

            return chose_correct_date_from_metadata(
                parsed_datetime, parsed_datetime_original, parsed_datetime_digitized
            )
    except Exception as ex:
        # Any Exception or error during work with file and extraction of file is OK in this case,
        # so None should be returned
        log.warning(ex)
        return None


def get_creation_datetime_by_pil(file_path: Path) -> float | None:
    """
    Retrieves the creation date and time of an image using PIL (Python Imaging Library).

    Args:
        file_path (Path): The path to the image file.

    Returns:
        float | None: The creation time of the image in seconds since the epoch,
        or None if the creation time cannot be determined.

    Raises:
        Any Exception: Any error during work with the file and extraction of metadata.
            This function handles any exception and returns None.

    Example:
        > file_path = Path("example.jpg")
        > get_creation_datetime_by_pil(file_path)
        1617744061.0
    """
    # Expected valid path, with existing file or folder and supported by exifread.
    # Should be verified before passing to the function
    try:
        with Image.open(file_path) as image:
            base_exif_data: Exif | None = image.getexif()
            ifd_exif_data: dict | None = base_exif_data.get_ifd(ExifTags.IFD.Exif) if base_exif_data else None

            image_datetime_str = None
            image_datetime_original_str = None
            image_datetime_digitized_str = None

            if base_exif_data is not None and ExifTags.Base.DateTime in base_exif_data:
                image_datetime_str = base_exif_data[ExifTags.Base.DateTime]

            if ifd_exif_data is not None and ExifTags.Base.DateTimeOriginal in ifd_exif_data:
                image_datetime_original_str = ifd_exif_data[ExifTags.Base.DateTimeOriginal]

            if ifd_exif_data is not None and ExifTags.Base.DateTimeDigitized in ifd_exif_data:
                image_datetime_digitized_str = ifd_exif_data[ExifTags.Base.DateTimeDigitized]

            parsed_datetime_original = parse_date_time(image_datetime_original_str)
            parsed_datetime = parse_date_time(image_datetime_str)
            parsed_datetime_digitized = parse_date_time(image_datetime_digitized_str)

            return chose_correct_date_from_metadata(
                parsed_datetime, parsed_datetime_original, parsed_datetime_digitized
            )
    except Exception as ex:
        # Any Exception or error during work with file and extraction of file is OK in this case,
        # so None should be returned
        log.warning(ex)
        return None


def get_metadata_creation_time(file_path: Path) -> float | None:
    """
    Retrieves the creation time of a file from its metadata.

    Args:
        file_path (Path): The path to the file.

    Returns:
        float | None: The creation time of the file in seconds since the epoch,
        or None if the creation time cannot be determined.

    Raises:
        FileNotFoundError: If the specified file does not exist.
        ValueError: If the file extension is not supported.
        TypeError: If the file format is not recognized.

    Example:
        file_path = Path("example.jpg")
        get_metadata_creation_time(file_path)
        1617744061.0
    """
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

        return min([date for date in [exifread_creation_val, pil_creation_val] if date is not None])

    except Exception as ex:
        log.warning(ex)
        return None


def get_metadata_tags(file_path: Path) -> dict[str, any]:
    """
    Retrieves metadata tags from an image file.

    Args:
        file_path (Path): The path to the image file.

    Returns:
        dict[str, any]: A dictionary containing metadata tags and their corresponding values.

    Raises:
        FileNotFoundError: If the specified file does not exist.
        ValueError: If the file extension is not supported.
        TypeError: If the file format is not recognized.
        PIL.UnidentifiedImageError: If the image file cannot be identified and opened.
    """
    validate_path(file_path)
    metadata: dict[str, any] = {}

    if is_ext_supported(file_path, PIL_SUPPORTED_EXT):
        try:
            with open(file_path, "rb") as opened_file:
                exif_by_exifread = process_file(opened_file)

                for key, value in exif_by_exifread.items():
                    metadata[key] = str(value)
        except Exception as ex:
            log.warning(ex)

    if is_ext_supported(file_path, PIL_SUPPORTED_EXT):
        try:
            with Image.open(file_path) as image:
                base_exif_data: Exif | None = image.getexif()
                ifd_exif_data: dict | None = base_exif_data.get_ifd(ExifTags.IFD.Exif) if base_exif_data else None

                if base_exif_data is not None:
                    for tag, value in base_exif_data.items():
                        tag_name = TAGS.get(tag, tag)
                        metadata[str(tag_name)] = value
                if ifd_exif_data is not None:
                    for tag, value in ifd_exif_data.items():
                        tag_name = TAGS.get(tag, tag)
                        metadata[str(tag_name)] = value
        except FileNotFoundError | PIL.UnidentifiedImageError | ValueError | TypeError as ex:
            log.warning(ex)

    return metadata


def get_metadata_dimensions(file_path: Path) -> tuple[int | None, int | None]:
    """
    Retrieves the dimensions (width and height) of an image file.

    Args:
        file_path (Path): The path to the image file.

    Returns:
        tuple[int | None, int | None]: A tuple containing the width and height of the image,
            or None if metadata retrieval fails.

    Raises:
        FileNotFoundError: If the specified file does not exist.
        ValueError: If the file extension is not supported.
        TypeError: If the file format is not recognized.
        PIL.UnidentifiedImageError: If the image file cannot be identified and opened.
    """
    validate_path(file_path)

    if is_ext_supported(file_path, PIL_SUPPORTED_EXT):
        try:
            with Image.open(file_path) as image:
                width, height = image.size
                return width, height
        except FileNotFoundError | PIL.UnidentifiedImageError | ValueError | TypeError as ex:
            log.warning(ex)

    return None, None


def get_metadata_audio(
    file_path: Path,
) -> tuple[str | None, str | None, str | None, str | None]:
    """
    Extracts audio metadata such as artist name, album name, song name, and year from the specified audio file.

    Args:
        file_path (Path): The path to the audio file.

    Returns:
        tuple[str | None, str | None, str | None, str | None]: A tuple containing the extracted metadata:
            - audio_artist_name: The artist name.
            - audio_album_name: The album name.
            - audio_song_name: The song name.
            - audio_year: The year.

    Raises:
        FileNotFoundError: If the specified file does not exist.
        ValueError: If the file extension is not supported.
    """
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


def rename_file(app_file: AppFile | None) -> tuple[bool, str]:
    """
    Renames a file represented by the provided AppFile object.

    Args:
        app_file (AppFile | None): The AppFile object representing the file to be renamed.

    Returns:
        tuple[bool, str]: A tuple indicating whether the file was successfully renamed (True or False)
        and the new absolute path of the file.

    Raises:
        PassedArgumentIsNone: If the app_file argument is None.
        TypeError: If the app_file argument is not an instance of AppFile.
    """
    if app_file is None:
        raise PassedArgumentIsNone("Passed appFile for renaming is none")

    if not isinstance(app_file, AppFile):
        raise TypeError("Passed object is not an instance of AppFile")

    if not app_file.is_name_changed:
        return False, app_file.absolute_path

    if not app_file.is_valid():
        return False, app_file.absolute_path

    file_absolute_path: str = app_file.absolute_path
    log.debug(f"Original File Name: {file_absolute_path}")

    path_without_file = file_absolute_path.removesuffix(f"{app_file.file_name}{app_file.file_extension}")

    new_file_path = path_without_file + app_file.next_name
    if not app_file.is_folder:
        new_file_path = new_file_path + app_file.file_extension_new

    log.debug(f"New File Name: {new_file_path}")
    try:
        rename(app_file.absolute_path, new_file_path)
    except Exception as ex:
        log.warning(ex)
        return False, app_file.absolute_path

    return True, new_file_path


def get_parent_folders(file_path: str) -> list[str]:
    """
    Retrieves the parent folders of a file or directory given its path.

    Args:
        file_path (str): The path of the file or directory.

    Returns:
        list[str]: A list containing the names of the parent folders.

    Raises:
        None

    Notes:
        - If the file_path is None or an empty string, an empty list is returned.
        - The function normalizes the file path by replacing backslashes with forward slashes and removing
            redundant slashes.
        - The function excludes the root element (drive letter for Windows or empty string for Unix)
            and the last item (filename or directory) from the split path items.
    """
    if file_path is None or len(file_path.strip()) == 0:
        return []

    file_path = file_path.replace("\\", "/")
    file_path = file_path.replace("//", "/")
    file_path = file_path.removesuffix("/")
    split_path_items = file_path.split("/")

    # Exclude the root element (drive letter for windows or empty string for unix)
    # and exclude last item (filename or directory)
    return split_path_items[1 : len(split_path_items) - 1]
