from PySide6.QtCore import Slot

from core.commands.prep_change_case import ChangeCasePreparePrepareCommand
from core.commons import PrepareCommand
from core.enums import TextCaseOptions
from core.text_values import TEXT_CASE_OPTIONS_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_checkbox_widget import LabelCheckboxWidget
from ui.widgets.customs.pairs.label_combobox_widget import LabelComboboxWidget


class ChangeCaseWidget(BasePrepareCommandWidget):
    _case_options: LabelComboboxWidget
    _capitalize: LabelCheckboxWidget
    _selected_value: TextCaseOptions
    _capitalize_value: bool

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._case_options = LabelComboboxWidget(
            parent=self, enum_class=TextCaseOptions, text_mapping=TEXT_CASE_OPTIONS_TEXT
        )
        self._capitalize = LabelCheckboxWidget(self)

    def configure_widgets(self):
        self._case_options.setMaximumHeight(50)
        self._capitalize.set_value(False)
        self._capitalize_value = self._capitalize.get_current_value()

        self._main_layout.addWidget(self._case_options)
        self._main_layout.addWidget(self._capitalize)

        self._selected_value = self._case_options.get_current_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._case_options.set_label_text("Chose case to use:")
        self._capitalize.set_label_text("Capitalize")

    def create_event_handlers(self):
        self._case_options.valueIsChanged.connect(self.handle_item_selected)
        self._capitalize.valueIsChanged.connect(self.handle_checked_changed)

    def request_command(self) -> PrepareCommand:
        return ChangeCasePreparePrepareCommand(
            capitalize=self._capitalize_value, text_case=self._selected_value
        )

    @Slot()
    def handle_item_selected(self, text_case: TextCaseOptions):
        print(f"handle_item_selected, {text_case}")
        self._selected_value = text_case

    @Slot()
    def handle_checked_changed(self, state: bool):
        print(f"handle_checked_changed, {state}")
        self._capitalize_value = state
