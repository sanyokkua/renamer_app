package ua.renamer.app.utils.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    static Stream<Arguments> getParentFoldersArguments() {
        return Stream.of(
                arguments("", List.of()),
                arguments("/", List.of()),
                arguments("/file", List.of()),
                arguments("/file/path", List.of("file")),
                arguments("/root/user/home/projects/sources/config/app.json",
                          List.of("root", "user", "home", "projects", "sources", "config")),
                arguments("\\root\\user\\home\\projects\\sources\\config\\app.json",
                          List.of("root", "user", "home", "projects", "sources", "config")),
                arguments("c:\\root\\user\\home\\projects\\sources\\config\\app.json",
                          List.of("root", "user", "home", "projects", "sources", "config"))
        );
    }

    // ============================================================================
    // File Base Name Tests
    // ============================================================================

    @Test
    void testGetFileBaseName_Simple() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.jpg"));

        String baseName = FileUtils.getFileBaseName(testFile);

        assertEquals("test", baseName);
    }

    @Test
    void testGetFileBaseName_MultipleDots() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("my.file.name.txt"));

        String baseName = FileUtils.getFileBaseName(testFile);

        assertEquals("my.file.name", baseName);
    }

    @Test
    void testGetFileBaseName_NoDots() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("README"));

        String baseName = FileUtils.getFileBaseName(testFile);

        assertEquals("README", baseName);
    }

    @Test
    void testGetFileBaseName_HiddenFile() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve(".gitignore"));

        String baseName = FileUtils.getFileBaseName(testFile);

        assertEquals(".gitignore", baseName);
    }

    @Test
    void testGetFileBaseName_HiddenFileWithExtension() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve(".config.json"));

        String baseName = FileUtils.getFileBaseName(testFile);

        assertEquals(".config", baseName);
    }

    // ============================================================================
    // File Absolute Path Tests
    // ============================================================================

    @Test
    void testGetFileAbsolutePath() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));

        String absolutePath = FileUtils.getFileAbsolutePath(testFile);

        assertNotNull(absolutePath);
        assertTrue(absolutePath.contains("test.txt"));
        assertTrue(absolutePath.startsWith("/") || absolutePath.matches("^[A-Z]:\\\\.*"));
    }

    @Test
    void testGetFileAbsolutePath_WithRelativePath() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));
        Path relativePath = testFile.getFileName();

        Path fullPath = tempDir.resolve(relativePath);
        String absolutePath = FileUtils.getFileAbsolutePath(fullPath);

        assertNotNull(absolutePath);
        assertTrue(absolutePath.endsWith("test.txt"));
    }

    // ============================================================================
    // File Extension Tests
    // ============================================================================

    @Test
    void testGetFileExtension_Simple() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test.jpg"));

        String extension = FileUtils.getFileExtension(testFile);

        assertEquals("jpg", extension);
    }

    @Test
    void testGetFileExtension_MultipleDots() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("file.tar.gz"));

        String extension = FileUtils.getFileExtension(testFile);

        assertEquals("gz", extension);
    }

    @Test
    void testGetFileExtension_NoExtension() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("README"));

        String extension = FileUtils.getFileExtension(testFile);

        assertEquals("", extension);
    }

    @Test
    void testGetFileExtension_HiddenFile() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve(".gitignore"));

        String extension = FileUtils.getFileExtension(testFile);

        assertEquals("", extension);
    }

    @Test
    void testGetFileExtension_HiddenFileWithExtension() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve(".config.json"));

        String extension = FileUtils.getFileExtension(testFile);

        assertEquals("json", extension);
    }

    // ============================================================================
    // Parent Folders Tests
    // ============================================================================

    @ParameterizedTest
    @MethodSource("getParentFoldersArguments")
    void testGetParentFolders(String path, List<String> expectedParents) {
        var result = FileUtils.getParentFolders(path);

        assertNotNull(result);
        assertEquals(expectedParents.size(), result.size());
        for (int i = 0; i < expectedParents.size(); i++) {
            assertEquals(expectedParents.get(i), result.get(i));
        }
    }

    @Test
    void testGetParentFolders_NullPath() {
        var result = FileUtils.getParentFolders(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============================================================================
    // Integration Tests
    // ============================================================================

    @Test
    void testCompleteFileInfoExtraction() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("integration_test.txt"));

        String baseName = FileUtils.getFileBaseName(testFile);
        assertEquals("integration_test", baseName);

        String extension = FileUtils.getFileExtension(testFile);
        assertEquals("txt", extension);

        String absolutePath = FileUtils.getFileAbsolutePath(testFile);
        assertTrue(absolutePath.contains("integration_test.txt"));
    }

    @Test
    void testFileWithSpecialCharacters() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("test file (1).txt"));

        String baseName = FileUtils.getFileBaseName(testFile);
        assertEquals("test file (1)", baseName);

        String extension = FileUtils.getFileExtension(testFile);
        assertEquals("txt", extension);
    }

    @Test
    void testUnicodeFileName() throws IOException {
        Path testFile = Files.createFile(tempDir.resolve("тест.txt"));

        String baseName = FileUtils.getFileBaseName(testFile);
        assertEquals("тест", baseName);

        String extension = FileUtils.getFileExtension(testFile);
        assertEquals("txt", extension);
    }

}
