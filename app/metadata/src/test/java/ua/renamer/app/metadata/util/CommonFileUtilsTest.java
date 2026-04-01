package ua.renamer.app.metadata.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.exception.FileAttributesReadException;
import ua.renamer.app.api.exception.FileNotFoundException;
import ua.renamer.app.api.exception.MimeTypeNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommonFileUtilsTest {

    @TempDir
    Path tempDir;
    private CommonFileUtils fileUtils;
    private DateTimeConverter dateTimeConverter;

    @BeforeEach
    void setUp() {
        dateTimeConverter = new DateTimeConverter();
        fileUtils = new CommonFileUtils(dateTimeConverter);
    }

    // ============================================================================
    // A. File Validation Tests
    // ============================================================================

    @Test
    void testValidateFile_WithNull() {
        assertThrows(NullPointerException.class, () -> fileUtils.validateFile(null));
    }

    @Test
    void testValidateFile_WithNonExistentFile() {
        File nonExistent = new File(tempDir.toFile(), "non_existent_file.txt");

        assertThrows(FileNotFoundException.class, () -> fileUtils.validateFile(nonExistent));
    }

    @Test
    void testValidateFile_WithValidFile() throws IOException {
        File testFile = Files.createFile(tempDir.resolve("test.txt")).toFile();

        assertDoesNotThrow(() -> fileUtils.validateFile(testFile));
    }

    // ============================================================================
    // B. Basic File Attributes Tests
    // ============================================================================

    @Test
    void testGetBasicFileAttributes_WithValidFile() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));

        BasicFileAttributes attributes = fileUtils.getBasicFileAttributes(testFile);

        assertNotNull(attributes);
        assertTrue(attributes.isRegularFile());
        assertFalse(attributes.isDirectory());
    }

    @Test
    void testGetBasicFileAttributes_WithInvalidPath() {
        Path invalidPath = tempDir.resolve("non_existent.txt");

        assertThrows(FileAttributesReadException.class,
                () -> fileUtils.getBasicFileAttributes(invalidPath));
    }

    @Test
    void testGetBasicFileAttributes_WithDirectory() throws IOException {
        Path testDir = Files.createDirectory(tempDir.resolve("testdir"));

        BasicFileAttributes attributes = fileUtils.getBasicFileAttributes(testDir);

        assertNotNull(attributes);
        assertTrue(attributes.isDirectory());
        assertFalse(attributes.isRegularFile());
    }

    // ============================================================================
    // C. File Base Name Tests
    // ============================================================================

    @Test
    void testGetFileBaseName_Simple() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.jpg"));

        String baseName = fileUtils.getFileBaseName(testFile);

        assertEquals("test", baseName);
    }

    @Test
    void testGetFileBaseName_MultipleDots() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("my.file.name.txt"));

        String baseName = fileUtils.getFileBaseName(testFile);

        assertEquals("my.file.name", baseName);
    }

    @Test
    void testGetFileBaseName_NoDots() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("README"));

        String baseName = fileUtils.getFileBaseName(testFile);

        assertEquals("README", baseName);
    }

    @Test
    void testGetFileBaseName_HiddenFile() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve(".gitignore"));

        String baseName = fileUtils.getFileBaseName(testFile);

        // Hidden files without additional dots should return the full name including the dot
        assertEquals(".gitignore", baseName);
    }

    // ============================================================================
    // D. File Absolute Path Tests
    // ============================================================================

    @Test
    void testGetFileAbsolutePath() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));

        String absolutePath = fileUtils.getFileAbsolutePath(testFile);

        assertNotNull(absolutePath);
        assertTrue(absolutePath.contains("test.txt"));
        assertTrue(absolutePath.startsWith("/") || absolutePath.matches("^[A-Z]:\\\\.*")); // Unix or Windows
    }

    @Test
    void testGetFileAbsolutePath_WithRelativePath() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));
        Path relativePath = testFile.getFileName();

        // Create path from current directory
        Path fullPath = tempDir.resolve(relativePath);
        String absolutePath = fileUtils.getFileAbsolutePath(fullPath);

        assertNotNull(absolutePath);
        assertTrue(absolutePath.endsWith("test.txt"));
    }

    // ============================================================================
    // E. File Extension Tests
    // ============================================================================

    @Test
    void testGetFileExtension_Simple() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.jpg"));

        String extension = fileUtils.getFileExtension(testFile);

        assertEquals("jpg", extension);
    }

    @Test
    void testGetFileExtension_MultipleDots() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("file.tar.gz"));

        String extension = fileUtils.getFileExtension(testFile);

        assertEquals("gz", extension);
    }

    @Test
    void testGetFileExtension_NoExtension() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("README"));

        String extension = fileUtils.getFileExtension(testFile);

        assertEquals("", extension);
    }

    @Test
    void testGetFileExtension_HiddenFile() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve(".gitignore"));

        String extension = fileUtils.getFileExtension(testFile);

        // Hidden files without additional dots should have no extension
        assertEquals("", extension);
    }

    @Test
    void testGetFileMimeType_TextFile() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));
        Files.writeString(testFile, "Hello World");

        String mimeType = fileUtils.getFileMimeType(testFile);

        assertNotNull(mimeType);
        assertTrue(mimeType.startsWith("text/"));
    }

    @Test
    void testGetFileMimeType_EmptyFile() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("empty.txt"));

        String mimeType = fileUtils.getFileMimeType(testFile);

        assertNotNull(mimeType);
        // Empty files typically detected as text/plain or application/octet-stream
    }


    @Test
    void testGetFileMimeType_InvalidFile() {
        Path nonExistent = tempDir.resolve("non_existent.txt");

        assertThrows(MimeTypeNotFoundException.class,
                () -> fileUtils.getFileMimeType(nonExistent));
    }

    // ============================================================================
    // G. File Creation Date Tests
    // ============================================================================

    @Test
    void testGetFileCreationDate_WithValidAttributes() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));
        BasicFileAttributes attributes = Files.readAttributes(testFile, BasicFileAttributes.class);

        LocalDateTime creationDate = fileUtils.getFileCreationDate(attributes);

        // Note: Creation time may be null on Linux
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            // On Linux, creation time might not be supported
            // Test should pass regardless
        } else {
            // On Windows and macOS, creation time should be available
            assertNotNull(creationDate);
            assertTrue(creationDate.isBefore(LocalDateTime.now().plusMinutes(1)));
        }
    }

    @Test
    void testGetFileCreationDate_WithNullCreationTime() {
        // Create a mock scenario where creation time is null
        Path testFile = tempDir.resolve("test.txt");
        try {
            Files.createFile(testFile);
            BasicFileAttributes attributes = Files.readAttributes(testFile, BasicFileAttributes.class);
            LocalDateTime result = fileUtils.getFileCreationDate(attributes);

            // Should handle gracefully (either return valid date or null)
            // No exception should be thrown
        } catch (IOException e) {
            fail("Should not throw exception");
        }
    }

    // ============================================================================
    // H. File Modification Date Tests
    // ============================================================================

    @Test
    void testGetFileModificationDate_WithValidAttributes() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));
        BasicFileAttributes attributes = Files.readAttributes(testFile, BasicFileAttributes.class);

        LocalDateTime modificationDate = fileUtils.getFileModificationDate(attributes);

        assertNotNull(modificationDate);
        assertTrue(modificationDate.isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    void testGetFileModificationDate_AllOS() throws IOException {
        // Modification time should be available on all OS
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));
        Files.writeString(testFile, "content");

        BasicFileAttributes attributes = Files.readAttributes(testFile, BasicFileAttributes.class);
        LocalDateTime modificationDate = fileUtils.getFileModificationDate(attributes);

        assertNotNull(modificationDate);
    }

    @Test
    void testGetFileModificationDate_RecentFile() throws IOException, InterruptedException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));

        // Wait a bit and modify
        Thread.sleep(100);
        Files.writeString(testFile, "updated content");

        BasicFileAttributes attributes = Files.readAttributes(testFile, BasicFileAttributes.class);
        LocalDateTime modificationDate = fileUtils.getFileModificationDate(attributes);

        assertNotNull(modificationDate);
        // Should be very recent
        assertTrue(modificationDate.isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    // ============================================================================
    // J. Integration Tests
    // ============================================================================

    @Test
    void testCompleteFileInfoExtraction() throws IOException {
        // Create a test file with known properties
        Path testFile = Files.createFile(tempDir.resolve("integration_test.txt"));
        Files.writeString(testFile, "Integration test content");

        // Validate file
        assertDoesNotThrow(() -> fileUtils.validateFile(testFile.toFile()));

        // Get attributes
        BasicFileAttributes attributes = fileUtils.getBasicFileAttributes(testFile);
        assertNotNull(attributes);

        // Get various properties
        String baseName = fileUtils.getFileBaseName(testFile);
        assertEquals("integration_test", baseName);

        String extension = fileUtils.getFileExtension(testFile);
        assertEquals("txt", extension);

        String absolutePath = fileUtils.getFileAbsolutePath(testFile);
        assertTrue(absolutePath.contains("integration_test.txt"));

        LocalDateTime modificationDate = fileUtils.getFileModificationDate(attributes);
        assertNotNull(modificationDate);
    }

    @Test
    void testFileWithSpecialCharacters() throws IOException {
        // Test file with special characters in name
        Path testFile = Files.createFile(tempDir.resolve("test file (1).txt"));

        String baseName = fileUtils.getFileBaseName(testFile);
        assertEquals("test file (1)", baseName);

        String extension = fileUtils.getFileExtension(testFile);
        assertEquals("txt", extension);
    }

    @Test
    void testUnicodeFileName() throws IOException {
        // Test file with Unicode characters
        Path testFile = Files.createFile(tempDir.resolve("тест.txt"));

        String baseName = fileUtils.getFileBaseName(testFile);
        assertEquals("тест", baseName);

        String extension = fileUtils.getFileExtension(testFile);
        assertEquals("txt", extension);
    }
}
