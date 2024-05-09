import logging
from datetime import datetime

from PySide6.QtWidgets import QTextEdit, QVBoxLayout

from core.models.app_file import AppFile
from ui.widgets.customs import BaseWidget

log: logging.Logger = logging.getLogger(__name__)


class FileInfoWidget(BaseWidget):
    """
    Widget to display information about an AppFile.

    Inherits from BaseWidget.

    Attributes:
        _main_layout (QVBoxLayout): The main layout of the widget.
        _text_edit (QTextEdit): The text edit widget to display file information.
    """

    _main_layout: QVBoxLayout
    _text_edit: QTextEdit

    def init_widgets(self):
        """
        Initialize widgets.

        Creates the main layout and the text edit widget.
        """
        self._main_layout = QVBoxLayout(self)
        self._text_edit = QTextEdit(self)

    def configure_widgets(self):
        """
        Configure widgets.

        Sets the layout, adds the text edit widget to the layout, and sets properties for the widget.
        """
        self.setLayout(self._main_layout)
        self._main_layout.addWidget(self._text_edit)
        self.setMaximumHeight(150)
        self.setContentsMargins(0, 0, 0, 0)
        self._main_layout.setContentsMargins(0, 0, 0, 0)

    def add_text_to_widgets(self):
        """
        Add text to widgets.

        This method is not implemented for this widget.
        """
        pass

    def create_event_handlers(self):
        """
        Create event handlers.

        This method is not implemented for this widget.
        """
        pass

    def set_file(self, file: AppFile | None):
        """
        Set the AppFile to display information.

        Args:
            file (AppFile | None): The AppFile object containing information to display.
        """
        text = build_app_file_info(file)
        self._text_edit.setHtml(text)


def build_app_file_info(app_file: AppFile | None) -> str:
    """
    Build HTML string containing information about the AppFile.

    Args:
        app_file (AppFile | None): The AppFile object containing information.

    Returns:
        str: HTML string containing information about the AppFile.
    """
    if app_file is None:
        return ""
    html_begin = "<!DOCTYPE html><html><head><title></title></head><body>"
    html_end = "</body></html>"

    file_cr_date = datetime.fromtimestamp(app_file.fs_creation_date)
    file_mod_date = datetime.fromtimestamp(app_file.fs_modification_date)
    file_cc_date = None
    if app_file.metadata is not None and app_file.metadata.creation_date is not None:
        file_cc_date = datetime.fromtimestamp(app_file.metadata.creation_date)

    datetime_format = "%Y-%m-%dT%H:%M:%S"
    file_cr_date_text = file_cr_date.strftime(datetime_format)
    file_mod_date_text = file_mod_date.strftime(datetime_format)
    file_cc_date_text = file_cc_date.strftime(datetime_format) if file_cc_date is not None else ""

    file_type = "Folder" if app_file.is_folder else "File"
    file_size = app_file.file_size

    img_width = None
    img_height = None

    if (
        app_file.metadata is not None
        and app_file.metadata.img_vid_width is not None
        and app_file.metadata.img_vid_height is not None
    ):
        img_width = app_file.metadata.img_vid_width
        img_height = app_file.metadata.img_vid_height

    items = [
        f"<h3>{app_file.file_name}</h3>",
        f"<div>File path: {app_file.absolute_path}</div>",
        f"<div>File type: {file_type}</div>",
        (f"<div>File extension: <b>{app_file.file_extension}</b></div>" if not app_file.is_folder else ""),
        f"<div>File creation date: {file_cr_date_text}</div>",
        f"<div>File modification date: {file_mod_date_text}</div>",
        f"<div>File content creation date: {file_cc_date_text}</div>",
        f"<div>File size: {file_size} bytes</div>",
        f"<div>File width: {img_width}</div>" if img_width is not None else "",
        f"<div>File height: {img_height}</div>" if img_height is not None else "",
    ]

    if app_file.metadata is not None and app_file.metadata.other_found_tag_values is not None:
        tags = app_file.metadata.other_found_tag_values.items()
        for key, value in tags:
            items.append(f"<div>{key}={value}</div>")

    body = "\n".join(items)
    return f"{html_begin}\n{body}\n{html_end}"
