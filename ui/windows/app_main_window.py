"""
This module contains the ApplicationMainWindow class, which represents the main window of the application.

It also includes a decorator function handle_exceptions for handling exceptions in slot functions.

Classes:
    - ApplicationMainWindow: Represents the main window of the application.

Functions:
    - handle_exceptions: Decorator function for handling exceptions in slot functions.
"""

import logging

from PySide6.QtCore import Qt, Slot
from PySide6.QtWidgets import (
    QApplication,
    QHBoxLayout,
    QMainWindow,
    QMessageBox,
    QProgressBar,
    QVBoxLayout,
    QWidget,
)

from core.commands.fix_same_names import FixSameNamesCommand
from core.commands.map_url_to_app_file import MapUrlToAppFileCommand
from core.commands.renaming_commands import RenameFilesCommand
from core.enums import AppModes
from core.models.app_file import AppFile
from core.text_values import APP_MODE_TEXT
from ui.widgets.customs.form_widgets import ComboboxForm
from ui.widgets.customs.single_widgets import build_combobox_items
from ui.widgets.main import ControlsWidget
from ui.widgets.main.file_info_widget import FileInfoWidget
from ui.widgets.views.app_files_list_view_widget import AppFilesListViewWidget
from ui.widgets.views.app_mode_view_widget import AppModeSelectViewWidget

log: logging.Logger = logging.getLogger(__name__)


def handle_exceptions(func):
    """
    Decorator function for handling exceptions in slot functions.

    Args:
        func (function): Slot function to decorate.

    Returns:
        function: Decorated function.
    """
    import sys

    def wrapper(*args, **kwargs):
        try:
            return func(*args, **kwargs)
        except Exception as e:
            log.warning(f"An exception occurred in {func.__name__}: {e}")
            log.warning((sys.exc_info()))
            msg_box = QMessageBox()
            msg_box.setWindowTitle("Error happened")
            msg_box.setText("Error occurred, check the logs (if possible) to figure out the reason")
            msg_box.setStandardButtons(QMessageBox.StandardButton.Ok)
            msg_box.exec_()

            return None  # Or return a default value if applicable

    return wrapper


MODE_CMB_ITEMS = build_combobox_items(APP_MODE_TEXT)
MAP_FILE_NAME_TO_APP_FILE_COMMAND = MapUrlToAppFileCommand()
FIX_SAME_NAMES_COMMAND = FixSameNamesCommand()
RENAME_COMMAND = RenameFilesCommand()


