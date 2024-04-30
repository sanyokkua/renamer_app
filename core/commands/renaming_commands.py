from typing import List, Optional

from core.commons import StatusFunction, Command
from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile
from core.utils.io_utils import rename_file


# TODO: Requires testing


class FilterOutNotChangedFilesCommand(Command):

    def execute(
        self, data: List[AppFile], status_callback: Optional[StatusFunction] = None
    ) -> List[AppFile]:
        if data is None:
            raise PassedArgumentIsNone()

        if not isinstance(data, list):
            raise TypeError("data argument type should be List[AppFile]")

        return [item for item in data if item.is_name_changed]


class FilterOutFilesWithErrorsCommand(Command):

    def execute(
        self, data: List[AppFile], status_callback: Optional[StatusFunction] = None
    ) -> List[AppFile]:
        if data is None:
            raise PassedArgumentIsNone()

        if not isinstance(data, list):
            raise TypeError("data argument type should be List[AppFile]")

        return [item for item in data if item.is_valid()]


class RenameFilesCommand(Command):

    def execute(
        self, data: List[AppFile], status_callback: Optional[StatusFunction]
    ) -> List[AppFile]:
        if data is None:
            raise PassedArgumentIsNone()

        if not isinstance(data, list):
            raise TypeError("data argument type should be List[AppFile]")

        renamed_files: list[AppFile] = []
        self.call_status_callback(status_callback, len(data), len(renamed_files))

        for item in data:
            rename_file(item)
            renamed_files.append(item)
            self.call_status_callback(status_callback, len(data), len(renamed_files))

        self.call_status_callback(status_callback, 100, 0)
        return renamed_files
