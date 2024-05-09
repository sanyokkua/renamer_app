import logging
from abc import ABC

from PySide6.QtCore import Slot
from PySide6.QtWidgets import QLineEdit

from ui.widgets.base_abstract_widgets import PATH_SYMBOLS_VALIDATOR
from ui.widgets.customs.abstracts import ValueWidgetApi

log: logging.Logger = logging.getLogger(__name__)


class Meta(type(ABC), type(QLineEdit)):
    pass


class LineTextEdit(QLineEdit, ValueWidgetApi, metaclass=Meta):
    """
    Custom QLineEdit widget with additional functionality.

    Inherits:
        QLineEdit: Standard QLineEdit widget.
        ValueWidgetApi: Custom widget for handling single values.

    Signals:
        valueIsChanged: Signal emitted when the text value changes.

    Methods:
        __init__: Initializes the LineTextEdit.
        _handle_event: Slot method to handle text changes.
        get_widget_value: Returns the current text value.
        set_widget_value: Sets the text value of the LineTextEdit.
    """

    def __init__(self, parent=None):
        """
        Initializes the LineTextEdit widget.

        Args:
            parent: Optional parent widget.
        """
        super().__init__(parent)
        self.textChanged.connect(self._handle_event)
        self.setValidator(PATH_SYMBOLS_VALIDATOR)

    @Slot()
    def _handle_event(self, text: str):
        """
        Slot method to handle text changes.
        Emits the valueIsChanged signal with the updated text.

        Args:
            text (str): The updated text value.
        """
        log.debug(f"LineTextEdit._handle_event, {text}")
        self.valueIsChanged.emit(text)

    def get_widget_value(self) -> str:
        """
        Returns the current text value of the LineTextEdit.

        Returns:
            str: The current text value.
        """
        return self.displayText()

    def set_widget_value(self, text: str):
        """
        Sets the text value of the LineTextEdit.

        Args:
            text (str): The text value to be set.
        """
        self.setText(text)
