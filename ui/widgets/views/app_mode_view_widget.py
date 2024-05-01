from PySide6.QtCore import Slot
from PySide6.QtWidgets import QVBoxLayout, QGroupBox

from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand
from core.enums import AppModes
from core.exceptions import WidgetNotFoundException
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.modes.add_text_widget import AddTextWidget
from ui.widgets.modes.change_case_widget import ChangeCaseWidget
from ui.widgets.modes.change_extension_widget import ChangeExtensionWidget
from ui.widgets.modes.date_time_widget import DateTimeWidget
from ui.widgets.modes.image_dimensions_widget import ImageDimensionsWidget
from ui.widgets.modes.parent_folders_widget import ParentFoldersWidget
from ui.widgets.modes.remove_text_widget import RemoveTextWidget
from ui.widgets.modes.replace_text_widget import ReplaceTextWidget
from ui.widgets.modes.seq_gen_widget import SequenceGeneratorWidget
from ui.widgets.modes.truncate_text_widget import TruncateTextWidget


class AppModeSelectViewWidget(BasePrepareCommandWidget):
    _main_layout: QVBoxLayout
    _app_modes_view_widget: QGroupBox
    _app_modes_view_widget_layout: QVBoxLayout

    _current_mode_widget: BasePrepareCommandWidget

    _mode_add_text_widget: AddTextWidget
    _mode_change_case_widget: ChangeCaseWidget
    _mode_date_time_widget: DateTimeWidget
    _mode_image_dimensions_widget: ImageDimensionsWidget
    _mode_parent_folders_widget: ParentFoldersWidget
    _mode_remove_text_widget: RemoveTextWidget
    _mode_replace_text_widget: ReplaceTextWidget
    _mode_sequence_generator_widget: SequenceGeneratorWidget
    _mode_truncate_text_widget: TruncateTextWidget
    _mode_change_extension_widget: ChangeExtensionWidget

    _mode_widget_dict: dict[AppModes, BasePrepareCommandWidget]

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._main_layout = QVBoxLayout(self)

        self._app_modes_view_widget = QGroupBox(self)
        self._app_modes_view_widget_layout = QVBoxLayout(self._app_modes_view_widget)

        self._mode_add_text_widget = AddTextWidget(self._app_modes_view_widget)
        self._mode_change_case_widget = ChangeCaseWidget(self._app_modes_view_widget)
        self._mode_date_time_widget = DateTimeWidget(self._app_modes_view_widget)
        self._mode_image_dimensions_widget = ImageDimensionsWidget(self._app_modes_view_widget)
        self._mode_parent_folders_widget = ParentFoldersWidget(self._app_modes_view_widget)
        self._mode_remove_text_widget = RemoveTextWidget(self._app_modes_view_widget)
        self._mode_replace_text_widget = ReplaceTextWidget(self._app_modes_view_widget)
        self._mode_sequence_generator_widget = SequenceGeneratorWidget(self._app_modes_view_widget)
        self._mode_truncate_text_widget = TruncateTextWidget(self._app_modes_view_widget)
        self._mode_change_extension_widget = ChangeExtensionWidget(self._app_modes_view_widget)

        self._mode_widget_dict = {
            AppModes.ADD_CUSTOM_TEXT: self._mode_add_text_widget,
            AppModes.CHANGE_CASE: self._mode_change_case_widget,
            AppModes.USE_DATETIME: self._mode_date_time_widget,
            AppModes.USE_IMAGE_DIMENSIONS: self._mode_image_dimensions_widget,
            AppModes.USE_PARENT_FOLDER_NAME: self._mode_parent_folders_widget,
            AppModes.REMOVE_CUSTOM_TEXT: self._mode_remove_text_widget,
            AppModes.REPLACE_CUSTOM_TEXT: self._mode_replace_text_widget,
            AppModes.ADD_SEQUENCE: self._mode_sequence_generator_widget,
            AppModes.TRUNCATE_FILE_NAME: self._mode_truncate_text_widget,
            AppModes.CHANGE_EXTENSION: self._mode_change_extension_widget,
        }

    def configure_widgets(self):
        self._mode_add_text_widget.hide()
        self._mode_change_case_widget.hide()
        self._mode_date_time_widget.hide()
        self._mode_image_dimensions_widget.hide()
        self._mode_parent_folders_widget.hide()
        self._mode_remove_text_widget.hide()
        self._mode_replace_text_widget.hide()
        self._mode_sequence_generator_widget.hide()
        self._mode_truncate_text_widget.hide()
        self._current_mode_widget = self._mode_add_text_widget

        self.setLayout(self._main_layout)
        self.setMinimumWidth(450)
        self.setMaximumWidth(450)

        self.setContentsMargins(0, 0, 0, 0)
        self._main_layout.addWidget(self._app_modes_view_widget)

        self._app_modes_view_widget.setLayout(self._app_modes_view_widget_layout)
        self._app_modes_view_widget_layout.setContentsMargins(0, 0, 0, 0)
        self._app_modes_view_widget_layout.addWidget(self._current_mode_widget)
        self._app_modes_view_widget_layout.addStretch(1)

        self.handle_app_mode_changed(AppModes.ADD_CUSTOM_TEXT)

    def add_text_to_widgets(self):
        self._app_modes_view_widget.setTitle(self.tr("Configure renaming"))

    def create_event_handlers(self):
        pass

    def request_command(self) -> AppFileItemByItemListProcessingCommand:
        return self._current_mode_widget.request_command()

    @Slot()
    def handle_app_mode_changed(self, app_mode: AppModes):
        widget: BasePrepareCommandWidget = self._mode_widget_dict[app_mode]

        if widget is None:
            raise WidgetNotFoundException()

        self._current_mode_widget.hide()
        self._app_modes_view_widget_layout.replaceWidget(self._current_mode_widget, widget)
        self._current_mode_widget = widget
        self._current_mode_widget.show()
