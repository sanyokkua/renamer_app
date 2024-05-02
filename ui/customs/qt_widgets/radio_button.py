from abc import ABC
from typing import TypeVar

from PySide6.QtCore import Slot
from PySide6.QtGui import QCursor, Qt
from PySide6.QtWidgets import QRadioButton

from ui.customs.abstracts import ValueWidgetWithLabelApi

T = TypeVar("T")


class Meta(type(ABC), type(QRadioButton)):
    pass


class RadioButton[T](QRadioButton, ValueWidgetWithLabelApi, metaclass=Meta):
    _btn_value: T

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.clicked.connect(self._handle_event)

    @Slot()
    def _handle_event(self):
        print("_handle_event, btn clicked")
        self.valueIsChanged.emit(self._btn_value)

    def set_widget_label(self, label_text: str):
        self.setText(self.tr(label_text))

    def get_widget_value(self) -> T:
        return self._btn_value

    def set_widget_value(self, value: T):
        self._btn_value = value
