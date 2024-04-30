from PySide6.QtCore import QDateTime, Slot
from PySide6.QtWidgets import QDateTimeEdit

from core.commands.prep_date_time import DateTimeRenamePrepareCommand
from core.commons import PrepareCommand
from core.enums import (
    ItemPositionWithReplacement,
    DateFormat,
    DateTimeFormat,
    DateTimeSource,
    TimeFormat,
)
from core.text_values import (
    ITEM_POSITION_WITH_REPLACEMENT_TEXT,
    DATE_FORMAT_TEXT,
    DATE_TIME_FORMAT_TEXT,
    DATE_TIME_SOURCE_TEXT,
    TIME_FORMAT_TEXT,
)
from ui.widgets.base_abstract_widgets import BasePrepareCommandWidget
from ui.widgets.customs.pairs.label_checkbox_widget import LabelCheckboxWidget
from ui.widgets.customs.pairs.label_combobox_widget import LabelComboboxWidget
from ui.widgets.customs.pairs.label_line_edit_widget import LabelLineEditWidget
from ui.widgets.customs.pairs.label_radio_buttons_widget import LabelRadioButtonsWidget


class DateTimeWidget(BasePrepareCommandWidget):
    _datetime_position_radio_btn: LabelRadioButtonsWidget
    _datetime_separator: LabelLineEditWidget
    _date_format_combobox: LabelComboboxWidget
    _time_format_combobox: LabelComboboxWidget
    _datetime_format_combobox: LabelComboboxWidget
    _datetime_source_combobox: LabelComboboxWidget
    _use_uppercase_checkbox: LabelCheckboxWidget
    _date_time_edit: QDateTimeEdit
    _datetime_position_value: ItemPositionWithReplacement
    _date_format_value: DateFormat
    _time_format_value: TimeFormat
    _datetime_format_value: DateTimeFormat
    _datetime_source_value: DateTimeSource
    _use_uppercase_value: bool
    _custom_datetime_value: str
    _datetime_separator_value: str

    def __init__(self, parent=None):
        super().__init__(parent)

    def init_widgets(self):
        self._datetime_position_radio_btn = LabelRadioButtonsWidget(
            parent=self,
            enum_class=ItemPositionWithReplacement,
            text_mapping=ITEM_POSITION_WITH_REPLACEMENT_TEXT,
            vertical=True,
        )
        self._datetime_separator = LabelLineEditWidget(self)
        self._date_format_combobox = LabelComboboxWidget(
            parent=self, enum_class=DateFormat, text_mapping=DATE_FORMAT_TEXT
        )
        self._time_format_combobox = LabelComboboxWidget(
            parent=self, enum_class=TimeFormat, text_mapping=TIME_FORMAT_TEXT
        )
        self._datetime_format_combobox = LabelComboboxWidget(
            parent=self, enum_class=DateTimeFormat, text_mapping=DATE_TIME_FORMAT_TEXT
        )
        self._datetime_source_combobox = LabelComboboxWidget(
            parent=self, enum_class=DateTimeSource, text_mapping=DATE_TIME_SOURCE_TEXT
        )
        self._use_uppercase_checkbox = LabelCheckboxWidget(self)
        self._date_time_edit = QDateTimeEdit(self)

        self._datetime_position_value = (
            self._datetime_position_radio_btn.get_current_value()
        )
        self._datetime_separator_value = self._datetime_separator.get_current_value()
        self._date_format_value = self._date_format_combobox.get_current_value()
        self._time_format_value = self._time_format_combobox.get_current_value()
        self._datetime_format_value = self._datetime_format_combobox.get_current_value()
        self._datetime_source_value = self._datetime_source_combobox.get_current_value()
        self._use_uppercase_value = self._use_uppercase_checkbox.get_current_value()
        self._custom_datetime_value = ""

    def configure_widgets(self):
        self._use_uppercase_checkbox.set_value(False)
        self._datetime_separator.show()
        self._date_time_edit.setDisplayFormat("dd/MM/yyyy - hh:mm:ss")
        self._date_time_edit.setDateTime(QDateTime.currentDateTime())
        self._date_time_edit.hide()
        self._main_layout.addWidget(self._datetime_position_radio_btn)
        self._main_layout.addWidget(self._datetime_separator)
        self._main_layout.addWidget(self._date_format_combobox)
        self._main_layout.addWidget(self._time_format_combobox)
        self._main_layout.addWidget(self._datetime_format_combobox)
        self._main_layout.addWidget(self._use_uppercase_checkbox)
        self._main_layout.addWidget(self._datetime_source_combobox)
        self._main_layout.addWidget(self._date_time_edit)
        self.setContentsMargins(0, 0, 0, 0)
        self._custom_datetime_value = self._date_time_edit.dateTime().toString(
            "yyyyMMdd_hhmmss"
        )

    def add_text_to_widgets(self):
        self._datetime_position_radio_btn.set_label_text(
            self.tr("Chose renaming mode:")
        )
        self._datetime_separator.set_label_text(
            self.tr("Enter separator for datetime:")
        )
        self._date_format_combobox.set_label_text(self.tr("Chose Date format:"))
        self._time_format_combobox.set_label_text(self.tr("Chose Time format:"))
        self._datetime_format_combobox.set_label_text(
            self.tr("Chose Date/Time format:")
        )
        self._datetime_source_combobox.set_label_text(
            self.tr("Chose the source of time:")
        )
        self._use_uppercase_checkbox.set_label_text(self.tr("Use am/pm in Uppercase:"))

    def create_event_handlers(self):
        self._datetime_position_radio_btn.valueIsChanged.connect(
            self.handle_position_changed
        )
        self._date_format_combobox.valueIsChanged.connect(
            self.handle_date_format_changed
        )
        self._time_format_combobox.valueIsChanged.connect(
            self.handle_time_format_changed
        )
        self._datetime_format_combobox.valueIsChanged.connect(
            self.handle_datetime_format_changed
        )
        self._use_uppercase_checkbox.valueIsChanged.connect(
            self.handle_use_uppercase_changed
        )
        self._datetime_source_combobox.valueIsChanged.connect(
            self.handle_source_changed
        )
        self._date_time_edit.dateTimeChanged.connect(
            self.handle_custom_datetime_changed
        )

    def request_command(self) -> PrepareCommand:
        return DateTimeRenamePrepareCommand(
            position=self._datetime_position_value,
            date_format=self._date_format_value,
            time_format=self._time_format_value,
            datetime_format=self._datetime_format_value,
            datetime_source=self._datetime_source_value,
            use_uppercase=self._use_uppercase_value,
            custom_datetime=self._custom_datetime_value,
            separator_for_name_and_datetime=self._datetime_separator_value,
        )

    @Slot()
    def handle_position_changed(self, value: ItemPositionWithReplacement):
        print(f"handle_position_changed: {value}")
        if (
            value == ItemPositionWithReplacement.BEGIN
            or value == ItemPositionWithReplacement.END
        ):
            self._datetime_separator.show()
            self._datetime_separator_value = (
                self._datetime_separator.get_current_value()
            )
        else:
            self._datetime_separator.hide()
            self._datetime_separator_value = ""

        self._datetime_position_value = value

    @Slot()
    def handle_date_format_changed(self, value: DateFormat):
        print(f"handle_date_format_changed: {value}")
        self._date_format_value = value

    @Slot()
    def handle_time_format_changed(self, value: TimeFormat):
        print(f"handle_time_format_changed: {value}")
        self._time_format_value = value

    @Slot()
    def handle_datetime_format_changed(self, value: DateTimeFormat):
        print(f"handle_datetime_format_changed: {value}")
        self._datetime_format_value = value

    @Slot()
    def handle_use_uppercase_changed(self, value: bool):
        print(f"handle_use_uppercase_changed: {value}")
        self._use_uppercase_value = value

    @Slot()
    def handle_source_changed(self, value: DateTimeSource):
        print(f"handle_source_changed: {value}")
        self._datetime_source_value = value
        if value == DateTimeSource.CUSTOM_DATE:
            self._date_time_edit.show()
        else:
            self._date_time_edit.hide()

    @Slot()
    def handle_custom_datetime_changed(self, value: QDateTime):
        print(f"handle_custom_datetime_changed: {value}")
        value_to_string = value.toString("yyyyMMdd_hhmmss")
        self._custom_datetime_value = value_to_string
