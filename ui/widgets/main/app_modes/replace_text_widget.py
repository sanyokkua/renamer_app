import logging

from PySide6.QtCore import Slot

from core.commands.prep_replace_text import ReplaceTextPrepareCommand
from core.enums import ItemPositionExtended
from core.text_values import ITEM_POSITION_EXTENDED_TEXT
from ui.widgets.customs.form_widgets import (
    LineTextEditForm,
    RadioButtonForm,
    build_radio_button_items,
)
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

log: logging.Logger = logging.getLogger(__name__)

POSITION_RADIO_BTN_ITEMS = build_radio_button_items(ITEM_POSITION_EXTENDED_TEXT)


class ReplaceTextWidget(ModeBaseWidget):
    _replace_position_radio_btn: RadioButtonForm
    _text_to_replace_line_edit: LineTextEditForm
    _text_to_add_line_edit: LineTextEditForm

    def init_widgets(self):
        self._replace_position_radio_btn = RadioButtonForm()
        self._text_to_replace_line_edit = LineTextEditForm(self)
        self._text_to_add_line_edit = LineTextEditForm(self)

    def configure_widgets(self):
        self._replace_position_radio_btn.set_widget_items(POSITION_RADIO_BTN_ITEMS)
        self.add_widget(self._replace_position_radio_btn)
        self.add_widget(self._text_to_replace_line_edit)
        self.add_widget(self._text_to_add_line_edit)

    def add_text_to_widgets(self):
        self._replace_position_radio_btn.set_widget_label("Chose where to replace text:")
        self._text_to_replace_line_edit.set_widget_label("Enter text to replace:")
        self._text_to_add_line_edit.set_widget_label("Enter text to add:")

    def create_event_handlers(self):
        self._replace_position_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._text_to_replace_line_edit.valueIsChanged.connect(self.handle_text_to_replace_changed)
        self._text_to_add_line_edit.valueIsChanged.connect(self.handle_text_to_add_changed)

    def request_command(self) -> ReplaceTextPrepareCommand:
        position = self._replace_position_radio_btn.get_widget_value()
        replace_text = self._text_to_replace_line_edit.get_widget_value()
        new_text = self._text_to_add_line_edit.get_widget_value()

        return ReplaceTextPrepareCommand(
            position=position,
            text_to_replace=replace_text,
            new_value=new_text,
        )

    @Slot()
    def handle_position_changed(self, value: ItemPositionExtended):
        log.debug(f"handle_position_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_text_to_replace_changed(self, value: str):
        log.debug(f"handle_text_to_replace_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_text_to_add_changed(self, value: str):
        log.debug(f"handle_text_to_add_changed: {value}")
        self.tell_about_changes()
