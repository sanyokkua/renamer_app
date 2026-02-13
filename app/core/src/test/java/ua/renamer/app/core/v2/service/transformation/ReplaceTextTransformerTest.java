package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.core.v2.enums.ItemPositionExtended;
import ua.renamer.app.core.v2.model.*;
import ua.renamer.app.core.v2.model.config.ReplaceTextConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ReplaceTextTransformer.
 * Tests cover successful replacements at different positions, edge cases,
 * error handling, and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReplaceTextTransformerTest {

    private ReplaceTextTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new ReplaceTextTransformer();
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

    /**
     * Helper method to create ReplaceTextConfig.
     */
    private ReplaceTextConfig createConfig(String textToReplace, String replacementText, ItemPositionExtended position) {
        return ReplaceTextConfig.builder()
                                .withTextToReplace(textToReplace)
                                .withReplacementText(replacementText)
                                .withPosition(position)
                                .build();
    }

    // ============================================================================
    // A. Basic Functionality Tests - Replace at BEGIN
    // ============================================================================

    @Test
    void testReplaceTextAtBegin_Success() {
        // Given
        FileModel input = createTestFileModel("old_document", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("new_document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertEquals("new_document.txt", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceTextAtBegin_NotPresent() {
        // Given - text to replace is not at the beginning
        FileModel input = createTestFileModel("document_old", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("document_old", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertFalse(result.needsRename(), "Should not rename if text not found at beginning");
    }

    @Test
    void testReplaceTextAtBegin_PartialMatch() {
        // Given - partial match at beginning
        FileModel input = createTestFileModel("oldfile", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("newfile", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. Basic Functionality Tests - Replace at END
    // ============================================================================

    @Test
    void testReplaceTextAtEnd_Success() {
        // Given
        FileModel input = createTestFileModel("document_old", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.END);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("document_new", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertEquals("document_new.txt", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceTextAtEnd_NotPresent() {
        // Given - text to replace is not at the end
        FileModel input = createTestFileModel("old_document", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.END);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("old_document", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertFalse(result.needsRename(), "Should not rename if text not found at end");
    }

    @Test
    void testReplaceTextAtEnd_PartialMatch() {
        // Given - partial match at end
        FileModel input = createTestFileModel("fileold", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.END);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("filenew", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // C. Basic Functionality Tests - Replace EVERYWHERE
    // ============================================================================

    @Test
    void testReplaceTextEverywhere_Success() {
        // Given - single occurrence in the middle
        FileModel input = createTestFileModel("document_old_file", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("document_new_file", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceTextEverywhere_NotPresent() {
        // Given - text to replace is not present anywhere
        FileModel input = createTestFileModel("document", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertFalse(result.needsRename(), "Should not rename if text not found");
    }

    @Test
    void testReplaceTextEverywhere_MultipleOccurrences() {
        // Given - multiple occurrences to replace
        FileModel input = createTestFileModel("old_document_old_file_old", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("new_document_new_file_new", result.getNewName());
        assertTrue(result.needsRename());
        // Verify all 3 occurrences were replaced
        assertEquals(3, countOccurrences(result.getNewName(), "new"));
        assertEquals(0, countOccurrences(result.getNewName(), "old"));
    }

    // ============================================================================
    // D. Edge Cases - Replace with Empty Text (Removal)
    // ============================================================================

    @Test
    void testReplaceTextWithEmpty_Begin() {
        // Given - effectively removes text from beginning
        FileModel input = createTestFileModel("prefix_document", "txt");
        ReplaceTextConfig config = createConfig("prefix_", "", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceTextWithEmpty_End() {
        // Given - effectively removes text from end
        FileModel input = createTestFileModel("document_suffix", "txt");
        ReplaceTextConfig config = createConfig("_suffix", "", ItemPositionExtended.END);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceTextWithEmpty_Everywhere() {
        // Given - removes all occurrences
        FileModel input = createTestFileModel("doc_x_file_x_name", "txt");
        ReplaceTextConfig config = createConfig("_x", "", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("doc_file_name", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceEmptyWithText_InsertsAtEveryPosition() {
        // Given - empty search string with EVERYWHERE inserts replacement between every character
        // This is the actual behavior of String.replace("", "text") in Java
        FileModel input = createTestFileModel("abc", "txt");
        ReplaceTextConfig config = createConfig("", "X", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        // String.replace("", "X") on "abc" produces "XaXbXcX"
        assertEquals("XaXbXcX", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceEmptyWithText_Begin() {
        // Given - empty search string at BEGIN position
        // startsWith("") is always true, so it prepends the replacement
        FileModel input = createTestFileModel("document", "txt");
        ReplaceTextConfig config = createConfig("", "prefix_", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("prefix_document", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceEmptyWithText_End() {
        // Given - empty search string at END position
        // endsWith("") is always true, so it appends the replacement
        FileModel input = createTestFileModel("document", "txt");
        ReplaceTextConfig config = createConfig("", "_suffix", ItemPositionExtended.END);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document_suffix", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // E. Edge Cases - Text Length Changes
    // ============================================================================

    @Test
    void testReplaceTextWithLongerText() {
        // Given - replacement is longer than original
        FileModel input = createTestFileModel("doc_a_file", "txt");
        ReplaceTextConfig config = createConfig("a", "abc", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("doc_abc_file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceTextWithShorterText() {
        // Given - replacement is shorter than original
        FileModel input = createTestFileModel("doc_abc_file", "txt");
        ReplaceTextConfig config = createConfig("abc", "a", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("doc_a_file", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // F. Case Sensitivity Tests
    // ============================================================================

    @Test
    void testReplaceCaseSensitive() {
        // Given - Java's replace() is case-sensitive
        FileModel input = createTestFileModel("ABC_document", "txt");
        ReplaceTextConfig config = createConfig("abc", "xyz", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("ABC_document", result.getNewName());
        assertFalse(result.needsRename(), "Case mismatch should not match");
    }

    @Test
    void testReplaceCaseSensitive_Everywhere() {
        // Given - case-sensitive replacement everywhere
        FileModel input = createTestFileModel("ABC_abc_Abc", "txt");
        ReplaceTextConfig config = createConfig("abc", "xyz", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("ABC_xyz_Abc", result.getNewName());
        assertTrue(result.needsRename());
        // Only exact case match should be replaced
        assertEquals(1, countOccurrences(result.getNewName(), "xyz"));
    }

    // ============================================================================
    // G. Same Name Result Tests
    // ============================================================================

    @Test
    void testReplaceTextResultsInSameName() {
        // Given - replacement results in the same name
        FileModel input = createTestFileModel("document", "txt");
        ReplaceTextConfig config = createConfig("doc", "doc", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("document", result.getNewName());
        assertFalse(result.needsRename(), "Same text replacement should not need rename");
    }

    // ============================================================================
    // H. needsRename() Validation Tests
    // ============================================================================

    @Test
    void testNeedsRename_TrueForBegin() {
        // Given
        FileModel input = createTestFileModel("old_file", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
        assertEquals("old_file.txt", result.getOldFullName());
        assertEquals("new_file.txt", result.getNewFullName());
    }

    @Test
    void testNeedsRename_TrueForEnd() {
        // Given
        FileModel input = createTestFileModel("file_old", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.END);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
        assertEquals("file_old.txt", result.getOldFullName());
        assertEquals("file_new.txt", result.getNewFullName());
    }

    @Test
    void testNeedsRename_TrueForEverywhere() {
        // Given
        FileModel input = createTestFileModel("old_file_old", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
        assertEquals("old_file_old.txt", result.getOldFullName());
        assertEquals("new_file_new.txt", result.getNewFullName());
    }

    @Test
    void testNeedsRename_FalseWhenNoMatch() {
        // Given - text not found
        FileModel input = createTestFileModel("document", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.needsRename());
        assertEquals(result.getOldFullName(), result.getNewFullName());
    }

    // ============================================================================
    // I. Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_NullInput() {
        // Given
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

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
        assertTrue(result.getErrorMessage().get().contains("Failed to replace text"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_NullPosition() {
        // Given - config with null position
        FileModel input = createTestFileModel("file", "txt");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                                                    .withTextToReplace("old")
                                                    .withReplacementText("new")
                                                    .withPosition(null)
                                                    .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should return error result
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("Failed to replace text"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_NullTextToReplace() {
        // Given - config with null text to replace
        FileModel input = createTestFileModel("file", "txt");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                                                    .withTextToReplace(null)
                                                    .withReplacementText("new")
                                                    .withPosition(ItemPositionExtended.BEGIN)
                                                    .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should return error result
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_NullReplacementText() {
        // Given - config with null replacement text
        FileModel input = createTestFileModel("old_file", "txt");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                                                    .withTextToReplace("old")
                                                    .withReplacementText(null)
                                                    .withPosition(ItemPositionExtended.BEGIN)
                                                    .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should return error result
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    // ============================================================================
    // J. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        FileModel input = createTestFileModel("old_file", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.REPLACE_TEXT, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertTrue(metadata.getAppliedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModel("old_document", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("old", configMap.get("textToReplace"));
        assertEquals("new", configMap.get("replacementText"));
        assertEquals("EVERYWHERE", configMap.get("position"));
    }

    @Test
    void testTransformationMetadata_BeginPosition() {
        // Given
        FileModel input = createTestFileModel("old_file", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("BEGIN", configMap.get("position"));
    }

    @Test
    void testTransformationMetadata_EndPosition() {
        // Given
        FileModel input = createTestFileModel("file_old", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.END);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("END", configMap.get("position"));
    }

    @Test
    void testTransformationMetadata_NullOnError() {
        // Given - config that will cause error
        FileModel input = createTestFileModel("file", "txt");

        // When
        PreparedFileModel result = transformer.transform(input, null);

        // Then
        assertNull(result.getTransformationMeta());
    }

    // ============================================================================
    // K. Integration and Complex Scenarios Tests
    // ============================================================================

    @Test
    void testMultipleFiles_DifferentConfigurations() {
        // Test that transformer handles different files correctly
        FileModel file1 = createTestFileModel("old_doc1", "txt");
        FileModel file2 = createTestFileModel("doc2_old", "pdf");
        FileModel file3 = createTestFileModel("old_doc3_old", "jpg");

        ReplaceTextConfig configBegin = createConfig("old", "new", ItemPositionExtended.BEGIN);
        ReplaceTextConfig configEnd = createConfig("old", "new", ItemPositionExtended.END);
        ReplaceTextConfig configEverywhere = createConfig("old", "new", ItemPositionExtended.EVERYWHERE);

        PreparedFileModel result1 = transformer.transform(file1, configBegin);
        PreparedFileModel result2 = transformer.transform(file2, configEnd);
        PreparedFileModel result3 = transformer.transform(file3, configEverywhere);

        assertEquals("new_doc1", result1.getNewName());
        assertEquals("doc2_new", result2.getNewName());
        assertEquals("new_doc3_new", result3.getNewName());
    }

    @Test
    void testExtensionPreservation() {
        // Verify that extension is always preserved correctly
        FileModel input = createTestFileModel("old_file", "custom_ext");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

        PreparedFileModel result = transformer.transform(input, config);

        assertEquals("custom_ext", result.getNewExtension());
        assertEquals(input.getExtension(), result.getNewExtension());
    }

    @Test
    void testOriginalFilePreservation() {
        // Verify that original file reference is preserved
        FileModel input = createTestFileModel("old_original", "txt");
        ReplaceTextConfig config = createConfig("old", "new", ItemPositionExtended.BEGIN);

        PreparedFileModel result = transformer.transform(input, config);

        assertSame(input, result.getOriginalFile());
        assertEquals("old_original", result.getOriginalFile().getName());
    }

    @Test
    void testSpecialCharacters() {
        // Test replacing special characters
        FileModel input = createTestFileModel("file@#$test", "txt");
        ReplaceTextConfig config = createConfig("@#$", "___", ItemPositionExtended.EVERYWHERE);

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("file___test", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testWhitespaceReplacement() {
        // Test replacing whitespace
        FileModel input = createTestFileModel("file   name", "txt");
        ReplaceTextConfig config = createConfig("   ", "_", ItemPositionExtended.EVERYWHERE);

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("file_name", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testNumericReplacement() {
        // Test replacing numeric text
        FileModel input = createTestFileModel("file123test", "txt");
        ReplaceTextConfig config = createConfig("123", "456", ItemPositionExtended.EVERYWHERE);

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("file456test", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testUnicodeCharacters() {
        // Test with Unicode characters
        FileModel input = createTestFileModel("file测试test", "txt");
        ReplaceTextConfig config = createConfig("测试", "テスト", ItemPositionExtended.EVERYWHERE);

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("fileテストtest", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testVeryLongReplacement() {
        // Test with very long replacement text
        FileModel input = createTestFileModel("short", "txt");
        String longText = "A".repeat(500);
        ReplaceTextConfig config = createConfig("short", longText, ItemPositionExtended.EVERYWHERE);

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals(longText, result.getNewName());
        assertEquals(500, result.getNewName().length());
        assertTrue(result.needsRename());
    }

    @Test
    void testReplaceEntireFilename() {
        // Test replacing the entire filename
        FileModel input = createTestFileModel("oldname", "txt");
        ReplaceTextConfig config = createConfig("oldname", "newname", ItemPositionExtended.EVERYWHERE);

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("newname", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testOverlappingPatterns() {
        // Test with overlapping patterns (replace doesn't repeat on replaced text)
        FileModel input = createTestFileModel("aaaa", "txt");
        ReplaceTextConfig config = createConfig("aa", "a", ItemPositionExtended.EVERYWHERE);

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - String.replace() replaces all non-overlapping occurrences
        assertFalse(result.isHasError());
        assertEquals("aa", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // L. Helper Methods for Tests
    // ============================================================================

    /**
     * Helper method to count occurrences of a substring in a string.
     */
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
