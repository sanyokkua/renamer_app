from PySide6.QtCore import Qt, Slot
from PySide6.QtWidgets import QVBoxLayout, QMainWindow, QWidget, QSpacerItem, QSizePolicy, QProgressBar, QApplication

from core.commands.fix_same_names import FixSameNamesCommand
from core.commands.map_url_to_app_file import MapUrlToAppFileCommand
from core.commands.renaming_commands import RenameFilesCommand
from core.enums import AppModes
from core.models.app_file import AppFile
from ui.widgets.customs import ComboboxForm
from ui.widgets.main import ControlsWidget
from ui.widgets.views.app_files_list_view_widget import AppFilesListViewWidget
from ui.widgets.views.app_mode_view_widget import AppModeSelectViewWidget
from ui.windows.bottom_widget import BottomWidget
from ui.windows.top_widget import TopWidget

MAP_FILE_NAME_TO_APP_FILE_COMMAND = MapUrlToAppFileCommand()
FIX_SAME_NAMES_COMMAND = FixSameNamesCommand()
RENAME_COMMAND = RenameFilesCommand()


class ApplicationMainWindow(QMainWindow):
    _main_widget: QWidget
    _main_widget_layout: QVBoxLayout

    _top_widget: TopWidget
    _bottom_widget: BottomWidget

    _link_mode_selector: ComboboxForm
    _link_mode_view: AppModeSelectViewWidget
    _link_controls: ControlsWidget
    _link_files_table: AppFilesListViewWidget
    _link_file_preview: QWidget
    _link_progress_bar: QProgressBar

    _app_file_list: list[AppFile] = []

    def __init__(self) -> None:
        super().__init__()

        self._main_widget = QWidget(self)
        self._main_widget_layout = QVBoxLayout(self._main_widget)

        self.setCentralWidget(self._main_widget)
        self.setLayout(self._main_widget_layout)

        self._top_widget = TopWidget(self._main_widget)
        self._bottom_widget = BottomWidget(self._main_widget)

        self._main_widget.setEnabled(True)
        self._main_widget.setLayoutDirection(Qt.LayoutDirection.LeftToRight)
        self._main_widget.setMinimumWidth(1000)
        self._main_widget.setMinimumHeight(600)
        self._main_widget.setContentsMargins(0, 0, 0, 0)
        self._main_widget_layout.setContentsMargins(0, 0, 0, 0)
        self._main_widget_layout.addWidget(self._top_widget)
        self._main_widget_layout.addItem(QSpacerItem(20, 40, QSizePolicy.Policy.Minimum, QSizePolicy.Policy.Expanding))
        self._main_widget_layout.addWidget(self._bottom_widget)

        self.create_widget_links()
        self.create_event_handlers()

    def create_widget_links(self):
        self._link_mode_selector = self._top_widget.left_widget.mode_selector
        self._link_mode_view = self._top_widget.left_widget.mode_view
        self._link_controls = self._top_widget.left_widget.controls
        self._link_files_table = self._top_widget.right_widget.files_table
        self._link_file_preview = self._top_widget.right_widget.file_preview
        self._link_progress_bar = self._bottom_widget.progress_bar

    def create_event_handlers(self):
        self._link_mode_selector.valueIsChanged.connect(self.handle_mode_changed)
        self._link_mode_view.widgetValuesAreChanged.connect(self.handle_mode_configs_changed)
        self._link_controls.previewBtnClicked.connect(self.handle_btn_click_preview)
        self._link_controls.renameBtnClicked.connect(self.handle_btn_click_rename)
        self._link_controls.clearBtnClicked.connect(self.handle_btn_click_clear)
        self._link_files_table.files_list_updated.connect(self.handle_files_added)

    @Slot()
    def handle_mode_changed(self, value: AppModes):
        self._link_mode_view.handle_app_mode_changed(value)
        self.reset_names()

    @Slot()
    def handle_mode_configs_changed(self):
        self.run_preparation_commands_for_files()

    @Slot()
    def handle_btn_click_preview(self):
        self.run_preparation_commands_for_files()

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

    @Slot()
    def handle_files_added(self, list_of_files: list[str]):
        mapped_files = MAP_FILE_NAME_TO_APP_FILE_COMMAND.execute(list_of_files, self.update_progress_bar)
        self._app_file_list = mapped_files
        self.update_files_table_view()

    @Slot()
    def update_progress_bar(self, current_val: int, max_val: int):
        self._link_progress_bar.setMinimum(0)
        self._link_progress_bar.setMaximum(max_val)
        self._link_progress_bar.setValue(current_val)
        self._link_progress_bar.setFormat(f"Progress: {current_val}%")
        self._link_progress_bar.update()
        QApplication.processEvents()  # Process events to update GUI
        QApplication.instance().processEvents()  # Process events to update GUI

    def run_preparation_commands_for_files(self):
        self.reset_names()
        command = self._link_mode_view.request_command()
        result = command.execute(self._app_file_list, self.update_progress_bar)
        result = FIX_SAME_NAMES_COMMAND.execute(result, self.update_progress_bar)
        self._app_file_list = result
        self.update_files_table_view()

    def reset_names(self):
        for item in self._app_file_list:
            item.next_name = item.file_name
        self.update_files_table_view()

    def update_files_table_view(self):
        self._link_files_table.update_table_data(self._app_file_list)
