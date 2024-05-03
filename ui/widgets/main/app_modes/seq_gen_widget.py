from PySide6.QtCore import Slot

from core.commands.prep_seq_gen import SequencePrepareCommand
from core.enums import SortSource
from core.text_values import SORT_SOURCE_TEXT
from ui.widgets.customs.form_widgets import ComboboxForm, NumberSpinForm
from ui.widgets.customs.single_widgets import build_combobox_items
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

SOURCE_CMB_ITEMS = build_combobox_items(SORT_SOURCE_TEXT)


class SequenceGeneratorWidget(ModeBaseWidget):
    _start_number_spinbox: NumberSpinForm
    _step_value_spinbox: NumberSpinForm
    _padding_spinbox: NumberSpinForm
    _sort_source_combobox: ComboboxForm

    def init_widgets(self):
        self._start_number_spinbox = NumberSpinForm(self)
        self._step_value_spinbox = NumberSpinForm(self)
        self._padding_spinbox = NumberSpinForm(self)
        self._sort_source_combobox = ComboboxForm(self)

    def configure_widgets(self):
        self._start_number_spinbox.spin_box_min = 0
        self._step_value_spinbox.spin_box_min = 1
        self._padding_spinbox.spin_box_min = 1
        self._sort_source_combobox.set_widget_items(SOURCE_CMB_ITEMS)
        self.add_widget(self._start_number_spinbox)
        self.add_widget(self._step_value_spinbox)
        self.add_widget(self._padding_spinbox)
        self.add_widget(self._sort_source_combobox)

    def add_text_to_widgets(self):
        self._start_number_spinbox.set_widget_label("Select start number:")
        self._step_value_spinbox.set_widget_label("Select step value:")
        self._padding_spinbox.set_widget_label("Select minimal amount of digits in number:")
        self._sort_source_combobox.set_widget_label("Select sorting source:")

    def create_event_handlers(self):
        self._start_number_spinbox.valueIsChanged.connect(self.handle_start_number_changed)
        self._step_value_spinbox.valueIsChanged.connect(self.handle_step_number_changed)
        self._padding_spinbox.valueIsChanged.connect(self.handle_padding_number_changed)
        self._sort_source_combobox.valueIsChanged.connect(self.handle_sort_source_changed)

    def request_command(self) -> SequencePrepareCommand:
        start_val = self._start_number_spinbox.get_widget_value()
        step_val = self._step_value_spinbox.get_widget_value()
        padding = self._padding_spinbox.get_widget_value()
        source = self._sort_source_combobox.get_widget_value()

        return SequencePrepareCommand(
            start_number=start_val,
            step_value=step_val,
            padding=padding,
            sort_source=source,
        )

    @Slot()
    def handle_start_number_changed(self, value: int):
        print(f"handle_start_number_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_step_number_changed(self, value: int):
        print(f"handle_step_number_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_padding_number_changed(self, value: int):
        print(f"handle_padding_number_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_sort_source_changed(self, value: SortSource):
        print(f"handle_sort_source_changed: {value}")
        self.tell_about_changes()
