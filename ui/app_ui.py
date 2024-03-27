from PySide6.QtWidgets import QApplication
from PySide6 import QtWidgets


class QApplicationAppUI(QApplication):
    def __init__(self) -> None:
        super().__init__([])
        self.widget = QtWidgets.QWidget()
        self.widget.resize(800, 600)
        self.widget.show()


def start_app() -> None:
    app: QApplicationAppUI = QApplicationAppUI()
    app.exec()


if __name__ == "__main__":
    start_app()
