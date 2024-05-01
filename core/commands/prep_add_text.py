from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand
from core.enums import ItemPosition
from core.models.app_file import AppFile


class AddTextPrepareCommand(AppFileItemByItemListProcessingCommand):
    """
    A command class to add text to the beginning or end of an AppFile's name.

    Inherits from AppFileItemByItemListProcessingCommand.

    Attributes:
        text (str): The text to be added.
        position (ItemPosition): The position where the text should be added (BEGIN or END).

    Methods:
        item_by_item_process(item: AppFile, index: int, data: list[AppFile]) -> AppFile:
            Create a new name for the AppFile by adding text to the beginning or end.
    """

    def __init__(self, text: str = "", position: ItemPosition = ItemPosition.BEGIN):
        """
        Initialize the AddTextPrepareCommand with the specified text and position.

        Args:
            text (str, optional): The text to be added. Defaults to "".
            position (ItemPosition, optional): The position where the text should be added (BEGIN or END).
                Defaults to ItemPosition.BEGIN.
        """
        self.text: str = text
        self.position: ItemPosition = position

    def item_by_item_process(self, item: AppFile, index: int, data: list[AppFile]) -> AppFile:
        """
        Create a new name for the AppFile by adding text to the beginning or end.

        Args:
            item (AppFile): The AppFile object for which the new name is to be created.
            index (int): The index of current item.
            data (list[AppFile]): The list of AppFile objects being processed.

        Returns:
            AppFile: The AppFile object with the new name.
        """
        current_name = item.file_name
        new_name = item.next_name
        match self.position:
            case ItemPosition.BEGIN:
                new_name = f"{self.text}{current_name}"
            case ItemPosition.END:
                new_name = f"{current_name}{self.text}"
        item.next_name = new_name
        return item
