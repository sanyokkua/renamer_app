package ua.renamer.app.core.v2.service.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.SortSource;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.SequenceConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for SequenceTransformer using real files and full orchestrator pipeline.
 * CRITICAL: Tests sequential ordering with various sort criteria.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SequenceTransformationIntegrationTest extends BaseTransformationIntegrationTest {

    // ==================== FILE_NAME SORT TESTS ====================

    @Test
    void testSequence_SortByFileName_BasicOrder() throws IOException {
        // Create files in specific order: charlie, alpha, bravo
        // After sort by name: alpha, bravo, charlie → 001, 002, 003
        createTestFile("charlie.txt");
        createTestFile("alpha.txt");
        createTestFile("bravo.txt");

        List<File> files = getAllFilesInTempDir();
        assertEquals(3, files.size());

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        // Verify results
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify files renamed in correct alphabetical order
        assertFileExists("001.txt");  // alpha
        assertFileExists("002.txt");  // bravo
        assertFileExists("003.txt");  // charlie

        // Original files should not exist
        assertFileNotExists("alpha.txt");
        assertFileNotExists("bravo.txt");
        assertFileNotExists("charlie.txt");
    }

    @Test
    void testSequence_SortByFileName_NumericNames() throws IOException {
        // Create files with numeric names in wrong order
        createTestFile("file_10.txt");
        createTestFile("file_2.txt");
        createTestFile("file_1.txt");

        List<File> files = getAllFilesInTempDir();

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(100)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Sorted alphabetically: file_1, file_10, file_2
        assertFileExists("100.txt");  // file_1
        assertFileExists("101.txt");  // file_10
        assertFileExists("102.txt");  // file_2
    }

    // ==================== FILE_SIZE SORT TESTS ====================

    @Test
    void testSequence_SortByFileSize() throws IOException {
        // Create files with different sizes
        createTestFileWithSize("small.txt", 100);   // 100 bytes
        createTestFileWithSize("large.txt", 1000);  // 1000 bytes
        createTestFileWithSize("medium.txt", 500);  // 500 bytes

        List<File> files = getAllFilesInTempDir();

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(2)
                .withSortSource(SortSource.FILE_SIZE)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Sorted by size: small (100), medium (500), large (1000)
        assertFileExists("01.txt");  // small
        assertFileExists("02.txt");  // medium
        assertFileExists("03.txt");  // large

        // Verify file sizes match
        assertEquals(100, tempDir.resolve("01.txt").toFile().length());
        assertEquals(500, tempDir.resolve("02.txt").toFile().length());
        assertEquals(1000, tempDir.resolve("03.txt").toFile().length());
    }

    // ==================== LARGE BATCH TESTS ====================

    @Test
    void testSequence_100Files_EnsureCounterWorks() throws IOException {
        // Create 100 files to ensure counter works correctly
        List<File> files = createTestFiles("item", "dat", 100);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                createTrackingCallback()
        );

        assertEquals(100, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify all 100 files exist with correct numbering
        for (int i = 1; i <= 100; i++) {
            String expected = String.format("%03d.dat", i);
            assertFileExists(expected);
        }

        // Verify original files don't exist
        for (int i = 1; i <= 100; i++) {
            assertFileNotExists("item_" + i + ".dat");
        }
    }

    @Test
    void testSequence_50Files_StartAt1000() throws IOException {
        List<File> files = createTestFiles("photo", "jpg", 50);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1000)
                .withStepValue(1)
                .withPadding(4)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(50, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify numbering starts at 1000
        assertFileExists("1000.jpg");
        assertFileExists("1001.jpg");
        assertFileExists("1049.jpg");
    }

    // ==================== PADDING TESTS ====================

    @Test
    void testSequence_NoPadding() throws IOException {
        List<File> files = createTestFilesWithNames("a.txt", "b.txt", "c.txt");

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(0)  // No padding
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // No padding: 1, 2, 3
        assertFileExists("1.txt");
        assertFileExists("2.txt");
        assertFileExists("3.txt");
    }

    @Test
    void testSequence_Padding2Digits() throws IOException {
        List<File> files = createTestFiles("file", "txt", 5);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(2)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(5, results.size());

        // 2-digit padding: 01, 02, 03, 04, 05
        assertFileExists("01.txt");
        assertFileExists("02.txt");
        assertFileExists("03.txt");
        assertFileExists("04.txt");
        assertFileExists("05.txt");
    }

    @Test
    void testSequence_Padding3Digits() throws IOException {
        List<File> files = createTestFiles("doc", "pdf", 10);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(0)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(10, results.size());

        // 3-digit padding starting at 0
        assertFileExists("000.pdf");
        assertFileExists("001.pdf");
        assertFileExists("009.pdf");
    }

    @Test
    void testSequence_Padding4Digits() throws IOException {
        List<File> files = createTestFiles("image", "png", 15);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(4)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(15, results.size());

        // 4-digit padding
        assertFileExists("0001.png");
        assertFileExists("0010.png");
        assertFileExists("0015.png");
    }

    // ==================== STEP VALUE TESTS ====================

    @Test
    void testSequence_StepValue2() throws IOException {
        List<File> files = createTestFilesWithNames("a.txt", "b.txt", "c.txt", "d.txt");

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(10)
                .withStepValue(2)  // Increment by 2
                .withPadding(2)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(4, results.size());

        // Start at 10, step by 2: 10, 12, 14, 16
        assertFileExists("10.txt");
        assertFileExists("12.txt");
        assertFileExists("14.txt");
        assertFileExists("16.txt");
    }

    @Test
    void testSequence_StepValue5() throws IOException {
        List<File> files = createTestFiles("item", "dat", 5);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(0)
                .withStepValue(5)
                .withPadding(2)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(5, results.size());

        // 0, 5, 10, 15, 20
        assertFileExists("00.dat");
        assertFileExists("05.dat");
        assertFileExists("10.dat");
        assertFileExists("15.dat");
        assertFileExists("20.dat");
    }

    @Test
    void testSequence_StepValue10() throws IOException {
        List<File> files = createTestFilesWithNames("x.txt", "y.txt", "z.txt");

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(100)
                .withStepValue(10)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(3, results.size());

        // 100, 110, 120
        assertFileExists("100.txt");
        assertFileExists("110.txt");
        assertFileExists("120.txt");
    }

    // ==================== START NUMBER TESTS ====================

    @Test
    void testSequence_StartAtZero() throws IOException {
        List<File> files = createTestFiles("test", "txt", 3);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(0)
                .withStepValue(1)
                .withPadding(2)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(3, results.size());

        assertFileExists("00.txt");
        assertFileExists("01.txt");
        assertFileExists("02.txt");
    }

    @Test
    void testSequence_StartAt1000() throws IOException {
        List<File> files = createTestFilesWithNames("a.doc", "b.doc");

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1000)
                .withStepValue(1)
                .withPadding(4)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(2, results.size());

        assertFileExists("1000.doc");
        assertFileExists("1001.doc");
    }

    // ==================== DIFFERENT EXTENSIONS ====================

    @Test
    void testSequence_MixedExtensions() throws IOException {
        // Create files with different extensions
        createTestFile("document.pdf");
        createTestFile("image.jpg");
        createTestFile("video.mp4");
        createTestFile("audio.mp3");

        List<File> files = getAllFilesInTempDir();

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(2)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(4, results.size());

        // Extensions are preserved
        // Sorted: audio.mp3, document.pdf, image.jpg, video.mp4
        assertFileExists("01.mp3");
        assertFileExists("02.pdf");
        assertFileExists("03.jpg");
        assertFileExists("04.mp4");
    }

    // ==================== PROGRESS TRACKING ====================

    @Test
    void testSequence_ProgressCallback() throws IOException {
        List<File> files = createTestFiles("file", "txt", 20);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(2)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                createTrackingCallback()
        );

        assertEquals(20, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify progress was tracked
        assertTrue(progressMax.get() > 0, "Progress max should be set");
    }

    // ==================== ORDER VERIFICATION TESTS ====================

    @Test
    void testSequence_VerifyCorrectOrder_ByName() throws IOException {
        // Create files with specific names to verify order
        createTestFile("zebra.txt");
        createTestFile("alpha.txt");
        createTestFile("mike.txt");
        createTestFile("bravo.txt");

        List<File> files = getAllFilesInTempDir();

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(1)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        // Sorted alphabetically: alpha, bravo, mike, zebra
        // So: alpha→1, bravo→2, mike→3, zebra→4
        assertEquals(4, results.size());

        assertFileExists("1.txt");  // alpha
        assertFileExists("2.txt");  // bravo
        assertFileExists("3.txt");  // mike
        assertFileExists("4.txt");  // zebra
    }

    @Test
    void testSequence_VerifyCorrectOrder_BySize() throws IOException {
        // Create files with specific sizes
        createTestFileWithSize("file_a.txt", 5000);
        createTestFileWithSize("file_b.txt", 1000);
        createTestFileWithSize("file_c.txt", 3000);

        List<File> files = getAllFilesInTempDir();

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(1)
                .withSortSource(SortSource.FILE_SIZE)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        // Sorted by size: 1000, 3000, 5000
        assertEquals(3, results.size());

        // Verify by checking file sizes
        assertEquals(1000, tempDir.resolve("1.txt").toFile().length());
        assertEquals(3000, tempDir.resolve("2.txt").toFile().length());
        assertEquals(5000, tempDir.resolve("3.txt").toFile().length());
    }

    // ==================== EDGE CASES ====================

    @Test
    void testSequence_SingleFile() throws IOException {
        File file = createTestFile("only.txt");

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                List.of(file),
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());

        assertFileExists("001.txt");
        assertFileNotExists("only.txt");
    }

    @Test
    void testSequence_NoExtensionFiles() throws IOException {
        // Files without extensions
        createTestFile("README");
        createTestFile("LICENSE");
        createTestFile("Makefile");

        List<File> files = getAllFilesInTempDir();

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(2)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_SEQUENCE,
                config,
                null
        );

        assertEquals(3, results.size());

        // Files without extension keep no extension
        // Sorted: LICENSE, Makefile, README
        assertFileExists("01");
        assertFileExists("02");
        assertFileExists("03");
    }
}
