from PySide6.QtCore import (Signal)
from PySide6.QtWidgets import (QPushButton,
                               QVBoxLayout)

from ui.widgets.base_abstract_widgets import BaseAbstractWidget


class FilePreviewWidget(BaseAbstractWidget):
    _main_layout: QVBoxLayout
    _preview_btn: QPushButton
    _rename_btn: QPushButton
    _reset_btn: QPushButton

    previewBtnClicked = Signal()
    renameBtnClicked = Signal()
    resetBtnClicked = Signal()

    def __init__(self):
        super().__init__()
