import logging

from PySide6.QtCore import Slot

from core.commands.prep_parent_folders import ParentFoldersPrepareCommand
from core.enums import ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.customs.form_widgets import (
    LineTextEditForm,
    NumberSpinForm,
    RadioButtonForm,
    build_radio_button_items,
)
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

log: logging.Logger = logging.getLogger(__name__)

POSITION_RADIO_BTN_ITEMS = build_radio_button_items(ITEM_POSITION_TEXT)


class ParentFoldersWidget(ModeBaseWidget):
    _folder_name_position_radio_btn: RadioButtonForm
    _number_of_parents_spinbox: NumberSpinForm
    _separator_line_edit: LineTextEditForm

    def init_widgets(self):
        self._folder_name_position_radio_btn = RadioButtonForm(self)
        self._number_of_parents_spinbox = NumberSpinForm(self)
        self._separator_line_edit = LineTextEditForm(self)

    def configure_widgets(self):
        self._folder_name_position_radio_btn.set_widget_items(POSITION_RADIO_BTN_ITEMS)
        self._number_of_parents_spinbox.spin_box_min = 1

        self.add_widget(self._folder_name_position_radio_btn)
        self.add_widget(self._number_of_parents_spinbox)
        self.add_widget(self._separator_line_edit)

    def add_text_to_widgets(self):
        self._folder_name_position_radio_btn.set_widget_label("Select renaming mode:")
        self._number_of_parents_spinbox.set_widget_label("Number of parent folders to include:")
        self._separator_line_edit.set_widget_label("Separator between folders:")

    def create_event_handlers(self):
        self._folder_name_position_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._number_of_parents_spinbox.valueIsChanged.connect(self.handle_parents_number_changed)
        self._separator_line_edit.valueIsChanged.connect(self.handle_separator_changed)

    def request_command(self) -> ParentFoldersPrepareCommand:
        position = self._folder_name_position_radio_btn.get_widget_value()
        number_of_parents = self._number_of_parents_spinbox.get_widget_value()
        separator = self._separator_line_edit.get_widget_value()

        return ParentFoldersPrepareCommand(
            position=position,
            number_of_parents=number_of_parents,
            separator=separator,
        )

    @Slot()
    def handle_position_changed(self, value: ItemPosition):
        log.debug(f"handle_position_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_parents_number_changed(self, value: int):
        log.debug(f"handle_parents_number_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_separator_changed(self, value: str):
        log.debug(f"handle_separator_changed: {value}")
        self.tell_about_changes()
