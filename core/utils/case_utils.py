from typing import Callable

from core.enums import TextCaseOptions


def to_camel_case(input_string: str) -> str:
    """
    Convert a string to CamelCase.

    This function replaces underscores, hyphens, and periods with spaces,
    splits the string into words, and capitalizes the first letter of each
    word except for the first one. Then it joins the words into a single
    string.

    Args:
        input_string (str): The input string.

    Returns:
        str: The string converted to camelCase. If the input string
        is None, an empty string is returned.
    """
    if (
            input_string is None
            or not isinstance(input_string, str)
            or len(input_string.strip()) == 0
    ):
        return ""
    replace_symbols = input_string.replace("_", " ")
    replace_symbols = replace_symbols.replace("-", " ")
    replace_symbols = replace_symbols.replace(".", " ")
    all_words = replace_symbols.split(" ")
    replace_symbols = " ".join([word for word in all_words if len(word.strip()) > 0])
    words = replace_symbols.split()

    camel_case_words = [words[0].lower()] + [word.capitalize() for word in words[1:]]

    camel_case_string = "".join(camel_case_words)

    return camel_case_string


def to_pascal_case(input_string: str) -> str:
    """
    Convert a string to PascalCase.

    This function replaces underscores, hyphens, and periods with spaces,
    splits the string into words, and capitalizes the first letter of each
    word. Then it joins the words into a single string.

    Args:
        input_string (str): The input string.

    Returns:
        str: The string converted to PascalCase. If the input string
        is None, an empty string is returned.
    """
    if (
            input_string is None
            or not isinstance(input_string, str)
            or len(input_string.strip()) == 0
    ):
        return ""

    replace_symbols = input_string.replace("_", " ")
    replace_symbols = replace_symbols.replace("-", " ")
    replace_symbols = replace_symbols.replace(".", " ")
    all_words = replace_symbols.split(" ")
    replace_symbols = " ".join([word for word in all_words if len(word.strip()) > 0])
    words = replace_symbols.split()

    pascal_case_words = [word.capitalize() for word in words]

    pascal_case_string = "".join(pascal_case_words)

    return pascal_case_string


def to_snake_case(input_string: str) -> str:
    """
    Convert a string to snake_case.

    This function replaces hyphens and periods with spaces, removes leading and
    trailing whitespaces, and then replaces spaces with underscores. Finally,
    it converts the string to lowercase.

    Args:
        input_string (str): The input string.

    Returns:
        str: The string converted to snake_case. If the input string is None,
        an empty string is returned.
    """
    if (
            input_string is None
            or not isinstance(input_string, str)
            or len(input_string.strip()) == 0
    ):
        return ""

    replace_symbols = input_string.replace("-", " ")
    replace_symbols = replace_symbols.replace(".", " ")
    all_words = replace_symbols.split(" ")
    replace_symbols = " ".join([word for word in all_words if len(word.strip()) > 0])
    snake_case_string = replace_symbols.replace(" ", "_")

    snake_case_string = snake_case_string.lower()
    return snake_case_string


def to_screaming_snake_case(input_string: str) -> str:
    """
    Convert a string to SCREAMING_SNAKE_CASE.

    This function replaces hyphens and periods with spaces, removes leading and
    trailing whitespaces, and then replaces spaces with underscores. Finally,
    it converts the string to uppercase.

    Args:
        input_string (str): The input string.

    Returns:
        str: The string converted to SCREAMING_SNAKE_CASE. If the input string
        is None, an empty string is returned.
    """
    if (
            input_string is None
            or not isinstance(input_string, str)
            or len(input_string.strip()) == 0
    ):
        return ""

    replace_symbols = input_string.replace("-", " ")
    replace_symbols = replace_symbols.replace(".", " ")
    all_words = replace_symbols.split(" ")
    replace_symbols = " ".join([word for word in all_words if len(word.strip()) > 0])

    snake_case_string = replace_symbols.replace(" ", "_")

    snake_case_string = snake_case_string.upper()
    return snake_case_string


def to_kebab_case(input_string: str) -> str:
    """
    Convert a string to kebab-case.

    This function replaces underscores with hyphens, and converts the string
    to lowercase.

    Args:
        input_string (str): The input string.

    Returns:
        str: The string converted to kebab-case. If the input string
        is None, an empty string is returned.
    """
    if (
            input_string is None
            or not isinstance(input_string, str)
            or len(input_string.strip()) == 0
    ):
        return ""

    replace_symbols = input_string.replace("_", " ")
    replace_symbols = replace_symbols.replace(".", " ")
    all_words = replace_symbols.split(" ")
    replace_symbols = " ".join([word for word in all_words if len(word.strip()) > 0])

    kebab_case_string = replace_symbols.replace(" ", "-")

    kebab_case_string = kebab_case_string.lower()
    return kebab_case_string


def to_uppercase(input_string: str) -> str:
    """
    Convert a string to UPPERCASE.

    This function converts the string to uppercase.

    Args:
        input_string (str): The input string.

    Returns:
        str: The string converted to UPPERCASE. If the input string
        is None, an empty string is returned.
    """
    if (
            input_string is None
            or not isinstance(input_string, str)
            or len(input_string.strip()) == 0
    ):
        return ""

    uppercase_string = input_string.upper()
    return uppercase_string


def to_lowercase(input_string: str) -> str:
    """
    Convert a string to lowercase.

    This function converts the string to lowercase.

    Args:
        input_string (str): The input string.

    Returns:
        str: The string converted to lowercase. If the input string
        is None, an empty string is returned.
    """
    if (
            input_string is None
            or not isinstance(input_string, str)
            or len(input_string.strip()) == 0
    ):
        return ""

    lowercase_string = input_string.lower()
    return lowercase_string


def to_title_case(input_string):
    """
    Convert a string to Title Case.

    This function replaces underscores, hyphens, and periods with spaces,
    and then converts the string to title case.

    Args:
        input_string (str): The input string.

    Returns:
        str: The string converted to Title Case. If the input string
        is None, an empty string is returned.
    """
    if (
            input_string is None
            or not isinstance(input_string, str)
            or len(input_string.strip()) == 0
    ):
        return ""

    replace_symbols = input_string.replace("_", " ")
    replace_symbols = replace_symbols.replace("-", " ")
    replace_symbols = replace_symbols.replace(".", " ")
    all_words = replace_symbols.split(" ")
    replace_symbols = " ".join([word for word in all_words if len(word.strip()) > 0])

    title_case_string = replace_symbols.title()
    return title_case_string


CASE_FUNC_MAPPING: dict[TextCaseOptions, Callable[[str], str]] = {
    TextCaseOptions.CAMEL_CASE: to_camel_case,
    TextCaseOptions.PASCAL_CASE: to_pascal_case,
    TextCaseOptions.SNAKE_CASE: to_snake_case,
    TextCaseOptions.SNAKE_CASE_SCREAMING: to_screaming_snake_case,
    TextCaseOptions.KEBAB_CASE: to_kebab_case,
    TextCaseOptions.UPPERCASE: to_uppercase,
    TextCaseOptions.LOWERCASE: to_lowercase,
    TextCaseOptions.TITLE_CASE: to_title_case,
}


def convert_case_of_string(text: str, case_opt: TextCaseOptions) -> str:
    """
    Convert the case of a string based on the specified TextCaseOptions.

    Args:
        text (str): The input string.
        case_opt (TextCaseOptions): The desired case conversion option.

    Returns:
        str: The string converted to the specified case. If the input string
        is None, an empty string is returned.
    """
    case_conv_func = CASE_FUNC_MAPPING.get(case_opt)
    return case_conv_func(text)
