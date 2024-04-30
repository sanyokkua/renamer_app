from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.exceptions import PassedArgumentIsNone
from core.models.app_file import AppFile


# TODO: Requires testing


def change_names(data: List[AppFile]) -> List[AppFile]:
    new_list: list[AppFile] = []  # List of AppFiles with changed name
    counter: int = 0
    padding: int = len(str(len(data)))

    for file_obj in data:
        suffix: str = "({:0{width}d})".format(counter, width=padding)
        file_obj.next_name = file_obj.next_name + suffix
        new_list.append(file_obj)
        counter += 1
    return new_list


class FixSameNamesCommand(PrepareCommand):

    def execute(
        self, data: List[AppFile], status_callback: Optional[StatusFunction]
    ) -> List[AppFile]:
        if data is None:
            raise PassedArgumentIsNone()
        if not isinstance(data, list):
            raise TypeError("data argument type should be List[str]")

        mapped_app_files: list[AppFile] = []
        self.call_status_callback(status_callback, len(data), len(mapped_app_files))

        names_dict: dict[str, list[AppFile]] = {}
        iter_counter: int = 0
        for item in data:
            name_key: str = item.next_name
            if name_key in names_dict:
                list_of_files = names_dict.get(name_key)
                list_of_files.append(item)
            else:
                names_dict[name_key] = [item]
            self.call_status_callback(status_callback, len(data), iter_counter)
            iter_counter += 1
        self.call_status_callback(status_callback, 100, 0)

        if len(names_dict) == 0:
            return data

        for app_files_list in names_dict.values():
            if len(app_files_list) == 0:
                self.call_status_callback(
                    status_callback, len(data), len(mapped_app_files)
                )
                continue

            if len(app_files_list) == 1:
                mapped_app_files.append(app_files_list[0])
                self.call_status_callback(
                    status_callback, len(data), len(mapped_app_files)
                )

            else:
                changed_files: list[AppFile] = change_names(app_files_list)
                for changed_file in changed_files:
                    mapped_app_files.append(changed_file)
                    self.call_status_callback(
                        status_callback, len(data), len(mapped_app_files)
                    )

        self.call_status_callback(status_callback, 100, 0)
        return mapped_app_files
