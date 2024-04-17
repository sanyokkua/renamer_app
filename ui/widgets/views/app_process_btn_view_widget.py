from PySide6.QtCore import Signal, Qt, Slot
from PySide6.QtGui import QCursor
from PySide6.QtWidgets import QVBoxLayout, QPushButton

from ui.widgets.base_abstract_widgets import BaseAbstractWidget


class AppProcessButtonsViewWidget(BaseAbstractWidget):
    _main_layout: QVBoxLayout
    _preview_btn: QPushButton
    _rename_btn: QPushButton
    _reset_btn: QPushButton

    previewBtnClicked = Signal()
    renameBtnClicked = Signal()
    resetBtnClicked = Signal()

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._main_layout = QVBoxLayout(self)
        self._preview_btn = QPushButton(self)
        self._rename_btn = QPushButton(self)
        self._reset_btn = QPushButton(self)

    def configure_widgets(self):
        self._preview_btn.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self._rename_btn.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))
        self._reset_btn.setCursor(QCursor(Qt.CursorShape.PointingHandCursor))

        self._main_layout.addStretch(1)
        self._main_layout.addWidget(self._preview_btn)
        self._main_layout.addWidget(self._rename_btn)
        self._main_layout.addWidget(self._reset_btn)
        self._main_layout.addStretch(4)

        self.setFixedWidth(90)
        self.setLayout(self._main_layout)

    def add_text_to_widgets(self):
        self._preview_btn.setText("Preview")
        self._rename_btn.setText("Rename")
        self._reset_btn.setText("Reset")

    def create_event_handlers(self):
        self._preview_btn.clicked.connect(self.handle_preview_btn_clicked)
        self._rename_btn.clicked.connect(self.handle_rename_btn_clicked)
        self._reset_btn.clicked.connect(self.handle_reset_btn_clicked)

    @Slot()
    def handle_preview_btn_clicked(self) -> None:
        self.previewBtnClicked.emit()

    @Slot()
    def handle_rename_btn_clicked(self) -> None:
        self.renameBtnClicked.emit()

    @Slot()
    def handle_reset_btn_clicked(self) -> None:
        self.resetBtnClicked.emit()
