from PySide6.QtCore import Qt, QModelIndex, QAbstractTableModel
from PySide6.QtCore import Signal, Slot
from PySide6.QtGui import QFont, QColor, QColorConstants
from PySide6.QtWidgets import QTableView, QHeaderView

from core.models.app_file import AppFile


class FilesTableModel(QAbstractTableModel):
    columns = ["Original Name", "Type", "New Name"]
    rows: list[AppFile] = []

    def __init__(self, parent=None):
        super().__init__(parent)

    def rowCount(self, parent: QModelIndex = QModelIndex()) -> int:
        return len(self.rows)

    def columnCount(self, parent: QModelIndex = QModelIndex()) -> int:
        return len(self.columns)

    def data(self, index: QModelIndex, role: int):
        if role == Qt.ItemDataRole.DisplayRole:
            row = index.row()
            col = index.column()

            data_obj: AppFile = self.rows[row]
            if col == 0:
                return f"{data_obj.file_name}{data_obj.file_extension}"
            elif col == 1:
                return "Folder" if data_obj.is_folder else "File"
            elif col == 2:
                return f"{data_obj.next_name}{data_obj.file_extension_new}"

        if role == Qt.ItemDataRole.BackgroundRole:
            row = index.row()
            data_obj: AppFile = self.rows[row]
            [is_valid, err] = data_obj.is_valid()
            if not is_valid:
                print(f"FilesTableModel.data. err {err}")
                return QColor(QColorConstants.Red)
            elif data_obj.is_name_changed:
                return QColor(QColorConstants.Green)
            else:
                return QColor(QColorConstants.White)

    def headerData(self, section, orientation, role=...):
        if orientation == Qt.Horizontal and role == Qt.DisplayRole:
            return self.columns[section]
        return super().headerData(section, orientation, role)

    def flags(self, index: QModelIndex) -> Qt.ItemFlags:
        return Qt.ItemIsEnabled | Qt.ItemIsSelectable

    def insertRows(self, row, files):
        self.beginInsertRows(QModelIndex(), row, row + len(files) - 1)
        self.rows.extend(files)
        self.endInsertRows()

    @Slot()
    def clear(self) -> None:
        self.beginResetModel()  # Notify views that the model is about to be reset
        self.rows = []  # Clear the data structure
        self.endResetModel()  # Notify views that the model has been reset


class FilesTableView(QTableView):
    files_dropped_to_widget = Signal(list)

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setModel(FilesTableModel())
        self.setAcceptDrops(True)
        self.setDragEnabled(True)
        self.viewport().setAcceptDrops(True)
        self.resizeColumnsToContents()
        self.resizeRowsToContents()
        self.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.verticalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.verticalHeader().setDefaultSectionSize(10)
        font = QFont("Arial", 10)
        self.setFont(font)
        self.setAutoFillBackground(False)

    def dragEnterEvent(self, event):
        if event.mimeData().hasUrls():
            event.acceptProposedAction()

    def dragMoveEvent(self, event):
        event.acceptProposedAction()

    def dropEvent(self, event) -> None:
        if event.mimeData().hasUrls():
            urls = event.mimeData().urls()
            file_paths = [url.toLocalFile() for url in urls]
            self.emit_files_dropped(file_paths)

    def emit_files_dropped(self, file_paths: list[str]) -> None:
        self.files_dropped_to_widget.emit(file_paths)

    @Slot()
    def update_table_data(self, files: list[AppFile]) -> None:
        model: FilesTableModel = self.model()
        model.clear()
        model.insertRows(len(model.rows), files)
        self.resizeColumnsToContents()
