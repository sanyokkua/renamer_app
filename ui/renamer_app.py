from PySide6.QtWidgets import QApplication

from ui.windows.application_main_window import ApplicationMainWindow


class RenamerApp(QApplication):
    def __init__(self) -> None:
        super().__init__([])
        self._main_widget: ApplicationMainWindow = ApplicationMainWindow()
        self._main_widget.show()


def start_app() -> None:
    app: RenamerApp = RenamerApp()
    app.exec()


if __name__ == "__main__":
    start_app()
