from abc import ABC

from PySide6.QtCore import Slot
from PySide6.QtGui import QCursor, Qt
from PySide6.QtWidgets import QPushButton

from ui.customs.abstracts import ValueWidgetWithLabelApi


class Meta(type(ABC), type(QPushButton)):
    pass


class Button(QPushButton, ValueWidgetWithLabelApi, metaclass=Meta):

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.clicked.connect(self._handle_event)

    @Slot()
    def _handle_event(self):
        print("_handle_event, btn clicked")
        self.valueIsChanged.emit(None)

    def set_widget_label(self, label_text: str):
        self.setText(self.tr(label_text))

    def get_widget_value(self):
        return None

    def set_widget_value(self, value):
        pass
