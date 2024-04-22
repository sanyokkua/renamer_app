from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.enums import SortSource
from core.models.app_file import AppFile


class SequencePrepareCommand(PrepareCommand):
    def __init__(self, start_number: int = 0, step_value: int = 1, padding: int = 0,
                 sort_source: SortSource = SortSource.FILE_NAME):
        self.start_number: int = start_number
        self.step_value: int = step_value
        self.padding: int = padding
        self.sort_source: SortSource = sort_source

    def execute(self, data: List[AppFile], status_callback: Optional[StatusFunction]) -> List[AppFile]:
        print(self)
        return []
