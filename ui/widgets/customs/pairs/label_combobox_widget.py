from enum import IntEnum
from typing import TypeVar, Type

from PySide6.QtCore import Slot, Signal
from PySide6.QtGui import QCursor, Qt
from PySide6.QtWidgets import QWidget, QComboBox, QSizePolicy

from ui.widgets.base_abstract_widgets import BaseLabelWidget

T = TypeVar("T", bound=IntEnum)


class LabelComboboxWidget(BaseLabelWidget):
    _combobox: QComboBox

    _enum_class: Type[T]
    _text_mapping: dict[T, str]

    valueIsChanged = Signal(IntEnum)

    def __init__(self, enum_class: Type[T], text_mapping: dict[T, str], parent=None):
        self._enum_class = enum_class
        self._text_mapping = text_mapping
        super().__init__(parent)
        self.setContentsMargins(0, 0, 0, 0)

    def create_pair_widget(self) -> QWidget:
        self._combobox = QComboBox(self)
        self._combobox.setSizePolicy(
            QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding
        )

        enum_list = list(self._enum_class)
        for enum_item in enum_list:
            text = self.tr(self._text_mapping.get(enum_item))
            self._combobox.addItem(text, enum_item.value)  # Use enum value as data
        self._combobox.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        return self._combobox

    def create_event_handlers(self):
        self._combobox.currentIndexChanged.connect(self.handle_combobox_index_changed)

    def get_current_value(self) -> T:
        index = self._combobox.currentIndex()
        return self._combobox.itemData(index)

    @Slot()
    def handle_combobox_index_changed(self, index: int):
        selected_enum_value = self._combobox.itemData(
            index
        )  # Get the data associated with the selected item
        self.valueIsChanged.emit(selected_enum_value)  # Emit the selected enum value
