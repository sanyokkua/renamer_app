from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.enums import TruncateOptions
from core.models.app_file import AppFile


class TruncateNamePrepareCommand(PrepareCommand):
    def __init__(self, number_of_symbols: int = 0,
                 truncate_options: TruncateOptions = TruncateOptions.TRUNCATE_EMPTY_SYMBOLS):
        self.number_of_symbols: int = number_of_symbols
        self.truncate_options: TruncateOptions = truncate_options

    def execute(
            self, data: List[AppFile], status_callback: Optional[StatusFunction]
    ) -> List[AppFile]:
        pass
