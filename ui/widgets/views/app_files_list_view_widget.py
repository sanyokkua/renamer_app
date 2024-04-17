from PySide6.QtCore import (Signal, Slot)
from PySide6.QtWidgets import (QProgressBar)
from PySide6.QtWidgets import (QVBoxLayout)

from ui.widgets.base_abstract_widgets import BaseAbstractWidget
from ui.widgets.customs.files_table_widget import FilesTableView


class AppFilesListViewWidget(BaseAbstractWidget):
    _main_layout: QVBoxLayout
    _files_table_view: FilesTableView
    _progress_bar: QProgressBar

    files_list_updated = Signal(list)

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._main_layout = QVBoxLayout(self)
        self._files_table_view = FilesTableView(self)
        self._progress_bar = QProgressBar(self)

    def configure_widgets(self):
        self.setMinimumWidth(400)
        self.setLayout(self._main_layout)
        self._progress_bar.setValue(0)

        self._main_layout.addWidget(self._files_table_view)
        self._main_layout.addWidget(self._progress_bar)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._files_table_view.files_dropped_to_widget.connect(self.handle_files_dropped)

    @Slot()
    def handle_files_dropped(self, files_list: list[str]):
        self.files_list_updated.emit(files_list)

    @Slot()
    def update_progress_bar(self, min_val: int, max_val: int, current_val: int):
        self._progress_bar.setMinimum(min_val)
        self._progress_bar.setMaximum(max_val)
        self._progress_bar.setValue(current_val)
