from PySide6.QtWidgets import QVBoxLayout, QWidget, QSpacerItem, QSizePolicy

from core.text_values import APP_MODE_TEXT
from ui.widgets.customs.single_widgets import build_combobox_items
from ui.widgets.views.app_files_list_view_widget import AppFilesListViewWidget

MODE_CMB_ITEMS = build_combobox_items(APP_MODE_TEXT)


class TopRightWidget(QWidget):
    _layout: QVBoxLayout

    files_table: AppFilesListViewWidget
    file_preview: QWidget

    def __init__(self, parent=None):
        super().__init__(parent)
        self._layout = QVBoxLayout(self)
        self.setLayout(self._layout)

        self.files_table = AppFilesListViewWidget(self)
        spacer = QSpacerItem(20, 40, QSizePolicy.Policy.Minimum, QSizePolicy.Policy.Expanding)
        self.file_preview = QWidget(self)

        self._layout.addWidget(self.files_table)
        self._layout.addItem(spacer)
        self._layout.addWidget(self.file_preview)
