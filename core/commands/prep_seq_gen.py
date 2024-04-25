from typing import List

from core.commons import BasePrepareCommand
from core.enums import SortSource
from core.models.app_file import AppFile


class SequencePrepareCommand(BasePrepareCommand):
    def __init__(self, start_number: int = 0, step_value: int = 1, padding: int = 1,
                 sort_source: SortSource = SortSource.FILE_NAME):
        self.start_number: int = start_number
        self.step_value: int = step_value
        self.padding: int = padding
        self.sort_source: SortSource = sort_source

    def generate_sequence(self) -> int:
        current_value: int = self.start_number
        self.start_number += self.step_value
        return current_value

    def format_number_with_padding(self, number: int):
        # Format the number with leading zeros based on the padding
        formatted_string = "{:0{width}d}".format(number, width=self.padding)
        return formatted_string

    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        seq_number: int = self.generate_sequence()
        number: str = self.format_number_with_padding(seq_number)
        item.next_name = number

        return item

    def sort_date(self, data: List[AppFile]) -> List[AppFile]:
        match self.sort_source:
            case SortSource.FILE_NAME:
                return sorted(data, key=lambda x: x.file_name)
            case SortSource.FILE_PATH:
                return sorted(data, key=lambda x: x.absolute_path)
            case SortSource.FILE_SIZE:
                return sorted(data, key=lambda x: x.file_size)
            case SortSource.FILE_CREATION_DATETIME:
                return sorted(data, key=lambda x: x.fs_creation_date)
            case SortSource.FILE_MODIFICATION_DATETIME:
                return sorted(data, key=lambda x: x.fs_modification_date)
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
