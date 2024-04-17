from core.commands.prep_truncate_text import TruncateNamePrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPosition
from core.enums import TruncateOptions
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget
from ui.widgets.customs.pairs.label_spinbox_widget import LabelSpinBoxWidget


class TruncateTextWidget(BasePrepareCommandWidget):
    _truncate_options_radio_btn: LabelRadioButtonsWidget
    _number_of_symbols_spinbox: LabelSpinBoxWidget

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._truncate_options_radio_btn = LabelRadioButtonsWidget(parent=self, enum_class=ItemPosition,
                                                                   text_mapping=ITEM_POSITION_TEXT)
        self._number_of_symbols_spinbox = LabelSpinBoxWidget(self)

    def configure_widgets(self):
        self._number_of_symbols_spinbox.spin_box_min = 0
        self._main_layout.addWidget(self._truncate_options_radio_btn)
        self._main_layout.addWidget(self._number_of_symbols_spinbox)

    def add_text_to_widgets(self):
        self._truncate_options_radio_btn.set_label_text(self.tr("Chose from which side truncate"))
        self._number_of_symbols_spinbox.set_label_text(self.tr("Number of symbols to truncate"))

    def create_event_handlers(self):
        pass

    def request_command(self) -> PrepareCommand:
        return TruncateNamePrepareCommand(
            number_of_symbols=0,
            truncate_options=TruncateOptions.TRUNCATE_EMPTY_SYMBOLS
        )
