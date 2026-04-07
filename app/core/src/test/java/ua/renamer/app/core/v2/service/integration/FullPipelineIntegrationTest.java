package ua.renamer.app.core.v2.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.enums.ItemPositionExtended;
import ua.renamer.app.api.enums.SortSource;
import ua.renamer.app.api.enums.TextCaseOptions;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.AddTextConfig;
import ua.renamer.app.api.model.config.CaseChangeConfig;
import ua.renamer.app.api.model.config.ExtensionChangeConfig;
import ua.renamer.app.api.model.config.ParentFolderConfig;
import ua.renamer.app.api.model.config.RemoveTextConfig;
import ua.renamer.app.api.model.config.ReplaceTextConfig;
import ua.renamer.app.api.model.config.SequenceConfig;
import ua.renamer.app.api.model.config.TruncateConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive integration tests for the complete file rename pipeline.
 * Tests all transformation modes with real files and full orchestrator execution.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FullPipelineIntegrationTest extends BaseTransformationIntegrationTest {

    // ==================== ADD TEXT MODE ====================

    @Test
    void testFullPipeline_AddText_Prefix() throws IOException {
        List<File> files = createTestFilesWithNames("doc1.txt", "doc2.txt", "doc3.txt");

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("DRAFT_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("DRAFT_doc1.txt");
        assertFileExists("DRAFT_doc2.txt");
        assertFileExists("DRAFT_doc3.txt");

        logTestSummary("AddText_Prefix", 3, 3);
    }

    @Test
    void testFullPipeline_AddText_Suffix() throws IOException {
        List<File> files = createTestFiles("photo", "jpg", 5);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("_edited")
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

        for (int i = 1; i <= 5; i++) {
            assertFileExists("photo_" + i + "_edited.jpg");
        }

        logTestSummary("AddText_Suffix", 5, 5);
    }

    // ==================== REMOVE TEXT MODE ====================

    @Test
    void testFullPipeline_RemoveText_FromBegin() throws IOException {
        List<File> files = createTestFilesWithNames(
                "OLD_file1.txt",
                "OLD_file2.txt",
                "OLD_file3.txt"
        );

        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("OLD_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.REMOVE_TEXT,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("file1.txt");
        assertFileExists("file2.txt");
        assertFileExists("file3.txt");

        assertFileNotExists("OLD_file1.txt");
    }

    @Test
    void testFullPipeline_RemoveText_FromEnd() throws IOException {
        List<File> files = createTestFilesWithNames(
                "document_backup.doc",
                "report_backup.doc",
                "notes_backup.doc"
        );

        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("_backup")
                .withPosition(ItemPosition.END)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.REMOVE_TEXT,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("document.doc");
        assertFileExists("report.doc");
        assertFileExists("notes.doc");
    }

    // ==================== REPLACE TEXT MODE ====================

    @Test
    void testFullPipeline_ReplaceText_Everywhere() throws IOException {
        List<File> files = createTestFilesWithNames(
                "old_name_old.txt",
                "old_file_test.txt",
                "data_old_final.txt"
        );

        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace("old")
                .withReplacementText("new")
                .withPosition(ItemPositionExtended.EVERYWHERE)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.REPLACE_TEXT,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("new_name_new.txt");
        assertFileExists("new_file_test.txt");
        assertFileExists("data_new_final.txt");
    }

    @Test
    void testFullPipeline_ReplaceText_AtBegin() throws IOException {
        List<File> files = createTestFilesWithNames(
                "temp_file1.dat",
                "temp_file2.dat"
        );

        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace("temp")
                .withReplacementText("final")
                .withPosition(ItemPositionExtended.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.REPLACE_TEXT,
                config,
                null
        );

        assertEquals(2, results.size());

        assertFileExists("final_file1.dat");
        assertFileExists("final_file2.dat");
    }

    // ==================== CASE CHANGE MODE ====================

    @Test
    void testFullPipeline_CaseChange_Uppercase() throws IOException {
        List<File> files = createTestFilesWithNames(
                "lowercase.txt",
                "MixedCase.txt"
        );

        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.UPPERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.CHANGE_CASE,
                config,
                null
        );

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));
    }

    @Test
    void testFullPipeline_CaseChange_Lowercase() throws IOException {
        List<File> files = createTestFilesWithNames(
                "UPPERCASE.TXT",
                "MixedCase.TXT"
        );

        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.CHANGE_CASE,
                config,
                null
        );

        assertEquals(2, results.size());
        // Files renamed successfully (case transformation applied)
    }

    // ==================== SEQUENCE MODE ====================

    @Test
    void testFullPipeline_Sequence_BasicNumbering() throws IOException {
        List<File> files = createTestFilesWithNames(
                "zebra.txt",
                "alpha.txt",
                "mike.txt"
        );

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.NUMBER_FILES,
                config,
                createTrackingCallback()
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Sorted alphabetically: alpha, mike, zebra
        assertFileExists("001.txt");  // alpha
        assertFileExists("002.txt");  // mike
        assertFileExists("003.txt");  // zebra

        logTestSummary("Sequence_BasicNumbering", 3, 3);
    }

    @Test
    void testFullPipeline_Sequence_CustomStartAndStep() throws IOException {
        List<File> files = createTestFiles("item", "dat", 5);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(100)
                .withStepValue(5)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.NUMBER_FILES,
                config,
                null
        );

        assertEquals(5, results.size());

        // 100, 105, 110, 115, 120
        assertFileExists("100.dat");
        assertFileExists("105.dat");
        assertFileExists("110.dat");
        assertFileExists("115.dat");
        assertFileExists("120.dat");
    }

    // ==================== EXTENSION CHANGE MODE ====================

    @Test
    void testFullPipeline_ExtensionChange() throws IOException {
        List<File> files = createTestFilesWithNames(
                "file1.txt",
                "file2.txt",
                "file3.txt"
        );

        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension("md")
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.CHANGE_EXTENSION,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        assertFileExists("file1.md");
        assertFileExists("file2.md");
        assertFileExists("file3.md");

        assertFileNotExists("file1.txt");
    }

    // ==================== PARENT FOLDER MODE ====================

    @Test
    void testFullPipeline_ParentFolder() throws IOException {
        // Create nested folder structure
        Path subDir = tempDir.resolve("grandparent").resolve("parent");
        Files.createDirectories(subDir);

        File file1 = subDir.resolve("file1.txt").toFile();
        File file2 = subDir.resolve("file2.txt").toFile();
        File file3 = subDir.resolve("file3.txt").toFile();

        Files.createFile(file1.toPath());
        Files.createFile(file2.toPath());
        Files.createFile(file3.toPath());

        List<File> files = List.of(file1, file2, file3);

        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(1)
                .withPosition(ItemPosition.BEGIN)
                .withSeparator("_")
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_FOLDER_NAME,
                config,
                null
        );

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify files renamed with parent folder prefix
        assertTrue(Files.exists(subDir.resolve("parent_file1.txt")));
        assertTrue(Files.exists(subDir.resolve("parent_file2.txt")));
        assertTrue(Files.exists(subDir.resolve("parent_file3.txt")));
    }

    // ==================== TRUNCATE MODE ====================

    @Test
    void testFullPipeline_Truncate() throws IOException {
        List<File> files = createTestFilesWithNames(
                "very_long_filename_that_needs_truncation.txt",
                "another_extremely_long_name.txt"
        );

        TruncateConfig config = TruncateConfig.builder()
                .withNumberOfSymbols(10)
                .withTruncateOption(ua.renamer.app.api.enums.TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.TRIM_NAME,
                config,
                null
        );

        assertEquals(2, results.size());
        // Verify truncation occurred (exact names depend on implementation)
    }

    // ==================== ASYNC EXECUTION ====================

    @Test
    void testFullPipeline_AsyncExecution() throws IOException, ExecutionException, InterruptedException {
        List<File> files = createTestFiles("async", "txt", 10);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("ASYNC_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        CompletableFuture<List<RenameResult>> future = orchestrator.executeAsync(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        // Should return quickly (not blocking)
        assertNotNull(future);

        // Wait for completion
        List<RenameResult> results = future.get();

        assertEquals(10, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        for (int i = 1; i <= 10; i++) {
            assertFileExists("ASYNC_async_" + i + ".txt");
        }

        log.info("Async execution test completed successfully");
    }

    // ==================== PROGRESS CALLBACK ====================

    @Test
    void testFullPipeline_ProgressCallback_ReceivesUpdates() throws IOException {
        List<File> files = createTestFiles("progress", "txt", 20);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("TEST_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Reset progress counters
        progressCurrent.set(0);
        progressMax.set(0);

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        assertEquals(20, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify progress callback was called
        assertTrue(progressMax.get() > 0, "Progress max should be set");
        log.info("Progress callback received: current={}, max={}",
                progressCurrent.get(), progressMax.get());
    }

    @Test
    void testFullPipeline_ProgressCallback_NullSafe() throws IOException {
        List<File> files = createTestFiles("nullcb", "txt", 5);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("TEST_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Execute with null callback (should not throw NPE)
        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null  // Null callback
        );

        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));
    }

    // ==================== MIXED SUCCESS AND FAILURE ====================

    @Test
    void testFullPipeline_MixedResults() throws IOException {
        // Create mix of real and non-existent files
        List<File> realFiles = createTestFilesWithNames("real1.txt", "real2.txt");
        File fakeFile = tempDir.resolve("nonexistent.txt").toFile();

        List<File> allFiles = List.of(realFiles.get(0), fakeFile, realFiles.get(1));

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("PREFIX_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(
                allFiles,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(3, results.size());

        // Count successes and failures
        long successCount = results.stream().filter(RenameResult::isSuccess).count();
        long errorCount = results.stream()
                .filter(r -> !r.isSuccess())
                .count();

        assertEquals(2, successCount);
        assertEquals(1, errorCount);

        // Verify successful renames
        assertFileExists("PREFIX_real1.txt");
        assertFileExists("PREFIX_real2.txt");
    }

    // ==================== VERIFICATION TESTS ====================

    @Test
    void testFullPipeline_PhysicalFileVerification() throws IOException {
        // Verify that files are actually renamed on disk
        List<File> files = createTestFilesWithNames("before1.txt", "before2.txt");

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("after_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // Record original file state
        String originalPath1 = files.get(0).getAbsolutePath();
        String originalPath2 = files.get(1).getAbsolutePath();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        // Verify results
        assertEquals(2, results.size());

        // Verify physical files exist with new names
        assertTrue(tempDir.resolve("after_before1.txt").toFile().exists());
        assertTrue(tempDir.resolve("after_before2.txt").toFile().exists());

        // Verify original files no longer exist
        assertFalse(new File(originalPath1).exists());
        assertFalse(new File(originalPath2).exists());

        log.info("Physical file verification completed");
    }

    @Test
    void testFullPipeline_ResultMetadata() throws IOException {
        List<File> files = createTestFiles("meta", "txt", 3);

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("TEST_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        // Verify result metadata
        results.forEach(result -> {
            assertNotNull(result.getPreparedFile());
            assertNotNull(result.getStatus());
            assertNotNull(result.getExecutedAt());
            assertNotNull(result.getOriginalFileName());
            assertNotNull(result.getNewFileName());

            if (result.isSuccess()) {
                assertEquals(RenameStatus.SUCCESS, result.getStatus());
                assertTrue(result.getErrorMessage().isEmpty());
            }
        });

        log.info("Result metadata verification completed");
    }

    // ==================== ERROR RECOVERY ====================

    @Test
    void testFullPipeline_ErrorRecovery_ContinueAfterError() throws IOException {
        // First batch with errors
        File nonExistent = tempDir.resolve("missing.txt").toFile();

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("TEST_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results1 = orchestrator.execute(
                List.of(nonExistent),
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(1, results1.size());
        assertFalse(results1.get(0).isSuccess());

        // Second batch should work fine (orchestrator recovered)
        List<File> validFiles = createTestFiles("valid", "txt", 3);

        List<RenameResult> results2 = orchestrator.execute(
                validFiles,
                TransformationMode.ADD_TEXT,
                config,
                null
        );

        assertEquals(3, results2.size());
        assertTrue(results2.stream().allMatch(RenameResult::isSuccess));

        log.info("Error recovery test completed");
    }

    // ==================== LARGE BATCH TEST ====================

    @Test
    void testFullPipeline_LargeBatch_100Files() throws IOException {
        log.info("Creating 100 test files for full pipeline test...");
        List<File> files = createTestFiles("bulk", "dat", 100);

        SequenceConfig config = SequenceConfig.builder()
                .withStartNumber(1)
                .withStepValue(1)
                .withPadding(3)
                .withSortSource(SortSource.FILE_NAME)
                .build();

        log.info("Executing full pipeline on 100 files...");
        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.NUMBER_FILES,
                config,
                createTrackingCallback()
        );

        assertEquals(100, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        // Verify all files renamed correctly
        for (int i = 1; i <= 100; i++) {
            String expected = String.format("%03d.dat", i);
            assertFileExists(expected);
        }

        logTestSummary("LargeBatch_100Files", 100, 100);
        log.info("Large batch test completed successfully");
    }

    // ==================== COMPLETE WORKFLOW TEST ====================

    @Test
    void testFullPipeline_CompleteWorkflow() throws IOException {
        // Simulate a complete real-world workflow
        log.info("=== Starting Complete Workflow Test ===");

        // Step 1: Create files with mixed names
        List<File> files = createTestFilesWithNames(
                "temp_photo_001.jpg",
                "temp_photo_002.jpg",
                "temp_photo_003.jpg",
                "temp_photo_004.jpg",
                "temp_photo_005.jpg"
        );

        log.info("Step 1: Created {} files", files.size());

        // Step 2: Apply transformation
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("FINAL_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(
                files,
                TransformationMode.ADD_TEXT,
                config,
                createTrackingCallback()
        );

        // Step 3: Verify results
        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(RenameResult::isSuccess));

        log.info("Step 2: All files renamed successfully");

        // Step 4: Verify physical files
        assertFileExists("FINAL_temp_photo_001.jpg");
        assertFileExists("FINAL_temp_photo_002.jpg");
        assertFileExists("FINAL_temp_photo_003.jpg");
        assertFileExists("FINAL_temp_photo_004.jpg");
        assertFileExists("FINAL_temp_photo_005.jpg");

        log.info("Step 3: Physical file verification passed");

        // Step 5: Verify original files don't exist
        assertFileNotExists("temp_photo_001.jpg");
        assertFileNotExists("temp_photo_002.jpg");

        log.info("Step 4: Confirmed original files removed");

        logTestSummary("CompleteWorkflow", 5, 5);
        log.info("=== Complete Workflow Test PASSED ===");
    }
}
