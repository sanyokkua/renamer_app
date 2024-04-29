from abc import ABC, abstractmethod
from typing import Callable, Optional
from typing import List

from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile

StatusFunction = Callable[[int, int, int], None]


class Command(ABC):
    """
    An abstract base class representing a command.

    Attributes:
        None
    """

    @staticmethod
    def call_status_callback(
            status_callback: Optional[StatusFunction], max_val: int, current_val: int
    ):
        """
        Calls the status callback function if provided.

        Args:
            status_callback (Optional[StatusFunction]): A function to call for status updates.
            max_val (int): The maximum value.
            current_val (int): The current value.

        Returns:
            None
        """
        if status_callback is not None:
            status_callback(0, max_val, current_val)

    @abstractmethod
    def execute(self, data, status_callback: Optional[StatusFunction]):
        """
        Executes the command.

        Args:
            data: The input data for the command.
            status_callback (Optional[StatusFunction]): A function to call for status updates.

        Returns:
            None
        """
        pass


class PrepareCommand(Command):
    """
    An abstract class representing a preparation command.

    Attributes:
        None
    """

    @abstractmethod
    def execute(
            self, data: List[AppFile], status_callback: Optional[StatusFunction]
    ) -> List[AppFile]:
        """
        Executes the preparation command.

        Args:
            data (List[AppFile]): The list of AppFile items to process.
            status_callback (Optional[StatusFunction]): A function to call for status updates.

        Returns:
            List[AppFile]: The list of processed AppFile items.
        """
        pass


class BasePrepareCommand(Command):
    """
    An abstract class representing a base preparation command.

    Attributes:
        None
    """

    def execute(
            self, data: List[AppFile], status_callback: Optional[StatusFunction] = None
    ) -> List[AppFile]:
        """
        Executes the base preparation command.

        Args:
            data (List[AppFile]): The list of AppFile items to process.
            status_callback (Optional[StatusFunction]): A function to call for status updates.

        Returns:
            List[AppFile]: The list of processed AppFile items.

        Raises:
            PassedArgumentIsNone: If the input data is None.
            TypeError: If the input data type is not List[AppFile].
        """
        if data is None:
            raise PassedArgumentIsNone()
        if not isinstance(data, list):
            raise TypeError("data argument type should be List[AppFile]")

        mapped_files: list[AppFile] = []
        self.call_status_callback(status_callback, len(data), len(mapped_files))
        data = self.sort_date(data)

        index: int = 0
        for item in data:
            self.create_new_name(item, index)
            mapped_files.append(item)
            self.call_status_callback(status_callback, len(data), len(mapped_files))
            index += 1

        self.call_status_callback(status_callback, 100, 0)
        return mapped_files

    @abstractmethod
    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        """
        Creates a new name for the given AppFile item.

        Args:
            item (AppFile): The AppFile item for which the new name will be created.
            index (int): The index of current item

        Returns:
            AppFile: The AppFile item with the new name.
        """
        pass

    def sort_date(self, data: List[AppFile]) -> List[AppFile]:
        return data
