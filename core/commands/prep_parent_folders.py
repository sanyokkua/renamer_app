from core.commons import BasePrepareCommand
from core.enums import ItemPosition
from core.models.app_file import AppFile
from core.utils.io_utils import get_parent_folders


class ParentFoldersPrepareCommand(BasePrepareCommand):
    """
    A command class to prepare files by including parent folder names in their names.

    This class inherits from BasePrepareCommand.

    Attributes:
        position (ItemPosition): The position where the parent folder names will be inserted.
            It can be either at the beginning or end of the file name.
        number_of_parents (int): The number of parent folders to include in the file name.
        separator (str): The separator between the parent folder names and the file name.
    """

    def __init__(
            self,
            position: ItemPosition = ItemPosition.BEGIN,
            number_of_parents: int = 1,
            separator: str = "_",
    ):
        """
        Initializes the ParentFoldersPrepareCommand with the specified parameters.

        Args:
            position (ItemPosition, optional): The position where the parent folder names will be inserted.
                Defaults to ItemPosition.BEGIN.
            number_of_parents (int, optional): The number of parent folders to include in the file name.
                Defaults to 1.
            separator (str, optional): The separator between the parent folder names and the file name.
                Defaults to "_".
        """
        self.position: ItemPosition = position
        self.number_of_parents: int = abs(number_of_parents)
        self.separator: str = separator

    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        """
        Creates a new name for the given AppFile object by including parent folder names in its name.

        Args:
            item (AppFile): The AppFile object for which the parent folder names need to be included in the name.
            index (int): The index of current item.

        Returns:
            AppFile: The AppFile object with the parent folder names included in its name.
        """
        if self.number_of_parents == 0:
            return item

        file_path_str: str = item.absolute_path

        parents: list[str] = get_parent_folders(file_path_str)
        parents.reverse()

        parents_list: list[str] = []
        number_of_parents: int = 0
        while number_of_parents < self.number_of_parents:
            if len(parents) > number_of_parents:
                parents_list.insert(0, parents[number_of_parents])
                number_of_parents += 1
            else:
                break

        parents_str: str = f"{self.separator}".join(parents_list)
        next_name: str = ""
        match self.position:
            case ItemPosition.BEGIN:
                next_name = f"{parents_str}{self.separator}{item.file_name}"
            case ItemPosition.END:
                parents_str.removesuffix(self.separator)
                next_name = f"{item.file_name}{self.separator}{parents_str}"

        item.next_name = next_name
        return item
