import logging

from PySide6.QtCore import Slot
from PySide6.QtWidgets import QFrame

from core.commands.prep_date_time import DateTimeRenamePrepareCommand
from core.enums import (
    DateFormat,
    DateTimeFormat,
    DateTimeSource,
    ItemPositionWithReplacement,
    TimeFormat,
)
from core.text_values import (
    DATE_FORMAT_TEXT,
    DATE_TIME_FORMAT_TEXT,
    DATE_TIME_SOURCE_TEXT,
    ITEM_POSITION_WITH_REPLACEMENT_TEXT,
    TIME_FORMAT_TEXT,
)
from ui.widgets.customs.form_widgets import (
    ComboboxForm,
    DateTimeForm,
    LineTextEditForm,
    RadioButtonForm,
    RadioButtonItem,
    build_radio_button_items,
)
from ui.widgets.customs.single_widgets import (
    CheckBox,
    ComboBoxItem,
    build_combobox_items,
)
from ui.widgets.main.app_modes.mode_base_widget import ModeBaseWidget

log: logging.Logger = logging.getLogger(__name__)

POSITION_RADIO_BTN_ITEMS: list[RadioButtonItem] = build_radio_button_items(ITEM_POSITION_WITH_REPLACEMENT_TEXT)
DATE_FORMAT_CMB_ITEMS: list[ComboBoxItem] = build_combobox_items(DATE_FORMAT_TEXT)
TIME_FORMAT_CMB_ITEMS: list[ComboBoxItem] = build_combobox_items(TIME_FORMAT_TEXT)
DATETIME_FORMAT_CMB_ITEMS: list[ComboBoxItem] = build_combobox_items(DATE_TIME_FORMAT_TEXT)
SOURCE_CMB_ITEMS: list[ComboBoxItem] = build_combobox_items(DATE_TIME_SOURCE_TEXT)


