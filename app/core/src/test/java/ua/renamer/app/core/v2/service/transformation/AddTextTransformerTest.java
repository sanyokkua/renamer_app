package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.AddTextConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AddTextTransformer.
 * Tests cover successful transformations, edge cases, error handling,
 * and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddTextTransformerTest {

    private AddTextTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new AddTextTransformer();
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
    // A. Basic Functionality Tests - Add Text at BEGIN
    // ============================================================================

    @Test
    void testAddTextAtBegin_Success() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("prefix_document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertEquals("prefix_document.txt", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testAddTextAtBegin_EmptyText() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("")
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
    void testAddTextAtBegin_SpecialCharacters() {
        // Given
        FileModel input = createTestFileModel("file", "pdf");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test@#$%")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("test@#$%file", result.getNewName());
        assertEquals("pdf", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testAddTextAtBegin_VeryLongText() {
        // Given
        FileModel input = createTestFileModel("short", "txt");
        String longText = "A".repeat(500);
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd(longText)
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals(longText + "short", result.getNewName());
        assertEquals(505, result.getNewName().length());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. Basic Functionality Tests - Add Text at END
    // ============================================================================

    @Test
    void testAddTextAtEnd_Success() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("_suffix")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("document_suffix", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertEquals("document_suffix.txt", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testAddTextAtEnd_EmptyText() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("")
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
    void testAddTextAtEnd_Unicode() {
        // Given
        FileModel input = createTestFileModel("file", "jpg");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("测试")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("file测试", result.getNewName());
        assertEquals("jpg", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // C. Edge Cases and Same Name Tests
    // ============================================================================

    @Test
    void testAddTextResultsInSameName() {
        // Given - file already has the prefix
        FileModel input = createTestFileModel("prefix_document", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("prefix_prefix_document", result.getNewName());
        assertTrue(result.needsRename(), "Adding prefix again should still result in rename");
    }

    @Test
    void testNeedsRename_True() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("new_")
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
        // Given - empty text results in same name
        FileModel input = createTestFileModel("file", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.needsRename());
        assertEquals(result.getOldFullName(), result.getNewFullName());
    }

    // ============================================================================
    // D. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.ADD_TEXT, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertTrue(metadata.getAppliedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("test_")
                .withPosition(ItemPosition.END)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("test_", configMap.get("textToAdd"));
        assertEquals("END", configMap.get("position"));
    }

    @Test
    void testTransformationMetadata_BeginPosition() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("start_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("BEGIN", configMap.get("position"));
    }

    // ============================================================================
    // E. Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_NullInput() {
        // Given
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("prefix_")
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
                AddTextConfig.builder()
                        .withTextToAdd("prefix_")
                        .withPosition(null)
                        .build()
        );
        assertTrue(ex.getMessage().contains("position must not be null"));
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
        FileModel file1 = createTestFileModel("doc1", "txt");
        FileModel file2 = createTestFileModel("doc2", "pdf");
        FileModel file3 = createTestFileModel("doc3", "jpg");

        AddTextConfig configBegin = AddTextConfig.builder()
                .withTextToAdd("prefix_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        AddTextConfig configEnd = AddTextConfig.builder()
                .withTextToAdd("_suffix")
                .withPosition(ItemPosition.END)
                .build();

        PreparedFileModel result1 = transformer.transform(file1, configBegin);
        PreparedFileModel result2 = transformer.transform(file2, configEnd);
        PreparedFileModel result3 = transformer.transform(file3, configBegin);

        assertEquals("prefix_doc1", result1.getNewName());
        assertEquals("doc2_suffix", result2.getNewName());
        assertEquals("prefix_doc3", result3.getNewName());
    }

    @Test
    void testExtensionPreservation() {
        // Verify that extension is always preserved correctly
        FileModel input = createTestFileModel("file", "custom_ext");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("new_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertEquals("custom_ext", result.getNewExtension());
        assertEquals(input.getExtension(), result.getNewExtension());
    }

    @Test
    void testOriginalFilePreservation() {
        // Verify that original file reference is preserved
        FileModel input = createTestFileModel("original", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("modified_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertSame(input, result.getOriginalFile());
        assertEquals("original", result.getOriginalFile().getName());
    }

    @Test
    void testWhitespaceText() {
        // Test adding whitespace
        FileModel input = createTestFileModel("file", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("   ")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("   file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testNumericText() {
        // Test adding numeric text
        FileModel input = createTestFileModel("file", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("12345_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("12345_file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testMixedCharactersText() {
        // Test adding mixed characters (letters, numbers, symbols)
        FileModel input = createTestFileModel("file", "txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("Test123!@#_")
                .withPosition(ItemPosition.END)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("fileTest123!@#_", result.getNewName());
        assertTrue(result.needsRename());
    }
}
