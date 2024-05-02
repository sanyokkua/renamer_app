from abc import ABC

from PySide6.QtCore import Qt, Slot
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import QSpinBox

from ui.customs.abstracts import ValueWidgetApi


class Meta(type(ABC), type(QSpinBox)):
    pass


class NumberSpin(QSpinBox, ValueWidgetApi, metaclass=Meta):

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.valueChanged.connect(self._handle_event)

    @Slot()
    def _handle_event(self, number_value: int):
        print(f"_handle_event, {number_value}")
        self.valueIsChanged.emit(number_value)

    def get_widget_value(self) -> int:
        return self.value()

    def set_widget_value(self, text: int):
        self.setValue(text)

    @property
    def minimum_value(self) -> int:
        return self.minimum()

    @minimum_value.setter
    def minimum_value(self, value: int):
        self.setMinimum(value)

    @property
    def maximum_value(self) -> int:
        return self.maximum()

    @maximum_value.setter
    def maximum_value(self, value: int):
        self.setMaximum(value)
