import logging
from abc import ABC
from dataclasses import dataclass
from typing import List, TypeVar

from PySide6.QtCore import Qt, Slot
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import QComboBox

from ui.widgets.customs.abstracts import MultipleValueWidgetApi

log: logging.Logger = logging.getLogger(__name__)

T = TypeVar("T")


class Meta(type(ABC), type(QComboBox)):
    pass


@dataclass
class ComboBoxItem[T]:
    """
    Dataclass representing an item in a ComboBox.

    Attributes:
        item_value (T): The value associated with the item.
        item_text (str): The text displayed for the item.
    """

    item_value: T
    item_text: str


def build_combobox_items(value_to_text: dict[object, str]) -> list[ComboBoxItem]:
    """
    Build a list of ComboBoxItems from a dictionary mapping values to text.

    Args:
        value_to_text (dict[object, str]): A dictionary mapping values to text.

    Returns:
        list[ComboBoxItem]: A list of ComboBoxItems.
    """
    result_list = []
    for value, text in value_to_text.items():
        result_list.append(ComboBoxItem(item_value=value, item_text=text))
    return result_list


class ComboBox(QComboBox, MultipleValueWidgetApi, metaclass=Meta):
    """
    Custom QComboBox widget with additional functionality.

    Inherits:
        QComboBox: Standard QComboBox widget.
        MultipleValueWidgetApi: Custom widget for handling multiple values.

    Signals:
        valueIsChanged: Signal emitted when the selected value changes.

    Methods:
        __init__: Initializes the combobox.
        _handle_event: Slot method to handle combobox item selection events.
        get_widget_value: Returns the currently selected value.
        set_widget_value: Sets the currently selected value.
        set_widget_items: Sets the items in the combobox.
        get_widget_items: Returns the items in the combobox.
    """

    def __init__(self, parent=None):
        """
        Initializes the ComboBox widget.

        Args:
            parent: Optional parent widget.
        """
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.currentIndexChanged.connect(self._handle_event)

    @Slot()
    def _handle_event(self, index: int):
        """
        Slot method to handle combobox item selection events.
        Emits the valueIsChanged signal with the selected value.

        Args:
            index (int): The index of the selected item.
        """
        log.debug(f"ComboBox._handle_event, {index}")
        selected_enum_value = self.itemData(index)
        self.valueIsChanged.emit(selected_enum_value)

    def get_widget_value(self) -> T:
        """
        Returns the currently selected value.

        Returns:
            T: The currently selected value.
        """
        index = self.currentIndex()
        return self.itemData(index)

    def set_widget_value(self, value: T):
        """
        Sets the currently selected value.

        Args:
            value (T): The value to be selected.
        """
        index = self.findData(value)
        self.setCurrentIndex(index)

    def set_widget_items(self, items: List[ComboBoxItem[T]]):
        """
        Sets the items in the combobox.

        Args:
            items (List[ComboBoxItem[T]]): The items to be set.
        """
        for item in items:
            text = self.tr(item.item_text)
            self.addItem(text, item.item_value)

    def get_widget_items(self) -> List[ComboBoxItem[T]]:
        """
        Returns the items in the combobox.

        Returns:
            List[ComboBoxItem[T]]: The items in the combobox.
        """
        items = []
        for index in range(self.count()):
            item_text = self.itemText(index)
            item_value = self.itemData(index)
            items.append(ComboBoxItem(item_value, item_text))
        return items
