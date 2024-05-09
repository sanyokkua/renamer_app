import logging
from abc import ABC

from PySide6.QtCore import Qt, Slot
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import QCheckBox

from ui.widgets.customs.abstracts import ValueWidgetWithLabelApi

log: logging.Logger = logging.getLogger(__name__)


class Meta(type(ABC), type(QCheckBox)):
    pass


class CheckBox(QCheckBox, ValueWidgetWithLabelApi, metaclass=Meta):
    """
    Custom QCheckBox widget with additional functionality.

    Inherits:
        QCheckBox: Standard QCheckBox widget.
        ValueWidgetWithLabelApi: Custom widget for handling values with labels.

    Signals:
        valueIsChanged: Signal emitted when the checkbox state changes.

    Methods:
        __init__: Initializes the checkbox.
        _handle_event: Slot method to handle checkbox state change events.
        set_widget_label: Sets the label text for the checkbox.
        get_widget_value: Returns the current value of the checkbox.
        set_widget_value: Sets the value of the checkbox.
    """

    def __init__(self, parent=None):
        """
        Initializes the CheckBox widget.

        Args:
            parent: Optional parent widget.
        """
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.checkStateChanged.connect(self._handle_event)

    @Slot()
    def _handle_event(self, state: Qt.CheckState):
        """
        Slot method to handle checkbox state change events.
        Emits the valueIsChanged signal with the current value.

        Args:
            state (Qt.CheckState): The new state of the checkbox.
        """
        log.debug(f"CheckBox._handle_event, {state}")
        current_val = self.get_widget_value()
        self.valueIsChanged.emit(current_val)

    def set_widget_label(self, label_text: str):
        """
        Sets the label text for the checkbox.

        Args:
            label_text (str): Text to be set as the label.
        """
        self.setText(self.tr(label_text))

    def get_widget_value(self) -> bool:
        """
        Returns the current value of the checkbox.

        Returns:
            bool: True if checked, False otherwise.
        """
        state: Qt.CheckState = self.checkState()
        if state == Qt.CheckState.Checked:
            return True
        else:
            return False

    def set_widget_value(self, value: bool):
        """
        Sets the value of the checkbox.

        Args:
            value (bool): The value to be set.
        """
        if value:
            self.setCheckState(Qt.CheckState.Checked)
        else:
            self.setCheckState(Qt.CheckState.Unchecked)
