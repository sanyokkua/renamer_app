import logging

from PySide6.QtCore import (
    QAbstractTableModel,
    QItemSelectionModel,
    QModelIndex,
    Qt,
    Signal,
    Slot,
)
from PySide6.QtGui import QColor, QColorConstants, QFont
from PySide6.QtWidgets import QHeaderView, QTableView

from core.models.app_file import AppFile

log: logging.Logger = logging.getLogger(__name__)


class FilesTableModel(QAbstractTableModel):
    columns = ["Name", "Type", "New Name"]
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
            match col:
                case 0:
                    return f"{data_obj.file_name}{data_obj.file_extension}"
                case 1:
                    return "Folder" if data_obj.is_folder else "File"
                case 2:
                    return f"{data_obj.next_name}{data_obj.file_extension_new}"

        if role == Qt.ItemDataRole.BackgroundRole:
            row = index.row()
            data_obj: AppFile = self.rows[row]
            [is_valid, err] = data_obj.is_valid()
            if not is_valid:
                log.debug(f"FilesTableModel.data. err {err}")
                return QColor(QColorConstants.Red)
            elif data_obj.is_name_changed:
                return QColor(QColorConstants.Green)
            else:
                return QColor(QColorConstants.White)

    def headerData(self, section, orientation, role=...):
        if orientation == Qt.Orientation.Horizontal and role == Qt.ItemDataRole.DisplayRole:
            return self.columns[section]
        return super().headerData(section, orientation, role)

    def flags(self, index: QModelIndex) -> Qt.ItemFlag:
        return Qt.ItemFlag.ItemIsEnabled | Qt.ItemFlag.ItemIsSelectable

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
    file_selected = Signal(AppFile)

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setModel(FilesTableModel())
        self.setDragEnabled(True)
        self.setAcceptDrops(True)
        self.viewport().setAcceptDrops(True)

        self.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.verticalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Interactive)
        self.verticalHeader().setDefaultSectionSize(10)
        font = QFont("Arial", 10)
        self.setFont(font)
        self.setAutoFillBackground(False)
        self.setContentsMargins(0, 0, 0, 0)

        self.clicked.connect(self.handle_click)

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

    def mousePressEvent(self, event):
        index = self.indexAt(event.pos())
        if index.isValid():
            selection_model = self.selectionModel()
            selection_model.clearSelection()
            row = index.row()
            for col in range(self.model().columnCount()):
                selection_model.select(
                    self.model().index(row, col),
                    QItemSelectionModel.SelectionFlag.Select,
                )
        super().mousePressEvent(event)

    def keyPressEvent(self, event):
        if event.key() in (Qt.Key.Key_Up, Qt.Key.Key_Down):
            selection_model = self.selectionModel()
            current_index = self.currentIndex()
            if current_index.isValid():
                row = current_index.row()
                if event.key() == Qt.Key.Key_Up:
                    row -= 1
                elif event.key() == Qt.Key.Key_Down:
                    row += 1
                row = max(0, min(row, self.model().rowCount() - 1))  # Ensure row is within bounds
                new_index = self.model().index(row, 0)
                selection_model.clearSelection()
                for col in range(self.model().columnCount()):
                    selection_model.select(
                        self.model().index(row, col),
                        QItemSelectionModel.SelectionFlag.Select,
                    )
                self.setCurrentIndex(new_index)
                self.handle_click(new_index)
        else:
            super().keyPressEvent(event)

    def emit_files_dropped(self, file_paths: list[str]) -> None:
        self.files_dropped_to_widget.emit(file_paths)

    @Slot()
    def update_table_data(self, files: list[AppFile]) -> None:
        model: FilesTableModel = self.model()
        model.clear()
        model.insertRows(len(model.rows), files)
        self.resizeColumnsToContents()

    def handle_click(self, index: QModelIndex):
        row = index.row()
        model: FilesTableModel = self.model()
        selected_file = model.rows[row]
        self.file_selected.emit(selected_file)
