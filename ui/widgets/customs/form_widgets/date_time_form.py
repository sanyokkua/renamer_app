from PySide6.QtCore import Signal, Qt
from PySide6.QtWidgets import QLabel, QFormLayout

from ui.widgets.customs.base_widget import BaseValueWidgetWithLabelApi
from ui.widgets.customs.single_widgets import DateTimeChooser


class DateTimeForm(BaseValueWidgetWithLabelApi):
    _date_time_chooser: DateTimeChooser
    _label: QLabel
    _layout: QFormLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        self._layout = QFormLayout(self)
        self._label = QLabel(self)
        self._date_time_chooser = DateTimeChooser(self)

    def configure_widgets(self):
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
        self._date_time_chooser.set_widget_value(timestamp)

    def get_widget_value(self) -> int:
        return self._date_time_chooser.get_widget_value()

    def set_widget_label(self, label_text: str):
        self._label.setText(self.tr(label_text))
