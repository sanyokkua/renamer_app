from abc import ABC, abstractmethod

from PySide6.QtCore import Slot, Qt, QRegularExpression
from PySide6.QtGui import QRegularExpressionValidator
from PySide6.QtWidgets import QWidget, QFormLayout, QLabel, QVBoxLayout

from core.commons import PrepareCommand

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
    def request_command(self) -> PrepareCommand:
        pass


class BaseLabelWidget[T](QWidget, ABC, metaclass=ABCMeta):

    def __init__(self, parent=None) -> None:
        super().__init__(parent)
        self._widget_label = None
        self._widget_layout = None
        self.custom_init()
        self.create_event_handlers()

    def custom_init(self):
        self._widget_layout: QFormLayout = QFormLayout(self)
        self._widget_label: QLabel = QLabel(self)
        self._widget_layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self._widget_layout.setRowWrapPolicy(QFormLayout.RowWrapPolicy.WrapLongRows)
        self._widget_layout.setWidget(
            0, QFormLayout.ItemRole.LabelRole, self._widget_label
        )
        self._widget_layout.setSpacing(0)
        self.setLayout(self._widget_layout)
        widget = self.create_pair_widget()
        self.add_widget_to_layout(widget)

    @abstractmethod
    def create_pair_widget(self) -> QWidget:
        pass

    @abstractmethod
    def create_event_handlers(self):
        pass

    @abstractmethod
    def get_current_value(self) -> T:
        pass

    def add_widget_to_layout(self, widget: QWidget):
        self._widget_layout.setWidget(0, QFormLayout.ItemRole.FieldRole, widget)

    @Slot()
    def set_label_text(self, text: str):
        self._widget_label.setText(self.tr(text))
