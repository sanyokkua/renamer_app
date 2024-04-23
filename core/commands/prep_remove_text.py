from core.commons import BasePrepareCommand
from core.enums import ItemPosition
from core.models.app_file import AppFile


class RemoveTextPrepareCommand(BasePrepareCommand):
    def __init__(self, text: str = "", position: ItemPosition = ItemPosition.BEGIN):
        self.text: str = text
        self.position: ItemPosition = position

    def create_new_name(self, item: AppFile) -> AppFile:
        return item

# TODO: add implementation
