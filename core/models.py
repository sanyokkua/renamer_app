from dataclasses import dataclass


@dataclass
class FileItemModel:
    file_path: str
    file_name: str
    file_type: str
    file_ext: str
    new_name: str
