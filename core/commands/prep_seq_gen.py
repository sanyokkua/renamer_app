from typing import List

from core.commons import BasePrepareCommand
from core.enums import SortSource
from core.models.app_file import AppFile


def get_default_for_none(value: any, default_val: any) -> any:
    """
    Returns the value if not None, otherwise returns the default value.

    Args:
        value (any): The value to check.
        default_val (any): The default value to return if value is None.

    Returns:
        any: The value if not None, otherwise the default value.
    """
    if value is not None:
        return value
    return default_val


class SequencePrepareCommand(BasePrepareCommand):
    """
    A command class for preparing filenames with sequential numbers.

    Attributes:
        start_number (int): The starting number of the sequence.
        step_value (int): The step value for incrementing the sequence.
        padding (int): The padding for the sequence numbers.
        sort_source (SortSource): The source for sorting the file list.

    Methods:
        generate_sequence(): Generates the next number in the sequence.
        format_number_with_padding(number: int) -> str: Formats the number with leading zeros based on the padding.
        create_new_name(item: AppFile, index: int) -> AppFile: Creates a new filename with sequential numbers.
        sort_date(data: List[AppFile]) -> List[AppFile]: Sorts the file list based on the specified source.
    """

    def __init__(
            self,
            start_number: int = 0,
            step_value: int = 1,
            padding: int = 1,
            sort_source: SortSource = SortSource.FILE_NAME,
    ):
        """
        Initializes a SequencePrepareCommand instance.

        Args:
            start_number (int, optional): The starting number of the sequence. Defaults to 0.
            step_value (int, optional): The step value for incrementing the sequence. Defaults to 1.
            padding (int, optional): The padding for the sequence numbers. Defaults to 1.
            sort_source (SortSource, optional): The source for sorting the file list. Defaults to SortSource.FILE_NAME.
        """
        self.start_number: int = start_number
        self.step_value: int = step_value
        self.padding: int = padding
        self.sort_source: SortSource = sort_source

    def generate_sequence(self) -> int:
        """
        Generates the next number in the sequence.

        Returns:
            int: The next number in the sequence.
        """
        current_value: int = self.start_number
        self.start_number += self.step_value
        return current_value

    def format_number_with_padding(self, number: int):
        """
        Formats the number with leading zeros based on the padding.

        Args:
            number (int): The number to format.

        Returns:
            str: The formatted number string.
        """
        formatted_string = "{:0{width}d}".format(number, width=self.padding)
        return formatted_string

    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        """
        Creates a new filename with sequential numbers.

        Args:
            item (AppFile): The AppFile item.
            index (int): The index of the current item.

        Returns:
            AppFile: The AppFile item with the new filename.
        """
        seq_number: int = self.generate_sequence()
        number: str = self.format_number_with_padding(seq_number)
        item.next_name = number

        return item

    def sort_date(self, data: List[AppFile]) -> List[AppFile]:
        """
        Sorts the file list based on the specified source.

        Args:
            data (List[AppFile]): The list of AppFile items to sort.

        Returns:
            List[AppFile]: The sorted list of AppFile items.
        """
        match self.sort_source:
            case SortSource.FILE_NAME:
                return sorted(data, key=lambda x: get_default_for_none(x.file_name, ""))
            case SortSource.FILE_PATH:
                return sorted(
                    data, key=lambda x: get_default_for_none(x.absolute_path, "")
                )
            case SortSource.FILE_SIZE:
                return sorted(data, key=lambda x: get_default_for_none(x.file_size, 0))
            case SortSource.FILE_CREATION_DATETIME:
                return sorted(
                    data, key=lambda x: get_default_for_none(x.fs_creation_date, 0)
                )
            case SortSource.FILE_MODIFICATION_DATETIME:
                return sorted(
                    data, key=lambda x: get_default_for_none(x.fs_modification_date, 0)
                )
            case SortSource.FILE_CONTENT_CREATION_DATETIME:

                def get_content_creation(app_file: AppFile):
                    metadata = app_file.metadata
                    if metadata is not None and metadata.creation_date is not None:
                        return metadata.creation_date
                    else:
                        return 0

                return sorted(data, key=get_content_creation)
            case SortSource.IMAGE_WIDTH:

                def get_img_width_creation(app_file: AppFile):
                    metadata = app_file.metadata
                    if metadata is not None and metadata.img_vid_width is not None:
                        return metadata.img_vid_width
                    else:
                        return 0

                return sorted(data, key=get_img_width_creation)
            case SortSource.IMAGE_HEIGHT:

                def get_img_height_creation(app_file: AppFile):
                    metadata = app_file.metadata
                    if metadata is not None and metadata.img_vid_height is not None:
                        return metadata.img_vid_height
                    else:
                        return 0

                return sorted(data, key=get_img_height_creation)
        return data
