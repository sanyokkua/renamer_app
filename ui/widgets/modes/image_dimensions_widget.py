from PySide6.QtCore import Slot
from PySide6.QtWidgets import QWidget, QHBoxLayout

from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPositionWithReplacement, ImageDimensionOptions
from core.text_values import ITEM_POSITION_WITH_REPLACEMENT_TEXT, IMAGE_DIMENSION_OPTIONS_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget, PATH_SYMBOLS_VALIDATOR
from ui.widgets.customs.pairs.label_combobox_widget import LabelComboboxWidget
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget


class ImageDimensionsWidget(BasePrepareCommandWidget):
    _dimension_position_radio_btn: LabelRadioButtonsWidget
    _widget_dimension_edit: QWidget
    _widget_dimension_edit_layout: QHBoxLayout
    _left_side_combobox: LabelComboboxWidget
    _right_side_combobox: LabelComboboxWidget
    _separator_between_line_edit: LabelLineEditWidget
    _separator_with_dimensions_line_edit: LabelLineEditWidget
    _dimension_position_value: ItemPositionWithReplacement
    _left_side_value: ImageDimensionOptions
    _right_side_value: ImageDimensionOptions
    _separator_between_value: str
    _separator_with_dimensions_value: str

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._dimension_position_radio_btn = LabelRadioButtonsWidget(parent=self,
                                                                     enum_class=ItemPositionWithReplacement,
                                                                     text_mapping=ITEM_POSITION_WITH_REPLACEMENT_TEXT,
                                                                     vertical=True)
        self._widget_dimension_edit = QWidget(self)
        self._widget_dimension_edit_layout = QHBoxLayout(self)
        self._left_side_combobox = LabelComboboxWidget(parent=self._widget_dimension_edit,
                                                       enum_class=ImageDimensionOptions,
                                                       text_mapping=IMAGE_DIMENSION_OPTIONS_TEXT)
        self._right_side_combobox = LabelComboboxWidget(parent=self._widget_dimension_edit,
                                                        enum_class=ImageDimensionOptions,
                                                        text_mapping=IMAGE_DIMENSION_OPTIONS_TEXT)
        self._separator_between_line_edit = LabelLineEditWidget(parent=self._widget_dimension_edit)
        self._separator_with_dimensions_line_edit = LabelLineEditWidget(parent=self)

    def configure_widgets(self):
        self._separator_between_line_edit.set_text_validator(PATH_SYMBOLS_VALIDATOR)
        self._separator_with_dimensions_line_edit.set_text_validator(PATH_SYMBOLS_VALIDATOR)

        self._widget_dimension_edit.setLayout(self._widget_dimension_edit_layout)
        self._widget_dimension_edit_layout.addWidget(self._left_side_combobox)
        self._widget_dimension_edit_layout.addWidget(self._separator_between_line_edit)
        self._widget_dimension_edit_layout.addWidget(self._right_side_combobox)

        self._main_layout.addWidget(self._dimension_position_radio_btn)
        self._main_layout.addWidget(self._widget_dimension_edit)
        self._main_layout.addWidget(self._separator_with_dimensions_line_edit)

        self._dimension_position_value = self._dimension_position_radio_btn.get_current_value()
        self._left_side_value = self._left_side_combobox.get_current_value()
        self._right_side_value = self._right_side_combobox.get_current_value()
        self._separator_between_value = self._separator_between_line_edit.get_current_value()
        self._separator_with_dimensions_value = self._separator_with_dimensions_line_edit.get_current_value()
        self.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        self._dimension_position_radio_btn.set_label_text(self.tr("Chose renaming mode:"))
        self._left_side_combobox.set_label_text(self.tr("Left:"))
        self._separator_between_line_edit.set_label_text(self.tr("Separator:"))
        self._right_side_combobox.set_label_text(self.tr("Right:"))
        self._separator_with_dimensions_line_edit.set_label_text(
            self.tr("Enter separator between existing name and dimension:"))

    def create_event_handlers(self):
        self._dimension_position_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._left_side_combobox.valueIsChanged.connect(self.handle_left_side_changed)
        self._separator_between_line_edit.valueIsChanged.connect(self.handle_separator_between_changed)
        self._right_side_combobox.valueIsChanged.connect(self.handle_right_side_changed)
        self._separator_with_dimensions_line_edit.valueIsChanged.connect(self.handle_separator_with_dimensions_changed)

    def request_command(self) -> PrepareCommand:
        return ImageDimensionsPrepareCommand(
            position=self._dimension_position_value,
            left_side=self._left_side_value,
            right_side=self._right_side_value,
            separator_between=self._separator_between_value,
            separator_before_or_after=self._separator_with_dimensions_value
        )

    @Slot()
    def handle_position_changed(self, value: ItemPositionWithReplacement):
        print(f"handle_position_changed: {value}")
        self._dimension_position_value = value

    @Slot()
    def handle_left_side_changed(self, value: ImageDimensionOptions):
        print(f"handle_left_side_changed: {value}")
        self._left_side_value = value

    @Slot()
    def handle_separator_between_changed(self, value: str):
        print(f"handle_separator_between_changed: {value}")
        self._separator_between_value = value

    @Slot()
    def handle_right_side_changed(self, value: ImageDimensionOptions):
        print(f"handle_right_side_changed: {value}")
        self._right_side_value = value

    @Slot()
    def handle_separator_with_dimensions_changed(self, value: str):
        print(f"handle_separator_with_dimensions_changed: {value}")
        self._separator_with_dimensions_value = value
