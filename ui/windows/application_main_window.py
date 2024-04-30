from PySide6.QtCore import Qt, Slot
from PySide6.QtWidgets import (
    QHBoxLayout,
    QVBoxLayout,
    QMainWindow,
    QWidget,
    QProgressBar,
    QApplication,
)

from core.commands.fix_same_names import FixSameNamesCommand
from core.commands.map_url_to_app_file import MapUrlToAppFileCommand
from core.commands.renaming_commands import (
    FilterOutNotChangedFilesCommand,
    RenameFilesCommand,
    FilterOutFilesWithErrorsCommand,
)
from core.commons import PrepareCommand
from core.enums import AppModes
from core.models.app_file import AppFile
from ui.widgets.views.app_controls_widget import AppControlsWidget
from ui.widgets.views.app_files_list_view_widget import AppFilesListViewWidget
from ui.widgets.views.app_mode_view_widget import AppModeSelectViewWidget

FILTER_ONLY_CHANGED_COMMAND = FilterOutNotChangedFilesCommand()
FILTER_ONLY_VALID_COMMAND = FilterOutFilesWithErrorsCommand()
FIX_SAME_NAMES_COMMAND = FixSameNamesCommand()
RENAME_COMMAND = RenameFilesCommand()


class ApplicationMainWindow(QMainWindow):
    _main_widget_layout: QVBoxLayout
    _main_widget: QWidget

    _content_widget: QWidget
    _content_widget_layout: QHBoxLayout

    _app_controls_widget: AppControlsWidget
    _app_modes_widget: AppModeSelectViewWidget
    _files_view_widget: AppFilesListViewWidget
    _progress_bar: QProgressBar

    _mapping_command: MapUrlToAppFileCommand
    _app_file_list: list[AppFile] = []

    def __init__(self) -> None:
        super().__init__()
        self._mapping_command = MapUrlToAppFileCommand()

        # Create main widget of application where rest of the widgets will be displayed
        self._main_widget = QWidget(self)
        self._main_widget_layout = QVBoxLayout(self._main_widget)
        self._main_widget_layout.setContentsMargins(0, 0, 0, 0)

        # Create Main control widgets
        self._app_controls_widget = AppControlsWidget(self._main_widget)
        self._progress_bar = QProgressBar(self._main_widget)
        self._content_widget = QWidget(self._main_widget)
        self._content_widget_layout = QHBoxLayout(self._content_widget)
        self._app_modes_widget = AppModeSelectViewWidget(self._content_widget)
        self._files_view_widget = AppFilesListViewWidget(self._content_widget)

        # Configure main application widget
        self.setCentralWidget(self._main_widget)
        self.setLayout(self._main_widget_layout)
        self._main_widget.setEnabled(True)
        self._main_widget.setLayoutDirection(Qt.LayoutDirection.LeftToRight)
        self._main_widget.setMinimumWidth(1000)
        self._main_widget.setMinimumHeight(600)
        self._main_widget.setContentsMargins(0, 0, 0, 0)
        self._progress_bar.setValue(0)

        # Add Control widgets to the main layout
        self._content_widget_layout.addWidget(self._app_modes_widget)
        self._content_widget_layout.addWidget(self._files_view_widget)

        self._main_widget_layout.addWidget(self._app_controls_widget)
        self._main_widget_layout.addWidget(self._content_widget)
        self._main_widget_layout.addWidget(self._progress_bar)

        # Configure event handling for the widgets events
        self._app_controls_widget.appModeSelected.connect(self.handle_app_mode_changed)
        self._app_controls_widget.previewBtnClicked.connect(
            self.handle_preview_btn_clicked
        )
        self._app_controls_widget.renameBtnClicked.connect(
            self.handle_rename_btn_clicked
        )
        self._app_controls_widget.clearBtnClicked.connect(self.handle_clear_btn_clicked)
        self._files_view_widget.files_list_updated.connect(self.handle_files_dropped)

    @Slot()
    def handle_app_mode_changed(self, value: AppModes):
        self._app_modes_widget.handle_app_mode_changed(value)
        self.reset_names()

    @Slot()
    def handle_preview_btn_clicked(self):
        self.run_preparation_commands_for_files()

    @Slot()
    def handle_rename_btn_clicked(self):
        self.run_preparation_commands_for_files()
        # TODO: After renaming new file names should be displayed. In other words list of files should be re-opened
        RENAME_COMMAND.execute(self._app_file_list, self.update_progress_bar)

    def run_preparation_commands_for_files(self):
        self.reset_names()
        command: PrepareCommand = self._app_modes_widget.request_command()
        result = command.execute(self._app_file_list, self.update_progress_bar)
        result = FILTER_ONLY_VALID_COMMAND.execute(result, self.update_progress_bar)
        result = FILTER_ONLY_CHANGED_COMMAND.execute(result, self.update_progress_bar)
        result = FIX_SAME_NAMES_COMMAND.execute(result, self.update_progress_bar)
        self._app_file_list = result
        self.update_files_table_view()

    @Slot()
    def handle_clear_btn_clicked(self):
        self._app_file_list = []
        self.update_files_table_view()

    @Slot()
    def handle_files_dropped(self, list_of_files: list[str]):
        mapped_files = self._mapping_command.execute(
            list_of_files, self.update_progress_bar
        )
        self._app_file_list = mapped_files
        self.update_files_table_view()

    @Slot()
    def update_progress_bar(self, min_val: int, max_val: int, current_val: int):
        self._progress_bar.setMinimum(min_val)
        self._progress_bar.setMaximum(max_val)
        self._progress_bar.setValue(current_val)
        self._progress_bar.setFormat(f"Progress: {current_val}%")
        self._progress_bar.update()
        QApplication.processEvents()  # Process events to update GUI
        QApplication.instance().processEvents()  # Process events to update GUI

    def update_files_table_view(self):
        self._files_view_widget.update_table_data(self._app_file_list)

    def reset_names(self):
        for item in self._app_file_list:
            item.next_name = item.file_name
        self.update_files_table_view()
