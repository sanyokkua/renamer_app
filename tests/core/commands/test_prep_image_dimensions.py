import pytest

import tests.core.commands.test_base_command_tests as base
from core.enums import ImageDimensionOptions, ItemPositionWithReplacement
from tests.core.commands.test_commons import verify_command_result


def test_command_with_none_arguments():
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand

    test_command = ImageDimensionsPrepareCommand(
        position=ItemPositionWithReplacement.BEGIN,
        left_side=ImageDimensionOptions.WIDTH,
        right_side=ImageDimensionOptions.HEIGHT,
        dimension_separator="x",
        name_separator="_",
    )

    base.command_validates_none_input(test_command)


def test_command_with_empty_arguments():
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand

    test_command = ImageDimensionsPrepareCommand(
        position=ItemPositionWithReplacement.BEGIN,
        left_side=ImageDimensionOptions.WIDTH,
        right_side=ImageDimensionOptions.HEIGHT,
        dimension_separator="x",
        name_separator="_",
    )

    base.command_returns_empty_array_on_empty_input(test_command)


def test_command_with_incorrect_data_type_arguments():
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand

    test_command = ImageDimensionsPrepareCommand(
        position=ItemPositionWithReplacement.BEGIN,
        left_side=ImageDimensionOptions.WIDTH,
        right_side=ImageDimensionOptions.HEIGHT,
        dimension_separator="x",
        name_separator="_",
    )

    base.command_validates_data_input_type(test_command)


def test_command_call_callback():
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand

    test_command = ImageDimensionsPrepareCommand(
        position=ItemPositionWithReplacement.BEGIN,
        left_side=ImageDimensionOptions.WIDTH,
        right_side=ImageDimensionOptions.HEIGHT,
        dimension_separator="x",
        name_separator="_",
    )

    base.command_calls_callback_in_each_stage(test_command)


@pytest.mark.parametrize(
    "position, left, right, dim_sep, name_sep, width, height, file_name, exp_name, changed",
    [
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "x",
            "_",
            1080,
            720,
            "TEST",
            "1080x720_TEST",
            True,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "-",
            "_",
            1000,
            2000,
            "TEST",
            "1000-2000_TEST",
            True,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            " ",
            "_",
            0,
            0,
            "TEST",
            "0 0_TEST",
            True,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "x",
            "_",
            -10,
            -5,
            "TEST",
            "10x5_TEST",
            True,
        ),
        (
            ItemPositionWithReplacement.END,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "x",
            "_",
            1080,
            720,
            "TEST",
            "TEST_1080x720",
            True,
        ),
        (
            ItemPositionWithReplacement.END,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "x",
            " ",
            1080,
            720,
            "TEST",
            "TEST 1080x720",
            True,
        ),
        (
            ItemPositionWithReplacement.END,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            " ",
            " ",
            1080,
            720,
            "TEST",
            "TEST 1080 720",
            True,
        ),
        (
            ItemPositionWithReplacement.END,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            " TO ",
            " Dim ",
            1080,
            720,
            "TEST",
            "TEST Dim 1080 TO 720",
            True,
        ),
        (
            ItemPositionWithReplacement.REPLACE,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "x",
            "_",
            1080,
            720,
            "TEST",
            "1080x720",
            True,
        ),
        (
            ItemPositionWithReplacement.REPLACE,
            ImageDimensionOptions.HEIGHT,
            ImageDimensionOptions.WIDTH,
            "x",
            "_",
            1080,
            720,
            "TEST",
            "720x1080",
            True,
        ),
        (
            ItemPositionWithReplacement.REPLACE,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.WIDTH,
            "x",
            "_",
            1080,
            720,
            "TEST",
            "1080x1080",
            True,
        ),
        (
            ItemPositionWithReplacement.REPLACE,
            ImageDimensionOptions.HEIGHT,
            ImageDimensionOptions.HEIGHT,
            "x",
            "_",
            1080,
            720,
            "TEST",
            "720x720",
            True,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "",
            "_",
            1080,
            720,
            "TEST",
            "1080720_TEST",
            True,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "x",
            "",
            1080,
            720,
            "TEST",
            "1080x720TEST",
            True,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "",
            "",
            1080,
            720,
            "TEST",
            "1080720TEST",
            True,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "",
            "",
            None,
            None,
            "TEST",
            "TEST",
            False,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "",
            "",
            1080,
            None,
            "TEST",
            "TEST",
            False,
        ),
        (
            ItemPositionWithReplacement.BEGIN,
            ImageDimensionOptions.WIDTH,
            ImageDimensionOptions.HEIGHT,
            "",
            "",
            None,
            720,
            "TEST",
            "TEST",
            False,
        ),
    ],
)
def test_command_params_combinations(
    position: ItemPositionWithReplacement,
    left: ImageDimensionOptions,
    right: ImageDimensionOptions,
    dim_sep: str,
    name_sep: str,
    width: int,
    height: int,
    file_name: str,
    exp_name: str,
    changed: bool,
):
    from core.commands.prep_image_dimensions import ImageDimensionsPrepareCommand

    test_command = ImageDimensionsPrepareCommand(
        position=position,
        left_side=left,
        right_side=right,
        dimension_separator=dim_sep,
        name_separator=name_sep,
    )

    verify_command_result(
        test_command,
        file_name,
        exp_name,
        img_width=width,
        img_height=height,
        is_updated_name=changed,
    )
