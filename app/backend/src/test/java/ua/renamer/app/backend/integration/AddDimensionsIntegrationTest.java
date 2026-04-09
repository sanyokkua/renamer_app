package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ImageDimensionOptions;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.ImageDimensionsConfig;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddDimensionsIntegrationTest extends BaseRealMetadataIntegrationTest {

    @Test
    void addDimensions_widthHeight_jpegExif_prependsDimensions() throws Exception {
        File file = copyTestResource("media/photo_1920x1080.jpg");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withNameSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("1920x1080_photo_1920x1080.jpg");
        assertRenamed("photo_1920x1080.jpg", "1920x1080_photo_1920x1080.jpg");
    }

    @Test
    void addDimensions_widthHeight_png_appendsDimensions() throws Exception {
        File file = copyTestResource("media/image_800x600.png");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.END)
                .withNameSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("image_800x600_800x600.png");
        assertRenamed("image_800x600.png", "image_800x600_800x600.png");
    }

    @Test
    void addDimensions_widthOnly_jpeg_prependsWidthOnly() throws Exception {
        File file = copyTestResource("media/photo_1920x1080.jpg");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.DO_NOT_USE)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withNameSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("1920_photo_1920x1080.jpg");
        assertRenamed("photo_1920x1080.jpg", "1920_photo_1920x1080.jpg");
    }

    @Test
    void addDimensions_heightWidth_swapped_jpeg_prependsCorrectOrder() throws Exception {
        File file = copyTestResource("media/photo_1920x1080.jpg");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.HEIGHT)
                .withRightSide(ImageDimensionOptions.WIDTH)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withNameSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("1080x1920_photo_1920x1080.jpg");
        assertRenamed("photo_1920x1080.jpg", "1080x1920_photo_1920x1080.jpg");
    }

    @Test
    void addDimensions_replace_jpeg_replacesStemWithDimensions() throws Exception {
        File file = copyTestResource("media/photo_1920x1080.jpg");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withNameSeparator("")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("1920x1080.jpg");
        assertRenamed("photo_1920x1080.jpg", "1920x1080.jpg");
    }

    @Test
    void addDimensions_unicodeSeparator_jpeg_producesCorrectName() throws Exception {
        File file = copyTestResource("media/photo_1920x1080.jpg");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator(" \u00d7 ")
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withNameSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).contains("1920 \u00d7 1080");
    }

    @Test
    void addDimensions_jpegNoExif_sofHeaderDimensions_succeeds() throws Exception {
        File file = copyTestResource("media/photo_no_exif.jpg");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withNameSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("800x600_photo_no_exif.jpg");
        assertRenamed("photo_no_exif.jpg", "800x600_photo_no_exif.jpg");
    }

    @Test
    void addDimensions_mp3_notImage_errorTransformation() throws Exception {
        File file = copyTestResource("media/song_with_tags.mp3");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withNameSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("song_with_tags.mp3");
    }

    @Test
    void addDimensions_textFile_notImage_errorTransformation() throws Exception {
        File file = copyTestResource("flat/document.txt");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withNameSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_DIMENSIONS, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("document.txt");
    }

    /**
     * Full user flow: all test resources expanded, then renamed with ADD_DIMENSIONS WIDTH+HEIGHT at BEGIN.
     * JPEG/PNG files succeed; text and audio files are skipped (no image dimensions — by design).
     */
    @Test
    void addDimensions_fullTree_imageFilesSucceed_nonImageFilesSkipped() throws Exception {
        List<File> allItems = copyAndExpandFullTree();
        assertThat(allItems).isNotEmpty();

        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                .withLeftSide(ImageDimensionOptions.WIDTH)
                .withRightSide(ImageDimensionOptions.HEIGHT)
                .withSeparator("x")
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withNameSeparator("_")
                .build();

        List<RenameResult> results = orchestrator.execute(allItems, TransformationMode.ADD_DIMENSIONS, config,
                noOpCallback());

        assertThat(results).hasSameSizeAs(allItems);
        assertDiskStateForBatch(results);
    }
}
