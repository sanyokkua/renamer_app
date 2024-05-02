from PySide6.QtCore import Signal, Slot
from PySide6.QtGui import Qt, QCursor
from PySide6.QtWidgets import QHBoxLayout

from core.enums import AppModes
from core.text_values import APP_MODE_TEXT
from ui.customs.app_widgets.combobox_form import ComboboxForm
from ui.customs.qt_widgets import ComboBoxItem
from ui.customs.qt_widgets.button import Button
from ui.widgets.base_abstract_widgets import BaseAbstractWidget


class AppControlsWidget(BaseAbstractWidget):
    _main_layout: QHBoxLayout
    _app_modes_combobox: ComboboxForm
    _preview_btn: Button
    _rename_btn: Button
    _clear_btn: Button

    appModeSelected = Signal(AppModes)
    previewBtnClicked = Signal()
    renameBtnClicked = Signal()
    clearBtnClicked = Signal()

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._main_layout = QHBoxLayout(self)
        self._app_modes_combobox = ComboboxForm(parent=self)
        self._preview_btn = Button(self)
        self._rename_btn = Button(self)
        self._clear_btn = Button(self)

    def configure_widgets(self):
        self.setLayout(self._main_layout)
        self.setMaximumHeight(50)
        self._main_layout.setContentsMargins(0, 0, 0, 0)
        self._main_layout.addWidget(self._app_modes_combobox)
        self._main_layout.addWidget(self._preview_btn)
        self._main_layout.addWidget(self._rename_btn)
        self._main_layout.addWidget(self._clear_btn)
        self._main_layout.addStretch(1)
        cursor = QCursor(Qt.CursorShape.PointingHandCursor)
        self._preview_btn.setCursor(cursor)
        self._rename_btn.setCursor(cursor)
        self._clear_btn.setCursor(cursor)
        items = []
        for value, text in APP_MODE_TEXT.items():
            items.append(ComboBoxItem[AppModes](value, text))
        self._app_modes_combobox.set_widget_items(items)

    def add_text_to_widgets(self):
        self._app_modes_combobox.set_widget_label(self.tr("Select mode"))
        self._preview_btn.setText(self.tr("Preview"))
        self._rename_btn.setText(self.tr("Rename"))
        self._clear_btn.setText(self.tr("Clear"))

    def create_event_handlers(self):
        self._app_modes_combobox.valueIsChanged.connect(self.handle_app_mode_changed)
        self._preview_btn.clicked.connect(self.handle_preview_btn_clicked)
        self._rename_btn.clicked.connect(self.handle_rename_btn_clicked)
        self._clear_btn.clicked.connect(self.handle_clear_btn_clicked)

    @Slot()
    def handle_app_mode_changed(self, value: AppModes) -> None:
        print(f"handle_app_mode_changed: {value}")
        self.appModeSelected.emit(value)

    @Slot()
    def handle_preview_btn_clicked(self) -> None:
        print("handle_preview_btn_clicked")
        self.previewBtnClicked.emit()

    @Slot()
    def handle_rename_btn_clicked(self) -> None:
        print("handle_rename_btn_clicked")
        self.renameBtnClicked.emit()

    @Slot()
    def handle_clear_btn_clicked(self) -> None:
        print("handle_clear_btn_clicked")
        self.clearBtnClicked.emit()
