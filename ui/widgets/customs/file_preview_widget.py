from PySide6.QtWidgets import QVBoxLayout

from ui.widgets.base_abstract_widgets import BaseAbstractWidget


class FilePreviewWidget(BaseAbstractWidget):
    _main_layout: QVBoxLayout

    def __init__(self):
        super().__init__()
