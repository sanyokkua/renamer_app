from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand
from core.models.app_file import AppFile
from core.utils.io_utils import rename_file
from core.utils.other import build_app_file_from_path_str


# TODO: Add unit tests


class RenameFilesCommand(AppFileItemByItemListProcessingCommand):
    """
    A command class to rename files.

    Inherits from AppFileItemByItemListProcessingCommand.

    Methods:
        item_by_item_process(data_item: AppFile, current_index: int, data: list[AppFile]) -> AppFile:
            Rename a file based on the provided AppFile object.

    """

    def item_by_item_process(self, data_item: AppFile, current_index: int, data: list[AppFile]) -> AppFile:
        """
        Rename a file based on the provided AppFile object.

        Args:
            data_item (AppFile): The AppFile object representing the file to be renamed.
            current_index (int): The index of the current item being processed.
            data (list[AppFile]): The list of AppFile objects being processed.

        Returns:
            AppFile: The updated AppFile object after renaming, or the original object if renaming fails.
        """
        if data_item.is_name_changed and data_item.is_valid():
            is_renamed, absolute_path = rename_file(data_item)
            if not is_renamed:
                return data_item

            return build_app_file_from_path_str(absolute_path)

        return data_item