class DateTimeWidget(ModeBaseWidget):
    _position_radio_btn: RadioButtonForm
    _datetime_separator: LineTextEditForm
    _position_line: QFrame
    _date_format_combobox: ComboboxForm
    _time_format_combobox: ComboboxForm
    _datetime_format_combobox: ComboboxForm
    _format_line: QFrame
    _datetime_source_combobox: ComboboxForm
    _use_fallback_checkbox: CheckBox
    _use_fallback_custom_checkbox: CheckBox
    _use_fallback_custom_datetime_edit: DateTimeForm
    _custom_date_time_edit: DateTimeForm
    _source_line: QFrame
    _use_uppercase_checkbox: CheckBox

    def init_widgets(self):
        self._position_radio_btn = RadioButtonForm(self)
        self._datetime_separator = LineTextEditForm(self)
        self._position_line = QFrame(self)
        self._date_format_combobox = ComboboxForm(self)
        self._time_format_combobox = ComboboxForm(self)
        self._datetime_format_combobox = ComboboxForm(self)
        self._format_line = QFrame(self)
        self._datetime_source_combobox = ComboboxForm(self)
        self._use_fallback_checkbox = CheckBox(self)
        self._use_fallback_custom_checkbox = CheckBox(self)
        self._use_fallback_custom_datetime_edit = DateTimeForm(self)
        self._custom_date_time_edit = DateTimeForm(self)
        self._source_line = QFrame(self)
        self._use_uppercase_checkbox = CheckBox(self)

    def configure_widgets(self):
        self._position_radio_btn.set_widget_items(POSITION_RADIO_BTN_ITEMS)
        self._date_format_combobox.set_widget_items(DATE_FORMAT_CMB_ITEMS)
        self._time_format_combobox.set_widget_items(TIME_FORMAT_CMB_ITEMS)
        self._datetime_format_combobox.set_widget_items(DATETIME_FORMAT_CMB_ITEMS)
        self._datetime_source_combobox.set_widget_items(SOURCE_CMB_ITEMS)

        self._position_line.setFrameShape(QFrame.Shape.HLine)
        self._format_line.setFrameShape(QFrame.Shape.HLine)
        self._source_line.setFrameShape(QFrame.Shape.HLine)

        self.add_widget(self._position_radio_btn)
        self.add_widget(self._datetime_separator)
        self.add_widget(self._position_line)
        self.add_widget(self._date_format_combobox)
        self.add_widget(self._time_format_combobox)
        self.add_widget(self._datetime_format_combobox)
        self.add_widget(self._format_line)
        self.add_widget(self._datetime_source_combobox)
        self.add_widget(self._use_fallback_checkbox)
        self.add_widget(self._use_fallback_custom_checkbox)
        self.add_widget(self._use_fallback_custom_datetime_edit)
        self.add_widget(self._custom_date_time_edit)
        self.add_widget(self._source_line)
        self.add_widget(self._use_uppercase_checkbox)

        self.manage_displayed_widgets()

    def add_text_to_widgets(self):
        self._position_radio_btn.set_widget_label("Mode:")
        self._datetime_separator.set_widget_label("DateTime and file name separator:")
        self._date_format_combobox.set_widget_label("Date format:")
        self._time_format_combobox.set_widget_label("Time format:")
        self._datetime_format_combobox.set_widget_label("Date/Time format:")
        self._datetime_source_combobox.set_widget_label("Time source:")
        self._use_fallback_checkbox.set_widget_label("Use fallback datetime (any other available date from metadata):")
        self._use_fallback_custom_checkbox.set_widget_label("Use Custom datetime as fallback date")
        self._use_uppercase_checkbox.set_widget_label("Use am/pm in Uppercase:")

    def create_event_handlers(self):
        self._position_radio_btn.valueIsChanged.connect(self.handle_position_changed)
        self._date_format_combobox.valueIsChanged.connect(self.handle_date_format_changed)
        self._time_format_combobox.valueIsChanged.connect(self.handle_time_format_changed)
        self._datetime_format_combobox.valueIsChanged.connect(self.handle_datetime_format_changed)
        self._use_uppercase_checkbox.valueIsChanged.connect(self.handle_use_uppercase_changed)
        self._datetime_source_combobox.valueIsChanged.connect(self.handle_source_changed)
        self._custom_date_time_edit.valueIsChanged.connect(self.handle_custom_datetime_changed)
        self._use_fallback_checkbox.valueIsChanged.connect(self.handle_fallback_checkbox_changed)
        self._use_fallback_custom_checkbox.valueIsChanged.connect(self.handle_fallback_custom_checkbox_changed)
        self._use_fallback_custom_datetime_edit.valueIsChanged.connect(self.handle_fallback_datetime_changed)

    def request_command(self) -> DateTimeRenamePrepareCommand:
        datetime_position = self._position_radio_btn.get_widget_value()
        datetime_filename_separator = self._datetime_separator.get_widget_value()
        date_format = self._date_format_combobox.get_widget_value()
        time_format = self._time_format_combobox.get_widget_value()
        datetime_format = self._datetime_format_combobox.get_widget_value()
        datetime_source = self._datetime_source_combobox.get_widget_value()
        use_fallback_date = self._use_fallback_checkbox.get_widget_value()
        use_fallback_date_custom = self._use_fallback_custom_checkbox.get_widget_value()
        use_fallback_date_custom_value = self._use_fallback_custom_datetime_edit.get_widget_value()
        custom_date_time_edit = self._custom_date_time_edit.get_widget_value()
        use_uppercase_checkbox = self._use_uppercase_checkbox.get_widget_value()

        if datetime_source != DateTimeSource.CUSTOM_DATE:
            custom_date_time_edit = ""
        elif datetime_source in [
            DateTimeSource.FILE_CREATION_DATE,
            DateTimeSource.FILE_MODIFICATION_DATE,
            DateTimeSource.CONTENT_CREATION_DATE,
        ] and (not use_fallback_date or not use_fallback_date_custom):
            use_fallback_date_custom_value = None

        return DateTimeRenamePrepareCommand(
            position=datetime_position,
            separator_for_name_and_datetime=datetime_filename_separator,
            date_format=date_format,
            time_format=time_format,
            datetime_format=datetime_format,
            datetime_source=datetime_source,
            use_uppercase=use_uppercase_checkbox,
            custom_datetime=custom_date_time_edit,
            use_fallback_dates=use_fallback_date,
            use_fallback_date_timestamp=use_fallback_date_custom_value,
        )

    @Slot()
    def handle_position_changed(self, value: ItemPositionWithReplacement):
        log.debug(f"handle_position_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_date_format_changed(self, value: DateFormat):
        log.debug(f"handle_date_format_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_time_format_changed(self, value: TimeFormat):
        log.debug(f"handle_time_format_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_datetime_format_changed(self, value: DateTimeFormat):
        log.debug(f"handle_datetime_format_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_use_uppercase_changed(self, value: bool):
        log.debug(f"handle_use_uppercase_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_source_changed(self, value: DateTimeSource):
        log.debug(f"handle_source_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_custom_datetime_changed(self, value: int):
        log.debug(f"handle_custom_datetime_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_fallback_checkbox_changed(self, value: bool):
        log.debug(f"handle_fallback_checkbox_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_fallback_custom_checkbox_changed(self, value: bool):
        log.debug(f"handle_fallback_custom_checkbox_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    @Slot()
    def handle_fallback_datetime_changed(self, value: int):
        log.debug(f"handle_fallback_datetime_changed: {value}")
        self.manage_displayed_widgets()
        self.tell_about_changes()

    def manage_displayed_widgets(self):
        self._datetime_separator.hide()
        self._use_fallback_checkbox.hide()
        self._use_fallback_custom_checkbox.hide()
        self._use_fallback_custom_datetime_edit.hide()
        self._use_uppercase_checkbox.hide()
        self._custom_date_time_edit.hide()

        position_radio_btn = self._position_radio_btn.get_widget_value()
        time_format_combobox = self._time_format_combobox.get_widget_value()
        datetime_source_combobox = self._datetime_source_combobox.get_widget_value()

        if position_radio_btn in [
            ItemPositionWithReplacement.BEGIN,
            ItemPositionWithReplacement.END,
        ]:
            self._datetime_separator.show()

        am_pm_time_formats = [
            TimeFormat.HH_MM_SS_AM_PM_TOGETHER,
            TimeFormat.HH_MM_SS_AM_PM_WHITE_SPACED,
            TimeFormat.HH_MM_SS_AM_PM_UNDERSCORED,
            TimeFormat.HH_MM_SS_AM_PM_DOTTED,
            TimeFormat.HH_MM_SS_AM_PM_DASHED,
            TimeFormat.HH_MM_AM_PM_TOGETHER,
            TimeFormat.HH_MM_AM_PM_WHITE_SPACED,
            TimeFormat.HH_MM_AM_PM_UNDERSCORED,
            TimeFormat.HH_MM_AM_PM_DOTTED,
            TimeFormat.HH_MM_AM_PM_DASHED,
        ]
        if time_format_combobox in am_pm_time_formats:
            self._use_uppercase_checkbox.show()

        fallback_src = [
            DateTimeSource.FILE_CREATION_DATE,
            DateTimeSource.FILE_MODIFICATION_DATE,
            DateTimeSource.CONTENT_CREATION_DATE,
        ]

        if datetime_source_combobox in fallback_src:
            self._use_fallback_checkbox.show()

        fallback_is_used = self._use_fallback_checkbox.get_widget_value()
        fallback_custom_is_used = self._use_fallback_custom_checkbox.get_widget_value()

        if datetime_source_combobox in fallback_src and fallback_is_used:
            self._use_fallback_custom_checkbox.show()

        if datetime_source_combobox in fallback_src and fallback_is_used and fallback_custom_is_used:
            self._use_fallback_custom_datetime_edit.show()

        if datetime_source_combobox == DateTimeSource.CUSTOM_DATE:
            self._custom_date_time_edit.show()
