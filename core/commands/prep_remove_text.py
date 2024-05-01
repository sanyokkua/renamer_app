from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand
from core.enums import ItemPosition
from core.models.app_file import AppFile


class RemoveTextPrepareCommand(AppFileItemByItemListProcessingCommand):
    """
    A command class to prepare files by removing specified text from their names.

    This class inherits from AppFileItemByItemListProcessingCommand.

    Attributes:
        text (str): The text to remove from the file names.
        position (ItemPosition): The position where the text removal will start.
            It can be either at the beginning or end of the file name.
    """

    def __init__(self, text: str = "", position: ItemPosition = ItemPosition.BEGIN):
        """
        Initializes the RemoveTextPrepareCommand with the specified parameters.

        Args:
            text (str, optional): The text to remove from the file names. Defaults to "".
            position (ItemPosition, optional): The position where the text removal will start.
                Defaults to ItemPosition.BEGIN.
        """
        self.text: str = text
        self.position: ItemPosition = position

    def item_by_item_process(self, item: AppFile, index: int, data: list[AppFile]) -> AppFile:
        """
        Creates a new name for the given AppFile object by removing specified text from its name.

        Args:
            item (AppFile): The AppFile object from which the specified text needs to be removed.
            index (int): The index of current item.
            data (list[AppFile]): The list of AppFile objects being processed.

        Returns:
            AppFile: The AppFile object with the specified text removed from its name.
        """
        if len(self.text) == 0:
            return item

        current_name: str = item.file_name

        match self.position:
            case ItemPosition.BEGIN:
                current_name = current_name.removeprefix(self.text)
            case ItemPosition.END:
                current_name = current_name.removesuffix(self.text)

        item.next_name = current_name

        return item
