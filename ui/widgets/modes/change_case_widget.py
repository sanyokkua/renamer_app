from PySide6.QtCore import Slot
from PySide6.QtWidgets import (QCheckBox)

from core.commands.prep_change_case import ChangeCasePreparePrepareCommand
from core.commons import PrepareCommand
from core.enums import TextCaseOptions
from core.text_values import TEXT_CASE_OPTIONS_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_combobox_widget import LabelComboboxWidget


class ChangeCaseWidget(BasePrepareCommandWidget):
    _case_options: LabelComboboxWidget
    _capitalize: QCheckBox
    _selected_value: TextCaseOptions

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._case_options = LabelComboboxWidget(parent=self, enum_class=TextCaseOptions,
                                                 text_mapping=TEXT_CASE_OPTIONS_TEXT)
        self._capitalize = QCheckBox(self)

    def configure_widgets(self):
        self._case_options.setMaximumHeight(50)
        self._capitalize.setChecked(False)

        self._main_layout.addWidget(self._case_options)
        self._main_layout.addWidget(self._capitalize)

        self._selected_value = self._case_options.get_current_value()

    def add_text_to_widgets(self):
        self._case_options.set_label_text("Chose case to use:")
        self._capitalize.setText("Capitalize")

    def create_event_handlers(self):
        self._case_options.valueIsChanged.connect(self.handle_item_selected)

    def request_command(self) -> PrepareCommand:
        return ChangeCasePreparePrepareCommand(capitalize=self._capitalize.isChecked(), text_case=self._selected_value)

    @Slot()
    def handle_item_selected(self, text_case: TextCaseOptions):
        print(f"handle_item_selected, {text_case}")
        self._selected_value = text_case
