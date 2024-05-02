from abc import ABC
from dataclasses import dataclass
from typing import TypeVar, List

from PySide6.QtCore import Qt, Slot
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import QComboBox

from ui.customs.abstracts import MultipleValueWidgetApi

T = TypeVar("T")


class Meta(type(ABC), type(QComboBox)):
    pass


@dataclass
class ComboBoxItem[T]:
    item_value: T
    item_text: str


class ComboBox(QComboBox, MultipleValueWidgetApi, metaclass=Meta):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self.currentIndexChanged.connect(self._handle_event)

    @Slot()
    def _handle_event(self, index: int):
        selected_enum_value = self.itemData(index)
        self.valueIsChanged.emit(selected_enum_value)

    def get_widget_value(self) -> T:
        index = self.currentIndex()
        return self.itemData(index)

    def set_widget_value(self, value: T):
        index = self.findData(value)
        self.setCurrentIndex(index)

    def set_widget_items(self, items: List[ComboBoxItem[T]]):
        for item in items:
            text = self.tr(item.item_text)
            self.addItem(text, item.item_value)

    def get_widget_items(self) -> List[ComboBoxItem[T]]:
        items = []
        for index in range(self.count()):
            item_text = self.itemText(index)
            item_value = self.itemData(index)
            items.append(ComboBoxItem(item_value, item_text))
        return items
