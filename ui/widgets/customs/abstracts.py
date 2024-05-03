from abc import ABC, abstractmethod

from PySide6.QtCore import Signal


class WidgetApi(ABC):

    @abstractmethod
    def init_widgets(self):
        pass

    @abstractmethod
    def configure_widgets(self):
        pass

    @abstractmethod
    def add_text_to_widgets(self):
        pass

    @abstractmethod
    def create_event_handlers(self):
        pass


class ValueWidgetApi(ABC):
    valueIsChanged = Signal(object)

    @abstractmethod
    def get_widget_value(self):
        pass

    @abstractmethod
    def set_widget_value(self, value):
        pass


class ValueWidgetWithLabelApi(ValueWidgetApi):

    @abstractmethod
    def set_widget_label(self, label_text: str):
        pass


class MultipleValueWidgetApi(ValueWidgetApi):

    @abstractmethod
    def set_widget_items(self, values: list):
        pass

    @abstractmethod
    def get_widget_items(self) -> list:
        pass


class MultipleValueWidgetWithLabelApi(MultipleValueWidgetApi, ValueWidgetWithLabelApi, ABC):
    pass
