from core.commons import BasePrepareCommand
from core.models.app_file import AppFile


class ExtensionChangePrepareCommand(BasePrepareCommand):
    """
    A command class to prepare files by changing their extensions.

    This class inherits from BasePrepareCommand.

    Attributes:
        new_extension (str): The new extension to be applied to the files.
            If not provided or an empty string, the extension will be removed from the files.
    """

    def __init__(self, new_extension: str = ""):
        """
        Initializes the ExtensionChangePrepareCommand with the specified new_extension.

        Args:
            new_extension (str, optional): The new extension to be applied to the files.
                Defaults to an empty string.
        """
        self.new_extension: str = new_extension

    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        """
        Creates a new name for the given AppFile object by changing its extension.

        If new_extension is an empty string, the extension is removed from the file.
        If new_extension does not start with a dot, it adds a dot before the extension.

        Args:
            item (AppFile): The AppFile object for which the extension needs to be changed.
            index (int): The index of current item.

        Returns:
            AppFile: The AppFile object with the new extension applied.
        """
        if self.new_extension.strip() == "":
            item.file_extension_new = ""  # Remove extension from file
        elif not self.new_extension.startswith(".") and len(self.new_extension) > 0:
            new_ext = f".{self.new_extension}"  # Add "." before extension (in case if user forget to include it)
            item.file_extension_new = new_ext
        else:
            item.file_extension_new = self.new_extension
        return item
