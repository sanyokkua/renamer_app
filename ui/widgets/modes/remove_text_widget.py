from PySide6.QtCore import Slot

from core.commands.prep_remove_text import RemoveTextPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.base_abstract_widgets import (
    BasePrepareCommandWidget,
    PATH_SYMBOLS_VALIDATOR,
)
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget


class RemoveTextWidget(BasePrepareCommandWidget):
    _text_position_radio_btn: LabelRadioButtonsWidget
    _text_to_remove_line_edit: LabelLineEditWidget
    _text_position_value: ItemPosition
    _text_to_remove_value: str

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._text_position_radio_btn = LabelRadioButtonsWidget(
            parent=self,
            enum_class=ItemPosition,
            text_mapping=ITEM_POSITION_TEXT,
            vertical=True,
        )
        self._text_to_remove_line_edit = LabelLineEditWidget(self)

    def configure_widgets(self):
        self._text_to_remove_line_edit.set_text_validator(PATH_SYMBOLS_VALIDATOR)
        self._main_layout.addWidget(self._text_position_radio_btn)
        self._main_layout.addWidget(self._text_to_remove_line_edit)

        self._text_position_value = self._text_position_radio_btn.get_current_value()
        self._text_to_remove_value = self._text_to_remove_line_edit.get_current_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._text_position_radio_btn.set_label_text(
            self.tr("Select side where to remove text:")
        )
        self._text_to_remove_line_edit.set_label_text(self.tr("Enter text to remove:"))

    def create_event_handlers(self):
        self._text_position_radio_btn.valueIsChanged.connect(
            self.handle_position_changed
        )
        self._text_to_remove_line_edit.valueIsChanged.connect(
            self.handle_remove_text_changed
        )

    def request_command(self) -> PrepareCommand:
        return RemoveTextPrepareCommand(
            text=self._text_to_remove_value, position=self._text_position_value
        )

    @Slot()
    def handle_position_changed(self, value: ItemPosition):
        print(f"handle_position_changed: {value}")
        self._text_position_value = value

    @Slot()
    def handle_remove_text_changed(self, value: str):
        print(f"handle_remove_text_changed: {value}")
        self._text_to_remove_value = value
