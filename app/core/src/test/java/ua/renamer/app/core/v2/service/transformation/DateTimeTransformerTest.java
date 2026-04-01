package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.*;
import ua.renamer.app.api.interfaces.DateTimeUtils;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.DateTimeConfig;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.api.model.meta.category.VideoMeta;
import ua.renamer.app.core.v2.util.TestDateTimeUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DateTimeTransformer.
 * Tests cover all 5 datetime sources, all 3 positions, various formats,
 * error handling, and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DateTimeTransformerTest {

    private DateTimeTransformer transformer;
    private DateTimeUtils dateTimeUtils;

    @BeforeAll
    void setUp() {
        dateTimeUtils = new TestDateTimeUtils();
        transformer = new DateTimeTransformer(dateTimeUtils);
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
                .withCreationDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .withModificationDate(LocalDateTime.of(2024, 2, 20, 14, 45, 30))
                .withDetectedMimeType("text/plain")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.GENERIC)
                .withMetadata(null)
                .build();
    }

    private FileModel createTestFileModelWithImageMetadata(String name, String extension) {
        ImageMeta imageMeta = ImageMeta.builder()
                .withContentCreationDate(LocalDateTime.of(2023, 6, 10, 8, 15, 0))
                .withWidth(1920)
                .withHeight(1080)
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
                .withCreationDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .withModificationDate(LocalDateTime.of(2024, 2, 20, 14, 45, 30))
                .withDetectedMimeType("image/jpeg")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.IMAGE)
                .withMetadata(fileMeta)
                .build();
    }

    private FileModel createTestFileModelWithVideoMetadata(String name, String extension) {
        VideoMeta videoMeta = VideoMeta.builder()
                .withContentCreationDate(LocalDateTime.of(2023, 7, 5, 12, 0, 0))
                .withWidth(3840)
                .withHeight(2160)
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
                .withCreationDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .withModificationDate(LocalDateTime.of(2024, 2, 20, 14, 45, 30))
                .withDetectedMimeType("video/mp4")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.VIDEO)
                .withMetadata(fileMeta)
                .build();
    }

    // ============================================================================
    // A. DateTime Source Tests - FILE_CREATION_DATE
    // ============================================================================

    @Test
    void testFileCreationDate_AtBegin() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2024-01-15_document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testFileCreationDate_AtEnd() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.END)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document_2024-01-15", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testFileCreationDate_Replace() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.REPLACE)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2024-01-15", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. DateTime Source Tests - FILE_MODIFICATION_DATE
    // ============================================================================

    @Test
    void testFileModificationDate_AtBegin() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_MODIFICATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2024-02-20_document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testFileModificationDate_WithTime() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_MODIFICATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_SS_24_DASHED)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_UNDERSCORED)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertTrue(result.getNewName().startsWith("2024-02-20_14-45-30_document"));
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // C. DateTime Source Tests - CONTENT_CREATION_DATE (Image)
    // ============================================================================

    @Test
    void testContentCreationDate_ImageMeta_AtBegin() {
        // Given
        FileModel input = createTestFileModelWithImageMetadata("photo", "jpg");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CONTENT_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2023-06-10_photo", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testContentCreationDate_VideoMeta_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithVideoMetadata("video", "mp4");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CONTENT_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.END)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("video_2023-07-05", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testContentCreationDate_NoMetadata_Error() {
        // Given - file without metadata
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CONTENT_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("No datetime available"));
        assertFalse(result.needsRename());
    }

    // ============================================================================
    // D. DateTime Source Tests - CURRENT_DATE
    // ============================================================================

    @Test
    void testCurrentDate_AtBegin() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CURRENT_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should use current date (we can't predict exact value, just check format)
        assertFalse(result.isHasError());
        assertTrue(result.getNewName().matches("\\d{4}-\\d{2}-\\d{2}_document"));
        assertTrue(result.needsRename());
    }

    @Test
    void testCurrentDate_WithTime() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CURRENT_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_24_DASHED)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_UNDERSCORED)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertTrue(result.getNewName().matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}_document"));
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // E. DateTime Source Tests - CUSTOM_DATE
    // ============================================================================

    @Test
    void testCustomDate_AtBegin() {
        // Given
        LocalDateTime customDate = LocalDateTime.of(2020, 12, 25, 18, 30, 0);
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CUSTOM_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(customDate)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2020-12-25_document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testCustomDate_WithTime() {
        // Given
        LocalDateTime customDate = LocalDateTime.of(2020, 12, 25, 18, 30, 45);
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CUSTOM_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_SS_24_DASHED)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_UNDERSCORED)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(customDate)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2020-12-25_18-30-45_document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testCustomDate_NotProvided_Error() {
        // Config validation now rejects CUSTOM_DATE with null customDateTime at construction time
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                DateTimeConfig.builder()
                        .withSource(DateTimeSource.CUSTOM_DATE)
                        .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                        .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                        .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                        .withPosition(ItemPositionWithReplacement.BEGIN)
                        .withSeparator("_")
                        .withCustomDateTime(null)
                        .build()
        );
        assertTrue(ex.getMessage().contains("customDateTime must be set when source is CUSTOM_DATE"));
    }

    // ============================================================================
    // F. Date Format Variations Tests
    // ============================================================================

    @Test
    void testDateFormat_YYYY_MM_DD_TOGETHER() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_TOGETHER)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("20240115_document", result.getNewName());
    }

    @Test
    void testDateFormat_DD_MM_YYYY_DOTTED() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.DD_MM_YYYY_DOTTED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("15.01.2024_document", result.getNewName());
    }

    @Test
    void testDateFormat_MM_DD_YY_UNDERSCORED() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.MM_DD_YY_UNDERSCORED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("01_15_24_document", result.getNewName());
    }

    // ============================================================================
    // G. Time Format Variations Tests
    // ============================================================================

    @Test
    void testTimeFormat_24Hour_Together() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_MODIFICATION_DATE)
                .withDateFormat(DateFormat.DO_NOT_USE_DATE)
                .withTimeFormat(TimeFormat.HH_MM_SS_24_TOGETHER)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("144530_document", result.getNewName());
    }

    @Test
    void testTimeFormat_24Hour_Dotted() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_MODIFICATION_DATE)
                .withDateFormat(DateFormat.DO_NOT_USE_DATE)
                .withTimeFormat(TimeFormat.HH_MM_SS_24_DOTTED)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("14.45.30_document", result.getNewName());
    }

    // ============================================================================
    // H. DateTime Format Combinations Tests
    // ============================================================================

    @Test
    void testDateTimeFormat_Together() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_24_DASHED)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2024-01-1510-30_document", result.getNewName());
    }

    @Test
    void testDateTimeFormat_WhiteSpaced() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_24_DASHED)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_WHITE_SPACED)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2024-01-15 10-30_document", result.getNewName());
    }

    @Test
    void testDateTimeFormat_Reverse() {
        // Given - time before date
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.HH_MM_24_DASHED)
                .withDateTimeFormat(DateTimeFormat.REVERSE_DATE_TIME_UNDERSCORED)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("10-30_2024-01-15_document", result.getNewName());
    }

    // ============================================================================
    // I. Custom Separator Tests
    // ============================================================================

    @Test
    void testCustomSeparator_Dash() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("-")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2024-01-15-document", result.getNewName());
    }

    @Test
    void testCustomSeparator_Space() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator(" ")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2024-01-15 document", result.getNewName());
    }

    @Test
    void testCustomSeparator_Empty() {
        // Given - no separator
        FileModel input = createTestFileModel("document", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("2024-01-15document", result.getNewName());
    }

    // ============================================================================
    // J. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.USE_DATETIME, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_MODIFICATION_DATE)
                .withDateFormat(DateFormat.DD_MM_YYYY_DOTTED)
                .withTimeFormat(TimeFormat.HH_MM_24_DASHED)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_UNDERSCORED)
                .withPosition(ItemPositionWithReplacement.END)
                .withSeparator("-")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("FILE_MODIFICATION_DATE", configMap.get("source"));
        assertEquals("DD_MM_YYYY_DOTTED", configMap.get("dateFormat"));
        assertEquals("HH_MM_24_DASHED", configMap.get("timeFormat"));
        assertEquals("DATE_TIME_UNDERSCORED", configMap.get("dateTimeFormat"));
        assertEquals("END", configMap.get("position"));
        assertEquals("-", configMap.get("separator"));
    }

    // ============================================================================
    // K. Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_NullInput() {
        // Given
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CURRENT_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When/Then
        assertThrows(NullPointerException.class, () -> {
            transformer.transform(null, config);
        });
    }

    @Test
    void testErrorHandling_NullConfig() {
        // Given
        FileModel input = createTestFileModel("file", "txt");

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

    @Test
    void testErrorHandling_MissingCreationDate() {
        // Given - file without creation date
        FileModel input = FileModel.builder()
                .withFile(new File("/test/path/file.txt"))
                .withIsFile(true)
                .withFileSize(1024L)
                .withName("file")
                .withExtension("txt")
                .withAbsolutePath("/test/path/file.txt")
                .withCreationDate(null)
                .withModificationDate(LocalDateTime.now())
                .withDetectedMimeType("text/plain")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.GENERIC)
                .withMetadata(null)
                .build();

        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("No datetime available"));
    }

    // ============================================================================
    // L. Integration Tests
    // ============================================================================

    @Test
    void testMultipleFiles_DifferentConfigs() {
        // Given
        FileModel file1 = createTestFileModel("doc1", "txt");
        FileModel file2 = createTestFileModel("doc2", "pdf");

        DateTimeConfig config1 = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        DateTimeConfig config2 = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_MODIFICATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.END)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result1 = transformer.transform(file1, config1);
        PreparedFileModel result2 = transformer.transform(file2, config2);

        // Then
        assertEquals("2024-01-15_doc1", result1.getNewName());
        assertEquals("doc2_2024-02-20", result2.getNewName());
    }

    @Test
    void testExtensionPreservation() {
        // Given
        FileModel input = createTestFileModel("file", "custom_ext");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.CURRENT_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertEquals("custom_ext", result.getNewExtension());
        assertEquals(input.getExtension(), result.getNewExtension());
    }

    @Test
    void testNeedsRename_True() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        DateTimeConfig config = DateTimeConfig.builder()
                .withSource(DateTimeSource.FILE_CREATION_DATE)
                .withDateFormat(DateFormat.YYYY_MM_DD_DASHED)
                .withTimeFormat(TimeFormat.DO_NOT_USE_TIME)
                .withDateTimeFormat(DateTimeFormat.DATE_TIME_TOGETHER)
                .withPosition(ItemPositionWithReplacement.BEGIN)
                .withSeparator("_")
                .withCustomDateTime(null)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
    }
}
