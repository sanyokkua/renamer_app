from abc import ABC

from PySide6.QtCore import Qt, Slot
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import QCheckBox

from ui.widgets.customs.abstracts import ValueWidgetWithLabelApi


class Meta(type(ABC), type(QCheckBox)):
    pass


class CheckBox(QCheckBox, ValueWidgetWithLabelApi, metaclass=Meta):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.checkStateChanged.connect(self._handle_event)

    @Slot()
    def _handle_event(self, state: Qt.CheckState):
        print(f"_handle_event, {state}")
        current_val = self.get_widget_value()
        self.valueIsChanged.emit(current_val)

    def set_widget_label(self, label_text: str):
        self.setText(self.tr(label_text))

    def get_widget_value(self) -> bool:
        state: Qt.CheckState = self.checkState()
        if state == Qt.CheckState.Checked:
            return True
        else:
            return False

    def set_widget_value(self, value: bool):
        if value:
            self.setCheckState(Qt.CheckState.Checked)
        else:
            self.setCheckState(Qt.CheckState.Unchecked)
