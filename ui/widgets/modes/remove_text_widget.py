from core.commands.prep_remove_text import RemoveTextPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPositionExtended, ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget


class RemoveTextWidget(BasePrepareCommandWidget):
    _text_position_radio_btn: LabelRadioButtonsWidget
    _text_to_remove_line_edit: LabelLineEditWidget

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._text_position_radio_btn = LabelRadioButtonsWidget(parent=self, enum_class=ItemPosition,
                                                                text_mapping=ITEM_POSITION_TEXT)
        self._text_to_remove_line_edit = LabelLineEditWidget(self)

    def configure_widgets(self):
        self._main_layout.addWidget(self._text_position_radio_btn)
        self._main_layout.addWidget(self._text_to_remove_line_edit)

    def add_text_to_widgets(self):
        self._text_position_radio_btn.set_label_text(self.tr("Select side where to remove text:"))
        self._text_to_remove_line_edit.set_label_text(self.tr("Enter text to remove:"))

    def create_event_handlers(self):
        pass

    def request_command(self) -> PrepareCommand:
        return RemoveTextPrepareCommand(
            text="",
            position=ItemPositionExtended.BEGIN
        )
