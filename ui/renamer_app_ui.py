import logging

from PySide6.QtWidgets import QApplication

from ui.windows.app_main_window import ApplicationMainWindow

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s %(funcName)s] %(message)s",
)
log: logging.Logger = logging.getLogger(__name__)


class RenamerApplicationUI(QApplication):
    def __init__(self) -> None:
        super().__init__([])
        self._main_widget: ApplicationMainWindow = ApplicationMainWindow()
        self._main_widget.show()


def start_app() -> None:
    app: RenamerApplicationUI = RenamerApplicationUI()
    app.exec()


if __name__ == "__main__":
    log.info("Renamer app will be started")
    start_app()
    log.info("Renamer app will be closed")
