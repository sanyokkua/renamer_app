from PySide6.QtCore import Slot

from core.commands.abstract_commons import AppFileItemByItemListProcessingCommand
from core.enums import AppModes
from core.exceptions import WidgetNotFoundException
from ui.widgets.main.app_modes import (
    AddTextWidget,
    ChangeCaseWidget,
    ChangeExtensionWidget,
    DateTimeWidget,
    ImageDimensionsWidget,
    ParentFoldersWidget,
    RemoveTextWidget,
    ReplaceTextWidget,
    SequenceGeneratorWidget,
    TruncateTextWidget,
    ModeBaseWidget,
)


class AppModeSelectViewWidget(ModeBaseWidget):
    _current_mode_widget: ModeBaseWidget
    _mode_widget_dict: dict[AppModes, ModeBaseWidget]

    def init_widgets(self):
        self._mode_widget_dict = {
            AppModes.ADD_CUSTOM_TEXT: AddTextWidget(self),
            AppModes.CHANGE_CASE: ChangeCaseWidget(self),
            AppModes.USE_DATETIME: DateTimeWidget(self),
            AppModes.USE_IMAGE_DIMENSIONS: ImageDimensionsWidget(self),
            AppModes.USE_PARENT_FOLDER_NAME: ParentFoldersWidget(self),
            AppModes.REMOVE_CUSTOM_TEXT: RemoveTextWidget(self),
            AppModes.REPLACE_CUSTOM_TEXT: ReplaceTextWidget(self),
            AppModes.ADD_SEQUENCE: SequenceGeneratorWidget(self),
            AppModes.TRUNCATE_FILE_NAME: TruncateTextWidget(self),
            AppModes.CHANGE_EXTENSION: ChangeExtensionWidget(self),
        }

    def configure_widgets(self):
        for widget in self._mode_widget_dict.values():
            self.add_widget(widget)
            widget.hide()
        self._main_layout.addStretch(1)

        self._current_mode_widget = self._mode_widget_dict[AppModes.ADD_CUSTOM_TEXT]
        self._current_mode_widget.show()

        self.setContentsMargins(0, 0, 0, 0)
        self._main_layout.setContentsMargins(0, 0, 0, 0)

        self.setMinimumWidth(450)
        self.setMaximumWidth(450)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        for widget in self._mode_widget_dict.values():
            widget.widgetValuesAreChanged.connect(lambda: self.widgetValuesAreChanged.emit())

    def request_command(self) -> AppFileItemByItemListProcessingCommand:
        return self._current_mode_widget.request_command()

    @Slot()
    def handle_app_mode_changed(self, app_mode: AppModes):
        widget = self._mode_widget_dict[app_mode]

        if widget is None:
            raise WidgetNotFoundException()

        self._current_mode_widget.hide()
        self._current_mode_widget = widget
        self._current_mode_widget.show()
