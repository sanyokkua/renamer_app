import logging
from abc import ABC

from PySide6.QtCore import Qt, Slot
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import QSpinBox

from ui.widgets.customs.abstracts import ValueWidgetApi

log: logging.Logger = logging.getLogger(__name__)


class Meta(type(ABC), type(QSpinBox)):
    pass


class NumberSpin(QSpinBox, ValueWidgetApi, metaclass=Meta):
    """
    Custom QSpinBox widget for handling integer values.

    Inherits:
        QSpinBox: Standard QSpinBox widget.
        ValueWidgetApi: Custom widget for handling single values.

    Signals:
        valueIsChanged: Signal emitted when the value changes.

    Methods:
        __init__: Initializes the NumberSpin.
        _handle_event: Slot method to handle value changes.
        get_widget_value: Returns the current value.
        set_widget_value: Sets the value of the NumberSpin.
        minimum_value: Getter and setter for the minimum value.
        maximum_value: Getter and setter for the maximum value.
    """

    def __init__(self, parent=None):
        """
        Initializes the NumberSpin widget.

        Args:
            parent: Optional parent widget.
        """
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.valueChanged.connect(self._handle_event)

    @Slot()
    def _handle_event(self, number_value: int):
        """
        Slot method to handle value changes.
        Emits the valueIsChanged signal with the updated value.

        Args:
            number_value (int): The updated value.
        """
        log.debug(f"NumberSpin._handle_event, {number_value}")
        self.valueIsChanged.emit(number_value)

    def get_widget_value(self) -> int:
        """
        Returns the current value of the NumberSpin.

        Returns:
            int: The current value.
        """
        return self.value()

    def set_widget_value(self, text: int):
        """
        Sets the value of the NumberSpin.

        Args:
            text (int): The value to be set.
        """
        self.setValue(text)

    @property
    def minimum_value(self) -> int:
        """
        Getter for the minimum value of the NumberSpin.

        Returns:
            int: The minimum value.
        """
        return self.minimum()

    @minimum_value.setter
    def minimum_value(self, value: int):
        """
        Setter for the minimum value of the NumberSpin.

        Args:
            value (int): The minimum value to be set.
        """
        self.setMinimum(value)

    @property
    def maximum_value(self) -> int:
        """
        Getter for the maximum value of the NumberSpin.

        Returns:
            int: The maximum value.
        """
        return self.maximum()

    @maximum_value.setter
    def maximum_value(self, value: int):
        """
        Setter for the maximum value of the NumberSpin.

        Args:
            value (int): The maximum value to be set.
        """
        self.setMaximum(value)
