from PySide6.QtCore import Signal, Slot
from PySide6.QtWidgets import QVBoxLayout

from core.models.app_file import AppFile
from ui.widgets.base_abstract_widgets import BaseAbstractWidget
from ui.widgets.main.files_table_view import FilesTableView


class AppFilesListViewWidget(BaseAbstractWidget):
    _main_layout: QVBoxLayout
    _files_table_view: FilesTableView

    files_list_updated = Signal(list)
    file_selected = Signal(AppFile)

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._main_layout = QVBoxLayout(self)
        self._files_table_view = FilesTableView(self)

    def configure_widgets(self):
        self.setMinimumWidth(400)
        self.setLayout(self._main_layout)
        self._main_layout.addWidget(self._files_table_view)
        self.setContentsMargins(0, 0, 0, 0)
        self._main_layout.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        # There is no text on the widgets
        pass

    def create_event_handlers(self):
        self._files_table_view.files_dropped_to_widget.connect(self.handle_files_dropped)
        self._files_table_view.file_selected.connect(self.handle_files_selected)

    @Slot()
    def handle_files_dropped(self, files_list: list[str]):
        self.files_list_updated.emit(files_list)

    @Slot()
    def handle_files_selected(self, file: AppFile):
        self.file_selected.emit(file)

    @Slot()
    def update_table_data(self, files: list[AppFile]) -> None:
        self._files_table_view.update_table_data(files)
