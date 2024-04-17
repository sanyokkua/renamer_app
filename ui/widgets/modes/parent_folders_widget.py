from core.commands.prep_parent_folders import ParentFoldersPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPosition
from core.text_values import ITEM_POSITION_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget
from ui.widgets.customs.pairs.label_spinbox_widget import LabelSpinBoxWidget


class ParentFoldersWidget(BasePrepareCommandWidget):
    _folder_name_position_radio_btn: LabelRadioButtonsWidget
    _number_of_parents_spinbox: LabelSpinBoxWidget
    _separator_line_edit: LabelLineEditWidget

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._folder_name_position_radio_btn = LabelRadioButtonsWidget(parent=self, enum_class=ItemPosition,
                                                                       text_mapping=ITEM_POSITION_TEXT)
        self._number_of_parents_spinbox = LabelSpinBoxWidget(self)
        self._separator_line_edit = LabelLineEditWidget(self)

    def configure_widgets(self):
        self._main_layout.addWidget(self._folder_name_position_radio_btn)
        self._main_layout.addWidget(self._number_of_parents_spinbox)
        self._main_layout.addWidget(self._separator_line_edit)

    def add_text_to_widgets(self):
        self._folder_name_position_radio_btn.set_label_text(self.tr("Select renaming mode:"))
        self._number_of_parents_spinbox.set_label_text(self.tr("Number of parent folders to include:"))
        self._separator_line_edit.set_label_text(self.tr("Separator between folders:"))

    def create_event_handlers(self):
        pass

    def request_command(self) -> PrepareCommand:
        return ParentFoldersPrepareCommand(
            position=ItemPosition.BEGIN,
            number_of_parents=1,
            separator="_"
        )
