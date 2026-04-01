package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.ImageDimensionOptions;
import ua.renamer.app.api.enums.ItemPositionWithReplacement;
import ua.renamer.app.api.model.*;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.model.config.ImageDimensionsConfig;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.api.model.meta.category.VideoMeta;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ImageDimensionsTransformer.
 * Tests cover image and video metadata, all position options,
 * various dimension configurations, error handling, and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImageDimensionsTransformerTest {

    private ImageDimensionsTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new ImageDimensionsTransformer();
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private FileModel createTestFileModel(String name, String extension) {
        return FileModel.builder()
                        .withFile(new File("/test/path/" + name + "." + extension))
                        .withIsFile(true)
                        .withFileSize(1024L)
                        .withName(name)
                        .withExtension(extension)
                        .withAbsolutePath("/test/path/" + name + "." + extension)
                        .withCreationDate(LocalDateTime.now().minusDays(1))
                        .withModificationDate(LocalDateTime.now())
                        .withDetectedMimeType("text/plain")
                        .withDetectedExtensions(Collections.emptySet())
                        .withCategory(Category.GENERIC)
                        .withMetadata(null)
                        .build();
    }

    private FileModel createTestFileModelWithImageMetadata(String name, String extension, int width, int height) {
        ImageMeta imageMeta = ImageMeta.builder()
                                       .withContentCreationDate(LocalDateTime.now())
                                       .withWidth(width)
                                       .withHeight(height)
                                       .build();

        FileMeta fileMeta = FileMeta.builder()
                                    .withImage(imageMeta)
                                    .build();

        return FileModel.builder()
                        .withFile(new File("/test/path/" + name + "." + extension))
                        .withIsFile(true)
                        .withFileSize(1024L)
                        .withName(name)
                        .withExtension(extension)
                        .withAbsolutePath("/test/path/" + name + "." + extension)
                        .withCreationDate(LocalDateTime.now().minusDays(1))
                        .withModificationDate(LocalDateTime.now())
                        .withDetectedMimeType("image/jpeg")
                        .withDetectedExtensions(Collections.emptySet())
                        .withCategory(Category.IMAGE)
                        .withMetadata(fileMeta)
                        .build();
    }

    private FileModel createTestFileModelWithVideoMetadata(String name, String extension, int width, int height) {
        VideoMeta videoMeta = VideoMeta.builder()
                                       .withContentCreationDate(LocalDateTime.now())
                                       .withWidth(width)
                                       .withHeight(height)
                                       .build();

        FileMeta fileMeta = FileMeta.builder()
                                    .withVideo(videoMeta)
                                    .build();

        return FileModel.builder()
                        .withFile(new File("/test/path/" + name + "." + extension))
                        .withIsFile(true)
                        .withFileSize(2048L)
                        .withName(name)
                        .withExtension(extension)
                        .withAbsolutePath("/test/path/" + name + "." + extension)
                        .withCreationDate(LocalDateTime.now().minusDays(1))
                        .withModificationDate(LocalDateTime.now())
                        .withDetectedMimeType("video/mp4")
                        .withDetectedExtensions(Collections.emptySet())
                        .withCategory(Category.VIDEO)
                        .withMetadata(fileMeta)
                        .build();
    }

    // ============================================================================
    // A. Basic Functionality - WIDTH x HEIGHT Format
    // ============================================================================

    @Test
    void testWidthXHeight_AtBegin() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("1920x1080 photo", result.getNewName());
        assertEquals("jpg", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testWidthXHeight_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.END)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("photo 1920x1080", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testWidthXHeight_Replace() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.REPLACE)
                                                            .withNameSeparator("")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("1920x1080", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. HEIGHT x WIDTH Format (Reversed)
    // ============================================================================

    @Test
    void testHeightXWidth_AtBegin() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.HEIGHT)
                                                            .withRightSide(ImageDimensionOptions.WIDTH)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("1080x1920 photo", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testHeightXWidth_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.HEIGHT)
                                                            .withRightSide(ImageDimensionOptions.WIDTH)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.END)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("photo 1080x1920", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // C. Width Only Tests
    // ============================================================================

    @Test
    void testWidthOnly_AtBegin() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.DO_NOT_USE)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("1920 photo", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testWidthOnly_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.DO_NOT_USE)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.END)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("photo 1920", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // D. Height Only Tests
    // ============================================================================

    @Test
    void testHeightOnly_AtBegin() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.HEIGHT)
                                                            .withRightSide(ImageDimensionOptions.DO_NOT_USE)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("1080 photo", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testHeightOnly_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.HEIGHT)
                                                            .withRightSide(ImageDimensionOptions.DO_NOT_USE)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.END)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("photo 1080", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // E. Custom Separator Tests
    // ============================================================================

    @Test
    void testCustomSeparator_Underscore() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("_")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("1920_1080 photo", result.getNewName());
    }

    @Test
    void testCustomSeparator_Dash() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("-")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("1920-1080 photo", result.getNewName());
    }

    @Test
    void testCustomSeparator_Text() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator(" by ")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("1920 by 1080 photo", result.getNewName());
    }

    // ============================================================================
    // F. Video Metadata Tests
    // ============================================================================

    @Test
    void testVideoMetadata_WidthXHeight() {
        // Given
        FileModel input = createTestFileModelWithVideoMetadata("video", "mp4", 3840, 2160);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("3840x2160 video", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testVideoMetadata_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithVideoMetadata("video", "mp4", 1280, 720);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.END)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("video 1280x720", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // G. Various Resolution Tests
    // ============================================================================

    @Test
    void testVarious4KResolutions() {
        // Test 4K UHD
        FileModel input1 = createTestFileModelWithImageMetadata("photo", "jpg", 3840, 2160);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        PreparedFileModel result1 = transformer.transform(input1, config);
        assertEquals("3840x2160 photo", result1.getNewName());
    }

    @Test
    void testVariousHDResolutions() {
        // Test Full HD
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);
        assertEquals("1920x1080 photo", result.getNewName());
    }

    @Test
    void testSmallResolution() {
        // Test small resolution
        FileModel input = createTestFileModelWithImageMetadata("thumbnail", "jpg", 150, 150);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);
        assertEquals("150x150 thumbnail", result.getNewName());
    }

    @Test
    void testPortraitOrientation() {
        // Test portrait (height > width)
        FileModel input = createTestFileModelWithImageMetadata("portrait", "jpg", 1080, 1920);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);
        assertEquals("1080x1920 portrait", result.getNewName());
    }

    // ============================================================================
    // H. Missing Metadata Error Tests
    // ============================================================================

    @Test
    void testNoMetadata_Error() {
        // Given - file without any metadata
        FileModel input = createTestFileModel("photo", "jpg");
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator("")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("No image/video metadata"));
        assertFalse(result.needsRename());
    }

    @Test
    void testMissingWidth_UsesOnlyHeight() {
        // Given - metadata with height but no width
        ImageMeta imageMeta = ImageMeta.builder()
                                       .withContentCreationDate(LocalDateTime.now())
                                       .withWidth(null)
                                       .withHeight(1080)
                                       .build();

        FileMeta fileMeta = FileMeta.builder()
                                    .withImage(imageMeta)
                                    .build();

        FileModel input = FileModel.builder()
                                   .withFile(new File("/test/path/photo.jpg"))
                                   .withIsFile(true)
                                   .withFileSize(1024L)
                                   .withName("photo")
                                   .withExtension("jpg")
                                   .withAbsolutePath("/test/path/photo.jpg")
                                   .withCreationDate(LocalDateTime.now())
                                   .withModificationDate(LocalDateTime.now())
                                   .withDetectedMimeType("image/jpeg")
                                   .withDetectedExtensions(Collections.emptySet())
                                   .withCategory(Category.IMAGE)
                                   .withMetadata(fileMeta)
                                   .build();

        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should use only height (width is skipped when null)
        assertFalse(result.isHasError());
        assertEquals("1080 photo", result.getNewName());
    }

    @Test
    void testMissingHeight_UsesOnlyWidth() {
        // Given - metadata with width but no height
        ImageMeta imageMeta = ImageMeta.builder()
                                       .withContentCreationDate(LocalDateTime.now())
                                       .withWidth(1920)
                                       .withHeight(null)
                                       .build();

        FileMeta fileMeta = FileMeta.builder()
                                    .withImage(imageMeta)
                                    .build();

        FileModel input = FileModel.builder()
                                   .withFile(new File("/test/path/photo.jpg"))
                                   .withIsFile(true)
                                   .withFileSize(1024L)
                                   .withName("photo")
                                   .withExtension("jpg")
                                   .withAbsolutePath("/test/path/photo.jpg")
                                   .withCreationDate(LocalDateTime.now())
                                   .withModificationDate(LocalDateTime.now())
                                   .withDetectedMimeType("image/jpeg")
                                   .withDetectedExtensions(Collections.emptySet())
                                   .withCategory(Category.IMAGE)
                                   .withMetadata(fileMeta)
                                   .build();

        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator(" ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should use only width with separator (height is skipped when null, but separator remains)
        assertFalse(result.isHasError());
        assertEquals("1920x photo", result.getNewName());
    }

    @Test
    void testBothDimensionsMissing_Error() {
        // Given - metadata with no dimensions
        ImageMeta imageMeta = ImageMeta.builder()
                                       .withContentCreationDate(LocalDateTime.now())
                                       .withWidth(null)
                                       .withHeight(null)
                                       .build();

        FileMeta fileMeta = FileMeta.builder()
                                    .withImage(imageMeta)
                                    .build();

        FileModel input = FileModel.builder()
                                   .withFile(new File("/test/path/photo.jpg"))
                                   .withIsFile(true)
                                   .withFileSize(1024L)
                                   .withName("photo")
                                   .withExtension("jpg")
                                   .withAbsolutePath("/test/path/photo.jpg")
                                   .withCreationDate(LocalDateTime.now())
                                   .withModificationDate(LocalDateTime.now())
                                   .withDetectedMimeType("image/jpeg")
                                   .withDetectedExtensions(Collections.emptySet())
                                   .withCategory(Category.IMAGE)
                                   .withMetadata(fileMeta)
                                   .build();

        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator("")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("No image/video metadata"));
    }

    // ============================================================================
    // I. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator("")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.USE_IMAGE_DIMENSIONS, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.HEIGHT)
                                                            .withRightSide(ImageDimensionOptions.WIDTH)
                                                            .withSeparator("_")
                                                            .withPosition(ItemPositionWithReplacement.END)
                                                            .withNameSeparator("")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("HEIGHT", configMap.get("leftSide"));
        assertEquals("WIDTH", configMap.get("rightSide"));
        assertEquals("_", configMap.get("separator"));
        assertEquals("END", configMap.get("position"));
    }

    // ============================================================================
    // J. Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_NullInput() {
        // Given
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator("")
                                                            .build();

        // When/Then
        assertThrows(NullPointerException.class, () -> {
            transformer.transform(null, config);
        });
    }

    @Test
    void testErrorHandling_NullConfig() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);

        // When
        PreparedFileModel result = transformer.transform(input, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("Transformer configuration must not be null"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void givenNullConfig_whenTransform_thenErrorResultReturned() {
        FileModel input = createTestFileModel("document", "txt");
        PreparedFileModel result = transformer.transform(input, null);
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertFalse(result.getErrorMessage().isEmpty());
    }

    // ============================================================================
    // K. Integration and Multiple Scenarios Tests
    // ============================================================================

    @Test
    void testMultipleFiles_DifferentConfigurations() {
        // Test that transformer handles different files correctly
        FileModel file1 = createTestFileModelWithImageMetadata("photo1", "jpg", 1920, 1080);
        FileModel file2 = createTestFileModelWithVideoMetadata("video1", "mp4", 3840, 2160);

        ImageDimensionsConfig config1 = ImageDimensionsConfig.builder()
                                                             .withLeftSide(ImageDimensionOptions.WIDTH)
                                                             .withRightSide(ImageDimensionOptions.HEIGHT)
                                                             .withSeparator("x")
                                                             .withPosition(ItemPositionWithReplacement.BEGIN)
                                                             .withNameSeparator(" ")
                                                             .build();

        ImageDimensionsConfig config2 = ImageDimensionsConfig.builder()
                                                             .withLeftSide(ImageDimensionOptions.WIDTH)
                                                             .withRightSide(ImageDimensionOptions.HEIGHT)
                                                             .withSeparator("x")
                                                             .withPosition(ItemPositionWithReplacement.END)
                                                             .withNameSeparator(" ")
                                                             .build();

        PreparedFileModel result1 = transformer.transform(file1, config1);
        PreparedFileModel result2 = transformer.transform(file2, config2);

        assertEquals("1920x1080 photo1", result1.getNewName());
        assertEquals("video1 3840x2160", result2.getNewName());
    }

    @Test
    void testExtensionPreservation() {
        // Verify that extension is always preserved correctly
        FileModel input = createTestFileModelWithImageMetadata("photo", "custom_ext", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator("")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertEquals("custom_ext", result.getNewExtension());
        assertEquals(input.getExtension(), result.getNewExtension());
    }

    @Test
    void testOriginalFilePreservation() {
        // Verify that original file reference is preserved
        FileModel input = createTestFileModelWithImageMetadata("original", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator("")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertSame(input, result.getOriginalFile());
        assertEquals("original", result.getOriginalFile().getName());
    }

    @Test
    void testNeedsRename_True() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg", 1920, 1080);
        ImageDimensionsConfig config = ImageDimensionsConfig.builder()
                                                            .withLeftSide(ImageDimensionOptions.WIDTH)
                                                            .withRightSide(ImageDimensionOptions.HEIGHT)
                                                            .withSeparator("x")
                                                            .withPosition(ItemPositionWithReplacement.BEGIN)
                                                            .withNameSeparator("")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
    }
}
