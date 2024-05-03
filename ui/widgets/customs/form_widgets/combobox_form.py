from enum import IntEnum
from typing import TypeVar

from PySide6.QtCore import Signal, Qt
from PySide6.QtWidgets import QLabel, QFormLayout

from ui.widgets.customs.base_widget import BaseMultipleValueWidgetApi
from ui.widgets.customs.single_widgets import ComboBox, ComboBoxItem

T = TypeVar("T", bound=IntEnum)


class ComboboxForm(BaseMultipleValueWidgetApi):
    _combobox: ComboBox
    _label: QLabel
    _layout: QFormLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        self._layout = QFormLayout(self)
        self._label = QLabel(self)
        self._combobox = ComboBox(self)

    def configure_widgets(self):
        self._layout.setWidget(0, QFormLayout.ItemRole.LabelRole, self._label)
        self._layout.setWidget(0, QFormLayout.ItemRole.FieldRole, self._combobox)
        self._layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self.setLayout(self._layout)
        self.setMaximumHeight(50)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._combobox.valueIsChanged.connect(lambda x: self.valueIsChanged.emit(x))

    def set_widget_items(self, items: list[ComboBoxItem[T]]):
        self._combobox.set_widget_items(items)

    def get_widget_items(self) -> list[ComboBoxItem[T]]:
        return self._combobox.get_widget_items()

    def set_widget_value(self, value):
        self._combobox.set_widget_value(value)

    def get_widget_value(self):
        return self._combobox.get_widget_value()

    def set_widget_label(self, label_text: str):
        self._label.setText(self.tr(label_text))
