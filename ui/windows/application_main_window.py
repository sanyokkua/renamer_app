from PySide6.QtCore import (Qt, Slot)
from PySide6.QtWidgets import (QHBoxLayout,
                               QMainWindow, QWidget)

from core.commands.map_url_to_app_file import MapUrlToAppFileCommand
from core.commons import PrepareCommand
from core.models.app_file import AppFile
from ui.widgets.views.app_files_list_view_widget import AppFilesListViewWidget
from ui.widgets.views.app_mode_view_widget import AppModeSelectViewWidget
from ui.widgets.views.app_process_btn_view_widget import AppProcessButtonsViewWidget


class ApplicationMainWindow(QMainWindow):
    _main_widget_layout: QHBoxLayout
    _main_widget: QWidget

    _app_modes_widget: AppModeSelectViewWidget
    _controls_widget: AppProcessButtonsViewWidget
    _files_view_widget: AppFilesListViewWidget

    _mapping_command: MapUrlToAppFileCommand
    _app_file_list: list[AppFile] = []

    def __init__(self) -> None:
        super().__init__()
        self._mapping_command = MapUrlToAppFileCommand()

        # Create main widget of application where rest of the widgets will be displayed
        self._main_widget = QWidget(self)
        self._main_widget_layout = QHBoxLayout(self._main_widget)

        # Create Main control widgets
        self._app_modes_widget = AppModeSelectViewWidget(self._main_widget)
        self._controls_widget = AppProcessButtonsViewWidget(self._main_widget)
        self._files_view_widget = AppFilesListViewWidget(self._main_widget)

        # Configure main application widget
        self.setCentralWidget(self._main_widget)
        self.setLayout(self._main_widget_layout)
        self._main_widget.setEnabled(True)
        self._main_widget.setLayoutDirection(Qt.LayoutDirection.LeftToRight)
        self._main_widget.setMinimumWidth(1000)
        self._main_widget.setMinimumHeight(600)

        # Add Control widgets to the main layout
        self._main_widget_layout.addWidget(self._app_modes_widget)
        self._main_widget_layout.addWidget(self._controls_widget)
        self._main_widget_layout.addWidget(self._files_view_widget)

        # Configure event handling for the widgets events
        self._controls_widget.previewBtnClicked.connect(self.handle_preview_btn_clicked)
        self._controls_widget.renameBtnClicked.connect(self.handle_rename_btn_clicked)
        self._controls_widget.resetBtnClicked.connect(self.handle_reset_btn_clicked)
        self._files_view_widget.files_list_updated.connect(self.handle_files_dropped)

    @Slot()
    def handle_preview_btn_clicked(self):
        command: PrepareCommand = self._app_modes_widget.request_command()
        mapped_files = command.execute(self._app_file_list, self._files_view_widget.update_progress_bar)
        self._app_file_list = mapped_files

    @Slot()
    def handle_rename_btn_clicked(self):
        command: PrepareCommand = self._app_modes_widget.request_command()
        mapped_files = command.execute(self._app_file_list, self._files_view_widget.update_progress_bar)
        self._app_file_list = mapped_files

    @Slot()
    def handle_reset_btn_clicked(self):
        command: PrepareCommand = self._app_modes_widget.request_command()
        mapped_files = command.execute(self._app_file_list, self._files_view_widget.update_progress_bar)
        self._app_file_list = mapped_files

    @Slot()
    def handle_files_dropped(self, list_of_files: list[str]):
        mapped_files = self._mapping_command.execute(list_of_files, self._files_view_widget.update_progress_bar)
        self._app_file_list = mapped_files
