package ua.renamer.app.core.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.enums.TextCaseOptions;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.CaseChangeConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive unit tests for CaseChangeTransformer.
 * Tests cover all 8 case transformations, capitalizeFirstLetter option,
 * edge cases, error handling, and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CaseChangeTransformerTest {

    private CaseChangeTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new CaseChangeTransformer();
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
    // A. Test All 8 Case Types (Tests 1-8)
    // ============================================================================

    @Test
    void testCamelCase_Transformation() {
        // Given
        FileModel input = createTestFileModel("test_file_name", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("testFileName", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testPascalCase_Transformation() {
        // Given
        FileModel input = createTestFileModel("test_file_name", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.PASCAL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("TestFileName", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testSnakeCase_Transformation() {
        // Given
        FileModel input = createTestFileModel("TestFileName", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("test_file_name", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testScreamingSnakeCase_Transformation() {
        // Given
        FileModel input = createTestFileModel("TestFileName", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE_SCREAMING)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("TEST_FILE_NAME", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testKebabCase_Transformation() {
        // Given
        FileModel input = createTestFileModel("TestFileName", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.KEBAB_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("test-file-name", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testUppercase_Transformation() {
        // Given
        FileModel input = createTestFileModel("TestFileName", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.UPPERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("TESTFILENAME", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testLowercase_Transformation() {
        // Given
        FileModel input = createTestFileModel("TestFileName", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("testfilename", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testTitleCase_Transformation() {
        // Given
        FileModel input = createTestFileModel("test_file_name", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.TITLE_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("Test File Name", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. CapitalizeFirstLetter Tests (Tests 9-12)
    // ============================================================================

    @Test
    void testCapitalizeFirstLetter_True_ForCamelCase() {
        // Given - camelCase normally starts with lowercase
        FileModel input = createTestFileModel("test_file", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(true)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("TestFile", result.getNewName()); // First letter capitalized
        assertTrue(result.needsRename());
    }

    @Test
    void testCapitalizeFirstLetter_False_ForCamelCase() {
        // Given - camelCase without capitalize should start with lowercase
        FileModel input = createTestFileModel("test_file", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("testFile", result.getNewName()); // First letter lowercase
        assertTrue(result.needsRename());
    }

    @Test
    void testCapitalizeFirstLetter_True_ForSnakeCase() {
        // Given - snake_case with capitalize should have first letter uppercase
        FileModel input = createTestFileModel("TestFile", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE)
                .withCapitalizeFirstLetter(true)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("Test_file", result.getNewName()); // First letter capitalized
        assertTrue(result.needsRename());
    }

    @Test
    void testCapitalizeFirstLetter_True_EmptyName() {
        // Given - empty name should not throw exception
        FileModel input = createTestFileModel("", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(true)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("", result.getNewName());
        assertFalse(result.needsRename()); // Empty to empty, no rename needed
    }

    // ============================================================================
    // C. Edge Cases (Tests 13-19)
    // ============================================================================

    @Test
    void testTransformAlreadyCorrectCase() {
        // Given - file already in the target case
        FileModel input = createTestFileModel("test_file_name", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("test_file_name", result.getNewName());
        assertFalse(result.needsRename()); // Same name, no rename needed
    }

    @Test
    void testTransformWithNumbers() {
        // Given - filename with numbers
        FileModel input = createTestFileModel("test123file", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("test_123_file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testTransformWithSpecialChars() {
        // Given - filename with special characters
        FileModel input = createTestFileModel("test@file", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        // Special chars like @ are NOT treated as word separators, only _ - . are
        assertEquals("test@file", result.getNewName());
        assertFalse(result.needsRename()); // No change since @ is not a separator
    }

    @Test
    void testTransformSingleCharacter() {
        // Given - single character filename
        FileModel input = createTestFileModel("a", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.UPPERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("A", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testTransformMixedCaseInput() {
        // Given - mixed case input
        FileModel input = createTestFileModel("TeSt_FiLe", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.KEBAB_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("te-st-fi-le", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testTransformWithSpaces() {
        // Given - filename with spaces
        FileModel input = createTestFileModel("test file name", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("testFileName", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testTransformPreservesUnicode() {
        // Given - filename with unicode characters
        FileModel input = createTestFileModel("测试File", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("测试file", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // D. NeedsRename Tests (Tests 20-21)
    // ============================================================================

    @Test
    void testNeedsRename_True() {
        // Given
        FileModel input = createTestFileModel("TestFile", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
    }

    @Test
    void testNeedsRename_False() {
        // Given - already in correct case
        FileModel input = createTestFileModel("testfile", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.needsRename());
        assertEquals(result.getOldFullName(), result.getNewFullName());
    }

    // ============================================================================
    // E. Transformation Metadata Tests (Test 22)
    // ============================================================================

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModel("TestFile", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE)
                .withCapitalizeFirstLetter(true)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.CHANGE_CASE, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertTrue(metadata.getAppliedAt().isBefore(LocalDateTime.now().plusSeconds(1)));

        Map<String, Object> configMap = metadata.getConfig();
        assertNotNull(configMap);
        assertEquals("SNAKE_CASE", configMap.get("caseOption"));
        assertEquals(true, configMap.get("capitalizeFirstLetter"));
    }

    // ============================================================================
    // F. Error Handling Tests (Tests 23-25)
    // ============================================================================

    @Test
    void testErrorHandling_NullInput() {
        // Given
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When/Then
        assertThrows(NullPointerException.class, () -> {
            transformer.transform(null, config);
        });
    }

    @Test
    void testErrorHandling_NullConfig() {
        // Given
        FileModel input = createTestFileModel("TestFile", "txt");

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
    void testErrorHandling_NullCaseOption() {
        // Config validation now rejects null caseOption at construction time
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                CaseChangeConfig.builder()
                        .withCaseOption(null)
                        .withCapitalizeFirstLetter(false)
                        .build()
        );
        assertTrue(ex.getMessage().contains("caseOption must not be null"));
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
    // G. Additional Integration Tests
    // ============================================================================

    @Test
    void testMultipleFiles_DifferentCases() {
        // Test that transformer handles different files correctly
        FileModel file1 = createTestFileModel("test_file", "txt");
        FileModel file2 = createTestFileModel("AnotherFile", "pdf");
        FileModel file3 = createTestFileModel("THIRD-FILE", "jpg");

        CaseChangeConfig configCamel = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        CaseChangeConfig configSnake = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        CaseChangeConfig configKebab = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.KEBAB_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        PreparedFileModel result1 = transformer.transform(file1, configCamel);
        PreparedFileModel result2 = transformer.transform(file2, configSnake);
        PreparedFileModel result3 = transformer.transform(file3, configKebab);

        assertEquals("testFile", result1.getNewName());
        assertEquals("another_file", result2.getNewName());
        assertEquals("third-file", result3.getNewName());
    }

    @Test
    void testExtensionPreservation() {
        // Verify that extension is always preserved correctly
        FileModel input = createTestFileModel("TestFile", "custom_ext");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertEquals("custom_ext", result.getNewExtension());
        assertEquals(input.getExtension(), result.getNewExtension());
    }

    @Test
    void testOriginalFilePreservation() {
        // Verify that original file reference is preserved
        FileModel input = createTestFileModel("TestFile", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertSame(input, result.getOriginalFile());
        assertEquals("TestFile", result.getOriginalFile().getName());
    }

    @Test
    void testCapitalizeFirstLetter_WithUppercase() {
        // Given - capitalizeFirstLetter with UPPERCASE (should still apply)
        FileModel input = createTestFileModel("test", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.UPPERCASE)
                .withCapitalizeFirstLetter(true)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("TEST", result.getNewName()); // Already uppercase, capitalize has no effect
        assertTrue(result.needsRename());
    }

    @Test
    void testCapitalizeFirstLetter_WithTitleCase() {
        // Given - capitalizeFirstLetter with TITLE_CASE (already starts with uppercase)
        FileModel input = createTestFileModel("test_file", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.TITLE_CASE)
                .withCapitalizeFirstLetter(true)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("Test File", result.getNewName()); // Title case already capitalizes
        assertTrue(result.needsRename());
    }

    @Test
    void testComplexFilename_MultipleDelimiters() {
        // Given - complex filename with multiple delimiter types
        FileModel input = createTestFileModel("Test_File-Name.With.Dots", "txt");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("testFileNameWithDots", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // H. Directory / Non-file Input Tests
    // ============================================================================

    @Test
    void transform_whenInputIsDirectory_withDirectoryMime_shouldSucceed() {
        // A directory entry has isFile=false but MIME = "application/x-directory".
        // The transformer should treat it as a processable input, not an error.
        FileModel directory = FileModel.builder()
                .withFile(new File("/test/myFolder"))
                .withIsFile(false)
                .withFileSize(0L)
                .withName("myFolder")
                .withExtension("")
                .withAbsolutePath("/test/myFolder")
                .withDetectedMimeType("application/x-directory")
                .withMetadata(null)
                .build();

        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.UPPERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        PreparedFileModel result = transformer.transform(directory, config);

        assertNotNull(result);
        assertFalse(result.isHasError());
        assertEquals("MYFOLDER", result.getNewName());
    }

    @Test
    void transform_whenInputIsNotFileAndNotDirectory_shouldReturnError() {
        // isFile=false AND detectedMimeType is NOT "application/x-directory"
        // → extraction failure branch.
        FileModel badInput = FileModel.builder()
                .withFile(new File("/dev/null"))
                .withIsFile(false)
                .withFileSize(0L)
                .withName("null")
                .withExtension("")
                .withAbsolutePath("/dev/null")
                .withDetectedMimeType("application/octet-stream")
                .withMetadata(null)
                .build();

        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.UPPERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        PreparedFileModel result = transformer.transform(badInput, config);

        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("extraction failed"));
    }
}
