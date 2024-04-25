from core.commons import BasePrepareCommand
from core.enums import TextCaseOptions
from core.models.app_file import AppFile
from core.utils.case_utils import convert_case_of_string


class ChangeCasePreparePrepareCommand(BasePrepareCommand):
    """
    A class representing a command to change the case of file names in a preparation pipeline.

    This command takes an AppFile item and modifies its name by changing its case according to the specified options.

    Attributes:
        capitalize (bool): A flag indicating whether to capitalize the first letter of the new name.
        text_case (TextCaseOptions): The desired text case option to apply to the file names.
    """

    def __init__(self, capitalize: bool = False, text_case: TextCaseOptions = TextCaseOptions.TITLE_CASE):
        """
        Initialize the ChangeCasePreparePrepareCommand.

        Args:
            capitalize (bool, optional): Flag indicating whether to capitalize the first letter of the new name. Defaults to False.
            text_case (TextCaseOptions, optional): The desired text case option. Defaults to TextCaseOptions.TITLE_CASE.
        """
        self.capitalize: bool = capitalize
        self.text_case: TextCaseOptions = text_case

    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        """
        Create a new name for the AppFile item based on the specified text case option.

        Args:
            item (AppFile): The AppFile item for which the new name will be created.
            index (int): The index of current item.

        Returns:
            AppFile: The AppFile item with the new name.
        """
        curr_file_name: str = item.file_name
        next_file_name: str = convert_case_of_string(curr_file_name, self.text_case)
        if self.capitalize:
            next_file_name = next_file_name[0].upper() + next_file_name[1:]

        print(f"Orig: {item.file_name}, next: {next_file_name}, func: {self.text_case.name}, is cap: {self.capitalize}")
        item.next_name = next_file_name
        return item
