import logging
from abc import ABC
from typing import TypeVar

from PySide6.QtCore import Slot
from PySide6.QtGui import QCursor, Qt
from PySide6.QtWidgets import QRadioButton

from ui.widgets.customs.abstracts import ValueWidgetWithLabelApi

log: logging.Logger = logging.getLogger(__name__)

T = TypeVar("T")


class Meta(type(ABC), type(QRadioButton)):
    pass


class RadioButton[T](QRadioButton, ValueWidgetWithLabelApi, metaclass=Meta):
    """
    Custom QRadioButton widget for handling generic values.

    Inherits:
        QRadioButton: Standard QRadioButton widget.
        ValueWidgetWithLabelApi: Custom widget for handling values with labels.

    Signals:
        valueIsChanged: Signal emitted when the value changes.

    Methods:
        __init__: Initializes the RadioButton.
        _handle_event: Slot method to handle button clicks.
        set_widget_label: Sets the label text for the RadioButton.
        get_widget_value: Returns the value associated with the RadioButton.
        set_widget_value: Sets the value associated with the RadioButton.
    """

    _btn_value: T

    def __init__(self, parent=None):
        """
        Initializes the RadioButton widget.

        Args:
            parent: Optional parent widget.
        """
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.clicked.connect(self._handle_event)

    @Slot()
    def _handle_event(self):
        """
        Slot method to handle button clicks.
        Emits the valueIsChanged signal with the value associated with the RadioButton.
        """
        log.debug("RadioButton._handle_event, btn clicked")
        self.valueIsChanged.emit(self._btn_value)

    def set_widget_label(self, label_text: str):
        """
        Sets the label text for the RadioButton.

        Args:
            label_text (str): The text to be set as the label.
        """
        self.setText(self.tr(label_text))

    def get_widget_value(self) -> T:
        """
        Returns the value associated with the RadioButton.

        Returns:
            T: The value associated with the RadioButton.
        """
        return self._btn_value

    def set_widget_value(self, value: T):
        """
        Sets the value associated with the RadioButton.

        Args:
            value (T): The value to be associated with the RadioButton.
        """
        self._btn_value = value
