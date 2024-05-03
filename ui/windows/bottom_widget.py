from PySide6.QtWidgets import QWidget, QProgressBar, QVBoxLayout


class BottomWidget(QWidget):
    _layout: QVBoxLayout
    progress_bar: QProgressBar

    def __init__(self, parent=None):
        super().__init__(parent)
        self._layout = QVBoxLayout(self)
        self.setLayout(self._layout)

        self.progress_bar = QProgressBar(self)
        self._layout.addWidget(self.progress_bar)
        self.progress_bar.setMinimum(0)
        self.progress_bar.setValue(0)
        self.setMaximumHeight(50)
