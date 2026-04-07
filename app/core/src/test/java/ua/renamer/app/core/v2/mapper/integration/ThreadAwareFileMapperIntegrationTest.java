package ua.renamer.app.core.v2.mapper.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.interfaces.DateTimeUtils;
import ua.renamer.app.api.interfaces.FileMetadataMapper;
import ua.renamer.app.api.interfaces.FileUtils;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.core.mapper.ThreadAwareFileMapper;
import ua.renamer.app.core.v2.util.TestDateTimeUtils;
import ua.renamer.app.core.v2.util.TestFileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for ThreadAwareFileMapper.
 * Tests complete file mapping including basic attributes extraction.
 */
class ThreadAwareFileMapperIntegrationTest {

    @TempDir
    Path tempDir;
    private ThreadAwareFileMapper fileMapper;

    static Stream<Arguments> provideFileTypesForCategoryTest() {
        return Stream.of(
                arguments("test-data/image/jpg/test_jpg_clean.jpg", Category.IMAGE, "image/jpeg"),
                arguments("test-data/image/png/test_png_clean.png", Category.IMAGE, "image/png"),
                arguments("test-data/video/mp4/test_mp4_clean.mp4", Category.VIDEO, "video/mp4"),
                arguments("test-data/audio/mp3/test_mp3_clean.mp3", Category.AUDIO, "audio/mpeg")
        );
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    @BeforeEach
    void setUp() {
        DateTimeUtils dateTimeUtils = new TestDateTimeUtils();
        FileUtils fileUtils = new TestFileUtils(dateTimeUtils);

        // Simple mock metadata mapper that returns empty metadata
        FileMetadataMapper mockMetadataMapper = (file, category, mimeType) -> FileMeta.empty();

        fileMapper = new ThreadAwareFileMapper(fileUtils, mockMetadataMapper);
    }

    // ============================================================================
    // Basic File Attributes Tests
    // ============================================================================

    private File getTestFile(String path) {
        URL resource = getClass().getClassLoader().getResource(path);
        assertNotNull(resource, "Test file not found: " + path);
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            fail("Failed to load test file: " + path);
            return null;
        }
    }

    @Test
    void testMapFrom_BasicFileAttributes() throws IOException {
        Path testFile = tempDir.resolve("test_file.txt");
        Files.writeString(testFile, "Test content");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertNotNull(result);
        assertEquals("test_file", result.getName());
        assertEquals("txt", result.getExtension());
        assertTrue(result.getFileSize() > 0);
        assertTrue(result.getAbsolutePath().endsWith("test_file.txt"));
    }

    @Test
    void testMapFrom_FileName() throws IOException {
        Path testFile = tempDir.resolve("my_document.pdf");
        Files.writeString(testFile, "PDF content");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals("my_document", result.getName());
    }

    @Test
    void testMapFrom_FileExtension() throws IOException {
        Path testFile = tempDir.resolve("report.docx");
        Files.writeString(testFile, "Document");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals("docx", result.getExtension());
    }

    @Test
    void testMapFrom_FileWithoutExtension() throws IOException {
        Path testFile = tempDir.resolve("README");
        Files.writeString(testFile, "No extension");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals("README", result.getName());
        assertEquals("", result.getExtension());
    }

    @Test
    void testMapFrom_FileWithMultipleDots() throws IOException {
        Path testFile = tempDir.resolve("my.test.file.tar.gz");
        Files.writeString(testFile, "Content");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals("my.test.file.tar", result.getName());
        assertEquals("gz", result.getExtension());
    }

    // ============================================================================
    // File Size Tests
    // ============================================================================

    @Test
    void testMapFrom_HiddenFile() throws IOException {
        Path testFile = tempDir.resolve(".gitignore");
        Files.writeString(testFile, "*.class");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals(".gitignore", result.getName());
        assertEquals("", result.getExtension());
    }

    @Test
    void testMapFrom_FileSize() throws IOException {
        String content = "This is test content with known size";
        Path testFile = tempDir.resolve("sized_file.txt");
        Files.writeString(testFile, content);

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertTrue(result.getFileSize() > 0);
        assertEquals(content.length(), result.getFileSize());
    }

    @Test
    void testMapFrom_EmptyFile() throws IOException {
        Path testFile = tempDir.resolve("empty.txt");
        Files.createFile(testFile);

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals(0, result.getFileSize());
    }

    // ============================================================================
    // DateTime Tests
    // ============================================================================

    @Test
    void testMapFrom_LargeFile() throws IOException {
        Path testFile = tempDir.resolve("large.txt");
        String content = "x".repeat(10000);
        Files.writeString(testFile, content);

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals(10000, result.getFileSize());
    }

    // ============================================================================
    // Directory Tests
    // ============================================================================

    @Test
    void testMapFrom_CreationAndModificationDates() throws IOException, InterruptedException {
        Path testFile = tempDir.resolve("dated_file.txt");
        Files.writeString(testFile, "Initial content");

        FileModel result1 = fileMapper.mapFrom(testFile.toFile());

        // Verify at least one date is present (OS-dependent)
        assertTrue(result1.getCreationDate().isPresent() || result1.getModificationDate().isPresent(),
                "At least one date should be present");

        if (result1.getModificationDate().isPresent()) {
            LocalDateTime firstModTime = result1.getModificationDate().get();

            Thread.sleep(100);
            Files.writeString(testFile, "Modified content");

            FileModel result2 = fileMapper.mapFrom(testFile.toFile());

            assertTrue(result2.getModificationDate().isPresent());
            LocalDateTime secondModTime = result2.getModificationDate().get();

            assertFalse(secondModTime.isBefore(firstModTime),
                    "Second modification time should not be before first");
        }
    }

