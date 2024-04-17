from PySide6.QtCore import Signal
from PySide6.QtWidgets import (QWidget, QSpinBox)

from ui.widgets.base_abstract_widgets import BaseLabelWidget


class LabelSpinBoxWidget(BaseLabelWidget):
    _spin_box: QSpinBox

    valueIsChanged = Signal(int)

    def __init__(self, parent=None):
        super().__init__(parent)

    def create_pair_widget(self) -> QWidget:
        self._spin_box = QSpinBox(self)
        return self._spin_box

    def create_event_handlers(self):
        self._spin_box.valueChanged.connect(lambda x: self.valueIsChanged.emit(x))

    def get_current_value(self) -> int:
        return self._spin_box.value()

    @property
    def spin_box_min(self) -> int:
        return self._spin_box.minimum()

    @spin_box_min.setter
    def spin_box_min(self, value: int):
        self._spin_box.setMinimum(value)

    @property
    def spin_box_max(self) -> int:
        return self._spin_box.maximum()

    @spin_box_max.setter
    def spin_box_max(self, value: int):
        self._spin_box.setMaximum(value)
