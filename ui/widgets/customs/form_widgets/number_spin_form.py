import logging

from PySide6.QtCore import Qt, Signal
from PySide6.QtWidgets import QFormLayout, QLabel

from ui.widgets.customs.base_widget import BaseValueWidgetWithLabelApi
from ui.widgets.customs.single_widgets import NumberSpin

log: logging.Logger = logging.getLogger(__name__)


class NumberSpinForm(BaseValueWidgetWithLabelApi):
    """
    Custom form widget containing a QLabel and a NumberSpin.

    Inherits:
        BaseValueWidgetWithLabelApi: Base class for single value widgets with a label.

    Signals:
        valueIsChanged: Signal emitted when the value changes.

    Methods:
        init_widgets: Initializes the form widgets.
        configure_widgets: Configures the layout and alignment of the widgets.
        add_text_to_widgets: Adds text to the widgets.
        create_event_handlers: Creates event handlers for the widgets.
        set_widget_value: Sets the value of the NumberSpin.
        get_widget_value: Retrieves the value of the NumberSpin.
        set_widget_label: Sets the label text for the form.
        minimum_value: Property to get or set the minimum value of the NumberSpin.
        maximum_value: Property to get or set the maximum value of the NumberSpin.
    """

    _number_spin_edit: NumberSpin
    _label: QLabel
    _layout: QFormLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        """
        Initializes the form widgets.
        """
        self._layout = QFormLayout(self)
        self._label = QLabel(self)
        self._number_spin_edit = NumberSpin(self)

    def configure_widgets(self):
        """
        Configures the layout and alignment of the widgets.
        """
        self._layout.setWidget(0, QFormLayout.ItemRole.LabelRole, self._label)
        self._layout.setWidget(0, QFormLayout.ItemRole.FieldRole, self._number_spin_edit)
        self._layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self.setLayout(self._layout)
        self.setMaximumHeight(50)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._number_spin_edit.valueIsChanged.connect(lambda x: self.valueIsChanged.emit(x))

    def set_widget_value(self, value: int):
        """
        Sets the value of the NumberSpin.

        Args:
            value (int): The value to set.
        """
        self._number_spin_edit.set_widget_value(value)

    def get_widget_value(self) -> int:
        """
        Retrieves the value of the NumberSpin.

        Returns:
            int: The value of the NumberSpin.
        """
        return self._number_spin_edit.get_widget_value()

    def set_widget_label(self, label_text: str):
        """
        Sets the label text for the form.

        Args:
            label_text (str): The text to set as the label.
        """
        self._label.setText(self.tr(label_text))

    @property
    def minimum_value(self) -> int:
        """
        Property to get or set the minimum value of the NumberSpin.

        Returns:
            int: The minimum value of the NumberSpin.
        """
        return self._number_spin_edit.minimum_value

    @minimum_value.setter
    def minimum_value(self, value: int):
        """
        Property setter to set the minimum value of the NumberSpin.

        Args:
            value (int): The value to set as the minimum.
        """
        self._number_spin_edit.minimum_value = value

    @property
    def maximum_value(self) -> int:
        """
        Property to get or set the maximum value of the NumberSpin.

        Returns:
            int: The maximum value of the NumberSpin.
        """
        return self._number_spin_edit.maximum_value

    @maximum_value.setter
    def maximum_value(self, value: int):
        """
        Property setter to set the maximum value of the NumberSpin.

        Args:
            value (int): The value to set as the maximum.
        """
        self._number_spin_edit.maximum_value = value
