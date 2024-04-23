from core.commons import BasePrepareCommand
from core.enums import ItemPosition
from core.models.app_file import AppFile


class ParentFoldersPrepareCommand(BasePrepareCommand):
    def __init__(self, position: ItemPosition = ItemPosition.BEGIN, number_of_parents: int = 1, separator: str = "_"):
        self.position: ItemPosition = position
        self.number_of_parents: int = number_of_parents
        self.separator: str = separator

    def create_new_name(self, item: AppFile) -> AppFile:
        return item

# TODO: add implementation
