import logging

from PySide6.QtCore import Slot

from core.commands.prep_truncate_text import TruncateNamePrepareCommand
from core.enums import TruncateOptions
from core.text_values import TRUNCATE_OPTIONS_TEXT
from ui.widgets.customs.form_widgets import (
    NumberSpinForm,
    RadioButtonForm,
    build_radio_button_items,
)
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

log: logging.Logger = logging.getLogger(__name__)

POSITION_RADIO_BTN_ITEMS = build_radio_button_items(TRUNCATE_OPTIONS_TEXT)


class TruncateTextWidget(ModeBaseWidget):
    _truncate_options_radio_btn: RadioButtonForm
    _number_of_symbols_spinbox: NumberSpinForm

    def init_widgets(self):
        self._truncate_options_radio_btn = RadioButtonForm(self)
        self._number_of_symbols_spinbox = NumberSpinForm(self)

    def configure_widgets(self):
        self._truncate_options_radio_btn.set_widget_items(POSITION_RADIO_BTN_ITEMS)
        self._number_of_symbols_spinbox.spin_box_min = 0
        self.add_widget(self._truncate_options_radio_btn)
        self.add_widget(self._number_of_symbols_spinbox)

    def add_text_to_widgets(self):
        self._truncate_options_radio_btn.set_widget_label("Truncation side:")
        self._number_of_symbols_spinbox.set_widget_label("Number of symbols to truncate:")

    def create_event_handlers(self):
        self._truncate_options_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._number_of_symbols_spinbox.valueIsChanged.connect(self.handle_number_of_symbols_changed)

    def request_command(self) -> TruncateNamePrepareCommand:
        number_of_symbols = self._number_of_symbols_spinbox.get_widget_value()
        truncate_options = self._truncate_options_radio_btn.get_widget_value()
        return TruncateNamePrepareCommand(
            number_of_symbols=number_of_symbols,
            truncate_options=truncate_options,
        )

    @Slot()
    def handle_position_changed(self, value: TruncateOptions):
        log.debug(f"handle_position_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_number_of_symbols_changed(self, value: int):
        log.debug(f"handle_number_of_symbols_changed: {value}")
        self.tell_about_changes()
