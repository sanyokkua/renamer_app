from PySide6.QtCore import Signal
from PySide6.QtGui import QRegularExpressionValidator
from PySide6.QtWidgets import QWidget, QLineEdit

from ui.widgets.base_abstract_widgets import BaseLabelWidget


class LabelLineEditWidget(BaseLabelWidget):
    _line_edit: QLineEdit

    valueIsChanged = Signal(str)

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setContentsMargins(0, 0, 0, 0)

    def create_pair_widget(self) -> QWidget:
        self._line_edit = QLineEdit(self)
        return self._line_edit

    def create_event_handlers(self):
        self._line_edit.textChanged.connect(lambda x: self.valueIsChanged.emit(x))

    def get_current_value(self) -> str:
        return self._line_edit.displayText()

    def set_text_validator(self, validator: QRegularExpressionValidator):
        self._line_edit.setValidator(validator)
