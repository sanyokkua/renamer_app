from PySide6.QtCore import (Slot, Qt)
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import (QCheckBox, QFormLayout, QLabel,
                               QLineEdit, QSpinBox)

from core.command import RenameByPatternCommand, Command
from ui.widgets.base_params_widget import BaseParamsWidget


class RenameByPatternWidget(BaseParamsWidget):
    def __init__(self):
        super().__init__()
        self.main_layout = QFormLayout(self)
        self.main_layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self.main_layout.setRowWrapPolicy(QFormLayout.RowWrapPolicy.WrapLongRows)

        self.start_seq_from_label = QLabel(self)
        self.start_seq_from_spinbox = QSpinBox(self)
        self.start_seq_from_spinbox.setCursor(QCursor(Qt.PointingHandCursor))
        self.main_layout.setWidget(0, QFormLayout.LabelRole, self.start_seq_from_label)
        self.main_layout.setWidget(0, QFormLayout.FieldRole, self.start_seq_from_spinbox)

        self.use_number_seq_label = QLabel(self)
        self.use_number_seq_checkbox = QCheckBox(self)
        self.use_number_seq_checkbox.setCursor(QCursor(Qt.PointingHandCursor))
        self.main_layout.setWidget(1, QFormLayout.LabelRole, self.use_number_seq_label)
        self.main_layout.setWidget(1, QFormLayout.FieldRole, self.use_number_seq_checkbox)

        self.prefix_to_add_label = QLabel(self)
        self.prefix_to_add_line_edit = QLineEdit(self)
        self.main_layout.setWidget(2, QFormLayout.LabelRole, self.prefix_to_add_label)
        self.main_layout.setWidget(2, QFormLayout.FieldRole, self.prefix_to_add_line_edit)
        self.prefix_to_add_line_edit.setValidator(self.path_symbols_validator)

        self.suffix_to_add_label = QLabel(self)
        self.suffix_to_add_line_edit = QLineEdit(self)
        self.main_layout.setWidget(5, QFormLayout.LabelRole, self.suffix_to_add_label)
        self.main_layout.setWidget(5, QFormLayout.FieldRole, self.suffix_to_add_line_edit)
        self.suffix_to_add_line_edit.setValidator(self.path_symbols_validator)

        self.add_text_to_widgets()
        self.add_event_handlers()

    def add_text_to_widgets(self):
        self.setWindowTitle("Form")
        self.start_seq_from_label.setText("Start Sequence From")
        self.use_number_seq_label.setText("Use Number Sequence")
        self.use_number_seq_checkbox.setText("")
        self.prefix_to_add_label.setText("Prefix To Add")
        self.suffix_to_add_label.setText("Suffix To Add")

    def add_event_handlers(self):
        self.start_seq_from_spinbox.valueChanged.connect(self.handle_start_seq_number_changed)
        self.use_number_seq_checkbox.stateChanged.connect(self.handle_use_number_seq_checked)
        self.prefix_to_add_line_edit.textChanged.connect(self.handle_prefix_to_add_changed)
        self.suffix_to_add_line_edit.textChanged.connect(self.handle_suffix_to_add_changed)

    @Slot()
    def handle_start_seq_number_changed(self, number: int):
        print("handle_start_seq_number_changed, value={}".format(number))

    @Slot()
    def handle_use_number_seq_checked(self, state):
        print("handle_use_number_seq_checked, value={}".format(state))

    @Slot()
    def handle_prefix_to_add_changed(self, text: str):
        print("handle_prefix_to_add_changed, value={}".format(text))

    @Slot()
    def handle_suffix_to_add_changed(self, text: str):
        print("handle_suffix_to_add_changed, value={}".format(text))

    @Slot()
    def request_command(self) -> Command:
        start_seq_from_spinbox = self.start_seq_from_spinbox.value()
        use_number_seq_checkbox = self.use_number_seq_checkbox.isChecked()
        prefix_to_add_line_edit = self.prefix_to_add_line_edit.text()
        suffix_to_add_line_edit = self.suffix_to_add_line_edit.text()
        command = RenameByPatternCommand(
            sequence_start=start_seq_from_spinbox,
            use_seq=use_number_seq_checkbox,
            prefix_to_add=prefix_to_add_line_edit,
            suffix_to_add=suffix_to_add_line_edit
        )
        self.emit_command_built(command)
        return command
