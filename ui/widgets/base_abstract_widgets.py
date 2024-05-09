from abc import ABC, abstractmethod

from PySide6.QtCore import QRegularExpression
from PySide6.QtGui import QRegularExpressionValidator
from PySide6.QtWidgets import QVBoxLayout, QWidget

from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand

PATH_SYMBOLS_VALIDATOR = QRegularExpressionValidator()
PATH_SYMBOLS_REGEXP = QRegularExpression("^[A-Za-z0-9_. -\(\)]*$")
PATH_SYMBOLS_VALIDATOR.setRegularExpression(PATH_SYMBOLS_REGEXP)


class ABCMeta(type(QWidget), type(ABC)):
    pass


class BaseAbstractWidget(QWidget, ABC, metaclass=ABCMeta):
    """
    Abstract base class for creating custom widgets.

    This class provides a template for creating custom widgets with PySide6.

    Attributes:
        None

    Methods:
        init_widgets(): Abstract method to initialize widgets.
        configure_widgets(): Abstract method to configure widget properties.
        add_text_to_widgets(): Abstract method to add text to widgets.
        create_event_handlers(): Abstract method to create event handlers for widgets.
    """

    def __init__(self, parent=None):
        """
        Initializes the BaseAbstractWidget.

        Args:
            parent (QWidget, optional): Parent widget. Defaults to None.
        """
        super().__init__(parent)
        self.init_widgets()
        self.configure_widgets()
        self.add_text_to_widgets()
        self.create_event_handlers()

    @abstractmethod
    def init_widgets(self):
        """
        Abstract method to initialize widgets.
        """
        pass

    @abstractmethod
    def configure_widgets(self):
        """
        Abstract method to configure widget properties.
        """
        pass

    @abstractmethod
    def add_text_to_widgets(self):
        """
        Abstract method to add text to widgets.
        """
        pass

    @abstractmethod
    def create_event_handlers(self):
        """
        Abstract method to create event handlers for widgets.
        """
        pass


class BasePrepareCommandWidget(BaseAbstractWidget):
    """
    Abstract base class for widgets that prepare commands.

    This class provides a template for creating widgets that prepare commands to process app file items.

    Attributes:
        _main_layout (QVBoxLayout): Layout for the widget.

    Methods:
        __init__(parent=None): Initializes the BasePrepareCommandWidget.
        pre_parent_init(): Initializes common attributes before calling the parent class constructor.
        request_command() -> AppFileItemByItemListProcessingCommand: Abstract method to request a command.
    """

    _main_layout: QVBoxLayout

    def __init__(self, parent=None):
        """
        Initializes the BasePrepareCommandWidget.

        Args:
            parent (QWidget, optional): Parent widget. Defaults to None.
        """
        self.pre_parent_init()
        super().__init__(parent)
        self.setLayout(self._main_layout)

    def pre_parent_init(self):
        """
        Initializes common attributes before calling the parent class constructor.
        """
        self._main_layout = QVBoxLayout(self)
        self._main_layout.setSpacing(0)

    @abstractmethod
    def request_command(self) -> AppFileItemByItemListProcessingCommand:
        """
        Abstract method to request a command.

        Returns:
            AppFileItemByItemListProcessingCommand: The requested command.
        """
        pass
