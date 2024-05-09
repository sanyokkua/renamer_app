class PassedArgumentIsNone(Exception):
    """
    Exception raised when a function or method receives a None argument but a value was expected.

    Attributes:
        message (str): The error message describing the exception.
    """

    def __init__(self, message="Passed argument is None but value was expected"):
        """
        Initializes the PassedArgumentIsNone exception.

        Args:
            message (str, optional): The error message describing the exception.
                Defaults to "Passed argument is None but value was expected".
        """
        self.message = message
        super().__init__(self.message)


class PathStringIsEmpty(Exception):
    """
    Exception raised when a path string is empty.

    Attributes:
        message (str): The error message describing the exception.
    """

    def __init__(self, message="Path is empty string"):
        """
        Initializes the PathStringIsEmpty exception.

        Args:
            message (str, optional): The error message describing the exception. Defaults to "Path is empty string".
        """
        self.message = message
        super().__init__(self.message)


class FileNotFoundException(Exception):
    """
    Exception raised when a file is not found in the file system.

    Attributes:
        message (str): The error message describing the exception.
    """

    def __init__(self, message="File not found in the File System", path: str = ""):
        """
        Initializes the FileNotFoundException exception.

        Args:
            message (str, optional): The error message describing the exception.
                Defaults to "File not found in the File System".
            path (str, optional): The path of the file. Defaults to "".
        """
        self.message = f"{message}. {path}"
        super().__init__(self.message)


class WidgetNotFoundException(Exception):
    """
    Exception raised when a widget is not found.

    Attributes:
        message (str): The error message describing the exception.
    """

    def __init__(self, message="Widget not found", widget: str = ""):
        """
        Initializes the WidgetNotFoundException exception.

        Args:
            message (str, optional): The error message describing the exception. Defaults to "Widget not found".
            widget (str, optional): The name or identifier of the widget. Defaults to "".
        """
        self.message = f"{message}. {widget}"
        super().__init__(self.message)
