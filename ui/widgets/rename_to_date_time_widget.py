from enum import Enum

from PySide6.QtCore import (Qt, Slot)
from PySide6.QtGui import (QCursor)
from PySide6.QtWidgets import (QComboBox, QFormLayout, QLabel, QRadioButton, QVBoxLayout, QWidget,
                               QButtonGroup, QLineEdit)

from core.command import Command, RenameToDateTimeCommand
from core.constants import (DateTimeSource, DateTimeFormats, DateTimePlacing)
from ui.widgets.base_params_widget import BaseParamsWidget


class RadioBtnId(Enum):
    ID_RADIO_BTN_REPLACE = 1
    ID_RADIO_BTN_BEGIN = 2
    ID_RADIO_BTN_END = 3


class RenameToDateTimeForm(BaseParamsWidget):

    def __init__(self):
        super().__init__()
        self.main_widget_layout = QVBoxLayout(self)

        self.radio_btn_group_widget = QWidget(self)
        self.radio_btn_group_widget.setFixedHeight(100)
        self.radio_btn_group_widget_layout = QVBoxLayout(self.radio_btn_group_widget)

        self.replace_with_time_radio_btn = QRadioButton(self.radio_btn_group_widget)
        self.replace_with_time_radio_btn.setCursor(QCursor(Qt.PointingHandCursor))
        self.replace_with_time_radio_btn.setChecked(True)

        self.add_time_to_begin_radio_btn = QRadioButton(self.radio_btn_group_widget)
        self.add_time_to_begin_radio_btn.setCursor(QCursor(Qt.PointingHandCursor))

        self.add_time_to_end_radio_btn = QRadioButton(self.radio_btn_group_widget)
        self.add_time_to_end_radio_btn.setCursor(QCursor(Qt.PointingHandCursor))

        self.radio_button_group = QButtonGroup(self.radio_btn_group_widget)
        self.radio_button_group.addButton(self.replace_with_time_radio_btn, RadioBtnId.ID_RADIO_BTN_REPLACE.value)
        self.radio_button_group.addButton(self.add_time_to_begin_radio_btn, RadioBtnId.ID_RADIO_BTN_BEGIN.value)
        self.radio_button_group.addButton(self.add_time_to_end_radio_btn, RadioBtnId.ID_RADIO_BTN_END.value)

        self.radio_btn_group_widget_layout.addWidget(self.replace_with_time_radio_btn)
        self.radio_btn_group_widget_layout.addWidget(self.add_time_to_begin_radio_btn)
        self.radio_btn_group_widget_layout.addWidget(self.add_time_to_end_radio_btn)

        self.main_widget_layout.addWidget(self.radio_btn_group_widget)

        self.options_main_widget = QWidget(self)
        self.options_main_widget_layout = QVBoxLayout(self.options_main_widget)

        self.time_source_widget = QWidget(self.options_main_widget)
        self.time_source_widget_layout = QFormLayout(self.time_source_widget)
        self.time_source_widget_layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self.time_source_widget_layout.setRowWrapPolicy(QFormLayout.RowWrapPolicy.WrapLongRows)

        self.time_source_label = QLabel(self.time_source_widget)
        self.time_source_combo_box = QComboBox(self.time_source_widget)
        self.time_source_combo_box.setCursor(QCursor(Qt.PointingHandCursor))
        self.time_source_widget_layout.setWidget(0, QFormLayout.LabelRole, self.time_source_label)
        self.time_source_widget_layout.setWidget(0, QFormLayout.FieldRole, self.time_source_combo_box)

        self.time_format_label = QLabel(self.time_source_widget)
        self.time_format_combo_box = QComboBox(self.time_source_widget)
        self.time_format_combo_box.setCursor(QCursor(Qt.PointingHandCursor))
        self.time_source_widget_layout.setWidget(1, QFormLayout.LabelRole, self.time_format_label)
        self.time_source_widget_layout.setWidget(1, QFormLayout.FieldRole, self.time_format_combo_box)

        self.date_time_separator_label = QLabel(self.time_source_widget)
        self.date_time_separator_line_edit = QLineEdit(self.time_source_widget)
        self.time_source_widget_layout.setWidget(2, QFormLayout.LabelRole, self.date_time_separator_label)
        self.time_source_widget_layout.setWidget(2, QFormLayout.FieldRole, self.date_time_separator_line_edit)

        self.date_time_separator_line_edit.setValidator(self.path_symbols_validator)

        self.options_main_widget_layout.addWidget(self.time_source_widget)
        self.main_widget_layout.addWidget(self.options_main_widget)

        self.date_time_place = DateTimePlacing.REPLACE
        self.time_source = DateTimeSource.FILE_CREATION_TIME
        self.time_format = DateTimeFormats.YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_MILLIS
        self.date_time_separator = "_"

        self.add_text_to_widgets()
        self.add_event_handlers()

    def add_text_to_widgets(self):
        self.replace_with_time_radio_btn.setText("Replace with time")
        self.add_time_to_begin_radio_btn.setText("Add time to begin")
        self.add_time_to_end_radio_btn.setText("Add time to end")
        self.time_source_label.setText("Time Source")
        self.time_format_label.setText("Time Format")
        self.date_time_separator_label.setText("Separator between date and time")

        self.time_source_combo_box.addItem(DateTimeSource.FILE_CREATION_TIME.value)
        self.time_source_combo_box.addItem(DateTimeSource.FILE_MODIFICATION_TIME.value)
        self.time_source_combo_box.addItem(DateTimeSource.FILE_EXIF_CREATION_TIME.value)

        self.time_format_combo_box.addItem(DateTimeFormats.ISO_8601_FORMAT.value)
        self.time_format_combo_box.addItem(DateTimeFormats.US_FORMAT.value)
        self.time_format_combo_box.addItem(DateTimeFormats.EUROPEAN_FORMAT.value)
        self.time_format_combo_box.addItem(DateTimeFormats.NUMBER_OF_SECONDS_SINCE_JANUARY_1_1970.value)

        self.date_time_separator_line_edit.setText(self.date_time_separator)

    def add_event_handlers(self):
        self.radio_button_group.idToggled.connect(self.handle_radio_btn_toggled_event)
        self.time_source_combo_box.currentIndexChanged.connect(self.handle_time_source_changed_event)
        self.time_format_combo_box.currentIndexChanged.connect(self.handle_time_format_changed_event)
        self.date_time_separator_line_edit.textChanged.connect(self.handle_date_time_separator_changed_event)

    @Slot()
    def handle_radio_btn_toggled_event(self, btn_id: int, state: bool) -> None:
        if state:
            match btn_id:
                case RadioBtnId.ID_RADIO_BTN_REPLACE.value:
                    self.date_time_place = DateTimePlacing.REPLACE
                case RadioBtnId.ID_RADIO_BTN_BEGIN.value:
                    self.date_time_place = DateTimePlacing.ADD_TO_BEGIN
                case RadioBtnId.ID_RADIO_BTN_END.value:
                    self.date_time_place = DateTimePlacing.ADD_TO_END
                case _:
                    self.date_time_place = DateTimePlacing.REPLACE

        print("handle_radio_btn_toggled -> ID: {}, state: {}, chosen place: {}".format(btn_id, state,
                                                                                       self.date_time_place.value))

    @Slot()
    def handle_time_source_changed_event(self, index: int) -> None:
        selected_value = self.time_source_combo_box.itemText(index)
        match selected_value:
            case DateTimeSource.FILE_CREATION_TIME.value:
                self.time_source = DateTimeSource.FILE_CREATION_TIME
            case DateTimeSource.FILE_MODIFICATION_TIME.value:
                self.time_source = DateTimeSource.FILE_MODIFICATION_TIME
            case DateTimeSource.FILE_EXIF_CREATION_TIME.value:
                self.time_source = DateTimeSource.FILE_EXIF_CREATION_TIME
            case _:
                self.time_source = "DEFAULT"
        print("handle_time_source_changed_event: index={}, source={}".format(index, self.time_source))

    @Slot()
    def handle_time_format_changed_event(self, index: int) -> None:
        selected_value = self.time_format_combo_box.itemText(index)
        match selected_value:
            case DateTimeFormats.YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_MILLIS.value:
                self.time_format = DateTimeFormats.YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_MILLIS.value
            case DateTimeFormats.YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_MILLIS_AM_PM.value:
                self.time_format = DateTimeFormats.YEAR_MONTH_DAY_HOURS_MINUTES_SECONDS_MILLIS_AM_PM.value
            case _:
                self.time_format = "DEFAULT"
        print("handle_time_format_changed_event: index={}, source={}".format(index, self.time_format))

    @Slot()
    def handle_date_time_separator_changed_event(self, text: str) -> None:
        self.date_time_separator = text
        print("handle_date_time_separator_changed_event: text={}".format(text))

    @Slot()
    def request_command(self) -> Command:
        command = RenameToDateTimeCommand(
            date_time_place=self.date_time_place,
            date_time_source=self.time_source,
            date_time_format=self.time_format,
            date_time_separator=self.date_time_separator
        )
        self.emit_command_built(command)
        return command
