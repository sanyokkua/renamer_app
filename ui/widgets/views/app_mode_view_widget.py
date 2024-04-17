from PySide6.QtCore import Slot
from PySide6.QtWidgets import QVBoxLayout

from core.commons import PrepareCommand
from core.enums import AppModes
from core.exceptions import WidgetNotFoundException
from core.text_values import APP_MODE_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_combobox_widget import LabelComboboxWidget
from ui.widgets.modes.add_text_widget import AddTextWidget
from ui.widgets.modes.change_case_widget import ChangeCaseWidget
from ui.widgets.modes.date_time_widget import DateTimeWidget
from ui.widgets.modes.image_dimensions_widget import ImageDimensionsWidget
from ui.widgets.modes.parent_folders_widget import ParentFoldersWidget
from ui.widgets.modes.remove_text_widget import RemoveTextWidget
from ui.widgets.modes.replace_text_widget import ReplaceTextWidget
from ui.widgets.modes.seq_gen_widget import SequenceGeneratorWidget
from ui.widgets.modes.truncate_text_widget import TruncateTextWidget


class AppModeSelectViewWidget(BasePrepareCommandWidget):
    _main_layout: QVBoxLayout
    _app_modes_combobox: LabelComboboxWidget
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

    _mode_widget_dict: dict[AppModes, BasePrepareCommandWidget]

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._main_layout = QVBoxLayout(self)
        self._app_modes_combobox = LabelComboboxWidget(parent=self, enum_class=AppModes,
                                                       text_mapping=APP_MODE_TEXT)
        self._mode_add_text_widget = AddTextWidget(self)
        self._mode_change_case_widget = ChangeCaseWidget(self)
        self._mode_date_time_widget = DateTimeWidget(self)
        self._mode_image_dimensions_widget = ImageDimensionsWidget(self)
        self._mode_parent_folders_widget = ParentFoldersWidget(self)
        self._mode_remove_text_widget = RemoveTextWidget(self)
        self._mode_replace_text_widget = ReplaceTextWidget(self)
        self._mode_sequence_generator_widget = SequenceGeneratorWidget(self)
        self._mode_truncate_text_widget = TruncateTextWidget(self)
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

        self._app_modes_combobox.setFixedHeight(50)
        self._main_layout.addWidget(self._app_modes_combobox)
        self._main_layout.addWidget(self._current_mode_widget)
        self._main_layout.addStretch(1)

        self.handle_app_mode_changed(AppModes.ADD_CUSTOM_TEXT)

    def add_text_to_widgets(self):
        self._app_modes_combobox.set_label_text(self.tr("Select mode"))

    def create_event_handlers(self):
        self._app_modes_combobox.valueIsChanged.connect(self.handle_app_mode_changed)

    def request_command(self) -> PrepareCommand:
        return self._current_mode_widget.request_command()

    @Slot()
    def handle_app_mode_changed(self, app_mode: AppModes):
        widget: BasePrepareCommandWidget = self._mode_widget_dict[app_mode]

        if widget is None:
            raise WidgetNotFoundException()

        self._current_mode_widget.hide()
        self._main_layout.replaceWidget(self._current_mode_widget, widget)
        self._current_mode_widget = widget
        self._current_mode_widget.show()
