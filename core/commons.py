from abc import ABC, abstractmethod
from typing import Callable, Optional
from typing import List

from core.models.app_file import AppFile

StatusFunction = Callable[[int, int, int], None]


class Command(ABC):
    @abstractmethod
    def execute(self, data, status_callback: Optional[StatusFunction]):
        pass


class PrepareCommand(Command):
    @abstractmethod
    def execute(self, data: List[AppFile], status_callback: Optional[StatusFunction]) -> List[AppFile]:
        pass
