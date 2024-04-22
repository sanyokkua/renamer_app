from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.enums import TextCaseOptions
from core.models.app_file import AppFile


class ChangeCasePreparePrepareCommand(PrepareCommand):
    def __init__(self, capitalize: bool = False, text_case: TextCaseOptions = TextCaseOptions.TITLE_CASE):
        self.capitalize: bool = capitalize
        self.text_case: TextCaseOptions = text_case

    def execute(self, data: List[AppFile], status_callback: Optional[StatusFunction]) -> List[AppFile]:
        print(self)
        return []
