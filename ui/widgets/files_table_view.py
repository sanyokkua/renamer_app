from pathlib import Path

from PySide6.QtCore import Qt, QModelIndex, QAbstractTableModel
from PySide6.QtCore import Signal, Slot
from PySide6.QtWidgets import QTableView

from core.models import FileItemModel


class FilePassedToModelIsNotValid(Exception):
    def __init__(self, message="An error occurred"):
        self.message = message
        super().__init__(self.message)


def map_path_to_model(path: str) -> FileItemModel:
    file_path = Path(path)
    if not file_path.exists():
        raise FilePassedToModelIsNotValid("File does not exist")

    if file_path.is_file():
        is_file = True
    else:
        is_file = False

    file_path_str: str = path
    file_name: str = file_path.name
    file_type = "File" if is_file else "Folder"
    file_new_name: str = file_name

    return FileItemModel(file_path=file_path_str, file_name=file_name, file_type=file_type, new_name=file_new_name)


class FileTableModel(QAbstractTableModel):
    columns = ["Original Name", "Type", "New Name"]

    def __init__(self, parent=None):
        super().__init__(parent)
        self.file_list = []

    def rowCount(self, parent: QModelIndex = QModelIndex()) -> int:
        return len(self.file_list)

    def columnCount(self, parent: QModelIndex = QModelIndex()) -> int:
        return len(self.columns)

    def data(self, index: QModelIndex, role: int) -> str:
        if role == Qt.DisplayRole:
            row = index.row()
            col = index.column()

            data_obj: FileItemModel = self.file_list[row]
            if col == 0:
                return data_obj.file_name
            elif col == 1:
                return data_obj.file_type
            elif col == 2:
                return data_obj.new_name

            return super().data(index, role)

    def headerData(self, section, orientation, role=...):
        if orientation == Qt.Horizontal and role == Qt.DisplayRole:
            return self.columns[section]
        return super().headerData(section, orientation, role)

    def flags(self, index: QModelIndex) -> Qt.ItemFlags:
        return Qt.ItemIsEnabled | Qt.ItemIsSelectable

    def insertRows(self, row, files):
        self.beginInsertRows(QModelIndex(), row, row + len(files) - 1)
        self.file_list.extend(files)
        self.endInsertRows()

    def clear(self) -> None:
        self.beginResetModel()  # Notify views that the model is about to be reset
        self.file_list = []  # Clear the data structure
        self.endResetModel()  # Notify views that the model has been reset


class FileTableView(QTableView):
    files_dropped = Signal(list)

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setModel(FileTableModel())
        self.setAcceptDrops(True)
        self.setDragEnabled(True)
        self.viewport().setAcceptDrops(True)
        self.resizeColumnsToContents()

    def dragEnterEvent(self, event):
        if event.mimeData().hasUrls():
            event.acceptProposedAction()

    def dragMoveEvent(self, event):
        # if event.mimeData().hasUrls():
        event.acceptProposedAction()

    def dropEvent(self, event) -> None:
        if event.mimeData().hasUrls():
            urls = event.mimeData().urls()
            file_paths = [map_path_to_model(url.toLocalFile()) for url in urls]
            self.emit_files_dropped(file_paths)

    def emit_files_dropped(self, file_paths: list[FileItemModel]) -> None:
        self.files_dropped.emit(file_paths)

    @Slot()
    def update_table_data(self, files: list[FileItemModel]) -> None:
        self.model().clear()
        self.model().insertRows(len(self.model().file_list), files)
        self.resizeColumnsToContents()
