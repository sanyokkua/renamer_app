from core.commons import BasePrepareCommand
from core.enums import TruncateOptions
from core.models.app_file import AppFile


class TruncateNamePrepareCommand(BasePrepareCommand):
    def __init__(self, number_of_symbols: int = 0,
                 truncate_options: TruncateOptions = TruncateOptions.TRUNCATE_EMPTY_SYMBOLS):
        self.number_of_symbols: int = number_of_symbols
        self.truncate_options: TruncateOptions = truncate_options

    def create_new_name(self, item: AppFile) -> AppFile:
        return item

# TODO: add implementation
