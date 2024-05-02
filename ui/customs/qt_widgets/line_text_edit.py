from abc import ABC

from PySide6.QtCore import Slot
from PySide6.QtWidgets import QLineEdit

from ui.customs.abstracts import ValueWidgetApi
from ui.widgets.base_abstract_widgets import PATH_SYMBOLS_VALIDATOR


class Meta(type(ABC), type(QLineEdit)):
    pass


class LineTextEdit(QLineEdit, ValueWidgetApi, metaclass=Meta):

    def __init__(self, parent=None):
        super().__init__(parent)
        self.textChanged.connect(self._handle_event)
        self.setValidator(PATH_SYMBOLS_VALIDATOR)

    @Slot()
    def _handle_event(self, text: str):
        print(f"_handle_event, {text}")
        self.valueIsChanged.emit(text)

    def get_widget_value(self) -> str:
        return self.displayText()

    def set_widget_value(self, text: str):
        self.setText(text)
