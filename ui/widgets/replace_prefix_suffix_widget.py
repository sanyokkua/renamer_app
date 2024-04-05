from PySide6.QtCore import (Slot)
from PySide6.QtWidgets import (QFormLayout, QLabel,
                               QLineEdit)

from core.command import Command, RemovePrefixOrSuffixCommand
from ui.widgets.base_params_widget import BaseParamsWidget


class ReplacePrefixAndOrSuffixWidget(BaseParamsWidget):
    def __init__(self):
        super().__init__()
        self.main_layout = QFormLayout(self)

        self.prefix_to_remove_label = QLabel(self)
        self.prefix_to_remove_line_edit = QLineEdit(self)
        self.main_layout.setWidget(0, QFormLayout.LabelRole, self.prefix_to_remove_label)
        self.main_layout.setWidget(0, QFormLayout.FieldRole, self.prefix_to_remove_line_edit)

        self.suffix_to_remove = QLabel(self)
        self.suffix_to_remove_line_edit = QLineEdit(self)
        self.main_layout.setWidget(1, QFormLayout.LabelRole, self.suffix_to_remove)
        self.main_layout.setWidget(1, QFormLayout.FieldRole, self.suffix_to_remove_line_edit)

        self.add_text_to_widgets()
        self.add_event_handlers()

    def add_text_to_widgets(self):
        self.setWindowTitle("Form")
        self.prefix_to_remove_label.setText("Prefix To Remove")
        self.suffix_to_remove.setText("Suffix To Remove")

    def add_event_handlers(self):
        self.prefix_to_remove_line_edit.textChanged.connect(self.handle_prefix_to_remove_changed)
        self.suffix_to_remove_line_edit.textChanged.connect(self.handle_suffix_to_remove_changed)

    @Slot()
    def handle_prefix_to_remove_changed(self, text: str):
        print("handle_prefix_to_remove_changed, value={}".format(text))

    @Slot()
    def handle_suffix_to_remove_changed(self, text: str):
        print("handle_suffix_to_remove_changed, value={}".format(text))

    @Slot()
    def request_command(self) -> Command:
        prefix_to_remove_line_edit = self.prefix_to_remove_line_edit.text()
        suffix_to_remove_line_edit = self.suffix_to_remove_line_edit.text()

        command = RemovePrefixOrSuffixCommand(
            prefix_to_remove=prefix_to_remove_line_edit,
            suffix_to_remove=suffix_to_remove_line_edit
        )

        self.emit_command_built(command)
        return command
