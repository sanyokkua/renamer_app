from abc import ABC, abstractmethod

from core.constants import DateTimePlacing, DateTimeFormats, DateTimeSource
from core.models import FileItemModel


class CaseIsNotSupported(Exception):
    def __init__(self, message="An error occurred"):
        self.message = message
        super().__init__(self.message)


class Command(ABC):
    @abstractmethod
    def execute(self, list_of_files: list[FileItemModel]) -> list[FileItemModel]:
        pass


class RenameByPatternCommand(Command):
    def __init__(self, sequence_start: int = 0, use_seq: bool = True, prefix_to_add: str = "", suffix_to_add: str = ""):
        self.sequence_start: int = sequence_start
        self.use_seq: bool = use_seq
        self.prefix_to_add: str = prefix_to_add
        self.suffix_to_add: str = suffix_to_add

    def execute(self, list_of_files: list[FileItemModel]) -> list[FileItemModel]:
        renamed_files: list[FileItemModel] = []

        for index, file_model in enumerate(list_of_files):
            if self.use_seq:
                current_seq_number = "{}".format(self.sequence_start + index)
            else:
                current_seq_number = ""

            prefix = self.prefix_to_add
            suffix = self.suffix_to_add

            renamed_files.append(FileItemModel(
                file_path=file_model.file_path,
                file_name=file_model.file_name,
                file_type=file_model.file_type,
                file_ext=file_model.file_ext,
                new_name="{}{}{}{}".format(current_seq_number, prefix, file_model.file_name, suffix)
            ))

        return renamed_files


class RemovePrefixOrSuffixCommand(Command):
    def __init__(self, prefix_to_remove: str = "", suffix_to_remove: str = ""):
        self.prefix_to_remove: str = prefix_to_remove
        self.suffix_to_remove: str = suffix_to_remove

    def execute(self, list_of_files: list[FileItemModel]) -> list[FileItemModel]:
        renamed_files: list[FileItemModel] = []

        for index, file_model in enumerate(list_of_files):
            prefix = self.prefix_to_remove
            suffix = self.suffix_to_remove
            name = file_model.file_name

            name = name.removeprefix(prefix)
            name = name.removesuffix(suffix)

            renamed_files.append(FileItemModel(
                file_path=file_model.file_path,
                file_name=file_model.file_name,
                file_type=file_model.file_type,
                file_ext=file_model.file_ext,
                new_name=name
            ))

        return renamed_files


class RenameToDateTimeCommand(Command):
    def __init__(self, date_time_place: DateTimePlacing = DateTimePlacing.REPLACE,
                 date_time_source: DateTimeSource = DateTimeSource.FILE_EXIF_CREATION_TIME,
                 date_time_format: DateTimeFormats = DateTimeFormats.YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_MILLIS,
                 date_time_separator: str = "_"):
        self.date_time_place: DateTimePlacing = date_time_place
        self.date_time_source: DateTimeSource = date_time_source
        self.date_time_format: DateTimeFormats = date_time_format
        self.date_time_separator = date_time_separator

    def execute(self, list_of_files: list[FileItemModel]) -> list[FileItemModel]:
        renamed_files: list[FileItemModel] = []

        for index, file_model in enumerate(list_of_files):
            date_time = self.get_date_time_from_source(file_model, self.date_time_source)
            formatted_date_time = self.format_date_time_by_format(date_time, self.date_time_format)

            match self.date_time_place:
                case DateTimePlacing.REPLACE:
                    name = f"{formatted_date_time}{file_model.file_ext}"
                case DateTimePlacing.ADD_TO_BEGIN:
                    name = f"{formatted_date_time}{self.date_time_separator}{file_model.file_name}{file_model.file_ext}"
                case DateTimePlacing.ADD_TO_END:
                    name = f"{file_model.file_name}{self.date_time_separator}{formatted_date_time}{file_model.file_ext}"
                case _:
                    raise CaseIsNotSupported("Passed case to match is not supported")

            renamed_files.append(FileItemModel(
                file_path=file_model.file_path,
                file_name=file_model.file_name,
                file_type=file_model.file_type,
                file_ext=file_model.file_ext,
                new_name=name
            ))

        return renamed_files

    def get_date_time_from_source(self, file_model, date_time_source) -> str:
        return "SOURCE_STUB"  # TODO: Add Implementation

    def format_date_time_by_format(self, date_time, date_time_format) -> str:
        return "FORMAT_STUB"  # TODO: Add Implementation
