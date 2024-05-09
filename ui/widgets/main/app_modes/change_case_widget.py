import logging

from PySide6.QtCore import Slot

from core.commands.prep_change_case import ChangeCasePreparePrepareCommand
from core.enums import TextCaseOptions
from core.text_values import TEXT_CASE_OPTIONS_TEXT
from ui.widgets.customs.form_widgets import ComboboxForm
from ui.widgets.customs.single_widgets import (
    CheckBox,
    ComboBoxItem,
    build_combobox_items,
)
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

log: logging.Logger = logging.getLogger(__name__)

TEXT_CASE_CMB_ITEMS: list[ComboBoxItem] = build_combobox_items(TEXT_CASE_OPTIONS_TEXT)


class ChangeCaseWidget(ModeBaseWidget):
    _text_case_combobox: ComboboxForm
    _capitalize_checkbox: CheckBox

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._text_case_combobox = ComboboxForm(self)
        self._capitalize_checkbox = CheckBox(self)

    def configure_widgets(self):
        self._text_case_combobox.set_widget_items(TEXT_CASE_CMB_ITEMS)
        self._capitalize_checkbox.set_widget_value(False)
        self.add_widget(self._text_case_combobox)
        self.add_widget(self._capitalize_checkbox)

    def add_text_to_widgets(self):
        self._text_case_combobox.set_widget_label("Text Case:")
        self._capitalize_checkbox.set_widget_label("Capitalize?")

    def create_event_handlers(self):
        self._text_case_combobox.valueIsChanged.connect(self.handle_item_selected)
        self._capitalize_checkbox.valueIsChanged.connect(self.handle_checked_changed)

    def request_command(self) -> ChangeCasePreparePrepareCommand:
        capitalize = self._capitalize_checkbox.get_widget_value()
        text_case = self._text_case_combobox.get_widget_value()
        return ChangeCasePreparePrepareCommand(capitalize=capitalize, text_case=text_case)

    @Slot()
    def handle_item_selected(self, text_case: TextCaseOptions):
        log.debug(f"handle_item_selected, {text_case}")
        self.tell_about_changes()

    @Slot()
    def handle_checked_changed(self, state: bool):
        log.debug(f"handle_checked_changed, {state}")
        self.tell_about_changes()
