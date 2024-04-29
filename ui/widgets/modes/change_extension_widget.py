from PySide6.QtCore import Slot

from core.commands.prep_ext_change import ExtensionChangePrepareCommand
from core.commons import PrepareCommand
from ui.widgets.base_abstract_widgets import (
    BasePrepareCommandWidget,
    PATH_SYMBOLS_VALIDATOR,
)
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget


class ChangeExtensionWidget(BasePrepareCommandWidget):
    _new_extension: LabelLineEditWidget
    _new_extension_value: str

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._new_extension = LabelLineEditWidget(self)

    def configure_widgets(self):
        self._new_extension.setMaximumHeight(100)
        self._new_extension.set_text_validator(PATH_SYMBOLS_VALIDATOR)

        self._main_layout.addWidget(self._new_extension)

        self._new_extension_value = self._new_extension.get_current_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._new_extension.set_label_text(self.tr("New extension:"))

    def create_event_handlers(self):
        self._new_extension.valueIsChanged.connect(self.handle_text_changed)

    def request_command(self) -> PrepareCommand:
        return ExtensionChangePrepareCommand(new_extension=self._new_extension_value)

    @Slot()
    def handle_text_changed(self, text: str):
        print(f"handle_text_changed, {text}")
        self._new_extension_value = text
