from PySide6.QtCore import Signal, Qt
from PySide6.QtWidgets import QLabel, QFormLayout

from ui.customs.qt_widgets import NumberSpin
from ui.customs.qt_widgets.base_widget import BaseValueWidgetWithLabelApi


class NumberSpinForm(BaseValueWidgetWithLabelApi):
    _number_spin_edit: NumberSpin
    _label: QLabel
    _layout: QFormLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        self._layout = QFormLayout(self)
        self._label = QLabel(self)
        self._number_spin_edit = NumberSpin(self)

    def configure_widgets(self):
        self._layout.setWidget(0, QFormLayout.ItemRole.LabelRole, self._label)
        self._layout.setWidget(0, QFormLayout.ItemRole.FieldRole, self._number_spin_edit)
        self._layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self.setLayout(self._layout)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._number_spin_edit.valueIsChanged.connect(lambda x: self.valueIsChanged.emit(x))

    def set_widget_value(self, value: int):
        self._number_spin_edit.set_widget_value(value)

    def get_widget_value(self) -> int:
        return self._number_spin_edit.get_widget_value()

    def set_widget_label(self, label_text: str):
        self._label.setText(self.tr(label_text))
