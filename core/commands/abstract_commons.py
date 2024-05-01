from core.abstract import ItemByItemListProcessingCommand
from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile


def app_file_list_validation(data: list[AppFile] | None | object):
    """Validates a list of AppFile objects.

    Args:
        data (list[AppFile] | None | object): Input data to validate.

    Raises:
        PassedArgumentIsNone: If the input data is None.
        TypeError: If the input data is not a list or contains items that are not of type AppFile.
    """
    if data is None:
        raise PassedArgumentIsNone("Passed list[AppFile] is None, but expected non None value")
    if not isinstance(data, list):
        raise TypeError("data argument type should be list[AppFile]")
    for item in data:
        if not isinstance(item, AppFile):
            raise TypeError(f"One of data items is not of type AppFile, item: {item}")


class AppFileItemByItemListProcessingCommand(ItemByItemListProcessingCommand):
    """Concrete base implementation of ItemByItemListProcessingCommand for AppFile objects."""

    def data_validation(self, data: list[AppFile]):
        """Validates the input data.

        Args:
            data (list[AppFile]): Input data to validate.

        Raises:
            PassedArgumentIsNone: If the input data is None.
            TypeError: If the input data is not a list or contains items that are not of type AppFile.
        """
        app_file_list_validation(data)

    def item_by_item_preprocess(self, data_item: AppFile, current_index: int, data: list[AppFile]) -> AppFile:
        """Preprocesses a single item in the input data.

        Args:
            data_item (AppFile): Data item to preprocess.
            current_index (int): Current index.
            data (list[AppFile]): Input data.

        Returns:
            AppFile: Preprocessed data item.
        """
        return data_item

    def item_by_item_process(self, data_item: AppFile, current_index: int, data: list[AppFile]) -> AppFile:
        """Processes a single item in the input data.

        Args:
            data_item (AppFile): Data item to process.
            current_index (int): Current index.
            data (list[AppFile]): Input data.

        Returns:
            AppFile: Processed data item.
        """
        return data_item

    def item_by_item_postprocess(self, data_item: AppFile, current_index: int, data: list[AppFile]) -> AppFile:
        """Post-processes a single item in the input data.

        Args:
            data_item (AppFile): Data item to postprocess.
            current_index (int): Current index.
            data (list[AppFile]): Input data.

        Returns:
            AppFile: Post-processed data item.
        """
        return data_item
