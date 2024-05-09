import logging
from enum import IntEnum
from typing import TypeVar

from PySide6.QtCore import Qt, Signal
from PySide6.QtWidgets import QFormLayout, QLabel

from ui.widgets.customs.base_widget import BaseMultipleValueWidgetApi
from ui.widgets.customs.single_widgets import ComboBox, ComboBoxItem

log: logging.Logger = logging.getLogger(__name__)

T = TypeVar("T", bound=IntEnum)


class ComboboxForm(BaseMultipleValueWidgetApi):
    """
    Custom form widget containing a QLabel and a ComboBox.

    Inherits:
        BaseMultipleValueWidgetApi: Base class for multiple value widgets.

    Signals:
        valueIsChanged: Signal emitted when the value changes.

    Methods:
        init_widgets: Initializes the form widgets.
        configure_widgets: Configures the layout and alignment of the widgets.
        add_text_to_widgets: Adds text to the widgets.
        create_event_handlers: Creates event handlers for the widgets.
        set_widget_items: Sets the items for the ComboBox.
        get_widget_items: Retrieves the items from the ComboBox.
        set_widget_value: Sets the value of the ComboBox.
        get_widget_value: Retrieves the value of the ComboBox.
        set_widget_label: Sets the label text for the form.
    """

    _combobox: ComboBox
    _label: QLabel
    _layout: QFormLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        """
        Initializes the form widgets.
        """
        self._layout = QFormLayout(self)
        self._label = QLabel(self)
        self._combobox = ComboBox(self)

    def configure_widgets(self):
        """
        Configures the layout and alignment of the widgets.
        """
        self._layout.setWidget(0, QFormLayout.ItemRole.LabelRole, self._label)
        self._layout.setWidget(0, QFormLayout.ItemRole.FieldRole, self._combobox)
        self._layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self.setLayout(self._layout)
        self.setMaximumHeight(50)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._combobox.valueIsChanged.connect(lambda x: self.valueIsChanged.emit(x))

    def set_widget_items(self, items: list[ComboBoxItem[T]]):
        """
        Sets the items for the ComboBox.

        Args:
            items (list[ComboBoxItem[T]]): The items to set in the ComboBox.
        """
        self._combobox.set_widget_items(items)

    def get_widget_items(self) -> list[ComboBoxItem[T]]:
        """
        Retrieves the items from the ComboBox.

        Returns:
            list[ComboBoxItem[T]]: The items from the ComboBox.
        """
        return self._combobox.get_widget_items()

    def set_widget_value(self, value):
        """
        Sets the value of the ComboBox.

        Args:
            value: The value to set in the ComboBox.
        """
        self._combobox.set_widget_value(value)

    def get_widget_value(self):
        """
        Retrieves the value of the ComboBox.

        Returns:
            The value of the ComboBox.
        """
        return self._combobox.get_widget_value()

    def set_widget_label(self, label_text: str):
        """
        Sets the label text for the form.

        Args:
            label_text (str): The text to set as the label.
        """
        self._label.setText(self.tr(label_text))
