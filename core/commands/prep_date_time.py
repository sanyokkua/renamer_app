from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.enums import ItemPositionWithReplacement, DateFormat, TimeFormat, DateTimeFormat, DateTimeSource
from core.models.app_file import AppFile


class DateTimeRenamePrepareCommand(PrepareCommand):
    def __init__(self, position: ItemPositionWithReplacement = ItemPositionWithReplacement.REPLACE,
                 date_format: DateFormat = DateFormat.YYYY_MM_DD_TOGETHER,
                 time_format: TimeFormat = TimeFormat.HH_MM_SS_24_TOGETHER,
                 datetime_format: DateTimeFormat = DateTimeFormat.DATE_TIME_UNDERSCORED,
                 datetime_source: DateTimeSource = DateTimeSource.CONTENT_CREATION_DATE,
                 use_uppercase: bool = True):
        self.position: ItemPositionWithReplacement = position
        self.date_format: DateFormat = date_format
        self.time_format: TimeFormat = time_format
        self.datetime_format: DateTimeFormat = datetime_format
        self.datetime_source: DateTimeSource = datetime_source
        self.use_uppercase: bool = use_uppercase

    def execute(
            self, data: List[AppFile], status_callback: Optional[StatusFunction]
    ) -> List[AppFile]:
        pass
