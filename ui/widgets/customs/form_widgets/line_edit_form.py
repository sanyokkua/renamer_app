import logging

from PySide6.QtCore import Qt, Signal
from PySide6.QtWidgets import QFormLayout, QLabel

from ui.widgets.customs.base_widget import BaseValueWidgetWithLabelApi
from ui.widgets.customs.single_widgets import LineTextEdit

log: logging.Logger = logging.getLogger(__name__)


class LineTextEditForm(BaseValueWidgetWithLabelApi):
    """
    Custom form widget containing a QLabel and a LineTextEdit.

    Inherits:
        BaseValueWidgetWithLabelApi: Base class for single value widgets with a label.

    Signals:
        valueIsChanged: Signal emitted when the value changes.

    Methods:
        init_widgets: Initializes the form widgets.
        configure_widgets: Configures the layout and alignment of the widgets.
        add_text_to_widgets: Adds text to the widgets.
        create_event_handlers: Creates event handlers for the widgets.
        set_widget_value: Sets the value of the LineTextEdit.
        get_widget_value: Retrieves the value of the LineTextEdit.
        set_widget_label: Sets the label text for the form.
    """

    _line_text_edit: LineTextEdit
    _label: QLabel
    _layout: QFormLayout

    valueIsChanged = Signal(object)

    def init_widgets(self):
        """
        Initializes the form widgets.
        """
        self._layout = QFormLayout(self)
        self._label = QLabel(self)
        self._line_text_edit = LineTextEdit(self)

    def configure_widgets(self):
        """
        Configures the layout and alignment of the widgets.
        """
        self._layout.setWidget(0, QFormLayout.ItemRole.LabelRole, self._label)
        self._layout.setWidget(0, QFormLayout.ItemRole.FieldRole, self._line_text_edit)
        self._layout.setFormAlignment(Qt.AlignmentFlag.AlignLeft)
        self.setLayout(self._layout)
        self.setMaximumHeight(50)

    def add_text_to_widgets(self):
        pass

    def create_event_handlers(self):
        self._line_text_edit.valueIsChanged.connect(lambda x: self.valueIsChanged.emit(x))

    def set_widget_value(self, value: str):
        """
        Sets the value of the LineTextEdit.

        Args:
            value: The value to set.
        """
        self._line_text_edit.set_widget_value(value)

    def get_widget_value(self) -> str:
        """
        Retrieves the value of the LineTextEdit.

        Returns:
            The value of the LineTextEdit.
        """
        return self._line_text_edit.get_widget_value()

    def set_widget_label(self, label_text: str):
        """
        Sets the label text for the form.

        Args:
            label_text (str): The text to set as the label.
        """
        self._label.setText(self.tr(label_text))
