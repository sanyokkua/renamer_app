from PySide6.QtCore import QDateTime, Slot
from PySide6.QtWidgets import QDateTimeEdit

from core.commands.prep_date_time import DateTimeRenamePrepareCommand
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
    _custom_date_time_edit: QDateTimeEdit
    _datetime_position_value: ItemPositionWithReplacement
    _date_format_value: DateFormat
    _time_format_value: TimeFormat
    _datetime_format_value: DateTimeFormat
    _datetime_source_value: DateTimeSource

    _use_fallback_checkbox: LabelCheckboxWidget
    _use_fallback_custom_checkbox: LabelCheckboxWidget
    _use_fallback_custom_datetime_edit: QDateTimeEdit

    _use_uppercase_value: bool
    _custom_datetime_value: str
    _datetime_separator_value: str

    _use_fallback_dates_value: bool
    _use_fallback_date_str_value: str

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
        self._custom_date_time_edit = QDateTimeEdit(self)

        self._use_fallback_checkbox = LabelCheckboxWidget(self)
        self._use_fallback_custom_checkbox = LabelCheckboxWidget(self)
        self._use_fallback_custom_datetime_edit = QDateTimeEdit(self)

        self._datetime_position_value = self._datetime_position_radio_btn.get_current_value()
        self._datetime_separator_value = self._datetime_separator.get_current_value()
        self._date_format_value = self._date_format_combobox.get_current_value()
        self._time_format_value = self._time_format_combobox.get_current_value()
        self._datetime_format_value = self._datetime_format_combobox.get_current_value()
        self._datetime_source_value = self._datetime_source_combobox.get_current_value()
        self._use_uppercase_value = self._use_uppercase_checkbox.get_current_value()
        self._custom_datetime_value = ""
        self._use_fallback_dates_value = self._use_fallback_checkbox.get_current_value()
        self._use_fallback_date_str_value = ""

    def configure_widgets(self):
        self._use_uppercase_checkbox.set_value(False)
        self._datetime_separator.show()
        self._custom_date_time_edit.setDisplayFormat("dd/MM/yyyy - hh:mm:ss")
        self._custom_date_time_edit.setDateTime(QDateTime.currentDateTime())
        self._custom_date_time_edit.hide()

        self._use_fallback_checkbox.hide()
        self._use_fallback_custom_datetime_edit.setDisplayFormat("dd/MM/yyyy - hh:mm:ss")
        self._use_fallback_custom_datetime_edit.setDateTime(QDateTime.currentDateTime())
        self._use_fallback_custom_datetime_edit.hide()

        self._main_layout.addWidget(self._datetime_position_radio_btn)
        self._main_layout.addWidget(self._datetime_separator)
        self._main_layout.addWidget(self._date_format_combobox)
        self._main_layout.addWidget(self._time_format_combobox)
        self._main_layout.addWidget(self._datetime_format_combobox)
        self._main_layout.addWidget(self._use_uppercase_checkbox)
        self._main_layout.addWidget(self._datetime_source_combobox)
        self._main_layout.addWidget(self._custom_date_time_edit)
        self._main_layout.addWidget(self._use_fallback_checkbox)
        self._main_layout.addWidget(self._use_fallback_custom_checkbox)
        self._main_layout.addWidget(self._use_fallback_custom_datetime_edit)
        self.setContentsMargins(0, 0, 0, 0)
        self._custom_datetime_value = self._custom_date_time_edit.dateTime().toString("yyyyMMdd_hhmmss")
        self.manage_displayed_widgets()

    def add_text_to_widgets(self):
        self._datetime_position_radio_btn.set_label_text(self.tr("Chose renaming mode:"))
        self._datetime_separator.set_label_text(self.tr("Enter separator for datetime:"))
        self._date_format_combobox.set_label_text(self.tr("Chose Date format:"))
        self._time_format_combobox.set_label_text(self.tr("Chose Time format:"))
        self._datetime_format_combobox.set_label_text(self.tr("Chose Date/Time format:"))
        self._datetime_source_combobox.set_label_text(self.tr("Chose the source of time:"))
        self._use_uppercase_checkbox.set_label_text(self.tr("Use am/pm in Uppercase:"))
        self._use_fallback_checkbox.set_label_text(
            self.tr("Use fallback date (any other available date from metadata):")
        )
        self._use_fallback_custom_checkbox.set_label_text(self.tr("Use Custom date as fallback date"))

    def create_event_handlers(self):
        self._datetime_position_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._date_format_combobox.valueIsChanged.connect(self.handle_date_format_changed)
        self._time_format_combobox.valueIsChanged.connect(self.handle_time_format_changed)
        self._datetime_format_combobox.valueIsChanged.connect(self.handle_datetime_format_changed)
        self._use_uppercase_checkbox.valueIsChanged.connect(self.handle_use_uppercase_changed)
        self._datetime_source_combobox.valueIsChanged.connect(self.handle_source_changed)
        self._custom_date_time_edit.dateTimeChanged.connect(self.handle_custom_datetime_changed)

        self._use_fallback_checkbox.valueIsChanged.connect(self.handle_fallback_checkbox_changed)
        self._use_fallback_custom_checkbox.valueIsChanged.connect(self.handle_fallback_custom_checkbox_changed)
        self._use_fallback_custom_datetime_edit.dateTimeChanged.connect(self.handle_fallback_datetime_changed)

    def request_command(self) -> DateTimeRenamePrepareCommand:
        use_fallback_dates = False
        fallback_custom_date = ""
        match self._datetime_source_value:
            case (
                DateTimeSource.FILE_CREATION_DATE
                | DateTimeSource.FILE_MODIFICATION_DATE
                | DateTimeSource.CONTENT_CREATION_DATE
            ):
                use_fallback_dates = self._use_fallback_dates_value
                if self._use_fallback_custom_checkbox.get_current_value():
                    fallback_custom_date = self._use_fallback_date_str_value

        return DateTimeRenamePrepareCommand(
            position=self._datetime_position_value,
            date_format=self._date_format_value,
            time_format=self._time_format_value,
            datetime_format=self._datetime_format_value,
            datetime_source=self._datetime_source_value,
            use_uppercase=self._use_uppercase_value,
            custom_datetime=self._custom_datetime_value,
            separator_for_name_and_datetime=self._datetime_separator_value,
            use_fallback_dates=use_fallback_dates,
            use_fallback_date_str=fallback_custom_date,
        )

    @Slot()
    def handle_position_changed(self, value: ItemPositionWithReplacement):
        print(f"handle_position_changed: {value}")
        if value == ItemPositionWithReplacement.BEGIN or value == ItemPositionWithReplacement.END:
            self._datetime_separator.show()
            self._datetime_separator_value = self._datetime_separator.get_current_value()
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
        self.manage_displayed_widgets()

    @Slot()
    def handle_custom_datetime_changed(self, value: QDateTime):
        print(f"handle_custom_datetime_changed: {value}")
        value_to_string = value.toString("yyyyMMdd_hhmmss")
        self._custom_datetime_value = value_to_string

    @Slot()
    def handle_fallback_checkbox_changed(self, value: bool):
        print(f"handle_fallback_checkbox_changed: {value}")
        self._use_fallback_dates_value = value
        self.manage_displayed_widgets()

    @Slot()
    def handle_fallback_custom_checkbox_changed(self, value: bool):
        print(f"handle_fallback_custom_checkbox_changed: {value}")
        self.manage_displayed_widgets()

    @Slot()
    def handle_fallback_datetime_changed(self, value: QDateTime):
        print(f"handle_fallback_datetime_changed: {value}")
        value_to_string = value.toString("yyyyMMdd_hhmmss")
        self._use_fallback_date_str_value = value_to_string

    def manage_displayed_widgets(self):
        match self._datetime_source_combobox.get_current_value():
            case (
                DateTimeSource.FILE_CREATION_DATE
                | DateTimeSource.FILE_MODIFICATION_DATE
                | DateTimeSource.CONTENT_CREATION_DATE
            ):
                self._use_fallback_checkbox.show()
                self._custom_date_time_edit.hide()
                if self._use_fallback_checkbox.get_current_value():
                    self._use_fallback_custom_checkbox.show()
                else:
                    self._use_fallback_custom_checkbox.hide()
                if (
                    self._use_fallback_checkbox.get_current_value()
                    and self._use_fallback_custom_checkbox.get_current_value()
                ):
                    self._use_fallback_custom_datetime_edit.show()
                else:
                    self._use_fallback_custom_datetime_edit.hide()
            case DateTimeSource.CUSTOM_DATE:
                self._custom_date_time_edit.show()
                self._use_fallback_checkbox.hide()
                self._use_fallback_custom_checkbox.hide()
                self._use_fallback_custom_datetime_edit.hide()
            case DateTimeSource.CURRENT_DATE:
                self._custom_date_time_edit.hide()
                self._use_fallback_checkbox.hide()
                self._use_fallback_custom_checkbox.hide()
                self._use_fallback_custom_datetime_edit.hide()
