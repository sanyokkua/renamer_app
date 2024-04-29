class PassedArgumentIsNone(Exception):
    def __init__(self, message="Passed argument is None but value was expected"):
        self.message = message
        super().__init__(self.message)


class PathStringIsEmpty(Exception):
    def __init__(self, message="Path is empty string"):
        self.message = message
        super().__init__(self.message)


class FileNotFoundException(Exception):
    def __init__(self, message="File not found in the File System", path: str = ""):
        self.message = f"{message}. {path}"
        super().__init__(self.message)


class WidgetNotFoundException(Exception):
    def __init__(self, message="Widget not found", widget: str = ""):
        self.message = f"{message}. {widget}"
        super().__init__(self.message)


class CaseIsNotSupported(Exception):
    def __init__(
            self, message="Case is not found. Check if all possible cases are handled."
    ):
        self.message = message
        super().__init__(self.message)


class SortSourceException(Exception):
    def __init__(
            self,
            message="Sort source is not found. Check if all possible sources are handled.",
    ):
        self.message = message
        super().__init__(self.message)


class DateTimeFormatNotSupported(Exception):
    def __init__(self, message="An error occurred"):
        self.message = message
        super().__init__(self.message)
