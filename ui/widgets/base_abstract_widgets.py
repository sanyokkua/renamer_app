from abc import ABC, abstractmethod

from PySide6.QtCore import QRegularExpression
from PySide6.QtGui import QRegularExpressionValidator
from PySide6.QtWidgets import QWidget, QVBoxLayout

from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand

PATH_SYMBOLS_VALIDATOR = QRegularExpressionValidator()
PATH_SYMBOLS_REGEXP = QRegularExpression("^[A-Za-z0-9_. -\(\)]*$")
PATH_SYMBOLS_VALIDATOR.setRegularExpression(PATH_SYMBOLS_REGEXP)


class ABCMeta(type(QWidget), type(ABC)):
    pass


class BaseAbstractWidget(QWidget, ABC, metaclass=ABCMeta):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.init_widgets()
        self.configure_widgets()
        self.add_text_to_widgets()
        self.create_event_handlers()

    @abstractmethod
    def init_widgets(self):
        pass

    @abstractmethod
    def configure_widgets(self):
        pass

    @abstractmethod
    def add_text_to_widgets(self):
        pass

    @abstractmethod
    def create_event_handlers(self):
        pass


class BasePrepareCommandWidget(BaseAbstractWidget):
    _main_layout: QVBoxLayout

    def __init__(self, parent=None):
        self.pre_parent_init()
        super().__init__(parent)
        self.setLayout(self._main_layout)

    def pre_parent_init(self):
        self._main_layout = QVBoxLayout(self)
        self._main_layout.setSpacing(0)

    @abstractmethod
    def request_command(self) -> AppFileItemByItemListProcessingCommand:
        pass
