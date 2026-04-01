package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.TruncateOptions;
import ua.renamer.app.api.model.*;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.model.config.TruncateConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for TruncateTransformer.
 * Tests cover successful transformations, edge cases, error handling,
 * and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TruncateTransformerTest {

    private TruncateTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new TruncateTransformer();
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
    // A. REMOVE_SYMBOLS_IN_BEGIN Tests
    // ============================================================================

    @Test
    void testRemoveSymbolsInBegin_Success() {
        // Given
        FileModel input = createTestFileModel("prefix_document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(7)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveSymbolsInBegin_OneCharacter() {
        // Given
        FileModel input = createTestFileModel("xdocument", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(1)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveSymbolsInBegin_ZeroCharacters() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertFalse(result.needsRename(), "Removing 0 characters should not result in rename");
    }

    @Test
    void testRemoveSymbolsInBegin_ExactLength() {
        // Given - remove exactly the file length
        FileModel input = createTestFileModel("document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(8) // "document" has 8 characters
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error because result is empty
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("empty filename"));
        assertFalse(result.needsRename());
    }

    @Test
    void testRemoveSymbolsInBegin_MoreThanLength() {
        // Given - remove more characters than file length
        FileModel input = createTestFileModel("doc", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(10) // "doc" has only 3 characters
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error because result is empty
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("empty filename"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testRemoveSymbolsInBegin_LeavesOneCharacter() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(7) // Leave "t"
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("t", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. REMOVE_SYMBOLS_FROM_END Tests
    // ============================================================================

    @Test
    void testRemoveSymbolsFromEnd_Success() {
        // Given
        FileModel input = createTestFileModel("document_suffix", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(7)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveSymbolsFromEnd_OneCharacter() {
        // Given
        FileModel input = createTestFileModel("documentx", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(1)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testRemoveSymbolsFromEnd_ZeroCharacters() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertFalse(result.needsRename(), "Removing 0 characters should not result in rename");
    }

    @Test
    void testRemoveSymbolsFromEnd_ExactLength() {
        // Given - remove exactly the file length
        FileModel input = createTestFileModel("document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(8) // "document" has 8 characters
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error because result is empty
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("empty filename"));
        assertFalse(result.needsRename());
    }

    @Test
    void testRemoveSymbolsFromEnd_MoreThanLength() {
        // Given - remove more characters than file length
        FileModel input = createTestFileModel("doc", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(10) // "doc" has only 3 characters
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error because result is empty
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("empty filename"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testRemoveSymbolsFromEnd_LeavesOneCharacter() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(7) // Leave "d"
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("d", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // C. TRUNCATE_EMPTY_SYMBOLS Tests
    // ============================================================================

    @Test
    void testTruncateEmptySymbols_LeadingSpaces() {
        // Given
        FileModel input = createTestFileModel("   document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0) // Not used for TRUNCATE_EMPTY_SYMBOLS
                                              .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testTruncateEmptySymbols_TrailingSpaces() {
        // Given
        FileModel input = createTestFileModel("document   ", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testTruncateEmptySymbols_BothEnds() {
        // Given
        FileModel input = createTestFileModel("   document   ", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testTruncateEmptySymbols_NoWhitespace() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertFalse(result.needsRename(), "No whitespace should result in no rename");
    }

    @Test
    void testTruncateEmptySymbols_OnlySpaces() {
        // Given - filename is only spaces
        FileModel input = createTestFileModel("     ", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error because result is empty
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("empty filename"));
        assertFalse(result.needsRename());
    }

    @Test
    void testTruncateEmptySymbols_PreservesMiddleSpaces() {
        // Given - spaces in the middle should be preserved
        FileModel input = createTestFileModel("  my document  ", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("my document", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // D. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        FileModel input = createTestFileModel("prefix_file", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(7)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.TRUNCATE_FILE_NAME, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertTrue(metadata.getAppliedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModel("file_test", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(5)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals(5, configMap.get("numberOfSymbols"));
        assertEquals("REMOVE_SYMBOLS_FROM_END", configMap.get("truncateOption"));
    }

    @Test
    void testTransformationMetadata_TruncateEmptySymbols() {
        // Given
        FileModel input = createTestFileModel("  file  ", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("TRUNCATE_EMPTY_SYMBOLS", configMap.get("truncateOption"));
    }

    // ============================================================================
    // E. Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_NullInput() {
        // Given
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(5)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
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

        // Then - should return error result
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("Failed to truncate"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_NullTruncateOption() {
        // Config validation now rejects null truncateOption at construction time
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
            TruncateConfig.builder()
                          .withNumberOfSymbols(5)
                          .withTruncateOption(null)
                          .build()
        );
        assertTrue(ex.getMessage().contains("truncateOption must not be null"));
    }

    // ============================================================================
    // F. Integration and Multiple Scenarios Tests
    // ============================================================================

    @Test
    void testMultipleFiles_DifferentConfigurations() {
        // Test that transformer handles different files correctly
        FileModel file1 = createTestFileModel("prefix_doc1", "txt");
        FileModel file2 = createTestFileModel("doc2_suffix", "pdf");
        FileModel file3 = createTestFileModel("  doc3  ", "jpg");

        TruncateConfig configBegin = TruncateConfig.builder()
                                                   .withNumberOfSymbols(7)
                                                   .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                                   .build();

        TruncateConfig configEnd = TruncateConfig.builder()
                                                 .withNumberOfSymbols(7)
                                                 .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                                 .build();

        TruncateConfig configTrim = TruncateConfig.builder()
                                                  .withNumberOfSymbols(0)
                                                  .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                                  .build();

        PreparedFileModel result1 = transformer.transform(file1, configBegin);
        PreparedFileModel result2 = transformer.transform(file2, configEnd);
        PreparedFileModel result3 = transformer.transform(file3, configTrim);

        assertEquals("doc1", result1.getNewName());
        assertEquals("doc2", result2.getNewName());
        assertEquals("doc3", result3.getNewName());
    }

    @Test
    void testExtensionPreservation() {
        // Verify that extension is always preserved correctly
        FileModel input = createTestFileModel("prefix_file", "custom_ext");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(7)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertEquals("custom_ext", result.getNewExtension());
        assertEquals(input.getExtension(), result.getNewExtension());
    }

    @Test
    void testOriginalFilePreservation() {
        // Verify that original file reference is preserved
        FileModel input = createTestFileModel("original_prefix", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(9)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertSame(input, result.getOriginalFile());
        assertEquals("original_prefix", result.getOriginalFile().getName());
    }

    @Test
    void testUnicodeCharacters() {
        // Test with Unicode characters
        FileModel input = createTestFileModel("prefix_测试文档", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(7) // Remove "prefix_"
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("测试文档", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testSpecialCharacters() {
        // Test with special characters
        FileModel input = createTestFileModel("@@###file", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(5) // Remove "@@###"
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testVeryLongFilename() {
        // Test with very long filename
        String longName = "A".repeat(500) + "suffix";
        FileModel input = createTestFileModel(longName, "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(6) // Remove "suffix"
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                                              .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("A".repeat(500), result.getNewName());
        assertEquals(500, result.getNewName().length());
        assertTrue(result.needsRename());
    }

    @Test
    void testNeedsRename_TrueForChange() {
        // Given
        FileModel input = createTestFileModel("prefix_file", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(7)
                                              .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
    }

    @Test
    void testNeedsRename_FalseForNoChange() {
        // Given - no whitespace to trim
        FileModel input = createTestFileModel("file", "txt");
        TruncateConfig config = TruncateConfig.builder()
                                              .withNumberOfSymbols(0)
                                              .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                                              .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.needsRename());
        assertEquals(result.getOldFullName(), result.getNewFullName());
    }
}
