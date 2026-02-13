package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.core.v2.model.*;
import ua.renamer.app.core.v2.model.config.ExtensionChangeConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ExtensionChangeTransformer.
 * Tests cover successful transformations, edge cases, error handling,
 * and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExtensionChangeTransformerTest {

    private ExtensionChangeTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new ExtensionChangeTransformer();
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
    // A. Basic Functionality Tests
    // ============================================================================

    @Test
    void testChangeExtension_Success() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("md")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("document", result.getNewName());
        assertEquals("md", result.getNewExtension());
        assertEquals("document.md", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testChangeExtension_WithLeadingDot() {
        // Given - extension with leading dot
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension(".md")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - dot should be removed
        assertFalse(result.isHasError());
        assertEquals("md", result.getNewExtension());
        assertEquals("document.md", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testChangeExtension_Uppercase() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("MD")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("MD", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testChangeExtension_MixedCase() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("MkDn")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("MkDn", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testChangeExtension_SameExtension() {
        // Given - changing to same extension
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("txt")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should still succeed but no rename needed
        assertFalse(result.isHasError());
        assertEquals("txt", result.getNewExtension());
        assertFalse(result.needsRename(), "Same extension should not require rename");
    }

    @Test
    void testChangeExtension_MultiDot() {
        // Given - compound extension like tar.gz
        FileModel input = createTestFileModel("archive", "tar");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("tar.gz")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should accept compound extension
        assertFalse(result.isHasError());
        assertEquals("tar.gz", result.getNewExtension());
        assertEquals("archive.tar.gz", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testChangeExtension_MultiDotWithLeadingDot() {
        // Given - compound extension with leading dot
        FileModel input = createTestFileModel("archive", "zip");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension(".tar.gz")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - only first dot should be removed
        assertFalse(result.isHasError());
        assertEquals("tar.gz", result.getNewExtension());
        assertEquals("archive.tar.gz", result.getNewFullName());
        assertTrue(result.needsRename());
    }

    @Test
    void testChangeExtension_WithWhitespace() {
        // Given - extension with surrounding whitespace
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("  md  ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - whitespace should be trimmed
        assertFalse(result.isHasError());
        assertEquals("md", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_EmptyExtension() {
        // Given - empty extension
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("empty"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_WhitespaceOnlyExtension() {
        // Given - extension is only whitespace
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("   ")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error after trimming
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("empty"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_OnlyDotExtension() {
        // Given - extension is only a dot
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension(".")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error because after removing dot, it's empty
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("empty"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_NullInput() {
        // Given
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("md")
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
        assertTrue(result.getErrorMessage().get().contains("Failed to change extension"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void testErrorHandling_NullNewExtension() {
        // Given - config with null extension
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension(null)
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should return error result
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("Failed to change extension"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    // ============================================================================
    // C. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("md")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.CHANGE_EXTENSION, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertTrue(metadata.getAppliedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModel("document", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("pdf")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals("pdf", configMap.get("newExtension"));
    }

    @Test
    void testTransformationMetadata_WithLeadingDot() {
        // Given - verify metadata stores original input (before dot removal)
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension(".md")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals(".md", configMap.get("newExtension"));
    }

    // ============================================================================
    // D. Integration and Multiple Scenarios Tests
    // ============================================================================

    @Test
    void testMultipleFiles_DifferentExtensions() {
        // Test that transformer handles different files correctly
        FileModel file1 = createTestFileModel("doc1", "txt");
        FileModel file2 = createTestFileModel("doc2", "pdf");
        FileModel file3 = createTestFileModel("doc3", "jpg");

        ExtensionChangeConfig config1 = ExtensionChangeConfig.builder()
                                                             .withNewExtension("md")
                                                             .build();

        ExtensionChangeConfig config2 = ExtensionChangeConfig.builder()
                                                             .withNewExtension("docx")
                                                             .build();

        ExtensionChangeConfig config3 = ExtensionChangeConfig.builder()
                                                             .withNewExtension("png")
                                                             .build();

        PreparedFileModel result1 = transformer.transform(file1, config1);
        PreparedFileModel result2 = transformer.transform(file2, config2);
        PreparedFileModel result3 = transformer.transform(file3, config3);

        assertEquals("md", result1.getNewExtension());
        assertEquals("docx", result2.getNewExtension());
        assertEquals("png", result3.getNewExtension());
    }

    @Test
    void testFileNamePreservation() {
        // Verify that filename is never modified
        FileModel input = createTestFileModel("my_complex_file_name", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("md")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertEquals("my_complex_file_name", result.getNewName());
        assertEquals(input.getName(), result.getNewName());
    }

    @Test
    void testOriginalFilePreservation() {
        // Verify that original file reference is preserved
        FileModel input = createTestFileModel("original", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("md")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertSame(input, result.getOriginalFile());
        assertEquals("original", result.getOriginalFile().getName());
        assertEquals("txt", result.getOriginalFile().getExtension());
    }

    @Test
    void testCommonFileExtensions() {
        // Test various common file extensions
        FileModel input = createTestFileModel("file", "old");

        String[] extensions = {"txt", "pdf", "jpg", "png", "docx", "xlsx", "mp4", "mp3", "zip", "tar.gz"};

        for (String ext : extensions) {
            ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                                .withNewExtension(ext)
                                                                .build();

            PreparedFileModel result = transformer.transform(input, config);

            assertFalse(result.isHasError(), "Failed for extension: " + ext);
            assertEquals(ext, result.getNewExtension());
        }
    }

    @Test
    void testSpecialCharactersInExtension() {
        // Test extension with special characters (though uncommon)
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("bak_2024")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("bak_2024", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testNumericExtension() {
        // Test numeric extension
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("001")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("001", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testVeryLongExtension() {
        // Test with very long extension
        String longExt = "a".repeat(100);
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension(longExt)
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals(longExt, result.getNewExtension());
        assertEquals(100, result.getNewExtension().length());
        assertTrue(result.needsRename());
    }

    @Test
    void testNeedsRename_TrueForDifferentExtension() {
        // Given
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("md")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
        assertEquals("file.txt", result.getOldFullName());
        assertEquals("file.md", result.getNewFullName());
    }

    @Test
    void testNeedsRename_FalseForSameExtension() {
        // Given - same extension
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("txt")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.needsRename());
        assertEquals(result.getOldFullName(), result.getNewFullName());
    }

    @Test
    void testNeedsRename_TrueForCaseDifference() {
        // Given - same extension but different case
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("TXT")
                                                            .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - case difference should require rename
        assertTrue(result.needsRename());
        assertEquals("file.txt", result.getOldFullName());
        assertEquals("file.TXT", result.getNewFullName());
    }

    @Test
    void testUnicodeInExtension() {
        // Test with Unicode characters in extension (unusual but possible)
        FileModel input = createTestFileModel("file", "txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                                                            .withNewExtension("测试")
                                                            .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertFalse(result.isHasError());
        assertEquals("测试", result.getNewExtension());
        assertTrue(result.needsRename());
    }
}
