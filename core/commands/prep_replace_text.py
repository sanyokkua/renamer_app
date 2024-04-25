from core.commons import BasePrepareCommand
from core.enums import ItemPositionExtended
from core.models.app_file import AppFile


class ReplaceTextPrepareCommand(BasePrepareCommand):
    """
    A command class to prepare files by replacing specified text with a new value in their names.

    This class inherits from BasePrepareCommand.

    Attributes:
        position (ItemPositionExtended): The position where the text replacement will occur.
            It can be at the beginning, end, or everywhere in the file name.
        text_to_replace (str): The text to be replaced in the file names.
        new_value (str): The new value to replace the specified text in the file names.
    """

    def __init__(self, position: ItemPositionExtended = ItemPositionExtended.BEGIN, text_to_replace: str = "",
                 new_value: str = ""):
        """
        Initializes the ReplaceTextPrepareCommand with the specified parameters.

        Args:
            position (ItemPositionExtended, optional): The position where the text replacement will occur.
                Defaults to ItemPositionExtended.BEGIN.
            text_to_replace (str, optional): The text to be replaced in the file names. Defaults to "".
            new_value (str, optional): The new value to replace the specified text in the file names. Defaults to "".
        """
        self.position: ItemPositionExtended = position
        self.text_to_replace: str = text_to_replace
        self.new_value: str = new_value

    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        """
        Creates a new name for the given AppFile object by replacing specified text with a new value.

        Args:
            item (AppFile): The AppFile object in which the text replacement needs to be performed.
            index (int): The index of current item.

        Returns:
            AppFile: The AppFile object with the specified text replaced by the new value in its name.
        """
        if len(self.text_to_replace) == 0:
            return item

        next_name: str = item.file_name

        if self.position == ItemPositionExtended.BEGIN:
            next_name = next_name.replace(self.text_to_replace, self.new_value, 1)
        elif self.position == ItemPositionExtended.END:
            reversed_name = next_name[::-1]
            reversed_value_to_replace = self.text_to_replace[::-1]
            reversed_new_value = self.new_value[::-1]
            next_name = reversed_name.replace(reversed_value_to_replace, reversed_new_value, 1)[::-1]
        elif self.position == ItemPositionExtended.EVERYWHERE:
            next_name = next_name.replace(self.text_to_replace, self.new_value)

        item.next_name = next_name

        return item
