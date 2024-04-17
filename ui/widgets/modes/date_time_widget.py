from PySide6.QtCore import QDateTime
from PySide6.QtWidgets import QCheckBox, QDateTimeEdit

from core.commands.prep_date_time import DateTimeRenamePrepareCommand
from core.commons import PrepareCommand
from core.enums import ItemPositionWithReplacement, DateFormat, DateTimeFormat, DateTimeSource, TimeFormat
from core.text_values import ITEM_POSITION_WITH_REPLACEMENT_TEXT, DATE_FORMAT_TEXT, DATE_TIME_FORMAT_TEXT, \
    DATE_TIME_SOURCE_TEXT, TIME_FORMAT_TEXT
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_combobox_widget import LabelComboboxWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget


class DateTimeWidget(BasePrepareCommandWidget):
    _datetime_position_radio_btn: LabelRadioButtonsWidget
    _date_format_combobox: LabelComboboxWidget
    _time_format_combobox: LabelComboboxWidget
    _datetime_format_combobox: LabelComboboxWidget
    _datetime_source_combobox: LabelComboboxWidget
    _use_uppercase_checkbox: QCheckBox
    _date_time_edit: QDateTimeEdit

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._datetime_position_radio_btn = LabelRadioButtonsWidget(parent=self, enum_class=ItemPositionWithReplacement,
                                                                    text_mapping=ITEM_POSITION_WITH_REPLACEMENT_TEXT)
        self._date_format_combobox = LabelComboboxWidget(parent=self, enum_class=DateFormat,
                                                         text_mapping=DATE_FORMAT_TEXT)
        self._time_format_combobox = LabelComboboxWidget(parent=self, enum_class=TimeFormat,
                                                         text_mapping=TIME_FORMAT_TEXT)
        self._datetime_format_combobox = LabelComboboxWidget(parent=self, enum_class=DateTimeFormat,
                                                             text_mapping=DATE_TIME_FORMAT_TEXT)
        self._datetime_source_combobox = LabelComboboxWidget(parent=self, enum_class=DateTimeSource,
                                                             text_mapping=DATE_TIME_SOURCE_TEXT)
        self._use_uppercase_checkbox = QCheckBox(self)
        self._date_time_edit = QDateTimeEdit(self)

    def configure_widgets(self):
        self._use_uppercase_checkbox.setChecked(False)
        self._date_time_edit.setDateTime(QDateTime.currentDateTime())
        self._date_time_edit.hide()
        self._main_layout.addWidget(self._datetime_position_radio_btn)
        self._main_layout.addWidget(self._date_format_combobox)
        self._main_layout.addWidget(self._time_format_combobox)
        self._main_layout.addWidget(self._datetime_format_combobox)
        self._main_layout.addWidget(self._use_uppercase_checkbox)
        self._main_layout.addWidget(self._datetime_source_combobox)
        self._main_layout.addWidget(self._date_time_edit)

    def add_text_to_widgets(self):
        self._datetime_position_radio_btn.set_label_text(self.tr("Chose renaming mode:"))
        self._date_format_combobox.set_label_text(self.tr("Chose Date format:"))
        self._time_format_combobox.set_label_text(self.tr("Chose Time format:"))
        self._datetime_format_combobox.set_label_text(self.tr("Chose Date/Time format:"))
        self._datetime_source_combobox.set_label_text(self.tr("Chose the source of time:"))
        self._use_uppercase_checkbox.setText(self.tr("Use am/pm in Uppercase:"))

    def create_event_handlers(self):
        pass

    def request_command(self) -> PrepareCommand:
        return DateTimeRenamePrepareCommand(
            position=ItemPositionWithReplacement.REPLACE,
            date_format=DateFormat.YYYY_MM_DD_TOGETHER,
            time_format=TimeFormat.HH_MM_SS_24_TOGETHER,
            datetime_format=DateTimeFormat.DATE_TIME_UNDERSCORED,
            datetime_source=DateTimeSource.CONTENT_CREATION_DATE,
            use_uppercase=True
        )
