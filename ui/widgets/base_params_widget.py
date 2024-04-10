from PySide6.QtCore import QRegularExpression
from PySide6.QtCore import Signal, Slot
from PySide6.QtGui import QRegularExpressionValidator
from PySide6.QtWidgets import (QWidget)

from core.command import Command


class BaseParamsWidget(QWidget):
    command_built = Signal(Command)

    def __init__(self):
        super().__init__()
        self.path_symbols_validator = QRegularExpressionValidator()
        regular_exp = QRegularExpression("^([A-Za-z]*[0-9]*[_ ]*)*$")
        self.path_symbols_validator.setRegularExpression(regular_exp)

    def emit_command_built(self, command: Command) -> None:
        self.command_built.emit(command)

    @Slot()
    def request_command(self) -> Command:
        # Abstract Method, should be implemented in the classes that extend this base class
        pass
