from PySide6.QtCore import Slot

from core.commands.prep_add_text import AddTextPrepareCommand
from core.enums import ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.customs.app_widgets.line_edit_form import LineTextEditForm
from ui.customs.app_widgets.radio_button_form import RadioButtonForm, RadioButtonItem
from ui.widgets.base_abstract_widgets import (
    BasePrepareCommandWidget,
)


class AddTextWidget(BasePrepareCommandWidget):
    _radio_enum_widget: RadioButtonForm
    _text_to_add: LineTextEditForm
    _selected_radio_value: ItemPosition
    _text_value: str

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._radio_enum_widget = RadioButtonForm(self)
        self._text_to_add = LineTextEditForm(self)

    def configure_widgets(self):
        items = []
        for value, text in ITEM_POSITION_TEXT.items():
            items.append(RadioButtonItem(value, text))
        self._radio_enum_widget.set_widget_items(items)

        self._text_to_add.setMaximumHeight(100)

        self._main_layout.addWidget(self._radio_enum_widget)
        self._main_layout.addWidget(self._text_to_add)

        self._text_value = self._text_to_add.get_widget_value()
        self._selected_radio_value = self._radio_enum_widget.get_widget_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._text_to_add.set_widget_label(self.tr("Text to add"))
        self._radio_enum_widget.set_widget_label(self.tr("Select position of text:"))

    def create_event_handlers(self):
        self._radio_enum_widget.valueIsChanged.connect(self.handle_radio_changed)
        self._text_to_add.valueIsChanged.connect(self.handle_text_changed)

    def request_command(self) -> AddTextPrepareCommand:
        return AddTextPrepareCommand(text=self._text_value, position=self._selected_radio_value)

    @Slot()
    def handle_radio_changed(self, selected_radio: ItemPosition):
        print(f"handle_radio_changed, {selected_radio}")
        self._selected_radio_value = selected_radio

    @Slot()
    def handle_text_changed(self, text: str):
        print(f"handle_text_changed, {text}")
        self._text_value = text
