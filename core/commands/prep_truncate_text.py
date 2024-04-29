from core.commons import BasePrepareCommand
from core.enums import TruncateOptions
from core.models.app_file import AppFile


class TruncateNamePrepareCommand(BasePrepareCommand):
    """
    A command class for truncating filenames.

    Attributes:
        number_of_symbols (int): The number of symbols to keep or remove.
        truncate_options (TruncateOptions): The options for truncating filenames.

    Methods:
        create_new_name(item: AppFile, index: int) -> AppFile: Creates a new filename by truncating the original filename.
    """

    def __init__(
            self,
            number_of_symbols: int = 0,
            truncate_options: TruncateOptions = TruncateOptions.TRUNCATE_EMPTY_SYMBOLS,
    ):
        """
        Initializes a TruncateNamePrepareCommand instance.

        Args:
            number_of_symbols (int, optional): The number of symbols to keep or remove. Defaults to 0.
            truncate_options (TruncateOptions, optional): The options for truncating filenames.
                Defaults to TruncateOptions.TRUNCATE_EMPTY_SYMBOLS.
        """
        self.number_of_symbols: int = number_of_symbols
        self.truncate_options: TruncateOptions = truncate_options

    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        """
        Creates a new filename by truncating the original filename.

        Args:
            item (AppFile): The AppFile item.
            index (int): The index of the current item.

        Returns:
            AppFile: The AppFile item with the new filename.
        """

        next_name: str = item.file_name

        if (
                self.truncate_options != TruncateOptions.TRUNCATE_EMPTY_SYMBOLS
                and self.number_of_symbols > len(next_name)
        ):
            item.next_name = ""
            return item

        if (
                self.truncate_options != TruncateOptions.TRUNCATE_EMPTY_SYMBOLS
                and self.number_of_symbols == 0
        ):
            item.next_name = next_name
            return item

        match self.truncate_options:
            case TruncateOptions.TRUNCATE_EMPTY_SYMBOLS:
                next_name = next_name.strip()
            case TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN:
                trimmed_string = next_name[self.number_of_symbols:]
                next_name = trimmed_string
            case TruncateOptions.REMOVE_SYMBOLS_FROM_END:
                trimmed_string = next_name[: -self.number_of_symbols]
                next_name = trimmed_string

        item.next_name = next_name
        return item
