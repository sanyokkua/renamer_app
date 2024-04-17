from enum import IntEnum
from typing import TypeVar, Type

from PySide6.QtCore import Qt, Signal
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import (QButtonGroup, QLabel, QVBoxLayout, QHBoxLayout, QRadioButton)

from ui.widgets.base_abstract_widgets import BaseLabelWidget

T = TypeVar('T', bound=IntEnum)


class LabelRadioButtonsWidget(BaseLabelWidget):
    _radio_button_group: QButtonGroup

    _enum_class: Type[T]
    _text_mapping: dict[T, str]
    _vertical: bool

    valueIsChanged = Signal(IntEnum)

    def __init__(self, enum_class: Type[T], text_mapping: dict[T, str], parent=None, vertical: bool = False):
        self._enum_class = enum_class
        self._text_mapping = text_mapping
        self._vertical = vertical
        super().__init__(parent)

    def custom_init(self):
        self._radio_button_group = QButtonGroup(self)
        self._widget_label: QLabel = QLabel(self)
        self._widget_layout = QVBoxLayout(self) if self._vertical else QHBoxLayout(self)
        self._widget_layout.addWidget(self._widget_label)
        self.setLayout(self._widget_layout)
        self.create_pair_widget()

    def create_pair_widget(self):
        enum_list = list(self._enum_class)
        for enum_item in enum_list:
            text = self.tr(self._text_mapping.get(enum_item))

            btn = QRadioButton(self)
            btn.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
            btn.setText(text)

            self._widget_layout.addWidget(btn)
            self._radio_button_group.addButton(btn, enum_item)
            if enum_item == 0:
                btn.setChecked(True)

    def create_event_handlers(self):
        self._radio_button_group.idToggled.connect(lambda btn_id: self.valueIsChanged.emit(btn_id))

    def get_current_value(self) -> IntEnum:
        return self._radio_button_group.checkedId()
