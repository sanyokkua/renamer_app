from PySide6.QtWidgets import QVBoxLayout, QWidget

from core.text_values import APP_MODE_TEXT
from ui.widgets.customs.form_widgets import ComboboxForm
from ui.widgets.customs.single_widgets import build_combobox_items
from ui.widgets.main import ControlsWidget
from ui.widgets.views.app_mode_view_widget import AppModeSelectViewWidget

MODE_CMB_ITEMS = build_combobox_items(APP_MODE_TEXT)


class TopLeftWidget(QWidget):
    _layout: QVBoxLayout

    mode_selector: ComboboxForm
    mode_view: AppModeSelectViewWidget
    controls: ControlsWidget

    def __init__(self, parent=None):
        super().__init__(parent)
        self._layout = QVBoxLayout(self)
        self.setLayout(self._layout)

        self.mode_selector = ComboboxForm(self)
        self.mode_view = AppModeSelectViewWidget(self)
        self.controls = ControlsWidget(self)

        self.mode_selector.set_widget_items(MODE_CMB_ITEMS)
        self.mode_selector.set_widget_label("Renaming mode:")

        self._layout.addWidget(self.mode_selector)
        self._layout.addWidget(self.mode_view, 1)
        self._layout.addWidget(self.controls)
