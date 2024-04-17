from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.enums import ItemPositionExtended
from core.models.app_file import AppFile


class ReplaceTextPrepareCommand(PrepareCommand):
    def __init__(self, text: str = "", position: ItemPositionExtended = ItemPositionExtended.BEGIN):
        self.text: str = text
        self.position: ItemPositionExtended = position

    def execute(
            self, data: List[AppFile], status_callback: Optional[StatusFunction]
    ) -> List[AppFile]:
        pass
