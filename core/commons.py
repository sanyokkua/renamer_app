from abc import ABC, abstractmethod
from typing import Callable, Optional
from typing import List

from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile

StatusFunction = Callable[[int, int, int], None]


class Command(ABC):
    @staticmethod
    def call_status_callback(status_callback: Optional[StatusFunction], max_val: int, current_val: int):
        if status_callback is not None:
            status_callback(0, max_val, current_val)

    @abstractmethod
    def execute(self, data, status_callback: Optional[StatusFunction]):
        pass


class PrepareCommand(Command):
    @abstractmethod
    def execute(self, data: List[AppFile], status_callback: Optional[StatusFunction]) -> List[AppFile]:
        pass


class BasePrepareCommand(Command):

    def execute(self, data: List[AppFile], status_callback: Optional[StatusFunction] = None) -> List[AppFile]:
        if data is None:
            raise PassedArgumentIsNone()
        if not isinstance(data, list):
            raise TypeError("data argument type should be List[AppFile]")

        mapped_files: list[AppFile] = []
        self.call_status_callback(status_callback, len(data), len(mapped_files))

        for item in data:
            self.create_new_name(item)
            mapped_files.append(item)
            self.call_status_callback(status_callback, len(data), len(mapped_files))

        self.call_status_callback(status_callback, 100, 0)
        return mapped_files

    @abstractmethod
    def create_new_name(self, item: AppFile) -> AppFile:
        pass
