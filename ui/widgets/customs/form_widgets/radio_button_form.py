from dataclasses import dataclass
from enum import IntEnum
from typing import TypeVar

from PySide6.QtCore import Signal
from PySide6.QtWidgets import QLabel, QButtonGroup, QHBoxLayout

from ui.widgets.customs.base_widget import BaseMultipleValueWidgetApi
from ui.widgets.customs.single_widgets import RadioButton

T = TypeVar("T", bound=IntEnum)


@dataclass
class RadioButtonItem[T]:
    item_value: T
    item_text: str


def build_radio_button_items(value_to_text: dict[object, str]) -> list[RadioButtonItem]:
    result_list = []
    for value, text in value_to_text.items():
        result_list.append(RadioButtonItem(item_value=value, item_text=text))
    return result_list


class RadioButtonForm(BaseMultipleValueWidgetApi):
    _items: list[RadioButtonItem[T]] = []
    _button_group: QButtonGroup
    _label: QLabel
    _layout: QHBoxLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        self._layout = QHBoxLayout(self)
        self._label = QLabel(self)
        self._button_group = QButtonGroup(self)

    def configure_widgets(self):
        self._layout.addWidget(self._label)
        self.setLayout(self._layout)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._button_group.buttonToggled.connect(lambda btn: self.valueIsChanged.emit(btn.get_widget_value()))

    def set_widget_items(self, items: list[RadioButtonItem[T]]):
        self._items.clear()
        for item in items:
            self._items.append(item)
        self.build_buttons()

    def build_buttons(self):
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
        return [item for item in self._items]

    def set_widget_label(self, label_text: str):
        self._label.setText(self.tr(label_text))

    def get_widget_value(self):
        selected_btn = self._button_group.checkedButton()
        if isinstance(selected_btn, RadioButton):
            return selected_btn.get_widget_value()
        raise TypeError("For some reason returned button is not of type RadioButton")

    def set_widget_value(self, value: T):
        for button in self._button_group.buttons():
            if isinstance(button, RadioButton) and button.get_widget_value() == value:
                button.setChecked(True)
