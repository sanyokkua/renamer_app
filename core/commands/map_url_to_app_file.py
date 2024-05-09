from typing import List, Optional

from core.abstract import InputValidationCommand, StatusFunction
from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile
from core.utils.other import build_app_file_from_path_str


class MapUrlToAppFileCommand(InputValidationCommand):
    """
    A command class to map URL paths to AppFile objects.

    Inherits from InputValidationCommand.

    Methods:
        execute(data: List[str], status_callback: Optional[StatusFunction]) -> List[AppFile]:
            Execute the command to map URL paths to AppFile objects.
    """

    def data_validation(self, data):
        """
        Validate the input data.

        Args:
            data: Input data.

        Raises:
            PassedArgumentIsNone: If the input data is None.
            TypeError: If the input data type is not List[str].
        """
        if data is None:
            raise PassedArgumentIsNone()
        if not isinstance(data, list):
            raise TypeError("data argument type should be List[str]")
        for item in data:
            if not isinstance(item, str):
                raise TypeError(f"One of data items is not of type AppFile, item: {item}")

    def process_cmd_data(self, data: List[str], status_callback: Optional[StatusFunction] = None) -> List[AppFile]:
        """
        Execute the command to map URL paths to AppFile objects.

        Args:
            data (List[str]): A list of URL paths to map to AppFile objects.
            status_callback (Optional[StatusFunction]): An optional callback function to report status.

        Returns:
            List[AppFile]: A list of mapped AppFile objects.
        """

        mapped_app_files: list[AppFile] = []
        self.call_status_callback(status_callback, len(mapped_app_files), len(data))

        for path_url in data:
            app_file: AppFile = build_app_file_from_path_str(path_url)
            mapped_app_files.append(app_file)
            self.call_status_callback(status_callback, len(mapped_app_files), len(data))

        self.call_status_callback(status_callback, 0, 100)
        return mapped_app_files
