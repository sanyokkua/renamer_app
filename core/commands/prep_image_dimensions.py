from core.commons import BasePrepareCommand
from core.enums import ItemPositionWithReplacement, ImageDimensionOptions
from core.models.app_file import AppFile


class ImageDimensionsPrepareCommand(BasePrepareCommand):
    """
    A command class to prepare files by including image dimensions in their names.

    This class inherits from BasePrepareCommand.

    Attributes:
        position (ItemPositionWithReplacement): The position where the dimensions will be inserted.
            It can be either at the beginning, end, or replace the existing name.
        left_side (ImageDimensionOptions): Determines which dimension (width or height) will be on the left side.
        right_side (ImageDimensionOptions): Determines which dimension (width or height) will be on the right side.
        dimension_separator (str): The separator between the two dimensions.
        name_separator (str): The separator to be placed before or after the dimensions.
    """

    def __init__(self, position: ItemPositionWithReplacement = ItemPositionWithReplacement.BEGIN,
                 left_side: ImageDimensionOptions = ImageDimensionOptions.WIDTH,
                 right_side: ImageDimensionOptions = ImageDimensionOptions.HEIGHT,
                 dimension_separator: str = "x",
                 name_separator: str = "_"):
        """
        Initializes the ImageDimensionsPrepareCommand with the specified parameters.

        Args:
            position (ItemPositionWithReplacement, optional): The position where the dimensions will be inserted.
                Defaults to ItemPositionWithReplacement.BEGIN.
            left_side (ImageDimensionOptions, optional): Determines which dimension (width or height) will be on the left side.
                Defaults to ImageDimensionOptions.WIDTH.
            right_side (ImageDimensionOptions, optional): Determines which dimension (width or height) will be on the right side.
                Defaults to ImageDimensionOptions.HEIGHT.
            dimension_separator (str, optional): The separator between the two dimensions.
                Defaults to "x".
            name_separator (str, optional): The separator to be placed before or after the dimensions.
                Defaults to "_".
        """
        self.position: ItemPositionWithReplacement = position
        self.left_side: ImageDimensionOptions = left_side
        self.right_side: ImageDimensionOptions = right_side
        self.dimension_separator: str = dimension_separator
        self.name_separator: str = name_separator

    def create_new_name(self, item: AppFile, index: int) -> AppFile:
        """
        Creates a new name for the given AppFile object by including image dimensions in its name.

        Args:
            item (AppFile): The AppFile object for which the image dimensions need to be included in the name.
            index (int): The index of current item.

        Returns:
            AppFile: The AppFile object with the image dimensions included in its name.
        """
        if item.metadata is None or item.metadata.img_vid_width is None or item.metadata.img_vid_height is None:
            return item

        width: int = item.metadata.img_vid_width
        height: int = item.metadata.img_vid_height
        left_side: int = width if self.left_side == ImageDimensionOptions.WIDTH else height
        right_side: int = width if self.right_side == ImageDimensionOptions.WIDTH else height

        # ABS is used in case if negative number will be recorded in metadata
        dimension: str = f"{abs(left_side)}{self.dimension_separator}{abs(right_side)}"
        next_name: str = ""

        match self.position:
            case ItemPositionWithReplacement.BEGIN:
                next_name = f"{dimension}{self.name_separator}{item.file_name}"
            case ItemPositionWithReplacement.END:
                next_name = f"{item.file_name}{self.name_separator}{dimension}"
            case ItemPositionWithReplacement.REPLACE:
                next_name = dimension

        item.next_name = next_name
        return item
