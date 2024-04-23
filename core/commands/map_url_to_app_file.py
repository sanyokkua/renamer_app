from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile
from core.utils.other import build_app_file_from_path_str


class MapUrlToAppFileCommand(PrepareCommand):
    """
    A command class to map URL paths to AppFile objects.

    Inherits from PrepareCommand.

    Attributes:
        None

    Methods:
        execute(data: List[str], status_callback: Optional[StatusFunction]) -> List[AppFile]:
            Execute the command to map URL paths to AppFile objects.
    """

    def execute(self, data: List[str], status_callback: Optional[StatusFunction]) -> List[AppFile]:
        """
        Execute the command to map URL paths to AppFile objects.

        Args:
            data (List[str]): A list of URL paths to map to AppFile objects.
            status_callback (Optional[StatusFunction]): An optional callback function to report status.

        Raises:
            PassedArgumentIsNone: If the data argument is None.
            TypeError: If the data argument type is not List[str].

        Returns:
            List[AppFile]: A list of mapped AppFile objects.
        """
        if data is None:
            raise PassedArgumentIsNone()
        if not isinstance(data, list):
            raise TypeError("data argument type should be List[str]")

        mapped_app_files: list[AppFile] = []
        self.call_status_callback(status_callback, len(data), len(mapped_app_files))

        for path_url in data:
            app_file: AppFile = build_app_file_from_path_str(path_url)
            mapped_app_files.append(app_file)
            self.call_status_callback(status_callback, len(data), len(mapped_app_files))

        self.call_status_callback(status_callback, 100, 0)
        return mapped_app_files
