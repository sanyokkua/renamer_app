from PySide6.QtCore import Slot, Signal, Qt
from PySide6.QtWidgets import (QWidget, QCheckBox, QVBoxLayout)


class LabelCheckboxWidget(QWidget):
    _main_layout: QVBoxLayout
    _checkbox: QCheckBox

    valueIsChanged = Signal(bool)

    def __init__(self, parent=None, label: str = ""):
        super().__init__(parent)
        self._main_layout = QVBoxLayout(self)
        self._checkbox = QCheckBox(self)

        self._checkbox.setText(self.tr(label))
        self.setLayout(self._main_layout)

        self._main_layout.addWidget(self._checkbox)

        self._checkbox.checkStateChanged.connect(self.handle_checked_changed)

    @Slot()
    def get_current_value(self) -> bool:
        return self._checkbox.isChecked()

    @Slot()
    def set_label_text(self, text: str):
        self._checkbox.setText(self.tr(text))

    @Slot()
    def set_value(self, checked: bool):
        self._checkbox.setChecked(checked)

    @Slot()
    def handle_checked_changed(self, state: Qt.CheckState):
        print(f"handle_checked_changed, {state}")
        value = True if state == Qt.CheckState.Checked else False
        self.valueIsChanged.emit(value)
