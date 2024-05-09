import datetime
from datetime import datetime

from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand
from core.enums import (
    DateFormat,
    DateTimeFormat,
    DateTimeSource,
    ItemPositionWithReplacement,
    TimeFormat,
)
from core.models.app_file import AppFile
from core.utils.datetime_utils import make_datetime_string


class DateTimeRenamePrepareCommand(AppFileItemByItemListProcessingCommand):
    """
    A command to rename files using date and time information.

    This command modifies the names of files by incorporating date and time information based on specified formats
    and sources.

    Attributes:
        position (ItemPositionWithReplacement): The position where the date and time information should be placed
            in the new name.
        date_format (DateFormat): The format of the date to be included in the new name.
        time_format (TimeFormat): The format of the time to be included in the new name.
        datetime_format (DateTimeFormat): The format of the combined date and time to be included in the new name.
        datetime_source (DateTimeSource): The source of the date and time information.
        use_uppercase (bool): A flag indicating whether AM/PM indicators should be in uppercase.
        custom_datetime (str): A custom date and time string to use if the source is set to CUSTOM_DATE.
        separator_for_name_and_datetime (str): The separator to use between the file name and the date-time string
            in the new name.
        use_fallback_dates (bool): A flag indicating whether to use fallback dates if the original dates are
            not available.
        use_fallback_date_timestamp (float | None): The timestamp to use as a fallback if original dates are
            not available.
    """

    def __init__(
        self,
        position: ItemPositionWithReplacement = ItemPositionWithReplacement.REPLACE,
        date_format: DateFormat = DateFormat.YYYY_MM_DD_TOGETHER,
        time_format: TimeFormat = TimeFormat.HH_MM_SS_24_TOGETHER,
        datetime_format: DateTimeFormat = DateTimeFormat.DATE_TIME_UNDERSCORED,
        datetime_source: DateTimeSource = DateTimeSource.CONTENT_CREATION_DATE,
        use_uppercase: bool = True,
        custom_datetime: str = "",
        separator_for_name_and_datetime: str = "",
        use_fallback_dates: bool = False,
        use_fallback_date_timestamp: float | None = None,
    ):
        """
        Initialize the DateTimeRenamePrepareCommand.

        Args:
            position (ItemPositionWithReplacement, optional): The position where the date and time information should
                be placed in the new name. Defaults to ItemPositionWithReplacement.REPLACE.
            date_format (DateFormat, optional): The format of the date to be included in the new name.
                Defaults to DateFormat.YYYY_MM_DD_TOGETHER.
            time_format (TimeFormat, optional): The format of the time to be included in the new name.
                Defaults to TimeFormat.HH_MM_SS_24_TOGETHER.
            datetime_format (DateTimeFormat, optional): The format of the combined date and time to be included
                in the new name. Defaults to DateTimeFormat.DATE_TIME_UNDERSCORED.
            datetime_source (DateTimeSource, optional): The source of the date and time information.
                Defaults to DateTimeSource.CONTENT_CREATION_DATE.
            use_uppercase (bool, optional): A flag indicating whether AM/PM indicators should be in uppercase.
                Defaults to True.
            custom_datetime (str, optional): A custom date and time string to use if the source is set to CUSTOM_DATE.
                Defaults to "".
            separator_for_name_and_datetime (str, optional): The separator to use between the file name
                and the date-time string in the new name. Defaults to "".
            use_fallback_dates (bool, optional): A flag indicating whether to use fallback dates
                if the original dates are not available. Defaults to False.
            use_fallback_date_timestamp (float | None, optional): The timestamp to use as a fallback
                if original dates are not available. Defaults to None.
        """
        self.position: ItemPositionWithReplacement = position
        self.date_format: DateFormat = date_format
        self.time_format: TimeFormat = time_format
        self.datetime_format: DateTimeFormat = datetime_format
        self.datetime_source: DateTimeSource = datetime_source
        self.use_uppercase: bool = use_uppercase
        self.custom_datetime: str = custom_datetime
        self.separator_for_name_and_datetime = separator_for_name_and_datetime
        self.use_fallback_dates: bool = use_fallback_dates
        self.use_fallback_date_timestamp: float | None = use_fallback_date_timestamp

    def item_by_item_process(self, item: AppFile, index: int, data: list[AppFile]) -> AppFile:
        """
        Create a new name for the AppFile item based on the specified date and time formats and sources.

        Args:
            item (AppFile): The AppFile item for which the new name will be created.
            index (int): The index of current item.
            data (list[AppFile]): The list of AppFile objects being processed.

        Returns:
            AppFile: The AppFile item with the new name.
        """
        current_name: str = item.file_name

        timestamp: float | None = self.get_timestamp(item)

        if timestamp is None or timestamp == 0:
            return item

        date_time_str: str = make_datetime_string(self.datetime_format, self.date_format, self.time_format, timestamp)
        next_name: str = self.build_next_name(current_name, date_time_str)
        next_name = self.fix_case_of_am_pm(next_name)

        item.next_name = next_name

        return item

    def get_timestamp(self, item: AppFile) -> float | None:
        """
        Get the timestamp based on the specified source of date and time information.

        Args:
            item (AppFile): The AppFile item containing metadata.

        Returns:
            float: The timestamp representing the date and time.
        """
        timestamp: float = 0
        match self.datetime_source:
            case DateTimeSource.FILE_CREATION_DATE:
                timestamp = item.fs_creation_date
            case DateTimeSource.FILE_MODIFICATION_DATE:
                timestamp = item.fs_modification_date
            case DateTimeSource.CONTENT_CREATION_DATE:
                timestamp = None if item.metadata is None else item.metadata.creation_date
            case DateTimeSource.CURRENT_DATE:
                now: datetime = datetime.now()
                timestamp = now.timestamp()
            case DateTimeSource.CUSTOM_DATE:
                dt: datetime = datetime.strptime(self.custom_datetime, "%Y%m%d_%H%M%S")
                timestamp = dt.timestamp()

        if self.use_fallback_dates and (timestamp is None or timestamp == 0):
            if self.use_fallback_date_timestamp is not None:
                timestamp = self.use_fallback_date_timestamp
            else:
                cr_d = item.fs_creation_date
                mod_d = item.fs_modification_date
                cc_d = None if item.metadata is None else item.metadata.creation_date
                dates = [date for date in [cr_d, mod_d, cc_d] if date is not None]
                timestamp = min(dates) if len(dates) > 0 else None

        return timestamp

    def build_next_name(self, current_name: str, date_time_str: str) -> str:
        """
        Build the next name by combining the current file name and the formatted date-time string.

        Args:
            current_name (str): The current file name.
            date_time_str (str): The formatted date-time string.

        Returns:
            str: The next name for the file.
        """
        next_name: str = ""
        match self.position:
            case ItemPositionWithReplacement.BEGIN:
                next_name = f"{date_time_str}{self.separator_for_name_and_datetime}{current_name}"
            case ItemPositionWithReplacement.END:
                next_name = f"{current_name}{self.separator_for_name_and_datetime}{date_time_str}"
            case ItemPositionWithReplacement.REPLACE:
                next_name = date_time_str
        return next_name

    def fix_case_of_am_pm(self, next_name) -> str:
        """
        Fix the case of the AM/PM indicators based on the specified option.

        Args:
            next_name: The name containing AM/PM indicators.

        Returns:
            str: The name with corrected case for AM/PM indicators.
        """
        if self.use_uppercase:
            if next_name.endswith("am"):
                next_name = next_name.replace("am", "AM")
            elif next_name.endswith("pm"):
                next_name = next_name.replace("pm", "PM")
        else:
            if next_name.endswith("AM"):
                next_name = next_name.replace("AM", "am")
            elif next_name.endswith("PM"):
                next_name = next_name.replace("PM", "pm")
        return next_name
