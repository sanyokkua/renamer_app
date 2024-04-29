from PySide6.QtCore import Slot

from core.commands.prep_replace_text import ReplaceTextPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPositionExtended
from core.text_values import ITEM_POSITION_EXTENDED_TEXT
from ui.widgets.base_abstract_widgets import (
    BasePrepareCommandWidget,
    PATH_SYMBOLS_VALIDATOR,
)
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget


class ReplaceTextWidget(BasePrepareCommandWidget):
    _replace_position_radio_btn: LabelRadioButtonsWidget
    _text_to_replace_line_edit: LabelLineEditWidget
    _text_to_add_line_edit: LabelLineEditWidget
    _replace_position_value: ItemPositionExtended
    _text_to_replace_value: str
    _text_to_add_value: str

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._replace_position_radio_btn = LabelRadioButtonsWidget(
            parent=self,
            enum_class=ItemPositionExtended,
            text_mapping=ITEM_POSITION_EXTENDED_TEXT,
            vertical=True,
        )
        self._text_to_replace_line_edit = LabelLineEditWidget(self)
        self._text_to_add_line_edit = LabelLineEditWidget(self)

    def configure_widgets(self):
        self._text_to_replace_line_edit.set_text_validator(PATH_SYMBOLS_VALIDATOR)
        self._main_layout.addWidget(self._replace_position_radio_btn)
        self._main_layout.addWidget(self._text_to_replace_line_edit)
        self._main_layout.addWidget(self._text_to_add_line_edit)

        self._replace_position_value = (
            self._replace_position_radio_btn.get_current_value()
        )
        self._text_to_replace_value = (
            self._text_to_replace_line_edit.get_current_value()
        )
        self._text_to_add_value = self._text_to_add_line_edit.get_current_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._replace_position_radio_btn.set_label_text(
            self.tr("Chose where to replace text:")
        )
        self._text_to_replace_line_edit.set_label_text(
            self.tr("Enter text to replace:")
        )
        self._text_to_add_line_edit.set_label_text(self.tr("Enter text to add:"))

    def create_event_handlers(self):
        self._replace_position_radio_btn.valueIsChanged.connect(
            self.handle_position_changed
        )
        self._text_to_replace_line_edit.valueIsChanged.connect(
            self.handle_text_to_replace_changed
        )
        self._text_to_add_line_edit.valueIsChanged.connect(
            self.handle_text_to_add_changed
        )

    def request_command(self) -> PrepareCommand:
        return ReplaceTextPrepareCommand(
            position=self._replace_position_value,
            text_to_replace=self._text_to_replace_value,
            new_value=self._text_to_add_value,
        )

    @Slot()
    def handle_position_changed(self, value: ItemPositionExtended):
        print(f"handle_position_changed: {value}")
        self._replace_position_value = value

    @Slot()
    def handle_text_to_replace_changed(self, value: str):
        print(f"handle_text_to_replace_changed: {value}")
        self._text_to_replace_value = value

    @Slot()
    def handle_text_to_add_changed(self, value: str):
        print(f"handle_text_to_add_changed: {value}")
        self._text_to_add_value = value
