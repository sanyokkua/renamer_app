from abc import ABC

from PySide6.QtWidgets import QWidget

from ui.widgets.customs.abstracts import WidgetApi, MultipleValueWidgetWithLabelApi, ValueWidgetWithLabelApi


class Meta(type(ABC), type(QWidget)):
    pass


class BaseWidget(QWidget, WidgetApi, ABC, metaclass=Meta):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.init_widgets()
        self.configure_widgets()
        self.add_text_to_widgets()
        self.create_event_handlers()


class BaseMultipleValueWidgetApi(BaseWidget, MultipleValueWidgetWithLabelApi, ABC):
    pass


class BaseValueWidgetWithLabelApi(BaseWidget, ValueWidgetWithLabelApi, ABC):
    pass
