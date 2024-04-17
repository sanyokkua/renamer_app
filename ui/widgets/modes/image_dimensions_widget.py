from PySide6.QtWidgets import QWidget, QHBoxLayout

from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPositionWithReplacement, ImageDimensionOptions
from core.text_values import ITEM_POSITION_WITH_REPLACEMENT_TEXT, IMAGE_DIMENSION_OPTIONS_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
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

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._dimension_position_radio_btn = LabelRadioButtonsWidget(parent=self,
                                                                     enum_class=ItemPositionWithReplacement,
                                                                     text_mapping=ITEM_POSITION_WITH_REPLACEMENT_TEXT)
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
        self._widget_dimension_edit.setLayout(self._widget_dimension_edit_layout)
        self._widget_dimension_edit_layout.addWidget(self._left_side_combobox)
        self._widget_dimension_edit_layout.addWidget(self._separator_between_line_edit)
        self._widget_dimension_edit_layout.addWidget(self._right_side_combobox)

        self._main_layout.addWidget(self._dimension_position_radio_btn)
        self._main_layout.addWidget(self._widget_dimension_edit)
        self._main_layout.addWidget(self._separator_with_dimensions_line_edit)

    def add_text_to_widgets(self):
        self._dimension_position_radio_btn.set_label_text(self.tr("Chose renaming mode:"))
        self._left_side_combobox.set_label_text(self.tr("Left:"))
        self._separator_between_line_edit.set_label_text(self.tr("Separator:"))
        self._right_side_combobox.set_label_text(self.tr("Right:"))
        self._separator_with_dimensions_line_edit.set_label_text(
            self.tr("Enter separator between existing name and dimension:"))

    def create_event_handlers(self):
        pass

    def request_command(self) -> PrepareCommand:
        return ImageDimensionsPrepareCommand(
            position=ItemPositionWithReplacement.BEGIN,
            left_side=ImageDimensionOptions.WIDTH,
            right_side=ImageDimensionOptions.HEIGHT,
            separator_between="x",
            separator_before_or_after="_"
        )
