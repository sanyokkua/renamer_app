from PySide6.QtCore import Signal, Slot
from PySide6.QtGui import Qt
from PySide6.QtWidgets import QGridLayout, QMessageBox

from ui.widgets.customs import BaseWidget, Button, CheckBox


class ControlsWidget(BaseWidget):
    _main_layout: QGridLayout
    _auto_preview: CheckBox
    _preview_btn: Button
    _rename_btn: Button
    _clear_btn: Button
    _confirmation_dialog: QMessageBox

    previewBtnClicked = Signal()
    renameBtnClicked = Signal()
    clearBtnClicked = Signal()

    def init_widgets(self):
        self._main_layout = QGridLayout(self)
        self._auto_preview = CheckBox(self)
        self._preview_btn = Button(self)
        self._rename_btn = Button(self)
        self._clear_btn = Button(self)
        self._confirmation_dialog = QMessageBox(self)

    def configure_widgets(self):
        self.setLayout(self._main_layout)

        self._main_layout.addWidget(self._auto_preview, 0, 0, Qt.AlignmentFlag.AlignLeft)
        self._main_layout.addWidget(self._preview_btn, 0, 1, Qt.AlignmentFlag.AlignLeft)
        self._main_layout.addWidget(self._rename_btn, 0, 2, Qt.AlignmentFlag.AlignLeft)
        self._main_layout.addWidget(self._clear_btn, 0, 3, Qt.AlignmentFlag.AlignLeft)

        self.setMaximumHeight(50)

        self._confirmation_dialog.setWindowTitle("Confirm Renaming")
        self._confirmation_dialog.setText("Are you sure you want to proceed?")
        self._confirmation_dialog.setIcon(QMessageBox.Icon.Question)
        self._confirmation_dialog.setStandardButtons(QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No)

        self._rename_btn.setDisabled(True)

    def add_text_to_widgets(self):
        self._auto_preview.set_widget_label("Auto Preview")
        self._preview_btn.set_widget_label("Preview Renaming")
        self._rename_btn.set_widget_label("Rename Files")
        self._clear_btn.set_widget_label("Clear Table")

    def create_event_handlers(self):
        self._auto_preview.valueIsChanged.connect(self.handle_auto_preview_changed)
        self._preview_btn.valueIsChanged.connect(self.handle_preview_btn_clicked)
        self._rename_btn.valueIsChanged.connect(self.handle_rename_btn_clicked)
        self._clear_btn.valueIsChanged.connect(self.handle_clear_btn_clicked)

    @Slot()
    def handle_auto_preview_changed(self, checked: bool) -> None:
        print(f"handle_auto_preview_changed: {checked}")
        if checked:
            self._preview_btn.setDisabled(True)
            self.handle_preview_btn_clicked()
        else:
            self._preview_btn.setDisabled(False)

    @Slot()
    def handle_preview_btn_clicked(self) -> None:
        print("handle_preview_btn_clicked")
        self.previewBtnClicked.emit()

    @Slot()
    def handle_rename_btn_clicked(self) -> None:
        print("handle_rename_btn_clicked")
        result = self._confirmation_dialog.exec()
        if result == QMessageBox.StandardButton.Yes:
            print("User clicked Yes.")
            # Add code here to proceed with the action
            self.renameBtnClicked.emit()
        else:
            print("User clicked No.")

    @Slot()
    def handle_clear_btn_clicked(self) -> None:
        print("handle_clear_btn_clicked")
        self.clearBtnClicked.emit()

    def is_auto_preview_enabled(self) -> bool:
        return self._auto_preview.get_widget_value()

    def set_rename_btn_enabled(self, state: bool):
        if state:
            self._rename_btn.setEnabled(True)
        else:
            self._rename_btn.setDisabled(True)