    @Test
    void testMapFrom_Directory() throws IOException {
        Path testDir = tempDir.resolve("test_directory");
        Files.createDirectory(testDir);

        FileModel result = fileMapper.mapFrom(testDir.toFile());

        assertEquals("test_directory", result.getName());
        assertEquals("", result.getExtension());
        assertEquals("application/x-directory", result.getDetectedMimeType());
        assertEquals(Category.GENERIC, result.getCategory());
    }

    // ============================================================================
    // MIME Type and Category Tests
    // ============================================================================

    @Test
    void testMapFrom_DirectoryVsFile() throws IOException {
        Path dir = tempDir.resolve("folder");
        Files.createDirectory(dir);

        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "content");

        FileModel dirResult = fileMapper.mapFrom(dir.toFile());
        FileModel fileResult = fileMapper.mapFrom(file.toFile());

    }

    @ParameterizedTest
    @MethodSource("provideFileTypesForCategoryTest")
    void testMapFrom_CategoryDetection(String filePath, Category expectedCategory, String expectedMimeType) {
        File testFile = getTestFile(filePath);

        FileModel result = fileMapper.mapFrom(testFile);

        assertNotNull(result);
        assertEquals(expectedCategory, result.getCategory(),
                "Category mismatch for: " + filePath);
        assertEquals(expectedMimeType, result.getDetectedMimeType(),
                "MIME type mismatch for: " + filePath);
    }

    // ============================================================================
    // Special Characters Tests
    // ============================================================================

    @Test
    void testMapFrom_FileWithSpaces() throws IOException {
        Path testFile = tempDir.resolve("file with spaces.txt");
        Files.writeString(testFile, "Content");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals("file with spaces", result.getName());
        assertEquals("txt", result.getExtension());
    }

    @Test
    void testMapFrom_FileWithUnicode() throws IOException {
        Path testFile = tempDir.resolve("файл_测试_ファイル.txt");
        Files.writeString(testFile, "Unicode content");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals("файл_测试_ファイル", result.getName());
        assertEquals("txt", result.getExtension());
    }

    @Test
    void testMapFrom_FileWithSpecialChars() throws IOException {
        Path testFile = tempDir.resolve("file-name_v2.0.txt");
        Files.writeString(testFile, "Content");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertEquals("file-name_v2.0", result.getName());
        assertEquals("txt", result.getExtension());
    }

    // ============================================================================
    // Absolute Path Tests
    // ============================================================================

    @Test
    void testMapFrom_AbsolutePath() throws IOException {
        Path testFile = tempDir.resolve("path_test.txt");
        Files.writeString(testFile, "Content");

        FileModel result = fileMapper.mapFrom(testFile.toFile());

        assertNotNull(result.getAbsolutePath());
        assertTrue(result.getAbsolutePath().contains("path_test.txt"));
        assertTrue(new File(result.getAbsolutePath()).isAbsolute());
    }

    // ============================================================================
    // Integration Tests with Real Files
    // ============================================================================

    @Test
    void testMapFrom_RealImageFile() {
        File testFile = getTestFile("test-data/image/jpg/test_jpg_clean.jpg");

        FileModel result = fileMapper.mapFrom(testFile);

        assertEquals("test_jpg_clean", result.getName());
        assertEquals("jpg", result.getExtension());
        assertTrue(result.getFileSize() > 0);
        assertEquals("image/jpeg", result.getDetectedMimeType());
        assertEquals(Category.IMAGE, result.getCategory());
    }

    @Test
    void testMapFrom_RealVideoFile() {
        File testFile = getTestFile("test-data/video/mp4/test_mp4_clean.mp4");

        FileModel result = fileMapper.mapFrom(testFile);

        assertEquals("test_mp4_clean", result.getName());
        assertEquals("mp4", result.getExtension());
        assertEquals("video/mp4", result.getDetectedMimeType());
        assertEquals(Category.VIDEO, result.getCategory());
    }

    @Test
    void testMapFrom_RealAudioFile() {
        File testFile = getTestFile("test-data/audio/mp3/test_mp3_clean.mp3");

        FileModel result = fileMapper.mapFrom(testFile);

        assertEquals("test_mp3_clean", result.getName());
        assertEquals("mp3", result.getExtension());
        assertEquals("audio/mpeg", result.getDetectedMimeType());
        assertEquals(Category.AUDIO, result.getCategory());
    }

    @Test
    void testMapFrom_VerifyAllBasicFieldsPopulated() {
        File testFile = getTestFile("test-data/image/jpg/test_jpg_std_2025-12-11_21-00-35.jpg");

        FileModel result = fileMapper.mapFrom(testFile);

        // Verify all basic fields are populated
        assertNotNull(result.getFile());
        assertNotNull(result.getName());
        assertNotNull(result.getExtension());
        assertNotNull(result.getAbsolutePath());
        assertTrue(result.getFileSize() > 0);
        assertNotNull(result.getDetectedMimeType());
        assertNotNull(result.getDetectedExtensions());
        assertNotNull(result.getCategory());

        // At least one date should be present (OS-dependent)
        assertTrue(result.getCreationDate().isPresent() || result.getModificationDate().isPresent());
    }
}
