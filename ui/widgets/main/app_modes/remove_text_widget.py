from PySide6.QtCore import Slot

from core.commands.prep_remove_text import RemoveTextPrepareCommand
from core.enums import ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.customs.form_widgets import LineTextEditForm
from ui.widgets.customs.form_widgets import RadioButtonForm, build_radio_button_items
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

POSITION_RADIO_BTN_ITEMS = build_radio_button_items(ITEM_POSITION_TEXT)


class RemoveTextWidget(ModeBaseWidget):
    _text_position_radio_btn: RadioButtonForm
    _text_to_remove_line_edit: LineTextEditForm

    def init_widgets(self):
        self._text_position_radio_btn = RadioButtonForm(self)
        self._text_to_remove_line_edit = LineTextEditForm(self)

    def configure_widgets(self):
        self._text_position_radio_btn.set_widget_items(POSITION_RADIO_BTN_ITEMS)
        self.add_widget(self._text_position_radio_btn)
        self.add_widget(self._text_to_remove_line_edit)

    def add_text_to_widgets(self):
        self._text_position_radio_btn.set_widget_label("Select side where to remove text:")
        self._text_to_remove_line_edit.set_widget_label("Enter text to remove:")

    def create_event_handlers(self):
        self._text_position_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._text_to_remove_line_edit.valueIsChanged.connect(self.handle_remove_text_changed)

    def request_command(self) -> RemoveTextPrepareCommand:
        position = self._text_position_radio_btn.get_widget_value()
        text_to_remove = self._text_to_remove_line_edit.get_widget_value()
        return RemoveTextPrepareCommand(text=text_to_remove, position=position)

    @Slot()
    def handle_position_changed(self, value: ItemPosition):
        print(f"handle_position_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_remove_text_changed(self, value: str):
        print(f"handle_remove_text_changed: {value}")
        self.tell_about_changes()
