import logging
from abc import ABC

from PySide6.QtCore import Slot
from PySide6.QtGui import QCursor, Qt
from PySide6.QtWidgets import QPushButton

from ui.widgets.customs.abstracts import ValueWidgetWithLabelApi

log: logging.Logger = logging.getLogger(__name__)


class Meta(type(ABC), type(QPushButton)):
    pass


class Button(QPushButton, ValueWidgetWithLabelApi, metaclass=Meta):
    """
    Custom QPushButton widget with additional functionality.

    Inherits:
        QPushButton: Standard QPushButton widget.
        ValueWidgetWithLabelApi: Custom widget for handling values with labels.

    Signals:
        valueIsChanged: Signal emitted when the button is clicked.

    Methods:
        __init__: Initializes the button.
        _handle_event: Slot method to handle button click events.
        set_widget_label: Sets the label text for the button.
        get_widget_value: Returns the current value of the button.
        set_widget_value: Sets the value of the button (not implemented).
    """

    def __init__(self, parent=None):
        """
        Initializes the Button widget.

        Args:
            parent: Optional parent widget.
        """
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.clicked.connect(self._handle_event)

    @Slot()
    def _handle_event(self):
        """
        Slot method to handle button click events.
        Emits the valueIsChanged signal.
        """
        log.debug("Button._handle_event, btn clicked")
        self.valueIsChanged.emit(None)

    def set_widget_label(self, label_text: str):
        """
        Sets the label text for the button.

        Args:
            label_text (str): Text to be set as the label.
        """
        self.setText(self.tr(label_text))

    def get_widget_value(self):
        """
        Returns the current value of the button.
        For compatibility with ValueWidgetWithLabelApi.
        """
        return None

    def set_widget_value(self, value):
        """
        Sets the value of the button (not implemented).
        For compatibility with ValueWidgetWithLabelApi.

        Args:
            value: Value to be set.
        """
        pass
