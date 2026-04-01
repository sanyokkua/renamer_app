package ua.renamer.app.core.v2.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.interfaces.DateTimeUtils;
import ua.renamer.app.api.interfaces.FileMetadataMapper;
import ua.renamer.app.api.interfaces.FileUtils;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.service.FileRenameOrchestrator;
import ua.renamer.app.api.service.ProgressCallback;
import ua.renamer.app.core.service.validator.impl.NameValidator;
import ua.renamer.app.core.v2.mapper.ThreadAwareFileMapper;
import ua.renamer.app.core.v2.service.DuplicateNameResolver;
import ua.renamer.app.core.v2.service.RenameExecutionService;
import ua.renamer.app.core.v2.service.impl.DuplicateNameResolverImpl;
import ua.renamer.app.core.v2.service.impl.FileRenameOrchestratorImpl;
import ua.renamer.app.core.v2.service.impl.RenameExecutionServiceImpl;
import ua.renamer.app.core.v2.service.transformation.*;
import ua.renamer.app.core.v2.util.TestDateTimeUtils;
import ua.renamer.app.core.v2.util.TestFileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for integration tests that use real files and the full orchestrator pipeline.
 * Provides helper methods for file creation, assertions, and orchestrator setup.
 * Manually assembles components instead of using Guice to avoid complex DI setup.
 */
@Slf4j
public abstract class BaseTransformationIntegrationTest {

    @TempDir
    protected Path tempDir;

    protected FileRenameOrchestrator orchestrator;

    // Progress tracking for tests
    protected AtomicInteger progressCurrent = new AtomicInteger(0);
    protected AtomicInteger progressMax = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        log.info("Setting up integration test with temp directory: {}", tempDir);

        // Manually assemble components (simpler than complex Guice setup)
        DateTimeUtils dateTimeUtils = new TestDateTimeUtils();
        FileUtils fileUtils = new TestFileUtils(dateTimeUtils);

        // Simple mock metadata mapper for testing (doesn't need full metadata extraction)
        FileMetadataMapper mockMetadataMapper = (file, category, mimeType) -> FileMeta.empty();

        ThreadAwareFileMapper fileMapper = new ThreadAwareFileMapper(fileUtils, mockMetadataMapper);
        DuplicateNameResolver duplicateResolver = new DuplicateNameResolverImpl();
        RenameExecutionService renameExecutor = new RenameExecutionServiceImpl(new NameValidator());

        // Create all transformers
        AddTextTransformer addTextTransformer = new AddTextTransformer();
        RemoveTextTransformer removeTextTransformer = new RemoveTextTransformer();
        ReplaceTextTransformer replaceTextTransformer = new ReplaceTextTransformer();
        CaseChangeTransformer caseChangeTransformer = new CaseChangeTransformer();
        DateTimeTransformer dateTimeTransformer = new DateTimeTransformer(dateTimeUtils);
        ImageDimensionsTransformer imageDimensionsTransformer = new ImageDimensionsTransformer();
        SequenceTransformer sequenceTransformer = new SequenceTransformer();
        ParentFolderTransformer parentFolderTransformer = new ParentFolderTransformer();
        TruncateTransformer truncateTransformer = new TruncateTransformer();
        ExtensionChangeTransformer extensionChangeTransformer = new ExtensionChangeTransformer();

        orchestrator = new FileRenameOrchestratorImpl(
                fileMapper,
                duplicateResolver,
                renameExecutor,
                addTextTransformer,
                removeTextTransformer,
                replaceTextTransformer,
                caseChangeTransformer,
                dateTimeTransformer,
                imageDimensionsTransformer,
                sequenceTransformer,
                parentFolderTransformer,
                truncateTransformer,
                extensionChangeTransformer
        );

        // Reset progress counters
        progressCurrent.set(0);
        progressMax.set(0);
    }

    @AfterEach
    void tearDown() throws IOException {
        log.info("Cleaning up test directory: {}", tempDir);
        // TempDir is automatically cleaned by JUnit 5
    }

    // ==================== FILE CREATION HELPERS ====================

    /**
     * Create an empty test file with the given name.
     */
    protected File createTestFile(String filename) throws IOException {
        Path filePath = tempDir.resolve(filename);
        Files.createFile(filePath);
        File file = filePath.toFile();
        assertTrue(file.exists(), "Created file should exist: " + filename);
        log.debug("Created test file: {}", file.getAbsolutePath());
        return file;
    }

    /**
     * Create a test file with specific size (filled with 'A' characters).
     */
    protected File createTestFileWithSize(String filename, long sizeInBytes) throws IOException {
        Path filePath = tempDir.resolve(filename);
        byte[] data = new byte[(int) sizeInBytes];
        for (int i = 0; i < data.length; i++) {
            data[i] = 'A';
        }
        Files.write(filePath, data);
        File file = filePath.toFile();
        assertTrue(file.exists(), "Created file should exist: " + filename);
        assertEquals(sizeInBytes, file.length(), "File size should match");
        log.debug("Created test file with size {}: {}", sizeInBytes, file.getAbsolutePath());
        return file;
    }

    /**
     * Create multiple test files with sequential names.
     * Example: createTestFiles("file", "txt", 3) → file_1.txt, file_2.txt, file_3.txt
     */
    protected List<File> createTestFiles(String baseName, String extension, int count) throws IOException {
        List<File> files = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String filename = baseName + "_" + i + "." + extension;
            files.add(createTestFile(filename));
        }
        log.debug("Created {} test files", count);
        return files;
    }

    /**
     * Create multiple test files with custom names.
     */
    protected List<File> createTestFilesWithNames(String... filenames) throws IOException {
        List<File> files = new ArrayList<>();
        for (String filename : filenames) {
            files.add(createTestFile(filename));
        }
        log.debug("Created {} test files with custom names", filenames.length);
        return files;
    }

    // ==================== FILE ASSERTION HELPERS ====================

    /**
     * Assert that a file exists in the temp directory.
     */
    protected void assertFileExists(String filename) {
        Path path = tempDir.resolve(filename);
        assertTrue(Files.exists(path), "File should exist: " + filename);
    }

    /**
     * Assert that a file does not exist in the temp directory.
     */
    protected void assertFileNotExists(String filename) {
        Path path = tempDir.resolve(filename);
        assertFalse(Files.exists(path), "File should not exist: " + filename);
    }


    /**
     * Get all files in the temp directory (non-recursive).
     */
    protected List<File> getAllFilesInTempDir() throws IOException {
        return Files.list(tempDir)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .toList();
    }

    /**
     * Count files in the temp directory.
     */
    protected long countFilesInTempDir() throws IOException {
        return Files.list(tempDir)
                .filter(Files::isRegularFile)
                .count();
    }

    // ==================== PROGRESS CALLBACK HELPERS ====================

    /**
     * Create a progress callback that tracks progress for testing.
     */
    protected ProgressCallback createTrackingCallback() {
        return (current, max) -> {
            progressCurrent.set(current);
            progressMax.set(max);
            log.debug("Progress: {}/{}", current, max);
        };
    }

    /**
     * Create a no-op progress callback (for tests that don't need progress tracking).
     */
    protected ProgressCallback createNoOpCallback() {
        return (current, max) -> {
            // Do nothing
        };
    }

    /**
     * Log test results summary.
     */
    protected void logTestSummary(String testName, int filesCreated, int filesRenamed) {
        log.info("=== Test Summary: {} ===", testName);
        log.info("  Files created: {}", filesCreated);
        log.info("  Files renamed: {}", filesRenamed);
    }
}
