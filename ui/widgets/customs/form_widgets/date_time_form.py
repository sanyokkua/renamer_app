import logging

from PySide6.QtCore import Qt, Signal
from PySide6.QtWidgets import QFormLayout, QLabel

from ui.widgets.customs.base_widget import BaseValueWidgetWithLabelApi
from ui.widgets.customs.single_widgets import DateTimeChooser

log: logging.Logger = logging.getLogger(__name__)


class DateTimeForm(BaseValueWidgetWithLabelApi):
    """
    Custom form widget containing a QLabel and a DateTimeChooser.

    Inherits:
        BaseValueWidgetWithLabelApi: Base class for single value widgets with a label.

    Signals:
        valueIsChanged: Signal emitted when the value changes.

    Methods:
        init_widgets: Initializes the form widgets.
        configure_widgets: Configures the layout and alignment of the widgets.
        add_text_to_widgets: Adds text to the widgets.
        create_event_handlers: Creates event handlers for the widgets.
        set_widget_value: Sets the value of the DateTimeChooser.
        get_widget_value: Retrieves the value of the DateTimeChooser.
        set_widget_label: Sets the label text for the form.
    """

    _date_time_chooser: DateTimeChooser
    _label: QLabel
    _layout: QFormLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        """
        Initializes the form widgets.
        """
        self._layout = QFormLayout(self)
        self._label = QLabel(self)
        self._date_time_chooser = DateTimeChooser(self)

    def configure_widgets(self):
        """
        Configures the layout and alignment of the widgets.
        """
        self._layout.setWidget(0, QFormLayout.ItemRole.LabelRole, self._label)
        self._layout.setWidget(0, QFormLayout.ItemRole.FieldRole, self._date_time_chooser)
        self._layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self.setLayout(self._layout)
        self.setMaximumHeight(50)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._date_time_chooser.valueIsChanged.connect(lambda x: self.valueIsChanged.emit(x))

    def set_widget_value(self, timestamp: int):
        """
        Sets the value of the DateTimeChooser.

        Args:
            timestamp (int): The timestamp to set.
        """
        self._date_time_chooser.set_widget_value(timestamp)

    def get_widget_value(self) -> int:
        """
        Retrieves the value of the DateTimeChooser.

        Returns:
            int: The timestamp value.
        """
        return self._date_time_chooser.get_widget_value()

    def set_widget_label(self, label_text: str):
        """
        Sets the label text for the form.

        Args:
            label_text (str): The text to set as the label.
        """
        self._label.setText(self.tr(label_text))
