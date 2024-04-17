from PySide6.QtCore import Slot

from core.commands.prep_add_text import AddTextPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget


class AddTextWidget(BasePrepareCommandWidget):
    _radio_enum_widget: LabelRadioButtonsWidget
    _text_to_add: LabelLineEditWidget
    _selected_radio_value: ItemPosition
    _text_value: str

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._radio_enum_widget = LabelRadioButtonsWidget(parent=self, enum_class=ItemPosition,
                                                          text_mapping=ITEM_POSITION_TEXT)
        self._text_to_add = LabelLineEditWidget(self)

    def configure_widgets(self):
        self._radio_enum_widget.setMaximumHeight(50)
        self._text_to_add.setMaximumHeight(100)

        self._main_layout.addWidget(self._radio_enum_widget)
        self._main_layout.addWidget(self._text_to_add)

        self._text_value = ""
        self._selected_radio_value = ItemPosition(self._radio_enum_widget.get_current_value())

    def add_text_to_widgets(self):
        self._text_to_add.set_label_text(self.tr("Text to add"))
        self._radio_enum_widget.set_label_text(self.tr("Select position of text:"))
        self._text_to_add.set_label_text(self.tr("Enter the text to add:"))

    def create_event_handlers(self):
        self._radio_enum_widget.valueIsChanged.connect(self.handle_radio_changed)
        self._text_to_add.valueIsChanged.connect(self.handle_text_changed)

    def request_command(self) -> PrepareCommand:
        return AddTextPrepareCommand(text=self._text_value, position=self._selected_radio_value)

    @Slot()
    def handle_radio_changed(self, selected_radio: ItemPosition):
        print(f"handle_radio_changed, {selected_radio}")
        self._selected_radio_value = selected_radio

    @Slot()
    def handle_text_changed(self, text: str):
        print(f"handle_text_changed, {text}")
        self._text_value = text
