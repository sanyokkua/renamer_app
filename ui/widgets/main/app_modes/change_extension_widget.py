import logging

from PySide6.QtCore import Slot

from core.commands.prep_ext_change import ExtensionChangePrepareCommand
from ui.widgets.customs.form_widgets import LineTextEditForm
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

log: logging.Logger = logging.getLogger(__name__)


class ChangeExtensionWidget(ModeBaseWidget):
    _ext_text_edit: LineTextEditForm

    def init_widgets(self):
        self._ext_text_edit = LineTextEditForm(self)

    def configure_widgets(self):
        self.add_widget(self._ext_text_edit)

    def add_text_to_widgets(self):
        self._ext_text_edit.set_widget_label("New extension:")

    def create_event_handlers(self):
        self._ext_text_edit.valueIsChanged.connect(self.handle_text_changed)

    def request_command(self) -> ExtensionChangePrepareCommand:
        return ExtensionChangePrepareCommand(new_extension=self._ext_text_edit.get_widget_value())

    @Slot()
    def handle_text_changed(self, text: str):
        log.debug(f"handle_text_changed, {text}")
        self.tell_about_changes()
