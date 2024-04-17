from typing import List, Optional

from core.commons import PrepareCommand, StatusFunction
from core.models.app_file import AppFile
from core.utils.other import build_app_file_from_path_str


class MapUrlToAppFileCommand(PrepareCommand):
    def execute(self, data: List[str], status_callback: Optional[StatusFunction]) -> List[AppFile]:
        mapped_app_files: list[AppFile] = []
        if status_callback is not None:
            status_callback(0, len(data), 0)
        for path_url in data:
            app_file: AppFile = build_app_file_from_path_str(path_url)
            mapped_app_files.append(app_file)
            if status_callback is not None:
                status_callback(0, len(data), len(mapped_app_files))
        if status_callback is not None:
            status_callback(0, 100, 0)
        return mapped_app_files
