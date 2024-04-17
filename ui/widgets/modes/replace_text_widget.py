from core.commands.prep_replace_text import ReplaceTextPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPositionExtended
from core.text_values import ITEM_POSITION_EXTENDED_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget


class ReplaceTextWidget(BasePrepareCommandWidget):
    _replace_position_radio_btn: LabelRadioButtonsWidget
    _text_to_replace_line_edit: LabelLineEditWidget

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._replace_position_radio_btn = LabelRadioButtonsWidget(parent=self, enum_class=ItemPositionExtended,
                                                                   text_mapping=ITEM_POSITION_EXTENDED_TEXT)
        self._text_to_replace_line_edit = LabelLineEditWidget(self)

    def configure_widgets(self):
        self._main_layout.addWidget(self._replace_position_radio_btn)
        self._main_layout.addWidget(self._text_to_replace_line_edit)

    def add_text_to_widgets(self):
        self._replace_position_radio_btn.set_label_text(self.tr("Chose where to replace text:"))
        self._text_to_replace_line_edit.set_label_text(self.tr("Enter text to replace:"))

    def create_event_handlers(self):
        pass

    def request_command(self) -> PrepareCommand:
        return ReplaceTextPrepareCommand(
            text="",
            position=ItemPositionExtended.BEGIN
        )
