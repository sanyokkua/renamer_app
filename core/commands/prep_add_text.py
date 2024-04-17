from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.enums import ItemPosition
from core.models.app_file import AppFile


class AddTextPrepareCommand(PrepareCommand):
    def __init__(self, text: str = "", position: ItemPosition = ItemPosition.BEGIN):
        self.text: str = text
        self.position: ItemPosition = position

    def execute(
            self, data: List[AppFile], status_callback: Optional[StatusFunction]
    ) -> List[AppFile]:
        pass
