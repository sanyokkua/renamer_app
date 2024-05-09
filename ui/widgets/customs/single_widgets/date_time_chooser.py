import logging
from abc import ABC

from PySide6.QtCore import QDateTime, Slot
from PySide6.QtWidgets import QDateTimeEdit

from ui.widgets.customs.abstracts import ValueWidgetApi

log: logging.Logger = logging.getLogger(__name__)


class Meta(type(ABC), type(QDateTimeEdit)):
    pass


class DateTimeChooser(QDateTimeEdit, ValueWidgetApi, metaclass=Meta):
    """
    Custom QDateTimeEdit widget with additional functionality.

    Inherits:
        QDateTimeEdit: Standard QDateTimeEdit widget.
        ValueWidgetApi: Custom widget for handling single values.

    Signals:
        valueIsChanged: Signal emitted when the selected value changes.

    Methods:
        __init__: Initializes the datetime chooser.
        _handle_event: Slot method to handle datetime changes.
        get_widget_value: Returns the currently selected timestamp.
        set_widget_value: Sets the currently selected timestamp.
    """

    def __init__(self, parent=None):
        """
        Initializes the DateTimeChooser widget.

        Args:
            parent: Optional parent widget.
        """
        super().__init__(parent)
        self.setDisplayFormat("dd/MM/yyyy - hh:mm:ss")
        self.setDateTime(QDateTime.currentDateTime())
        self.dateTimeChanged.connect(self._handle_event)

    @Slot()
    def _handle_event(self, value: QDateTime):
        """
        Slot method to handle datetime changes.
        Emits the valueIsChanged signal with the selected timestamp.

        Args:
            value (QDateTime): The selected datetime value.
        """
        log.debug(f"DateTimeChooser._handle_event, {value}")
        self.valueIsChanged.emit(value.toSecsSinceEpoch())

    def get_widget_value(self) -> int:
        """
        Returns the currently selected timestamp.

        Returns:
            int: The currently selected timestamp.
        """
        return self.dateTime().toSecsSinceEpoch()

    def set_widget_value(self, timestamp: int):
        """
        Sets the currently selected timestamp.

        Args:
            timestamp (int): The timestamp to be selected.
        """
        date_time = QDateTime()
        date_time.setSecsSinceEpoch(timestamp)
        self.setDateTime(date_time)
