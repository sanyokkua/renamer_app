package ua.renamer.app.core.v2.service.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.AddTextConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for AddTextTransformer using real files and full orchestrator pipeline.
 * Tests adding text (prefix/suffix) to multiple files with various scenarios.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddTextTransformationIntegrationTest extends BaseTransformationIntegrationTest {

    // ==================== PREFIX TESTS ====================

    @Test
    void testAddPrefix_SingleFile() throws IOException {
        // Create test file
        File file = createTestFile("document.txt");

        // Configure transformation: add "NEW_" prefix
        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("NEW_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        // Execute
        List<RenameResult> results = orchestrator.execute(
                List.of(file),
                TransformationMode.ADD_TEXT,
                config,
                createNoOpCallback()
        );

        // Verify results
        assertEquals(1, results.size());
        RenameResult result = results.get(0);
        assertTrue(result.isSuccess());
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertEquals("document.txt", result.getOriginalFileName());
        assertEquals("NEW_document.txt", result.getNewFileName());

        // Verify physical file was renamed
        assertFileNotExists("document.txt");
        assertFileExists("NEW_document.txt");
    }

    @Test
    void testAddPrefix_MultipleFiles() throws IOException {
        // Create test files
        List<File> files = createTestFilesWithNames("alpha.txt", "beta.txt", "gamma.txt");

        // Configure transformation
        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("PREFIX_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        // Execute
        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        // Verify results
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify each file
        assertFileExists("PREFIX_alpha.txt");
        assertFileExists("PREFIX_beta.txt");
        assertFileExists("PREFIX_gamma.txt");

        // Verify original files don't exist
        assertFileNotExists("alpha.txt");
        assertFileNotExists("beta.txt");
        assertFileNotExists("gamma.txt");
    }

    @Test
    void testAddPrefix_WithNumbers() throws IOException {
        // Create files with numbers
        List<File> files = createTestFilesWithNames("001.txt", "002.txt", "003.txt");

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("file_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(3, results.size());
        assertFileExists("file_001.txt");
        assertFileExists("file_002.txt");
        assertFileExists("file_003.txt");
    }

    // ==================== SUFFIX TESTS ====================

    @Test
    void testAddSuffix_SingleFile() throws IOException {
        File file = createTestFile("report.pdf");

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("_final")
                                            .withPosition(ItemPosition.END)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                List.of(file),
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals("report_final.pdf", results.get(0).getNewFileName());

        assertFileExists("report_final.pdf");
        assertFileNotExists("report.pdf");
    }

    @Test
    void testAddSuffix_MultipleFiles() throws IOException {
        List<File> files = createTestFiles("image", "jpg", 5);

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("_backup")
                                            .withPosition(ItemPosition.END)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify renamed files
        for (int i = 1; i <= 5; i++) {
            assertFileExists("image_" + i + "_backup.jpg");
            assertFileNotExists("image_" + i + ".jpg");
        }
    }

    // ==================== SPECIAL CHARACTERS TESTS ====================

    @Test
    void testAddText_WithSpecialCharacters() throws IOException {
        List<File> files = createTestFilesWithNames("test1.txt", "test2.txt");

        // Test with special characters: parentheses, brackets, dashes
        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("[2026-02-12]_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("[2026-02-12]_test1.txt");
        assertFileExists("[2026-02-12]_test2.txt");
    }

    @Test
    void testAddText_WithSpaces() throws IOException {
        File file = createTestFile("document.docx");

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("Draft Version ")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                List.of(file),
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertTrue(results.get(0).isSuccess());
        assertFileExists("Draft Version document.docx");
    }

    @Test
    void testAddText_WithUnderscores() throws IOException {
        List<File> files = createTestFilesWithNames("file1.txt", "file2.txt");

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("_v2")
                                            .withPosition(ItemPosition.END)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(2, results.size());
        assertFileExists("file1_v2.txt");
        assertFileExists("file2_v2.txt");
    }

    // ==================== UNICODE TESTS ====================

    @Test
    void testAddText_WithUnicodeCharacters() throws IOException {
        List<File> files = createTestFilesWithNames("doc1.txt", "doc2.txt");

        // Test with Unicode: Japanese, emoji, Cyrillic
        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("日本語_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("日本語_doc1.txt");
        assertFileExists("日本語_doc2.txt");
    }

    @Test
    void testAddText_WithCyrillicCharacters() throws IOException {
        File file = createTestFile("file.txt");

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("Документ_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                List.of(file),
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertTrue(results.get(0).isSuccess());
        assertFileExists("Документ_file.txt");
    }

    @Test
    void testAddText_WithEmoji() throws IOException {
        File file = createTestFile("photo.jpg");

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("📷_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                List.of(file),
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertTrue(results.get(0).isSuccess());
        // Note: Some file systems may not support emoji in filenames
        // This test verifies the transformer handles it, but result may vary by OS
    }

    // ==================== EMPTY/NULL TEXT TESTS ====================

    @Test
    void testAddText_EmptyString() throws IOException {
        File file = createTestFile("original.txt");

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                List.of(file),
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        // Empty string means no change, so status should be SKIPPED
        assertEquals(1, results.size());
        assertEquals(RenameStatus.SKIPPED, results.get(0).getStatus());

        // Original file should still exist (not renamed)
        assertFileExists("original.txt");
    }

    // ==================== PROGRESS CALLBACK TESTS ====================

    @Test
    void testAddText_ProgressCallback_SmallBatch() throws IOException {
        List<File> files = createTestFiles("file", "txt", 5);

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("test_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify progress was tracked
        // Note: Multiple phases in pipeline, so max may be called multiple times
        assertTrue(progressMax.get() > 0, "Progress max should be set");
    }

    @Test
    void testAddText_ProgressCallback_LargeBatch() throws IOException {
        // Create 50 files to test with larger batch
        List<File> files = createTestFiles("item", "dat", 50);

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("processed_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        assertEquals(50, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify all files were renamed
        for (int i = 1; i <= 50; i++) {
            assertFileExists("processed_item_" + i + ".dat");
        }
    }

    // ==================== DIFFERENT EXTENSIONS TESTS ====================

    @Test
    void testAddText_MixedExtensions() throws IOException {
        List<File> files = createTestFilesWithNames(
                "document.pdf",
                "image.jpg",
                "video.mp4",
                "audio.mp3",
                "data.json"
        );

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("archive_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("archive_document.pdf");
        assertFileExists("archive_image.jpg");
        assertFileExists("archive_video.mp4");
        assertFileExists("archive_audio.mp3");
        assertFileExists("archive_data.json");
    }

    @Test
    void testAddText_NoExtension() throws IOException {
        // Files without extensions
        List<File> files = createTestFilesWithNames("README", "LICENSE", "Makefile");

        AddTextConfig config = AddTextConfig.builder()
                                            .withTextToAdd("OLD_")
                                            .withPosition(ItemPosition.BEGIN)
                                            .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("OLD_README");
        assertFileExists("OLD_LICENSE");
        assertFileExists("OLD_Makefile");
    }
}
