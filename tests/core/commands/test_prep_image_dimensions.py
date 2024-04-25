from unittest import mock
from unittest.mock import MagicMock

import pytest

from core.enums import ItemPositionWithReplacement, ImageDimensionOptions
from core.models.app_file import AppFile
from tests.core.commands.test_commons import build_app_file, verify_command_result


def test_command_with_none_arguments():
    from core.exceptions import PassedArgumentIsNone
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand
    test_command = ImageDimensionsPrepareCommand(
        position=ItemPositionWithReplacement.BEGIN,
        left_side=ImageDimensionOptions.WIDTH,
        right_side=ImageDimensionOptions.HEIGHT,
        dimension_separator="x",
        name_separator="_",
    )
    with pytest.raises(PassedArgumentIsNone):
        test_command.execute(None, None)


def test_command_with_empty_arguments():
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand
    test_command = ImageDimensionsPrepareCommand(
        position=ItemPositionWithReplacement.BEGIN,
        left_side=ImageDimensionOptions.WIDTH,
        right_side=ImageDimensionOptions.HEIGHT,
        dimension_separator="x",
        name_separator="_",
    )

    result = test_command.execute([], None)

    assert result == []


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand
    test_command = ImageDimensionsPrepareCommand(
        position=ItemPositionWithReplacement.BEGIN,
        left_side=ImageDimensionOptions.WIDTH,
        right_side=ImageDimensionOptions.HEIGHT,
        dimension_separator="x",
        name_separator="_",
    )

    with pytest.raises(TypeError):
        test_command.execute("string", None)


def test_command_call_callback():
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand
    test_command = ImageDimensionsPrepareCommand(
        position=ItemPositionWithReplacement.BEGIN,
        left_side=ImageDimensionOptions.WIDTH,
        right_side=ImageDimensionOptions.HEIGHT,
        dimension_separator="x",
        name_separator="_",
    )

    mock_function = MagicMock()

    file1 = build_app_file("file_name_1")
    file2 = build_app_file("file_name_2", ".png")

    files = [file1, file2]

    result: list[AppFile] = test_command.execute(files, mock_function)

    assert len(result) == 2

    assert mock_function.call_count == 4
    mock_function.assert_has_calls([mock.call(0, 2, 0),
                                    mock.call(0, 2, 1),
                                    mock.call(0, 2, 2),
                                    mock.call(0, 100, 0)])


@pytest.mark.parametrize("position, left, right, dim_sep, name_sep, width, height, file_name, exp_name", [
    (ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "x", "_", 1080, 720,
     "TEST", "1080x720_TEST"),
    (ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "-", "_", 1000, 2000,
     "TEST", "1000-2000_TEST"),
    (ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, " ", "_", 0, 0,
     "TEST", "0 0_TEST"),
    (ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "x", "_", -10, -5,
     "TEST", "10x5_TEST"),

    (ItemPositionWithReplacement.END, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "x", "_", 1080, 720,
     "TEST", "TEST_1080x720"),
    (ItemPositionWithReplacement.END, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "x", " ", 1080, 720,
     "TEST", "TEST 1080x720"),
    (ItemPositionWithReplacement.END, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, " ", " ", 1080, 720,
     "TEST", "TEST 1080 720"),
    (ItemPositionWithReplacement.END, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, " TO ", " Dim ", 1080,
     720, "TEST", "TEST Dim 1080 TO 720"),

    (
            ItemPositionWithReplacement.REPLACE, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "x", "_",
            1080, 720,
            "TEST", "1080x720"),
    (
            ItemPositionWithReplacement.REPLACE, ImageDimensionOptions.HEIGHT, ImageDimensionOptions.WIDTH, "x", "_",
            1080, 720,
            "TEST", "720x1080"),

    (ItemPositionWithReplacement.REPLACE, ImageDimensionOptions.WIDTH, ImageDimensionOptions.WIDTH, "x", "_", 1080, 720,
     "TEST", "1080x1080"),
    (ItemPositionWithReplacement.REPLACE, ImageDimensionOptions.HEIGHT, ImageDimensionOptions.HEIGHT, "x", "_", 1080,
     720, "TEST", "720x720"),

    (ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "", "_", 1080, 720,
     "TEST", "1080720_TEST"),
    (ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "x", "", 1080, 720,
     "TEST", "1080x720TEST"),
    (ItemPositionWithReplacement.BEGIN, ImageDimensionOptions.WIDTH, ImageDimensionOptions.HEIGHT, "", "", 1080, 720,
     "TEST", "1080720TEST"),
])
def test_command_with_combination_of_params(position: ItemPositionWithReplacement, left: ImageDimensionOptions,
                                            right: ImageDimensionOptions, dim_sep: str, name_sep: str, width: int,
                                            height: int,
                                            file_name: str,
                                            exp_name: str):
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand

    test_command = ImageDimensionsPrepareCommand(position=position, left_side=left, right_side=right,
                                                 dimension_separator=dim_sep, name_separator=name_sep)

    verify_command_result(test_command, file_name, exp_name, img_width=width, img_height=height)
