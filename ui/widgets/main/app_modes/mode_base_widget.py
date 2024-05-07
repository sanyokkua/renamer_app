from abc import ABC, abstractmethod
from typing import TypeVar, Generic

from PySide6.QtCore import Signal
from PySide6.QtWidgets import QScrollArea, QVBoxLayout, QWidget

from core.abstract import Command
from ui.widgets.customs.abstracts import WidgetApi


class Meta(type(ABC), type(QScrollArea)):
    pass


class BaseScrollableWidget(QWidget, WidgetApi, ABC, metaclass=Meta):
    pass


T = TypeVar("T", bound=Command)


class ModeBaseWidget(BaseScrollableWidget, ABC, Generic[T]):
    _main_layout: QVBoxLayout
    _main_widget: QWidget
    widgetValuesAreChanged = Signal()

    def __init__(self, parent=None):
        super().__init__(parent)
        self.base_widget_init()
        self.init_widgets()
        self.configure_widgets()
        self.add_text_to_widgets()
        self.create_event_handlers()

    def base_widget_init(self):
        self._main_layout = QVBoxLayout()
        self._main_layout.setSpacing(0)

        self._main_widget = QWidget(self)
        self._main_widget.setLayout(self._main_layout)

        main_layout = QVBoxLayout(self)
        main_layout.addWidget(self._main_widget)
        self.setLayout(main_layout)
        self.setContentsMargins(0, 0, 0, 0)

        self._main_widget.setContentsMargins(0, 0, 0, 0)
        self._main_layout.setContentsMargins(0, 0, 0, 0)
        main_layout.setContentsMargins(0, 0, 0, 0)

    def add_widget(self, widget: QWidget):
        self._main_layout.addWidget(widget)

    def tell_about_changes(self):
        self.widgetValuesAreChanged.emit()

    @abstractmethod
    def request_command(self) -> T:
        pass
