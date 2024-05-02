from abc import ABC

from PySide6.QtCore import Slot, QDateTime
from PySide6.QtWidgets import QDateTimeEdit

from ui.customs.abstracts import ValueWidgetApi


class Meta(type(ABC), type(QDateTimeEdit)):
    pass


class DateTimeChooser(QDateTimeEdit, ValueWidgetApi, metaclass=Meta):

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setDisplayFormat("dd/MM/yyyy - hh:mm:ss")
        self.setDateTime(QDateTime.currentDateTime())
        self.dateTimeChanged.connect(self._handle_event)

    @Slot()
    def _handle_event(self, value: QDateTime):
        print(f"_handle_event, {value}")
        self.valueIsChanged.emit(value.toSecsSinceEpoch())

    def get_widget_value(self) -> int:
        return self.dateTime().toSecsSinceEpoch()

    def set_widget_value(self, timestamp: int):
        date_time = QDateTime()
        date_time.setSecsSinceEpoch(timestamp)
        self.setDateTime(date_time)
