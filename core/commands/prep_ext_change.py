from core.commons import BasePrepareCommand
from core.models.app_file import AppFile


class ExtensionChangePrepareCommand(BasePrepareCommand):
    def __init__(self, new_extension: str = ""):
        self.new_extension: str = new_extension

    def create_new_name(self, item: AppFile) -> AppFile:
        return item

# TODO: add implementation
