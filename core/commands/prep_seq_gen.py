from core.commons import BasePrepareCommand
from core.enums import SortSource
from core.models.app_file import AppFile


class SequencePrepareCommand(BasePrepareCommand):
    def __init__(self, start_number: int = 0, step_value: int = 1, padding: int = 0,
                 sort_source: SortSource = SortSource.FILE_NAME):
        self.start_number: int = start_number
        self.step_value: int = step_value
        self.padding: int = padding
        self.sort_source: SortSource = sort_source

    def create_new_name(self, item: AppFile) -> AppFile:
        return item

# TODO: add implementation
