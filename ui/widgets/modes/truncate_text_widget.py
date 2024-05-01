from PySide6.QtCore import Slot

from core.commands.prep_truncate_text import TruncateNamePrepareCommand
from core.enums import TruncateOptions
from core.text_values import TRUNCATE_OPTIONS_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget
from ui.widgets.customs.pairs.label_spinbox_widget import LabelSpinBoxWidget


class TruncateTextWidget(BasePrepareCommandWidget):
    _truncate_options_radio_btn: LabelRadioButtonsWidget
    _number_of_symbols_spinbox: LabelSpinBoxWidget
    _truncate_options_value: TruncateOptions
    _number_of_symbols_value: int

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._truncate_options_radio_btn = LabelRadioButtonsWidget(
            parent=self,
            enum_class=TruncateOptions,
            text_mapping=TRUNCATE_OPTIONS_TEXT,
            vertical=True,
        )
        self._number_of_symbols_spinbox = LabelSpinBoxWidget(self)

    def configure_widgets(self):
        self._number_of_symbols_spinbox.spin_box_min = 0
        self._main_layout.addWidget(self._truncate_options_radio_btn)
        self._main_layout.addWidget(self._number_of_symbols_spinbox)

        self._truncate_options_value = self._truncate_options_radio_btn.get_current_value()
        self._number_of_symbols_value = self._number_of_symbols_spinbox.get_current_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._truncate_options_radio_btn.set_label_text(self.tr("Chose from which side truncate:"))
        self._number_of_symbols_spinbox.set_label_text(self.tr("Number of symbols to truncate:"))

    def create_event_handlers(self):
        self._truncate_options_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._number_of_symbols_spinbox.valueIsChanged.connect(self.handle_number_of_symbols_changed)

    def request_command(self) -> TruncateNamePrepareCommand:
        return TruncateNamePrepareCommand(
            number_of_symbols=self._number_of_symbols_value,
            truncate_options=self._truncate_options_value,
        )

    @Slot()
    def handle_position_changed(self, value: TruncateOptions):
        print(f"handle_position_changed: {value}")
        self._truncate_options_value = value

    @Slot()
    def handle_number_of_symbols_changed(self, value: int):
        print(f"handle_number_of_symbols_changed: {value}")
        self._number_of_symbols_value = value
