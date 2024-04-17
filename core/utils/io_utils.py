from datetime import datetime
from os import rename
from pathlib import Path
from typing import Union, Optional

from PIL import Image
from exifread import process_file
from exifread.classes import IfdTag

from core.models.app_file import AppFile

TAG_EXIF_IMAGE_DATE_TIME = "Image DateTime"
TAG_EXIF_DATE_TIME_ORIGINAL = "EXIF DateTimeOriginal"
TAG_EXIF_DATE_TIME_DIGITIZED = "EXIF DateTimeDigitized"


def get_exif_creation_time(file_path: Path) -> float | None:
    try:
        with open(file_path, "rb") as opened_file:
            exif_by_exifread = process_file(opened_file)
            print(exif_by_exifread)

            tag_date_time_original: IfdTag | None = exif_by_exifread.get(TAG_EXIF_DATE_TIME_ORIGINAL)
            tag_img_date_time: IfdTag | None = exif_by_exifread.get(TAG_EXIF_IMAGE_DATE_TIME)
            tag_date_time_digit: IfdTag | None = exif_by_exifread.get(TAG_EXIF_DATE_TIME_DIGITIZED)

            def get_value(tag: IfdTag) -> str | None:
                return tag.printable if tag is not None else None

            exif_date_time_original = parse_date_time(get_value(tag_date_time_original))
            image_date_time = parse_date_time(get_value(tag_img_date_time))
            exif_date_time_digitized = parse_date_time(get_value(tag_date_time_digit))

            time_values: list[int | float | None] = [
                exif_date_time_original,
                image_date_time,
                exif_date_time_digitized,
            ]

            time_values = [
                time_value for time_value in time_values if time_value is not None
            ]
            time_values.sort()

            return time_values[0] if len(time_values) > 0 else None

    except OSError:
        return None


def get_exif_tags(file_path: Path) -> dict[str, str] | None:
    try:
        with open(file_path, "rb") as opened_file:
            exif_by_exifread = process_file(opened_file)
            new_dict: dict[str, str] = {}
            for key, value in exif_by_exifread.items():
                new_dict[key] = str(value)

            return new_dict
    except OSError:
        return None


def get_image_dimensions(file_path: Path) -> Union[tuple[int, int], None]:
    try:
        with Image.open(file_path) as image:
            width, height = image.size
            return width, height
    except OSError:
        return None


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


def rename_file(app_file: AppFile | None) -> None:
    if app_file is None:
        return None

    assert isinstance(app_file, AppFile)

    if not app_file.is_name_changed:
        return None

    if len(app_file.next_name) > 0:
        new_file_path = app_file.absolute_path.replace(app_file.file_name, app_file.next_name)
        rename(app_file.absolute_path, new_file_path)
