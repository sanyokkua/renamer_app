from PySide6.QtCore import Slot

from core.commands.prep_parent_folders import ParentFoldersPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget, PATH_SYMBOLS_VALIDATOR
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget
from ui.widgets.customs.pairs.label_spinbox_widget import LabelSpinBoxWidget


class ParentFoldersWidget(BasePrepareCommandWidget):
    _folder_name_position_radio_btn: LabelRadioButtonsWidget
    _number_of_parents_spinbox: LabelSpinBoxWidget
    _separator_line_edit: LabelLineEditWidget
    _folder_name_position_value: ItemPosition
    _number_of_parents_value: int
    _separator_value: str

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._folder_name_position_radio_btn = LabelRadioButtonsWidget(parent=self, enum_class=ItemPosition,
                                                                       text_mapping=ITEM_POSITION_TEXT, vertical=True)
        self._number_of_parents_spinbox = LabelSpinBoxWidget(self)
        self._separator_line_edit = LabelLineEditWidget(self)

    def configure_widgets(self):
        self._number_of_parents_spinbox.spin_box_min = 1
        self._separator_line_edit.set_text_validator(PATH_SYMBOLS_VALIDATOR)
        self._main_layout.addWidget(self._folder_name_position_radio_btn)
        self._main_layout.addWidget(self._number_of_parents_spinbox)
        self._main_layout.addWidget(self._separator_line_edit)

        self._folder_name_position_value = self._folder_name_position_radio_btn.get_current_value()
        self._number_of_parents_value = self._number_of_parents_spinbox.get_current_value()
        self._separator_value = self._separator_line_edit.get_current_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._folder_name_position_radio_btn.set_label_text(self.tr("Select renaming mode:"))
        self._number_of_parents_spinbox.set_label_text(self.tr("Number of parent folders to include:"))
        self._separator_line_edit.set_label_text(self.tr("Separator between folders:"))

    def create_event_handlers(self):
        self._folder_name_position_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._number_of_parents_spinbox.valueIsChanged.connect(self.handle_parents_number_changed)
        self._separator_line_edit.valueIsChanged.connect(self.handle_separator_changed)

    def request_command(self) -> PrepareCommand:
        return ParentFoldersPrepareCommand(
            position=self._folder_name_position_value,
            number_of_parents=self._number_of_parents_value,
            separator=self._separator_value
        )

    @Slot()
    def handle_position_changed(self, value: ItemPosition):
        print(f"handle_position_changed: {value}")
        self._folder_name_position_value = value

    @Slot()
    def handle_parents_number_changed(self, value: int):
        print(f"handle_parents_number_changed: {value}")
        self._number_of_parents_value = value

    @Slot()
    def handle_separator_changed(self, value: str):
        print(f"handle_separator_changed: {value}")
        self._separator_value = value
