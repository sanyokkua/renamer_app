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

    def add_text_to_widgets(self):
        self._start_number_spinbox.set_label_text(self.tr("Select start number:"))
        self._step_value_spinbox.set_label_text(self.tr("Select step value:"))
        self._padding_spinbox.set_label_text(self.tr("Select minimal amount of digits in number:"))
        self._sort_source_combobox.set_label_text(self.tr("Select sorting source:"))

    def create_event_handlers(self):
        pass

    def request_command(self) -> PrepareCommand:
        return SequencePrepareCommand(
            start_number=0,
            step_value=1,
            padding=0,
            sort_source=SortSource.FILE_NAME
        )
