package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.SortSource;
import ua.renamer.app.api.model.*;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.model.config.SequenceConfig;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for SequenceTransformer.
 * Tests cover sequential execution requirement, batch transformation,
 * all sort sources, various start/step/padding combinations,
 * and error handling.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SequenceTransformerTest {

    private SequenceTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new SequenceTransformer();
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

    private FileModel createTestFileModelWithSize(String name, String extension, long size) {
        return FileModel.builder()
                        .withFile(new File("/test/path/" + name + "." + extension))
                        .withIsFile(true)
                        .withFileSize(size)
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
                                       .withContentCreationDate(LocalDateTime.of(2023, 6, 10, 8, 15, 0))
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

    // ============================================================================
    // A. requiresSequentialExecution() Tests
    // ============================================================================

    @Test
    void testRequiresSequentialExecution_ReturnsTrue() {
        // When
        boolean result = transformer.requiresSequentialExecution();

        // Then
        assertTrue(result, "Sequence transformer MUST require sequential execution");
    }

    // ============================================================================
    // B. transform() Single File Method Tests
    // ============================================================================

    @Test
    void testTransformSingleFile_ThrowsUnsupportedOperationException() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> {
            transformer.transform(input, config);
        });
    }

    // ============================================================================
    // C. transformBatch() - Basic Sequence Tests
    // ============================================================================

    @Test
    void testTransformBatch_BasicSequence() {
        // Given - 3 files
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt"),
                createTestFileModel("file3", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
        assertFalse(results.get(0).isHasError());
        assertFalse(results.get(1).isHasError());
        assertFalse(results.get(2).isHasError());
        assertEquals("1", results.get(0).getNewName());
        assertEquals("2", results.get(1).getNewName());
        assertEquals("3", results.get(2).getNewName());
    }

    @Test
    void testTransformBatch_StartFromZero() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt"),
                createTestFileModel("file3", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(0)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertEquals("0", results.get(0).getNewName());
        assertEquals("1", results.get(1).getNewName());
        assertEquals("2", results.get(2).getNewName());
    }

    @Test
    void testTransformBatch_StartFromTen() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(10)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertEquals("10", results.get(0).getNewName());
        assertEquals("11", results.get(1).getNewName());
    }

    // ============================================================================
    // D. transformBatch() - Step Value Tests
    // ============================================================================

    @Test
    void testTransformBatch_StepValueTwo() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt"),
                createTestFileModel("file3", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(2)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertEquals("1", results.get(0).getNewName());
        assertEquals("3", results.get(1).getNewName());
        assertEquals("5", results.get(2).getNewName());
    }

    @Test
    void testTransformBatch_StepValueFive() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt"),
                createTestFileModel("file3", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(10)
                                              .withStepValue(5)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertEquals("10", results.get(0).getNewName());
        assertEquals("15", results.get(1).getNewName());
        assertEquals("20", results.get(2).getNewName());
    }

    @Test
    void testTransformBatch_StepValueTen() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(0)
                                              .withStepValue(10)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertEquals("0", results.get(0).getNewName());
        assertEquals("10", results.get(1).getNewName());
    }

    // ============================================================================
    // E. transformBatch() - Padding Tests
    // ============================================================================

    @Test
    void testTransformBatch_PaddingThree() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt"),
                createTestFileModel("file3", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(3)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertEquals("001", results.get(0).getNewName());
        assertEquals("002", results.get(1).getNewName());
        assertEquals("003", results.get(2).getNewName());
    }

    @Test
    void testTransformBatch_PaddingFive() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(5)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertEquals("00001", results.get(0).getNewName());
        assertEquals("00002", results.get(1).getNewName());
    }

    @Test
    void testTransformBatch_PaddingTwo() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(5)
                                              .withStepValue(1)
                                              .withPadding(2)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertEquals("05", results.get(0).getNewName());
        assertEquals("06", results.get(1).getNewName());
    }

    // ============================================================================
    // F. transformBatch() - Sort Source Tests
    // ============================================================================

    @Test
    void testTransformBatch_SortByFileName() {
        // Given - files in random order
        List<FileModel> files = List.of(
                createTestFileModel("charlie", "txt"),
                createTestFileModel("alpha", "txt"),
                createTestFileModel("bravo", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - should be sorted alphabetically
        assertEquals("alpha", results.get(0).getOriginalFile().getName());
        assertEquals("bravo", results.get(1).getOriginalFile().getName());
        assertEquals("charlie", results.get(2).getOriginalFile().getName());
        assertEquals("1", results.get(0).getNewName());
        assertEquals("2", results.get(1).getNewName());
        assertEquals("3", results.get(2).getNewName());
    }

    @Test
    void testTransformBatch_SortByFilePath() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("fileZ", "txt"),
                createTestFileModel("fileA", "txt"),
                createTestFileModel("fileM", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_PATH)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - sorted by absolute path
        assertTrue(results.get(0).getOriginalFile().getAbsolutePath()
                          .compareTo(results.get(1).getOriginalFile().getAbsolutePath()) < 0);
    }

    @Test
    void testTransformBatch_SortByFileSize() {
        // Given - files with different sizes
        List<FileModel> files = List.of(
                createTestFileModelWithSize("large", "txt", 5000),
                createTestFileModelWithSize("small", "txt", 100),
                createTestFileModelWithSize("medium", "txt", 1000)
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_SIZE)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - sorted by size (small to large)
        assertEquals("small", results.get(0).getOriginalFile().getName());
        assertEquals("medium", results.get(1).getOriginalFile().getName());
        assertEquals("large", results.get(2).getOriginalFile().getName());
    }

    @Test
    void testTransformBatch_SortByFileCreationDateTime() {
        // Given - files with different creation dates
        List<FileModel> files = new ArrayList<>();
        files.add(FileModel.builder()
                           .withFile(new File("/test/path/file3.txt"))
                           .withIsFile(true)
                           .withFileSize(1024L)
                           .withName("file3")
                           .withExtension("txt")
                           .withAbsolutePath("/test/path/file3.txt")
                           .withCreationDate(LocalDateTime.of(2024, 3, 1, 10, 0))
                           .withModificationDate(LocalDateTime.now())
                           .withDetectedMimeType("text/plain")
                           .withDetectedExtensions(Collections.emptySet())
                           .withCategory(Category.GENERIC)
                           .withMetadata(null)
                           .build());

        files.add(FileModel.builder()
                           .withFile(new File("/test/path/file1.txt"))
                           .withIsFile(true)
                           .withFileSize(1024L)
                           .withName("file1")
                           .withExtension("txt")
                           .withAbsolutePath("/test/path/file1.txt")
                           .withCreationDate(LocalDateTime.of(2024, 1, 1, 10, 0))
                           .withModificationDate(LocalDateTime.now())
                           .withDetectedMimeType("text/plain")
                           .withDetectedExtensions(Collections.emptySet())
                           .withCategory(Category.GENERIC)
                           .withMetadata(null)
                           .build());

        files.add(FileModel.builder()
                           .withFile(new File("/test/path/file2.txt"))
                           .withIsFile(true)
                           .withFileSize(1024L)
                           .withName("file2")
                           .withExtension("txt")
                           .withAbsolutePath("/test/path/file2.txt")
                           .withCreationDate(LocalDateTime.of(2024, 2, 1, 10, 0))
                           .withModificationDate(LocalDateTime.now())
                           .withDetectedMimeType("text/plain")
                           .withDetectedExtensions(Collections.emptySet())
                           .withCategory(Category.GENERIC)
                           .withMetadata(null)
                           .build());

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_CREATION_DATETIME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - sorted by creation date (oldest first)
        assertEquals("file1", results.get(0).getOriginalFile().getName());
        assertEquals("file2", results.get(1).getOriginalFile().getName());
        assertEquals("file3", results.get(2).getOriginalFile().getName());
    }

    @Test
    void testTransformBatch_SortByFileModificationDateTime() {
        // Given - files with different modification dates
        List<FileModel> files = new ArrayList<>();
        files.add(FileModel.builder()
                           .withFile(new File("/test/path/file3.txt"))
                           .withIsFile(true)
                           .withFileSize(1024L)
                           .withName("file3")
                           .withExtension("txt")
                           .withAbsolutePath("/test/path/file3.txt")
                           .withCreationDate(LocalDateTime.now())
                           .withModificationDate(LocalDateTime.of(2024, 3, 1, 10, 0))
                           .withDetectedMimeType("text/plain")
                           .withDetectedExtensions(Collections.emptySet())
                           .withCategory(Category.GENERIC)
                           .withMetadata(null)
                           .build());

        files.add(FileModel.builder()
                           .withFile(new File("/test/path/file1.txt"))
                           .withIsFile(true)
                           .withFileSize(1024L)
                           .withName("file1")
                           .withExtension("txt")
                           .withAbsolutePath("/test/path/file1.txt")
                           .withCreationDate(LocalDateTime.now())
                           .withModificationDate(LocalDateTime.of(2024, 1, 1, 10, 0))
                           .withDetectedMimeType("text/plain")
                           .withDetectedExtensions(Collections.emptySet())
                           .withCategory(Category.GENERIC)
                           .withMetadata(null)
                           .build());

        files.add(FileModel.builder()
                           .withFile(new File("/test/path/file2.txt"))
                           .withIsFile(true)
                           .withFileSize(1024L)
                           .withName("file2")
                           .withExtension("txt")
                           .withAbsolutePath("/test/path/file2.txt")
                           .withCreationDate(LocalDateTime.now())
                           .withModificationDate(LocalDateTime.of(2024, 2, 1, 10, 0))
                           .withDetectedMimeType("text/plain")
                           .withDetectedExtensions(Collections.emptySet())
                           .withCategory(Category.GENERIC)
                           .withMetadata(null)
                           .build());

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_MODIFICATION_DATETIME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - sorted by modification date (oldest first)
        assertEquals("file1", results.get(0).getOriginalFile().getName());
        assertEquals("file2", results.get(1).getOriginalFile().getName());
        assertEquals("file3", results.get(2).getOriginalFile().getName());
    }

    @Test
    void testTransformBatch_SortByImageWidth() {
        // Given - images with different widths
        List<FileModel> files = List.of(
                createTestFileModelWithImageMetadata("wide", "jpg", 3000, 1000),
                createTestFileModelWithImageMetadata("narrow", "jpg", 800, 1000),
                createTestFileModelWithImageMetadata("medium", "jpg", 1920, 1000)
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.IMAGE_WIDTH)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - sorted by width (narrow to wide)
        assertEquals("narrow", results.get(0).getOriginalFile().getName());
        assertEquals("medium", results.get(1).getOriginalFile().getName());
        assertEquals("wide", results.get(2).getOriginalFile().getName());
    }

    @Test
    void testTransformBatch_SortByImageHeight() {
        // Given - images with different heights
        List<FileModel> files = List.of(
                createTestFileModelWithImageMetadata("tall", "jpg", 1000, 3000),
                createTestFileModelWithImageMetadata("short", "jpg", 1000, 600),
                createTestFileModelWithImageMetadata("medium", "jpg", 1000, 1080)
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.IMAGE_HEIGHT)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - sorted by height (short to tall)
        assertEquals("short", results.get(0).getOriginalFile().getName());
        assertEquals("medium", results.get(1).getOriginalFile().getName());
        assertEquals("tall", results.get(2).getOriginalFile().getName());
    }

    // ============================================================================
    // G. Sequential Counter Preservation Tests (Critical!)
    // ============================================================================

    @Test
    void testTransformBatch_100Files_SequentialCounter() {
        // Given - 100 files to ensure counter is truly sequential
        List<FileModel> files = IntStream.range(1, 101)
                                         .mapToObj(i -> createTestFileModel("file" + i, "txt"))
                                         .toList();

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(3)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - verify all 100 files have correct sequential numbers
        assertEquals(100, results.size());
        for (int i = 0; i < 100; i++) {
            String expected = String.format("%03d", i + 1);
            assertEquals(expected, results.get(i).getNewName(),
                         "File at index " + i + " should have sequence number " + expected);
        }
    }

    @Test
    void testTransformBatch_LargeStep_SequentialCounter() {
        // Given - verify counter increments correctly with large steps
        List<FileModel> files = IntStream.range(1, 21)
                                         .mapToObj(i -> createTestFileModel("file" + i, "txt"))
                                         .toList();

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(100)
                                              .withStepValue(50)
                                              .withPadding(4)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - verify counter: 100, 150, 200, 250, ...
        assertEquals(20, results.size());
        for (int i = 0; i < 20; i++) {
            String expected = String.format("%04d", 100 + (i * 50));
            assertEquals(expected, results.get(i).getNewName());
        }
    }

    // ============================================================================
    // H. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(3)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        PreparedFileModel result = results.get(0);
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.ADD_SEQUENCE, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(10)
                                              .withStepValue(5)
                                              .withPadding(4)
                                              .withSortSource(SortSource.FILE_SIZE)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        PreparedFileModel result = results.get(0);
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals(10, configMap.get("startNumber"));
        assertEquals(5, configMap.get("stepValue"));
        assertEquals(4, configMap.get("padding"));
        assertEquals("FILE_SIZE", configMap.get("sortSource"));
    }

    // ============================================================================
    // I. Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_NullConfig() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file", "txt")
        );

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, null);

        // Then - should return error results
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isHasError());
        assertTrue(results.get(0).getErrorMessage().isPresent());
        assertNull(results.get(0).getTransformationMeta());
    }

    @Test
    void testErrorHandling_EmptyFileList() {
        // Given
        List<FileModel> files = Collections.emptyList();

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - should return empty list
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ============================================================================
    // J. Integration Tests
    // ============================================================================

    @Test
    void testExtensionPreservation() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file1", "txt"),
                createTestFileModel("file2", "pdf"),
                createTestFileModel("file3", "jpg")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then - verify extensions are preserved
        assertEquals("txt", results.get(0).getNewExtension());
        assertEquals("pdf", results.get(1).getNewExtension());
        assertEquals("jpg", results.get(2).getNewExtension());
    }

    @Test
    void testOriginalFilePreservation() {
        // Given
        FileModel original = createTestFileModel("original", "txt");
        List<FileModel> files = List.of(original);

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertSame(original, results.get(0).getOriginalFile());
        assertEquals("original", results.get(0).getOriginalFile().getName());
    }

    @Test
    void testNeedsRename_True() {
        // Given
        List<FileModel> files = List.of(
                createTestFileModel("file", "txt")
        );

        SequenceConfig config = SequenceConfig.builder()
                                              .withStartNumber(1)
                                              .withStepValue(1)
                                              .withPadding(0)
                                              .withSortSource(SortSource.FILE_NAME)
                                              .build();

        // When
        List<PreparedFileModel> results = transformer.transformBatch(files, config);

        // Then
        assertTrue(results.get(0).needsRename());
        assertNotEquals(results.get(0).getOldFullName(), results.get(0).getNewFullName());
    }
}
