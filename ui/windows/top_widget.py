from PySide6.QtWidgets import QHBoxLayout, QWidget

from core.text_values import APP_MODE_TEXT
from ui.widgets.customs.single_widgets import build_combobox_items
from ui.windows.top_left_widget import TopLeftWidget
from ui.windows.top_right_widget import TopRightWidget

MODE_CMB_ITEMS = build_combobox_items(APP_MODE_TEXT)


class TopWidget(QWidget):
    _layout: QHBoxLayout

    left_widget: TopLeftWidget
    right_widget: TopRightWidget

    def __init__(self, parent=None):
        super().__init__(parent)
        self._layout = QHBoxLayout(self)
        self.setLayout(self._layout)

        self.left_widget = TopLeftWidget(self)
        self.right_widget = TopRightWidget(self)

        self._layout.addWidget(self.left_widget)
        self._layout.addWidget(self.right_widget)
