from PySide6.QtCore import Slot

from core.commands.prep_seq_gen import SequencePrepareCommand
from core.commons import PrepareCommand
from core.enums import SortSource
from core.text_values import SORT_SOURCE_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_combobox_widget import LabelComboboxWidget
from ui.widgets.customs.pairs.label_spinbox_widget import LabelSpinBoxWidget


class SequenceGeneratorWidget(BasePrepareCommandWidget):
    _start_number_spinbox: LabelSpinBoxWidget
    _step_value_spinbox: LabelSpinBoxWidget
    _padding_spinbox: LabelSpinBoxWidget
    _sort_source_combobox: LabelComboboxWidget
    _start_number_value: int
    _step_value_value: int
    _padding_value: int
    _sort_source_value: SortSource

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._start_number_spinbox = LabelSpinBoxWidget(self)
        self._step_value_spinbox = LabelSpinBoxWidget(self)
        self._padding_spinbox = LabelSpinBoxWidget(self)
        self._sort_source_combobox = LabelComboboxWidget(parent=self, enum_class=SortSource,
                                                         text_mapping=SORT_SOURCE_TEXT)

    def configure_widgets(self):
        self._start_number_spinbox.spin_box_min = 0
        self._step_value_spinbox.spin_box_min = 1
        self._padding_spinbox.spin_box_min = 1
        self._main_layout.addWidget(self._start_number_spinbox)
        self._main_layout.addWidget(self._step_value_spinbox)
        self._main_layout.addWidget(self._padding_spinbox)
        self._main_layout.addWidget(self._sort_source_combobox)

        self._start_number_value = self._start_number_spinbox.get_current_value()
        self._step_value_value = self._step_value_spinbox.get_current_value()
        self._padding_value = self._padding_spinbox.get_current_value()
        self._sort_source_value = self._sort_source_combobox.get_current_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._start_number_spinbox.set_label_text(self.tr("Select start number:"))
        self._step_value_spinbox.set_label_text(self.tr("Select step value:"))
        self._padding_spinbox.set_label_text(self.tr("Select minimal amount of digits in number:"))
        self._sort_source_combobox.set_label_text(self.tr("Select sorting source:"))

    def create_event_handlers(self):
        self._start_number_spinbox.valueIsChanged.connect(self.handle_start_number_changed)
        self._step_value_spinbox.valueIsChanged.connect(self.handle_step_number_changed)
        self._padding_spinbox.valueIsChanged.connect(self.handle_padding_number_changed)
        self._sort_source_combobox.valueIsChanged.connect(self.handle_sort_source_changed)

    def request_command(self) -> PrepareCommand:
        return SequencePrepareCommand(
            start_number=self._start_number_value,
            step_value=self._step_value_value,
            padding=self._padding_value,
            sort_source=self._sort_source_value
        )

    @Slot()
    def handle_start_number_changed(self, value: int):
        print(f"handle_start_number_changed: {value}")
        self._start_number_value = value

    @Slot()
    def handle_step_number_changed(self, value: int):
        print(f"handle_step_number_changed: {value}")
        self._step_value_value = value

    @Slot()
    def handle_padding_number_changed(self, value: int):
        print(f"handle_padding_number_changed: {value}")
        self._padding_value = value

    @Slot()
    def handle_sort_source_changed(self, value: SortSource):
        print(f"handle_sort_source_changed: {value}")
        self._sort_source_value = value
