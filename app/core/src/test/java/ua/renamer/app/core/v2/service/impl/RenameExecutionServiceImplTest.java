package ua.renamer.app.core.v2.service.impl;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.model.*;
import ua.renamer.app.core.service.validator.impl.NameValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for RenameExecutionServiceImpl.
 * Tests file rename operations, error handling, and status reporting.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RenameExecutionServiceImplTest {

    @TempDir
    static Path tempDir;
    private RenameExecutionServiceImpl service;

    @BeforeAll
    void setUpClass() {
        // Use a mock that always approves valid (non-null, non-empty) names.
        // Real NameValidator.isValid() calls toRealPath() which throws NoSuchFileException
        // for filenames that do not yet exist on disk, causing false negatives.
        NameValidator mockValidator = mock(NameValidator.class);
        when(mockValidator.isValid(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        service = new RenameExecutionServiceImpl(mockValidator);
    }

    @BeforeEach
    void setUp() throws IOException {
        // Clean temp directory before each test
        if (Files.exists(tempDir)) {
            Files.walk(tempDir).filter(Files::isRegularFile).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up after each test
        if (Files.exists(tempDir)) {
            Files.walk(tempDir).filter(Files::isRegularFile).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Creates a real temporary file for testing.
     */
    private File createTempFile(String name, String extension) throws IOException {
        Path filePath = tempDir.resolve(name + "." + extension);
        Files.writeString(filePath, "test content");
        return filePath.toFile();
    }

    /**
     * Creates a FileModel from a real file.
     */
    private FileModel createFileModel(File file) {
        String fullName = file.getName();
        int dotIndex = fullName.lastIndexOf('.');
        String name = dotIndex > 0 ? fullName.substring(0, dotIndex) : fullName;
        String extension = dotIndex > 0 ? fullName.substring(dotIndex + 1) : "";

        return FileModel.builder()
                .withFile(file)
                .withIsFile(true)
                .withFileSize(file.length())
                .withName(name)
                .withExtension(extension)
                .withAbsolutePath(file.getAbsolutePath())
                .withCreationDate(LocalDateTime.now().minusDays(1))
                .withModificationDate(LocalDateTime.now())
                .withDetectedMimeType("text/plain")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.GENERIC)
                .withMetadata(null)
                .build();
    }

    /**
     * Creates a PreparedFileModel for testing.
     */
    private PreparedFileModel createPreparedFile(FileModel fileModel, String newName, String newExtension, boolean hasError, String errorMessage) {
        return PreparedFileModel.builder()
                .withOriginalFile(fileModel)
                .withNewName(newName)
                .withNewExtension(newExtension)
                .withHasError(hasError)
                .withErrorMessage(errorMessage)
                .withTransformationMeta(TransformationMetadata.builder()
                        .withMode(TransformationMode.ADD_TEXT)
                        .withAppliedAt(LocalDateTime.now())
                        .withConfig(Map.of("test", "data"))
                        .build())
                .build();
    }

    // ============================================================================
    // A. Success Scenarios
    // ============================================================================

    @Test
    void testExecute_Success_SimpleRename() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertNotNull(result);
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccess());
        assertTrue(result.getErrorMessage().isEmpty());
        assertNotNull(result.getExecutedAt());

        // Verify physical rename occurred
        assertFalse(Files.exists(oldFile.toPath()), "Old file should not exist");
        assertTrue(Files.exists(tempDir.resolve("new.txt")), "New file should exist");
    }

    @Test
    void testExecute_Success_WithSpacesInName() throws IOException {
        // Given
        File oldFile = createTempFile("old file", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new file name", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccess());
        assertTrue(Files.exists(tempDir.resolve("new file name.txt")));
    }

    @Test
    void testExecute_Success_ChangeExtension() throws IOException {
        // Given
        File oldFile = createTempFile("document", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "document", "md", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(Files.exists(tempDir.resolve("document.md")));
        assertFalse(Files.exists(tempDir.resolve("document.txt")));
    }

    @Test
    void testExecute_Success_ChangeBothNameAndExtension() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "md", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(Files.exists(tempDir.resolve("new.md")));
        assertFalse(Files.exists(oldFile.toPath()));
    }

    @Test
    void testExecute_Success_SpecialCharactersInName() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "file@#$%", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(Files.exists(tempDir.resolve("file@#$%.txt")));
    }

    @Test
    void testExecute_Success_UnicodeCharacters() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "文件_файл", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(Files.exists(tempDir.resolve("文件_файл.txt")));
    }

    // ============================================================================
    // B. Skip Scenarios
    // ============================================================================

    @Test
    void testExecute_Skip_WhenHasError() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", true, "Previous phase error");

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SKIPPED, result.getStatus());
        assertFalse(result.isSuccess());
        assertEquals("Previous phase error", result.getErrorMessage().orElse(""));

        // Verify file was NOT renamed
        assertTrue(Files.exists(oldFile.toPath()), "Original file should still exist");
    }

    @Test
    void testExecute_Skip_WhenSameName() throws IOException {
        // Given - Same name and extension
        File oldFile = createTempFile("file", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "file", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SKIPPED, result.getStatus());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("Same name"));

        // Verify file still exists
        assertTrue(Files.exists(oldFile.toPath()));
    }

    @Test
    void testExecute_Skip_WhenNoRenameNeeded() throws IOException {
        // Given - needsRename() returns false
        File oldFile = createTempFile("file", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "file", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SKIPPED, result.getStatus());
        assertFalse(result.getPreparedFile().needsRename());
    }

    // ============================================================================
    // C. Error Scenarios - Source File
    // ============================================================================

    @Test
    void testExecute_Error_SourceNotExists() {
        // Given - File that doesn't exist
        File nonExistentFile = tempDir.resolve("nonexistent.txt").toFile();
        FileModel fileModel = createFileModel(nonExistentFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.ERROR_EXECUTION, result.getStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("does not exist"));
    }

    @Test
    void testExecute_Error_SourceFileDeleted() throws IOException {
        // Given - File that exists initially but is deleted before rename
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        // Delete the file before execution
        Files.delete(oldFile.toPath());

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.ERROR_EXECUTION, result.getStatus());
        assertTrue(result.getErrorMessage().get().contains("does not exist"));
    }

    // ============================================================================
    // D. Error Scenarios - Target File
    // ============================================================================

    @Test
    void testExecute_ConflictResolved_TargetExists() throws IOException {
        // Given - Target file already exists; service now resolves with suffix instead of failing
        File oldFile = createTempFile("old", "txt");
        File targetFile = createTempFile("new", "txt");  // Occupies target slot

        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then: conflict resolved with " (001)" suffix — SUCCESS, not ERROR_EXECUTION
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccess());

        // old file was moved to the suffixed name; original target is untouched
        assertFalse(Files.exists(oldFile.toPath()), "source must have moved");
        assertTrue(Files.exists(targetFile.toPath()), "original new.txt must be untouched");
        assertTrue(Files.exists(tempDir.resolve("new (001).txt")), "suffix variant must exist");
    }

    @Test
    void testExecute_ConflictResolved_TargetExistsDifferentContent() throws IOException {
        // Given - Target exists with different content; source has its own content
        File oldFile = createTempFile("old", "txt");
        Files.writeString(oldFile.toPath(), "old content");

        Path targetPath = tempDir.resolve("new.txt");
        Files.writeString(targetPath, "different content");

        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then: conflict resolved with suffix — original target content must be preserved
        assertEquals(RenameStatus.SUCCESS, result.getStatus());

        // Pre-existing file content must not be touched
        assertEquals("different content", Files.readString(targetPath));

        // Source content landed at the suffixed path
        assertEquals("old content", Files.readString(tempDir.resolve("new (001).txt")));
    }

    // ============================================================================
    // E. Result Verification Tests
    // ============================================================================

    @Test
    void testExecute_ResultContainsPreparedFile() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertNotNull(result.getPreparedFile());
        assertEquals(preparedFile, result.getPreparedFile());
    }

    @Test
    void testExecute_ResultStatus_AllValues() throws IOException {
        // Test SUCCESS
        File file1 = createTempFile("file1", "txt");
        FileModel model1 = createFileModel(file1);
        PreparedFileModel prepared1 = createPreparedFile(model1, "renamed1", "txt", false, null);
        assertEquals(RenameStatus.SUCCESS, service.execute(prepared1).getStatus());

        // Test SKIPPED (same name)
        File file2 = createTempFile("file2", "txt");
        FileModel model2 = createFileModel(file2);
        PreparedFileModel prepared2 = createPreparedFile(model2, "file2", "txt", false, null);
        assertEquals(RenameStatus.SKIPPED, service.execute(prepared2).getStatus());

        // Test SKIPPED (has error)
        File file3 = createTempFile("file3", "txt");
        FileModel model3 = createFileModel(file3);
        PreparedFileModel prepared3 = createPreparedFile(model3, "renamed3", "txt", true, "Error");
        assertEquals(RenameStatus.SKIPPED, service.execute(prepared3).getStatus());

        // Test ERROR_EXECUTION (source doesn't exist)
        File nonExistent = tempDir.resolve("nonexistent.txt").toFile();
        FileModel model4 = createFileModel(nonExistent);
        PreparedFileModel prepared4 = createPreparedFile(model4, "renamed4", "txt", false, null);
        assertEquals(RenameStatus.ERROR_EXECUTION, service.execute(prepared4).getStatus());
    }

    @Test
    void testExecute_ExecutedAtTimestamp() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        LocalDateTime before = LocalDateTime.now();

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        LocalDateTime after = LocalDateTime.now();
        assertNotNull(result.getExecutedAt());
        assertTrue(result.getExecutedAt().isAfter(before.minusSeconds(1)));
        assertTrue(result.getExecutedAt().isBefore(after.plusSeconds(1)));
    }

    @Test
    void testExecute_OriginalFileName() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals("old.txt", result.getOriginalFileName());
    }

    @Test
    void testExecute_NewFileName() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "new", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals("new.txt", result.getNewFileName());
    }

    // ============================================================================
    // F. Edge Cases
    // ============================================================================

    @Test
    void testExecute_LargeFile() throws IOException {
        // Given - Large file (1MB)
        File oldFile = createTempFile("large", "txt");
        byte[] content = new byte[1024 * 1024];  // 1MB
        Files.write(oldFile.toPath(), content);

        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "renamed_large", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(Files.exists(tempDir.resolve("renamed_large.txt")));
        assertEquals(content.length, Files.size(tempDir.resolve("renamed_large.txt")));
    }

    @Test
    void testExecute_EmptyFile() throws IOException {
        // Given - Empty file
        File oldFile = createTempFile("empty", "txt");
        Files.writeString(oldFile.toPath(), "");

        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "renamed_empty", "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(Files.exists(tempDir.resolve("renamed_empty.txt")));
        assertEquals(0, Files.size(tempDir.resolve("renamed_empty.txt")));
    }

    @Test
    void testExecute_LongFileName() throws IOException {
        // Given - Very long filename
        File oldFile = createTempFile("old", "txt");
        String longName = "a".repeat(200);

        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, longName, "txt", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(Files.exists(tempDir.resolve(longName + ".txt")));
    }

    @Test
    void testExecute_NoExtension() throws IOException {
        // Given - File with no extension
        Path noExtPath = tempDir.resolve("noext");
        Files.writeString(noExtPath, "content");
        File oldFile = noExtPath.toFile();

        FileModel fileModel = FileModel.builder()
                .withFile(oldFile)
                .withIsFile(true)
                .withFileSize(oldFile.length())
                .withName("noext")
                .withExtension("")
                .withAbsolutePath(oldFile.getAbsolutePath())
                .withCreationDate(LocalDateTime.now())
                .withModificationDate(LocalDateTime.now())
                .withDetectedMimeType("application/octet-stream")
                .withDetectedExtensions(Collections.emptySet())
                .withCategory(Category.GENERIC)
                .withMetadata(null)
                .build();

        PreparedFileModel preparedFile = createPreparedFile(fileModel, "renamed", "", false, null);

        // When
        RenameResult result = service.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(Files.exists(tempDir.resolve("renamed")));
    }

    // ============================================================================
    // G. Multiple Operations
    // ============================================================================

    @Test
    void testExecute_MultipleSequentialRenames() throws IOException {
        // Given
        File file1 = createTempFile("file1", "txt");
        File file2 = createTempFile("file2", "txt");
        File file3 = createTempFile("file3", "txt");

        FileModel model1 = createFileModel(file1);
        FileModel model2 = createFileModel(file2);
        FileModel model3 = createFileModel(file3);

        PreparedFileModel prepared1 = createPreparedFile(model1, "renamed1", "txt", false, null);
        PreparedFileModel prepared2 = createPreparedFile(model2, "renamed2", "txt", false, null);
        PreparedFileModel prepared3 = createPreparedFile(model3, "renamed3", "txt", false, null);

        // When
        RenameResult result1 = service.execute(prepared1);
        RenameResult result2 = service.execute(prepared2);
        RenameResult result3 = service.execute(prepared3);

        // Then
        assertEquals(RenameStatus.SUCCESS, result1.getStatus());
        assertEquals(RenameStatus.SUCCESS, result2.getStatus());
        assertEquals(RenameStatus.SUCCESS, result3.getStatus());

        assertTrue(Files.exists(tempDir.resolve("renamed1.txt")));
        assertTrue(Files.exists(tempDir.resolve("renamed2.txt")));
        assertTrue(Files.exists(tempDir.resolve("renamed3.txt")));
    }

    // ============================================================================
    // H. NameValidator Tests
    // ============================================================================

    @Test
    void givenFilenameWithSlash_whenExecute_thenErrorTransformationReturnedOnAllPlatforms() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "bad/name", "txt", false, null);
        RenameExecutionServiceImpl svc = new RenameExecutionServiceImpl(new NameValidator());

        // When
        RenameResult result = svc.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.ERROR_TRANSFORMATION, result.getStatus());
        assertTrue(result.getErrorMessage().orElse("").contains("invalid characters"));
        assertTrue(Files.exists(oldFile.toPath()), "Source file must not be touched");
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void givenFilenameWithColonOnWindows_whenExecute_thenErrorTransformationReturned() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "2024:01:01", "txt", false, null);
        RenameExecutionServiceImpl svc = new RenameExecutionServiceImpl(new NameValidator());

        // When
        RenameResult result = svc.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.ERROR_TRANSFORMATION, result.getStatus());
        assertTrue(result.getErrorMessage().orElse("").contains("invalid characters"));
    }

    @Test
    void givenValidFilename_whenExecute_thenNameValidatorPassesAndRenameProceeds() throws IOException {
        // Given
        File oldFile = createTempFile("old", "txt");
        FileModel fileModel = createFileModel(oldFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "valid-new-name", "txt", false, null);

        NameValidator mockValidator = mock(NameValidator.class);
        when(mockValidator.isValid("valid-new-name.txt")).thenReturn(true);
        RenameExecutionServiceImpl svc = new RenameExecutionServiceImpl(mockValidator);

        // When
        RenameResult result = svc.execute(preparedFile);

        // Then
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        verify(mockValidator).isValid("valid-new-name.txt");
        assertTrue(Files.exists(tempDir.resolve("valid-new-name.txt")));
    }
}
