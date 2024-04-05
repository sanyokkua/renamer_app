from PySide6.QtCore import (Qt, Slot)
from PySide6.QtGui import (QAction)
from PySide6.QtWidgets import (QComboBox, QFormLayout, QHBoxLayout,
                               QLabel, QMainWindow,
                               QMenu, QMenuBar, QProgressBar, QPushButton,
                               QStatusBar, QVBoxLayout,
                               QWidget)

from core.constants import RenamingModes
from core.models import FileItemModel
from ui.widgets.files_table_view import FileTableView
from ui.widgets.rename_by_pattern_widget import RenameByPatternWidget
from ui.widgets.rename_to_date_time_widget import RenameToDateTimeForm
from ui.widgets.replace_prefix_suffix_widget import ReplacePrefixAndOrSuffixWidget


class AppUiWindow(QMainWindow):
    files_list: list[FileItemModel] = []

    def __init__(self) -> None:
        super().__init__()
        # Main Widget

        self.main_widget = QWidget(self)
        self.main_widget.setEnabled(True)
        self.main_widget.setLayoutDirection(Qt.LeftToRight)
        self.main_widget_layout = QHBoxLayout(self.main_widget)

        # Content widgets in Main Widget

        self.left_widget = QWidget(self.main_widget)
        self.left_widget_layout = QVBoxLayout(self.left_widget)

        self.center_widget = QWidget(self.main_widget)
        self.center_widget_layout = QVBoxLayout(self.center_widget)

        self.right_widget = QWidget(self.main_widget)
        self.right_widget_layout = QVBoxLayout(self.right_widget)

        self.main_widget_layout.addWidget(self.left_widget)
        self.main_widget_layout.addWidget(self.center_widget)
        self.main_widget_layout.addWidget(self.right_widget)

        # Left content widgets

        self.left_widget_control_widget = QWidget(self.left_widget)
        self.left_widget_control_widget_layout = QFormLayout(self.left_widget_control_widget)
        self.select_mode_label = QLabel(self.left_widget_control_widget)
        self.select_mode_label_combo_box = QComboBox(self.left_widget_control_widget)
        self.left_widget_control_widget_layout.setWidget(0, QFormLayout.LabelRole, self.select_mode_label)
        self.left_widget_control_widget_layout.setWidget(0, QFormLayout.FieldRole, self.select_mode_label_combo_box)

        self.left_widget_content_widget = QWidget(self.left_widget)

        self.left_widget_layout.addWidget(self.left_widget_control_widget)
        self.left_widget_layout.addWidget(self.left_widget_content_widget)

        # Center content widgets

        self.preview_btn = QPushButton(self.center_widget)
        self.rename_btn = QPushButton(self.center_widget)
        self.reset_btn = QPushButton(self.center_widget)

        self.center_widget_layout.addWidget(self.preview_btn)
        self.center_widget_layout.addWidget(self.rename_btn)
        self.center_widget_layout.addWidget(self.reset_btn)

        # Right content widgets

        self.files_table_view = FileTableView(self.right_widget)
        self.files_table_view.setAutoFillBackground(False)
        self.files_table_view.setAcceptDrops(True)
        self.progress_bar = QProgressBar(self.center_widget)
        self.progress_bar.setValue(0)

        self.right_widget_layout.addWidget(self.files_table_view)
        self.right_widget_layout.addWidget(self.progress_bar)

        # Widgets with modes

        self.rename_by_pattern_widget = RenameByPatternWidget()
        self.rename_to_date_time_widget = RenameToDateTimeForm()
        self.replace_prefix_and_or_suffix_widget = ReplacePrefixAndOrSuffixWidget()
        self.left_widget_layout.replaceWidget(self.left_widget_content_widget, self.rename_by_pattern_widget)
        self.left_widget_content_widget = self.rename_by_pattern_widget

        # Other

        self.setCentralWidget(self.main_widget)

        self.menubar = QMenuBar(self)
        self.setMenuBar(self.menubar)

        self.app_menu = QMenu(self.menubar)
        self.action_exit = QAction(self)
        self.action_open = QAction(self)
        self.app_menu.addAction(self.action_open)
        self.app_menu.addAction(self.action_exit)

        self.menubar.addAction(self.app_menu.menuAction())

        self.statusbar = QStatusBar(self)
        self.setStatusBar(self.statusbar)

        self.add_text_to_widgets()
        self.add_event_handlers()

    def add_text_to_widgets(self):
        self.setWindowTitle("Renamer App - Rename your files")
        self.action_exit.setText("Exit")
        self.action_open.setText("Open Files")
        self.select_mode_label.setText("Select Mode")
        self.preview_btn.setText("Preview")
        self.rename_btn.setText("Rename")
        self.reset_btn.setText("Reset")
        self.app_menu.setTitle("App")

        cmb_val1 = RenamingModes.RENAME_BY_PATTERN.value
        cmb_val2 = RenamingModes.RENAME_TO_DATE_TIME.value
        cmb_val3 = RenamingModes.REPLACE_PREFIX_SUFFIX.value
        self.select_mode_label_combo_box.addItem(cmb_val1)
        self.select_mode_label_combo_box.addItem(cmb_val2)
        self.select_mode_label_combo_box.addItem(cmb_val3)

    def add_event_handlers(self):
        self.select_mode_label_combo_box.currentIndexChanged.connect(self.handle_select_mode_event)
        self.preview_btn.clicked.connect(self.handle_preview_btn_clicked)
        self.rename_btn.clicked.connect(self.handle_rename_btn_clicked)
        self.reset_btn.clicked.connect(self.handle_reset_btn_clicked)
        self.action_open.triggered.connect(self.handle_action_open)
        self.action_exit.triggered.connect(self.handle_action_exit)
        self.files_table_view.files_dropped.connect(self.handle_files_dropped)

    @Slot()
    def handle_select_mode_event(self, index: int) -> None:
        selected_widget = self.select_mode_label_combo_box.itemText(index)
        match selected_widget:
            case RenamingModes.RENAME_BY_PATTERN.value:
                widget = self.rename_by_pattern_widget
            case RenamingModes.RENAME_TO_DATE_TIME.value:
                widget = self.rename_to_date_time_widget
            case RenamingModes.REPLACE_PREFIX_SUFFIX.value:
                widget = self.replace_prefix_and_or_suffix_widget
            case _:
                widget = QLabel("ERROR. Widget is not found")
        self.left_widget_layout.replaceWidget(self.left_widget_content_widget, widget)
        self.left_widget_content_widget.hide()
        self.left_widget_content_widget = widget
        self.left_widget_content_widget.show()
        print("handle_select_mode_event. Selected widget index={}".format(index))

    @Slot()
    def handle_preview_btn_clicked(self) -> None:
        command = self.left_widget_content_widget.request_command()
        self.files_list = command.execute(self.files_list)
        self.handle_files_dropped(self.files_list)
        print("handle_preview_btn_clicked")

    @Slot()
    def handle_rename_btn_clicked(self) -> None:
        command = self.left_widget_content_widget.request_command()
        self.files_list = command.execute(self.files_list)
        self.handle_files_dropped(self.files_list)
        print("handle_rename_btn_clicked")

    @Slot()
    def handle_reset_btn_clicked(self) -> None:
        self.handle_files_dropped([])
        print("handle_reset_btn_clicked")

    @Slot()
    def handle_action_open(self) -> None:
        print("handle_action_open")

    @Slot()
    def handle_action_exit(self) -> None:
        print("handle_action_exit")

    @Slot()
    def handle_files_dropped(self, files_list) -> None:
        self.files_list = files_list
        self.files_table_view.update_table_data(files_list)
        print("handle_files_dropped")
