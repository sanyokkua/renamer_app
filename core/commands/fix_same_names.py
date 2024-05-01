from collections import defaultdict
from typing import Optional

from core.abstract import StatusFunction
from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand
from core.models.app_file import AppFile


class FixSameNamesCommand(AppFileItemByItemListProcessingCommand):
    """A command to fix names of files with the same name."""

    def process_data(self, data: list[AppFile], status_callback: Optional[StatusFunction] = None) -> list[AppFile]:
        """Process the data to fix names.

        Args:
            data (list[AppFile]): The list of AppFile items.
            status_callback (Optional[StatusFunction]): A function to call for status updates.

        Returns:
            list[AppFile]: The list of processed AppFile items.
        """
        unique_names: set[str] = set()
        name_groups = defaultdict(list)

        data_size = len(data) * 2  # Two times we will go by each data item
        global_counter = 0
        self.call_status_callback(status_callback, global_counter, data_size)

        for file in data:
            name_groups[(file.next_name, file.file_extension_new)].append(file)
            if file.is_name_changed:
                unique_names.add(f"{file.next_name}{file.file_extension_new}")
            else:
                unique_names.add(f"{file.file_name}{file.file_extension}")

            self.call_status_callback(status_callback, global_counter, data_size)
            global_counter += 1

        for files_with_same_name in name_groups.values():
            if len(files_with_same_name) == 1:
                # If only one file in the group, no need to fix its name
                self.call_status_callback(status_callback, global_counter, data_size)
                global_counter += 1
            else:
                # If multiple files with the same name, fix their names
                num_files = len(files_with_same_name)
                digits = len(str(num_files))  # Calculate number of digits required
                for i, file in enumerate(files_with_same_name):
                    if file.is_name_changed:
                        counter = 1
                        suffix = f" ({counter:0{digits}d})"  # Generate suffix dynamically ({:0{width}d})
                        new_name = f"{file.next_name}{suffix}"
                        new_name_with_ext = f"{new_name}{file.file_extension_new}"

                        while new_name_with_ext in unique_names:
                            counter += 1
                            suffix = f" ({counter:0{digits}d})"  # Generate suffix dynamically ({:0{width}d})
                            new_name = f"{file.next_name}{suffix}"
                            new_name_with_ext = f"{new_name}{file.file_extension_new}"

                        unique_names.add(new_name_with_ext)
                        file.next_name = new_name
                    self.call_status_callback(status_callback, global_counter, data_size)
                    global_counter += 1

        self.call_status_callback(status_callback, 0, 100)
        return data  # returning data because only object values will be changed
