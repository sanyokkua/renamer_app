import logging

from PySide6.QtCore import Qt, Slot
from PySide6.QtWidgets import QGridLayout, QLabel, QWidget

from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand
from core.enums import ImageDimensionOptions, ItemPositionWithReplacement
from core.text_values import (
    IMAGE_DIMENSION_OPTIONS_TEXT,
    ITEM_POSITION_WITH_REPLACEMENT_TEXT,
)
from ui.widgets.customs.form_widgets import (
    LineTextEditForm,
    RadioButtonForm,
    build_radio_button_items,
)
from ui.widgets.customs.single_widgets import (
    ComboBox,
    LineTextEdit,
    build_combobox_items,
)
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

log: logging.Logger = logging.getLogger(__name__)

POSITION_RADIO_BTN_ITEMS = build_radio_button_items(ITEM_POSITION_WITH_REPLACEMENT_TEXT)
DIMENSIONS_CMB_ITEMS = build_combobox_items(IMAGE_DIMENSION_OPTIONS_TEXT)


class ImageDimensionSetup(QWidget):
    _layout: QGridLayout
    left_side_label: QLabel
    left_side_combobox: ComboBox

    center_label: QLabel
    center_text_edit: LineTextEdit

    right_side_label: QLabel
    right_side_combobox: ComboBox

    def __init__(self, parent=None):
        super().__init__(parent)
        self.left_side_label = QLabel(self)
        self.left_side_combobox = ComboBox(self)
        self.center_label = QLabel(self)
        self.center_text_edit = LineTextEdit(self)
        self.right_side_label = QLabel(self)
        self.right_side_combobox = ComboBox(self)
        self._layout = QGridLayout(self)
        self.setLayout(self._layout)
        self._layout.addWidget(self.left_side_label, 0, 0, Qt.AlignmentFlag.AlignLeft)
        self._layout.addWidget(self.left_side_combobox, 1, 0, Qt.AlignmentFlag.AlignLeft)
        self._layout.addWidget(self.center_label, 0, 1, Qt.AlignmentFlag.AlignLeft)
        self._layout.addWidget(self.center_text_edit, 1, 1, Qt.AlignmentFlag.AlignLeft)
        self._layout.addWidget(self.right_side_label, 0, 2, Qt.AlignmentFlag.AlignLeft)
        self._layout.addWidget(self.right_side_combobox, 1, 2, Qt.AlignmentFlag.AlignLeft)


class ImageDimensionsWidget(ModeBaseWidget):
    _position_radio_btn: RadioButtonForm
    _dimensions_setup: ImageDimensionSetup
    _filename_dimensions_separator: LineTextEditForm

    def init_widgets(self):
        self._position_radio_btn = RadioButtonForm(self)
        self._dimensions_setup = ImageDimensionSetup(self)
        self._filename_dimensions_separator = LineTextEditForm()

    def configure_widgets(self):
        self._position_radio_btn.set_widget_items(POSITION_RADIO_BTN_ITEMS)
        self._dimensions_setup.left_side_combobox.set_widget_items(DIMENSIONS_CMB_ITEMS)
        self._dimensions_setup.right_side_combobox.set_widget_items(DIMENSIONS_CMB_ITEMS)

        self.add_widget(self._position_radio_btn)
        self.add_widget(self._dimensions_setup)
        self.add_widget(self._filename_dimensions_separator)

    def add_text_to_widgets(self):
        self._position_radio_btn.set_widget_label("Chose mode:")

        self._dimensions_setup.left_side_label.setText(self.tr("Left Side"))
        self._dimensions_setup.center_label.setText(self.tr("Separator"))
        self._dimensions_setup.right_side_label.setText(self.tr("Right Side"))

        self._filename_dimensions_separator.set_widget_label("Enter separator between existing name and dimension:")

    def create_event_handlers(self):
        self._position_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._dimensions_setup.left_side_combobox.valueIsChanged.connect(self.handle_left_side_changed)
        self._dimensions_setup.center_text_edit.valueIsChanged.connect(self.handle_separator_between_changed)
        self._dimensions_setup.right_side_combobox.valueIsChanged.connect(self.handle_right_side_changed)
        self._filename_dimensions_separator.valueIsChanged.connect(self.handle_separator_with_dimensions_changed)

    def request_command(self) -> ImageDimensionsPrepareCommand:
        return ImageDimensionsPrepareCommand(
            position=self._position_radio_btn.get_widget_value(),
            left_side=self._dimensions_setup.left_side_combobox.get_widget_value(),
            right_side=self._dimensions_setup.right_side_combobox.get_widget_value(),
            dimension_separator=self._dimensions_setup.center_text_edit.get_widget_value(),
            name_separator=self._filename_dimensions_separator.get_widget_value(),
        )

    @Slot()
    def handle_position_changed(self, value: ItemPositionWithReplacement):
        log.debug(f"handle_position_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_left_side_changed(self, value: ImageDimensionOptions):
        log.debug(f"handle_left_side_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_separator_between_changed(self, value: str):
        log.debug(f"handle_separator_between_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_right_side_changed(self, value: ImageDimensionOptions):
        log.debug(f"handle_right_side_changed: {value}")
        self.tell_about_changes()

    @Slot()
    def handle_separator_with_dimensions_changed(self, value: str):
        log.debug(f"handle_separator_with_dimensions_changed: {value}")
        self.tell_about_changes()
