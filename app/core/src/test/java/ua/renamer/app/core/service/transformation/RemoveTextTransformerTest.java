package ua.renamer.app.core.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.RemoveTextConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for RemoveTextTransformer.
 * Tests cover successful transformations, edge cases, error handling,
 * and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemoveTextTransformerTest {

    private RemoveTextTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new RemoveTextTransformer();
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Creates a test FileModel with the given name and extension.
     *
     * @param name      the file name without extension
     * @param extension the file extension without dot
     * @return a FileModel for testing
     */
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

    // ============================================================================
    // A. Basic Functionality Tests - Remove Text from BEGIN
    // ============================================================================

    @Test
    void testRemoveTextFromBegin_Success() {
        // Given
        FileModel input = createTestFileModel("prefix_document", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertEquals("document.txt", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveTextFromBegin_NotPresent() {
        // Given - file doesn't have the prefix
        FileModel input = createTestFileModel("document", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertFalse(result.needsRename(), "No change should result in no rename needed");
    }

    @Test
    void testRemoveTextFromBegin_EmptyText() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertFalse(result.needsRename(), "Empty text should result in no rename needed");
    }

    @Test
    void testRemoveTextFromBegin_EntireFilename() {
        // Given - remove entire name (edge case)
        FileModel input = createTestFileModel("document", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("document")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveTextFromBegin_PartialMatch() {
        // Given - text appears in middle but not at beginning
        FileModel input = createTestFileModel("document_prefix_test", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("document_prefix_test", result.getNewName(),
                "Text not at beginning should not be removed");
        assertFalse(result.needsRename());
    }

    @Test
    void testRemoveTextFromBegin_SpecialCharacters() {
        // Given
        FileModel input = createTestFileModel("test@#$%file", "pdf");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("test@#$%")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("file", result.getNewName());
        assertEquals("pdf", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. Basic Functionality Tests - Remove Text from END
    // ============================================================================

    @Test
    void testRemoveTextFromEnd_Success() {
        // Given
        FileModel input = createTestFileModel("document_suffix", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("_suffix")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertEquals("document.txt", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveTextFromEnd_NotPresent() {
        // Given - file doesn't have the suffix
        FileModel input = createTestFileModel("document", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("_suffix")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertFalse(result.needsRename(), "No change should result in no rename needed");
    }

    @Test
    void testRemoveTextFromEnd_EmptyText() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertFalse(result.needsRename(), "Empty text should result in no rename needed");
    }

    @Test
    void testRemoveTextFromEnd_EntireFilename() {
        // Given - remove entire name (edge case)
        FileModel input = createTestFileModel("document", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("document")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveTextFromEnd_PartialMatch() {
        // Given - text appears in middle but not at end
        FileModel input = createTestFileModel("test_suffix_document", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("_suffix")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("test_suffix_document", result.getNewName(),
                "Text not at end should not be removed");
        assertFalse(result.needsRename());
    }

    @Test
    void testRemoveTextFromEnd_Unicode() {
        // Given
        FileModel input = createTestFileModel("file测试", "jpg");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("测试")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("file", result.getNewName());
        assertEquals("jpg", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // C. Edge Cases and NeedsRename Tests
    // ============================================================================

    @Test
    void testNeedsRename_True() {
        // Given
        FileModel input = createTestFileModel("prefix_file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
    }

    @Test
    void testNeedsRename_False() {
        // Given - text not present, no change
        FileModel input = createTestFileModel("file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("notpresent_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.needsRename());
        assertEquals(result.getOldFullName(), result.getNewFullName());
    }

    @Test
    void testRemoveMultipleOccurrences_OnlyRemovesFromPosition() {
        // Given - text appears multiple times, should only remove from specified position
        FileModel input = createTestFileModel("test_test", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("test")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("_test", result.getNewName(), "Only first occurrence at BEGIN should be removed");
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveWhitespace() {
        // Given - remove whitespace from beginning
        FileModel input = createTestFileModel("   file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("   ")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveNumericText() {
        // Given - remove numeric prefix
        FileModel input = createTestFileModel("12345_file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("12345_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("file", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // D. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        FileModel input = createTestFileModel("prefix_file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.REMOVE_TEXT, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertTrue(metadata.getAppliedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModel("file_test", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("_test")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("_test", configMap.get("textToRemove"));
        assertEquals("END", configMap.get("position"));
    }

    @Test
    void testTransformationMetadata_BeginPosition() {
        // Given
        FileModel input = createTestFileModel("start_file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("start_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("BEGIN", configMap.get("position"));
    }

    @Test
    void testTransformationMetadata_NotPresentNoChange() {
        // Given - text not present, but metadata still created
        FileModel input = createTestFileModel("file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("notpresent")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta(),
                "Metadata should be populated even when no change occurs");
        assertEquals(TransformationMode.REMOVE_TEXT, result.getTransformationMeta().getMode());
    }

    // ============================================================================
    // E. Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_NullInput() {
        // Given
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
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

        // Then - should return error result instead of throwing exception
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("Transformer configuration must not be null"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_ExceptionInTransform() {
        // Config validation now rejects null position at construction time
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                RemoveTextConfig.builder()
                        .withTextToRemove("prefix_")
                        .withPosition(null)
                        .build()
        );
        assertTrue(ex.getMessage().contains("position must not be null"));
    }

    @Test
    void testErrorHandling_NullTextToRemove() {
        // Config validation now rejects null textToRemove at construction time
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                RemoveTextConfig.builder()
                        .withTextToRemove(null)
                        .withPosition(ItemPosition.BEGIN)
                        .build()
        );
        assertTrue(ex.getMessage().contains("textToRemove must not be null"));
    }

    @Test
    void givenNullConfig_whenTransform_thenErrorResultReturned() {
        FileModel input = createTestFileModel("document", "txt");
        PreparedFileModel result = transformer.transform(input, null);
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertFalse(result.getErrorMessage().isEmpty());
    }

    // ============================================================================
    // F. Integration and Multiple Scenarios Tests
    // ============================================================================

    @Test
    void testMultipleFiles_DifferentConfigurations() {
        // Test that transformer handles different files correctly
        FileModel file1 = createTestFileModel("prefix_doc1", "txt");
        FileModel file2 = createTestFileModel("doc2_suffix", "pdf");
        FileModel file3 = createTestFileModel("start_doc3", "jpg");

        RemoveTextConfig configBegin = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RemoveTextConfig configEnd = RemoveTextConfig.builder()
                .withTextToRemove("_suffix")
                .withPosition(ItemPosition.END)
                .build();

        RemoveTextConfig configBegin2 = RemoveTextConfig.builder()
                .withTextToRemove("start_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result1 = transformer.transform(file1, configBegin);
        PreparedFileModel result2 = transformer.transform(file2, configEnd);
        PreparedFileModel result3 = transformer.transform(file3, configBegin2);

        assertEquals("doc1", result1.getNewName());
        assertEquals("doc2", result2.getNewName());
        assertEquals("doc3", result3.getNewName());
    }

    @Test
    void testExtensionPreservation() {
        // Verify that extension is always preserved correctly
        FileModel input = createTestFileModel("prefix_file", "custom_ext");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertEquals("custom_ext", result.getNewExtension());
        assertEquals(input.getExtension(), result.getNewExtension());
    }

    @Test
    void testOriginalFilePreservation() {
        // Verify that original file reference is preserved
        FileModel input = createTestFileModel("original_prefix", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("original_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertSame(input, result.getOriginalFile());
        assertEquals("original_prefix", result.getOriginalFile().getName());
    }

    @Test
    void testCaseSensitiveRemoval() {
        // Given - text with different case
        FileModel input = createTestFileModel("PREFIX_file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_") // lowercase
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should not remove (case sensitive)
        assertFalse(result.isHasError());
        assertEquals("PREFIX_file", result.getNewName());
        assertFalse(result.needsRename(), "Case-sensitive match should not find lowercase 'prefix_'");
    }

    @Test
    void testRemoveLongText() {
        // Given - remove long prefix
        String longPrefix = "A".repeat(100);
        FileModel input = createTestFileModel(longPrefix + "file", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove(longPrefix)
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveMixedCharacters() {
        // Given - mixed characters suffix
        FileModel input = createTestFileModel("fileTest123!@#_", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("Test123!@#_")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveResultsInEmptyName_StillProcesses() {
        // Given - entire name will be removed
        FileModel input = createTestFileModel("temp", "txt");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("temp")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should handle gracefully, even if resulting name is empty
        assertFalse(result.isHasError());
        assertEquals("", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // G. Directory / Non-file Input Tests
    // ============================================================================

    @Test
    void transform_whenInputIsDirectory_withDirectoryMime_shouldSucceed() {
        // isFile=false but MIME = "application/x-directory" → valid directory entry
        FileModel directory = FileModel.builder()
                .withFile(new File("/test/prefix_folder"))
                .withIsFile(false)
                .withFileSize(0L)
                .withName("prefix_folder")
                .withExtension("")
                .withAbsolutePath("/test/prefix_folder")
                .withDetectedMimeType("application/x-directory")
                .withMetadata(null)
                .build();

        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result = transformer.transform(directory, config);

        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("folder", result.getNewName());
    }

    @Test
    void transform_whenInputIsNotFileAndNotDirectory_shouldReturnError() {
        // isFile=false AND detectedMimeType is NOT "application/x-directory"
        FileModel badInput = FileModel.builder()
                .withFile(new File("/special/device"))
                .withIsFile(false)
                .withFileSize(0L)
                .withName("device")
                .withExtension("")
                .withAbsolutePath("/special/device")
                .withDetectedMimeType("application/octet-stream")
                .withMetadata(null)
                .build();

        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("device")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result = transformer.transform(badInput, config);

        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("extraction failed"));
    }
}
