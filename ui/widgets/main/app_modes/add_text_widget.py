from PySide6.QtCore import Slot

from core.commands.prep_add_text import AddTextPrepareCommand
from core.enums import ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.customs.form_widgets import LineTextEditForm
from ui.widgets.customs.form_widgets import RadioButtonForm, RadioButtonItem, build_radio_button_items
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

RADIO_BTN_ITEMS: list[RadioButtonItem] = build_radio_button_items(ITEM_POSITION_TEXT)


class AddTextWidget(ModeBaseWidget):
    _position_radio_btn: RadioButtonForm
    _text_to_add: LineTextEditForm

    def init_widgets(self):
        self._position_radio_btn = RadioButtonForm(self)
        self._text_to_add = LineTextEditForm(self)

    def configure_widgets(self):
        self._position_radio_btn.set_widget_items(RADIO_BTN_ITEMS)

        self.add_widget(self._position_radio_btn)
        self.add_widget(self._text_to_add)

    def add_text_to_widgets(self):
        self._position_radio_btn.set_widget_label("Add Text To:")
        self._text_to_add.set_widget_label("Text:")

    def create_event_handlers(self):
        self._position_radio_btn.valueIsChanged.connect(self.handle_radio_changed)
        self._text_to_add.valueIsChanged.connect(self.handle_text_changed)

    def request_command(self) -> AddTextPrepareCommand:
        text_value = self._text_to_add.get_widget_value()
        text_position = self._position_radio_btn.get_widget_value()
        return AddTextPrepareCommand(text=text_value, position=text_position)

    @Slot()
    def handle_radio_changed(self, selected_radio: ItemPosition):
        print(f"handle_radio_changed, {selected_radio}")
        self.tell_about_changes()

    @Slot()
    def handle_text_changed(self, text: str):
        print(f"handle_text_changed, {text}")
        self.tell_about_changes()
