import logging
from dataclasses import dataclass
from enum import IntEnum
from typing import TypeVar

from PySide6.QtCore import Signal
from PySide6.QtWidgets import QButtonGroup, QHBoxLayout, QLabel

from ui.widgets.customs.base_widget import BaseMultipleValueWidgetApi
from ui.widgets.customs.single_widgets import RadioButton

log: logging.Logger = logging.getLogger(__name__)

T = TypeVar("T", bound=IntEnum)


@dataclass
class RadioButtonItem[T]:
    """
    Data class representing an item for a radio button.

    Attributes:
        item_value (T): The value associated with the item.
        item_text (str): The text displayed for the item.
    """

    item_value: T
    item_text: str


def build_radio_button_items(value_to_text: dict[object, str]) -> list[RadioButtonItem]:
    """
    Builds a list of radio button items from a dictionary mapping values to texts.

    Args:
        value_to_text (dict[object, str]): The dictionary mapping values to texts.

    Returns:
        list[RadioButtonItem]: The list of radio button items.
    """
    result_list = []
    for value, text in value_to_text.items():
        result_list.append(RadioButtonItem(item_value=value, item_text=text))
    return result_list


class RadioButtonForm(BaseMultipleValueWidgetApi):
    """
    Custom form widget containing a label and radio buttons.

    Inherits:
        BaseMultipleValueWidgetApi: Base class for multiple value widgets.

    Signals:
        valueIsChanged: Signal emitted when the value changes.

    Methods:
        init_widgets: Initializes the form widgets.
        configure_widgets: Configures the layout of the widgets.
        add_text_to_widgets: Adds text to the widgets.
        create_event_handlers: Creates event handlers for the widgets.
        set_widget_items: Sets the radio button items.
        build_buttons: Builds the radio buttons.
        get_widget_items: Retrieves the radio button items.
        set_widget_label: Sets the label text for the form.
        get_widget_value: Retrieves the value of the selected radio button.
        set_widget_value: Sets the value of the selected radio button.
    """

    _items: list[RadioButtonItem[T]] = []
    _button_group: QButtonGroup
    _label: QLabel
    _layout: QHBoxLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        """
        Initializes the form widgets.
        """
        self._layout = QHBoxLayout(self)
        self._label = QLabel(self)
        self._button_group = QButtonGroup(self)

    def configure_widgets(self):
        """
        Configures the layout of the widgets.
        """
        self._layout.addWidget(self._label)
        self.setLayout(self._layout)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._button_group.buttonToggled.connect(lambda btn: self.valueIsChanged.emit(btn.get_widget_value()))

    def set_widget_items(self, items: list[RadioButtonItem[T]]):
        """
        Sets the radio button items.

        Args:
            items (list[RadioButtonItem[T]]): The list of radio button items.
        """
        self._items.clear()
        for item in items:
            self._items.append(item)
        self.build_buttons()

    def build_buttons(self):
        """
        Builds the radio buttons.
        """
        for button in self._button_group.buttons():
            self._button_group.removeButton(button)
            self._layout.removeWidget(button)
        for index, item in enumerate(self._items):
            btn = RadioButton(self)
            btn.set_widget_label(item.item_text)
            btn.set_widget_value(item.item_value)

            self._button_group.addButton(btn, item.item_value)
            self._layout.addWidget(btn)
            if index == 0:
                btn.setChecked(True)

    def get_widget_items(self) -> list[RadioButtonItem[T]]:
        """
        Retrieves the radio button items.

        Returns:
            list[RadioButtonItem[T]]: The list of radio button items.
        """
        return [item for item in self._items]

    def set_widget_label(self, label_text: str):
        """
        Sets the label text for the form.

        Args:
            label_text (str): The text to set as the label.
        """
        self._label.setText(self.tr(label_text))

    def get_widget_value(self):
        """
        Retrieves the value of the selected radio button.

        Returns:
            object: The value of the selected radio button.

        Raises:
            TypeError: If the returned button is not of type RadioButton.
        """
        selected_btn = self._button_group.checkedButton()
        if isinstance(selected_btn, RadioButton):
            return selected_btn.get_widget_value()
        raise TypeError("For some reason returned button is not of type RadioButton")

    def set_widget_value(self, value: T):
        """
        Sets the value of the selected radio button.

        Args:
            value (T): The value to set.
        """
        for button in self._button_group.buttons():
            if isinstance(button, RadioButton) and button.get_widget_value() == value:
                button.setChecked(True)
