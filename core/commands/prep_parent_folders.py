from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.enums import ItemPosition
from core.models.app_file import AppFile


class ParentFoldersPrepareCommand(PrepareCommand):
    def __init__(self, position: ItemPosition = ItemPosition.BEGIN, number_of_parents: int = 1, separator: str = "_"):
        self.position: ItemPosition = position
        self.number_of_parents: int = number_of_parents
        self.separator: str = separator

    def execute(
            self, data: List[AppFile], status_callback: Optional[StatusFunction]
    ) -> List[AppFile]:
        pass