class ApplicationMainWindow(QMainWindow):
    """
    Main window of the application.

    Attributes:
        _main_widget (QWidget): Main widget containing all other widgets.
        _main_widget_layout (QVBoxLayout): Layout for the main widget.
        _mode_selector (ComboboxForm): Dropdown for selecting renaming mode.
        _mode_view (AppModeSelectViewWidget): Widget for displaying selected renaming mode.
        _mode_controls (ControlsWidget): Widget containing control buttons.
        _mode_container (QWidget): Container for mode selector, mode view, and mode controls.
        _mode_container_layout (QVBoxLayout): Layout for the mode container.
        _files_table (AppFilesListViewWidget): Widget for displaying app files in a list view.
        _files_info (FileInfoWidget): Widget for displaying information about selected app file.
        _files_container (QWidget): Container for files table and file info widgets.
        _files_container_layout (QVBoxLayout): Layout for the files container.
        _mode_files_container (QWidget): Container for mode and files containers.
        _mode_files_container_layout (QHBoxLayout): Layout for the mode files container.
        _progress_bar (QProgressBar): Progress bar widget.
        _app_file_list (list[AppFile]): List of app files.

    Methods:
        init_widgets(): Initialize all widgets.
        config_widgets(): Configure the layout and styling of widgets.
        create_event_handlers(): Create event handlers for widget signals.
        handle_mode_changed(value: AppModes): Slot for handling mode change.
        handle_mode_configs_changed(): Slot for handling mode configuration change.
        handle_btn_click_preview(): Slot for handling preview button click.
        handle_btn_click_rename(): Slot for handling rename button click.
        handle_btn_click_clear(): Slot for handling clear button click.
        handle_files_added(list_of_files: list[str]): Slot for handling addition of files.
        handle_file_selected(file: AppFile): Slot for handling file selection.
        update_progress_bar(current_val: int, max_val: int): Update progress bar.
        run_preparation_commands_for_files(): Run preparation commands for app files.
        reset_names(): Reset file names.
        update_files_table_view(): Update files table view.
    """

    # App Main Window
    # _______________________________________________________
    # | Mode Selector   |                                   |
    # |_________________|   Files                           |
    # | Mode            |   Table                           |
    # | Widget          |                                   |
    # | Content         |                                   |
    # | Displayed       |                                   |
    # | Here            |___________________________________|
    # |_________________|                                   |
    # | Control Buttons |   Selected File Information       |
    # |_________________|___________________________________|
    # |                 progress bar                        |
    # |_____________________________________________________|

    _main_widget: QWidget
    _main_widget_layout: QVBoxLayout

    _mode_selector: ComboboxForm
    _mode_view: AppModeSelectViewWidget
    _mode_controls: ControlsWidget
    _mode_container: QWidget
    _mode_container_layout: QVBoxLayout

    _files_table: AppFilesListViewWidget
    _files_info: FileInfoWidget
    _files_container: QWidget
    _files_container_layout: QVBoxLayout

    _mode_files_container: QWidget
    _mode_files_container_layout: QHBoxLayout

    _progress_bar: QProgressBar

    _app_file_list: list[AppFile] = []

    def __init__(self) -> None:
        """
        Initialize the ApplicationMainWindow.
        """
        super().__init__()
        self.init_widgets()
        self.config_widgets()
        self.create_event_handlers()

    def init_widgets(self):
        self._main_widget = QWidget(self)
        self._main_widget_layout = QVBoxLayout(self._main_widget)

        self._mode_files_container = QWidget(self._main_widget)
        self._mode_files_container_layout = QHBoxLayout(self._mode_files_container)

        self._mode_container = QWidget(self._mode_files_container)
        self._mode_container_layout = QVBoxLayout(self._mode_container)

        self._mode_selector = ComboboxForm(self._mode_container)
        self._mode_view = AppModeSelectViewWidget(self._mode_container)
        self._mode_controls = ControlsWidget(self._mode_container)

        self._files_container = QWidget(self._mode_files_container)
        self._files_container_layout = QVBoxLayout(self._files_container)

        self._files_table = AppFilesListViewWidget(self._files_container)
        self._files_info = FileInfoWidget(self._files_container)

        self._progress_bar = QProgressBar(self._main_widget)

    def config_widgets(self):
        self.setCentralWidget(self._main_widget)
        self.setLayout(self._main_widget_layout)
        self.setContentsMargins(0, 0, 0, 0)

        self._main_widget.setEnabled(True)
        self._main_widget.setLayoutDirection(Qt.LayoutDirection.LeftToRight)
        self._main_widget.setMinimumWidth(1000)
        self._main_widget.setMinimumHeight(600)
        self._main_widget.setContentsMargins(0, 0, 0, 0)
        self._main_widget_layout.setContentsMargins(0, 0, 0, 0)

        self._progress_bar.setMinimum(0)
        self._progress_bar.hide()

        self._mode_files_container.setContentsMargins(0, 0, 0, 0)
        self._mode_files_container_layout.setContentsMargins(0, 0, 0, 0)

        self._mode_container.setContentsMargins(0, 0, 0, 0)
        self._mode_container_layout.setContentsMargins(0, 0, 0, 0)

        self._files_container.setContentsMargins(0, 0, 0, 0)
        self._files_container_layout.setContentsMargins(0, 0, 0, 0)

        self._main_widget_layout.addWidget(self._mode_files_container)
        self._main_widget_layout.addWidget(self._progress_bar)

        self._mode_files_container_layout.addWidget(self._mode_container)
        self._mode_files_container_layout.addWidget(self._files_container)

        self._mode_container_layout.addWidget(self._mode_selector)
        self._mode_container_layout.addWidget(self._mode_view)
        self._mode_container_layout.addWidget(self._mode_controls)

        self._files_container_layout.addWidget(self._files_table)
        self._files_container_layout.addWidget(self._files_info)

        self._mode_selector.set_widget_items(MODE_CMB_ITEMS)
        self._mode_selector.set_widget_label("Renaming mode:")

        self._main_widget_layout.setStretchFactor(self._mode_files_container, 9)
        self._main_widget_layout.setStretchFactor(self._progress_bar, 1)

        self._mode_files_container_layout.setStretchFactor(self._mode_container, 3)
        self._mode_files_container_layout.setStretchFactor(self._files_container, 7)

        self._mode_container_layout.setStretchFactor(self._mode_selector, 1)
        self._mode_container_layout.setStretchFactor(self._mode_view, 8)
        self._mode_container_layout.setStretchFactor(self._mode_controls, 1)

        self._files_container_layout.setStretchFactor(self._files_table, 7)
        self._files_container_layout.setStretchFactor(self._files_info, 3)

        self.setWindowTitle(self.tr("Renamer App. Rename your files"))

    def create_event_handlers(self):
        self._mode_selector.valueIsChanged.connect(self.handle_mode_changed)
        self._mode_view.widgetValuesAreChanged.connect(self.handle_mode_configs_changed)
        self._mode_controls.previewBtnClicked.connect(self.handle_btn_click_preview)
        self._mode_controls.renameBtnClicked.connect(self.handle_btn_click_rename)
        self._mode_controls.clearBtnClicked.connect(self.handle_btn_click_clear)
        self._files_table.files_list_updated.connect(self.handle_files_added)
        self._files_table.file_selected.connect(self.handle_file_selected)

    @Slot()
    def handle_mode_changed(self, value: AppModes):
        self._mode_view.handle_app_mode_changed(value)
        self.reset_names()

    @Slot()
    def handle_mode_configs_changed(self):
        if self._mode_controls.is_auto_preview_enabled():
            self.run_preparation_commands_for_files()

    @Slot()
    def handle_btn_click_preview(self):
        self.run_preparation_commands_for_files()

    @handle_exceptions
    @Slot()
    def handle_btn_click_rename(self):
        self.run_preparation_commands_for_files()
        result = RENAME_COMMAND.execute(self._app_file_list, self.update_progress_bar)
        self._app_file_list = result
        self.update_files_table_view()

    @Slot()
    def handle_btn_click_clear(self):
        self._app_file_list = []
        self.update_files_table_view()

    @handle_exceptions
    @Slot()
    def handle_files_added(self, list_of_files: list[str]):
        mapped_files = MAP_FILE_NAME_TO_APP_FILE_COMMAND.execute(list_of_files, self.update_progress_bar)
        self._app_file_list = mapped_files
        self.update_files_table_view()

    @Slot()
    def handle_file_selected(self, file: AppFile):
        self._files_info.set_file(file)

    @Slot()
    def update_progress_bar(self, current_val: int, max_val: int):
        if current_val == 0:
            self._progress_bar.hide()
        else:
            self._progress_bar.show()
            self._progress_bar.setValue(current_val)
            self._progress_bar.setMaximum(max_val)
            self._progress_bar.setFormat(f"Progress: {current_val}%")
            self._progress_bar.update()

        QApplication.processEvents()
        QApplication.instance().processEvents()

    @handle_exceptions
    def run_preparation_commands_for_files(self):
        self.reset_names()
        command = self._mode_view.request_command()
        result = command.execute(self._app_file_list, self.update_progress_bar)
        result = FIX_SAME_NAMES_COMMAND.execute(result, self.update_progress_bar)
        self._app_file_list = result
        self.update_files_table_view()

    def reset_names(self):
        for item in self._app_file_list:
            item.reset()
        self.update_files_table_view()

    def update_files_table_view(self):
        self._files_table.update_table_data(self._app_file_list)
        if len(self._app_file_list) > 0:
            self._mode_controls.set_rename_btn_enabled(True)
        else:
            self._mode_controls.set_rename_btn_enabled(False)
