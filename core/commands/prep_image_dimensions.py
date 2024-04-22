from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.enums import ItemPositionWithReplacement, ImageDimensionOptions
from core.models.app_file import AppFile


class ImageDimensionsPrepareCommand(PrepareCommand):
    def __init__(self, position: ItemPositionWithReplacement = ItemPositionWithReplacement.BEGIN,
                 left_side: ImageDimensionOptions = ImageDimensionOptions.WIDTH,
                 right_side: ImageDimensionOptions = ImageDimensionOptions.HEIGHT,
                 separator_between: str = "x",
                 separator_before_or_after: str = "_"):
        self.position: ItemPositionWithReplacement = position
        self.left_side: ImageDimensionOptions = left_side
        self.right_side: ImageDimensionOptions = right_side
        self.separator_between: str = separator_between
        self.separator_before_or_after: str = separator_before_or_after

    def execute(self, data: List[AppFile], status_callback: Optional[StatusFunction]) -> List[AppFile]:
        print(self)
        return []
