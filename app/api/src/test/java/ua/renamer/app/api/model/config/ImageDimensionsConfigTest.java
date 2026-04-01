package ua.renamer.app.api.model.config;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ImageDimensionOptions;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;

import static org.junit.jupiter.api.Assertions.*;

class ImageDimensionsConfigTest {

    @Test
    void givenValidParams_whenBuild_thenConfigCreatedSuccessfully() {
        assertDoesNotThrow(() ->
            ImageDimensionsConfig.builder()
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .build()
        );
    }

    @Test
    void givenNullPosition_whenBuild_thenNullPointerException() {
        var builder = ImageDimensionsConfig.builder()
            .withPosition(null)
            .withLeftSide(ImageDimensionOptions.WIDTH)
            .withRightSide(ImageDimensionOptions.HEIGHT)
            .withSeparator("x");

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullLeftSide_whenBuild_thenNullPointerException() {
        var builder = ImageDimensionsConfig.builder()
            .withPosition(ItemPositionWithReplacement.END)
            .withLeftSide(null)
            .withRightSide(ImageDimensionOptions.HEIGHT)
            .withSeparator("x");

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenNullRightSide_whenBuild_thenNullPointerException() {
        var builder = ImageDimensionsConfig.builder()
            .withPosition(ItemPositionWithReplacement.END)
            .withLeftSide(ImageDimensionOptions.WIDTH)
            .withRightSide(null)
            .withSeparator("x");

        assertThrows(NullPointerException.class, builder::build);
    }

    @Test
    void givenBothSidesDoNotUse_whenBuild_thenIllegalArgumentException() {
        var builder = ImageDimensionsConfig.builder()
            .withPosition(ItemPositionWithReplacement.BEGIN)
            .withLeftSide(ImageDimensionOptions.DO_NOT_USE)
            .withRightSide(ImageDimensionOptions.DO_NOT_USE)
            .withSeparator("x");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void givenOnlyLeftSideSet_whenBuild_thenSucceeds() {
        assertDoesNotThrow(() ->
            ImageDimensionsConfig.builder()
                .withPosition(ItemPositionWithReplacement.END)
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.DO_NOT_USE)
                .withSeparator("x")
                .build()
        );
    }

    @Test
    void givenOnlyRightSideSet_whenBuild_thenSucceeds() {
        assertDoesNotThrow(() ->
            ImageDimensionsConfig.builder()
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withLeftSide(ImageDimensionOptions.DO_NOT_USE)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .build()
        );
    }

    @Test
    void givenNullSeparator_whenBuild_thenSucceeds() {
        assertDoesNotThrow(() ->
            ImageDimensionsConfig.builder()
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator(null)
                .build()
        );
    }
}
