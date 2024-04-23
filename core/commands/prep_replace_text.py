from core.commons import BasePrepareCommand
from core.enums import ItemPositionExtended
from core.models.app_file import AppFile


class ReplaceTextPrepareCommand(BasePrepareCommand):
    def __init__(self, text: str = "", position: ItemPositionExtended = ItemPositionExtended.BEGIN):
        self.text: str = text
        self.position: ItemPositionExtended = position

    def create_new_name(self, item: AppFile) -> AppFile:
        return item

# TODO: add implementation
