from abc import ABC, abstractmethod
from typing import Callable, Optional

StatusFunction = Callable[[int, int], None]
"""Type alias for status callback function.

Args:
    int: Current value.
    int: Maximum value.
"""


class Command(ABC):
    """Abstract base class for command execution."""

    @staticmethod
    def call_status_callback(status_callback: Optional[StatusFunction], current_val: int, max_val: int):
        """Calls the status callback function if provided.

        Args:
            status_callback (Optional[StatusFunction]): Status callback function.
            current_val (int): Current value.
            max_val (int): Maximum value.
        """
        if status_callback is not None:
            status_callback(current_val, max_val)

    @abstractmethod
    def execute(self, data, status_callback: Optional[StatusFunction] = None):
        """Abstract method to execute the command.

        Args:
            data: Input data.
            status_callback (Optional[StatusFunction]): Status callback function.
        """
        pass


class InputValidationCommand(Command):
    """Abstract base class for input validation command."""

    def execute(self, data, status_callback: Optional[StatusFunction] = None):
        """Executes the command.

        Args:
            data: Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            Any: Processed result.
        """
        self.data_validation(data)
        self.callback_validation(status_callback)

        process_result = self.process_cmd_data(data, status_callback)

        return process_result

    @abstractmethod
    def process_cmd_data(self, data, status_callback: Optional[StatusFunction] = None):
        """Abstract method to process command data.

        Args:
            data: Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            Any: Processed result.
        """
        pass

    @abstractmethod
    def data_validation(self, data):
        """Abstract method to validate input data.

        Args:
            data: Input data.
        """
        pass

    def callback_validation(self, status_callback: Optional[StatusFunction]):
        """Validates the status callback function.

        Args:
            status_callback (Optional[StatusFunction]): Status callback function.

        Raises:
            TypeError: If the status callback is not callable or not of type StatusFunction.
        """
        if status_callback is None:
            # If status_callback is None - it is an appropriate value
            return

        if not callable(status_callback):
            raise TypeError("Argument is not a callable function")


class ThreeStepsListProcessingCommand(InputValidationCommand):
    """Abstract base class for three-step list processing command."""

    def process_cmd_data(self, data: list, status_callback: Optional[StatusFunction] = None) -> list:
        """Processes the command data.

        Args:
            data (list): Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            list: Processed data.
        """
        preprocess_data: list = self.preprocess_data(data, status_callback)
        process_data: list = self.process_data(preprocess_data, status_callback)
        post_process_data: list = self.postprocess_data(process_data, status_callback)

        return post_process_data

    @abstractmethod
    def preprocess_data(self, data: list, status_callback: Optional[StatusFunction] = None) -> list:
        """Abstract method to preprocess data.

        Args:
            data (list): Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            list: Preprocessed data.
        """
        pass

    @abstractmethod
    def process_data(self, data: list, status_callback: Optional[StatusFunction] = None) -> list:
        """Abstract method to process data.

        Args:
            data (list): Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            list: Processed data.
        """
        pass

    @abstractmethod
    def postprocess_data(self, data: list, status_callback: Optional[StatusFunction] = None) -> list:
        """Abstract method to postprocess data.

        Args:
            data (list): Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            list: Postprocessed data.
        """
        pass


class ItemByItemListProcessingCommand(ThreeStepsListProcessingCommand):
    """Abstract base class for item-by-item list processing command."""

    def preprocess_data(self, data: list, status_callback: Optional[StatusFunction] = None) -> list:
        """Preprocesses the data item by item.

        Args:
            data (list): Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            list: Preprocessed data.
        """
        data_items: list = []
        index = 0
        self.call_status_callback(status_callback, len(data_items), len(data))

        for item in data:
            new_item = self.item_by_item_preprocess(item, index, data)
            data_items.append(new_item)
            index += 1
            self.call_status_callback(status_callback, len(data_items), len(data))

        self.call_status_callback(status_callback, 0, len(data))
        return data_items

    def process_data(self, data: list, status_callback: Optional[StatusFunction] = None) -> list:
        """Processes the data item by item.

        Args:
            data (list): Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            list: Processed data.
        """
        data_items: list = []
        index = 0
        self.call_status_callback(status_callback, len(data_items), len(data))

        for item in data:
            new_item = self.item_by_item_process(item, index, data)
            data_items.append(new_item)
            index += 1
            self.call_status_callback(status_callback, len(data_items), len(data))

        self.call_status_callback(status_callback, 0, len(data))
        return data_items

    def postprocess_data(self, data: list, status_callback: Optional[StatusFunction] = None) -> list:
        """Postprocesses the data item by item.

        Args:
            data (list): Input data.
            status_callback (Optional[StatusFunction]): Status callback function.

        Returns:
            list: Postprocessed data.
        """
        data_items: list = []
        index = 0
        self.call_status_callback(status_callback, len(data_items), len(data))

        for item in data:
            new_item = self.item_by_item_postprocess(item, index, data)
            data_items.append(new_item)
            index += 1
            self.call_status_callback(status_callback, len(data_items), len(data))

        self.call_status_callback(status_callback, 0, len(data))
        return data_items

    @abstractmethod
    def item_by_item_preprocess(self, data_item, current_index: int, data: list):
        """Abstract method to preprocess data item by item.

        Args:
            data_item: Data item.
            current_index (int): Current index.
            data (list): Input data.
        """
        pass

    @abstractmethod
    def item_by_item_process(self, data_item, current_index: int, data: list):
        """Abstract method to process data item by item.

        Args:
            data_item: Data item.
            current_index (int): Current index.
            data (list): Input data.
        """
        pass

    @abstractmethod
    def item_by_item_postprocess(self, data_item, current_index: int, data: list):
        """Abstract method to postprocess data item by item.

        Args:
            data_item: Data item.
            current_index (int): Current index.
            data (list): Input data.
        """
        pass
