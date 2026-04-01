package ua.renamer.app.core.v2.service.transformation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.*;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.model.config.ParentFolderConfig;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ParentFolderTransformer.
 * Tests cover various numbers of parent folders, both positions,
 * custom separators, edge cases with missing parents,
 * error handling, and metadata generation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParentFolderTransformerTest {

    private ParentFolderTransformer transformer;

    @BeforeAll
    void setUp() {
        transformer = new ParentFolderTransformer();
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Creates a test FileModel with a specific path structure.
     */
    private FileModel createTestFileModelWithPath(String path) {
        File file = new File(path);
        String name = file.getName();
        String extension = "";
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            extension = name.substring(lastDot + 1);
            name = name.substring(0, lastDot);
        }

        return FileModel.builder()
                        .withFile(file)
                        .withIsFile(true)
                        .withFileSize(1024L)
                        .withName(name)
                        .withExtension(extension)
                        .withAbsolutePath(path)
                        .withCreationDate(LocalDateTime.now().minusDays(1))
                        .withModificationDate(LocalDateTime.now())
                        .withDetectedMimeType("text/plain")
                        .withDetectedExtensions(Collections.emptySet())
                        .withCategory(Category.GENERIC)
                        .withMetadata(null)
                        .build();
    }

    // ============================================================================
    // A. One Parent Folder Tests
    // ============================================================================

    @Test
    void testOneParentFolder_AtBegin() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result);
        assertFalse(result.isHasError());
        assertTrue(result.getErrorMessage().isEmpty());
        assertEquals("parent_file", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    @Test
    void testOneParentFolder_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.END)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("file_parent", result.getNewName());
        assertEquals("txt", result.getNewExtension());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // B. Two Parent Folders Tests
    // ============================================================================

    @Test
    void testTwoParentFolders_AtBegin() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/grandparent/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(2)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        // Should be in order: grandparent first, then parent (furthest to closest after reverse)
        assertEquals("grandparent_parent_file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testTwoParentFolders_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/grandparent/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(2)
                                                      .withPosition(ItemPosition.END)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("file_grandparent_parent", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // C. Three Parent Folders Tests
    // ============================================================================

    @Test
    void testThreeParentFolders_AtBegin() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/great/grandparent/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(3)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("great_grandparent_parent_file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testThreeParentFolders_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/great/grandparent/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(3)
                                                      .withPosition(ItemPosition.END)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("file_great_grandparent_parent", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // D. Custom Separator Tests
    // ============================================================================

    @Test
    void testCustomSeparator_Dash() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("-")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("parent-file", result.getNewName());
    }

    @Test
    void testCustomSeparator_Space() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator(" ")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("parent file", result.getNewName());
    }

    @Test
    void testCustomSeparator_Dot() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator(".")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("parent.file", result.getNewName());
    }

    @Test
    void testCustomSeparator_MultipleFolders() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/grandparent/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(2)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("-")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("grandparent-parent-file", result.getNewName());
    }

    // ============================================================================
    // E. Parent Folder Order Tests
    // ============================================================================

    @Test
    void testParentFolderOrder_FurthestFirst() {
        // Given - verify order is furthest parent first
        FileModel input = createTestFileModelWithPath("/root/folder1/folder2/folder3/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(3)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - folder1 (furthest), folder2, folder3 (closest)
        assertFalse(result.isHasError());
        assertEquals("folder1_folder2_folder3_file", result.getNewName());
    }

    @Test
    void testParentFolderOrder_AtEnd() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/folder1/folder2/folder3/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(3)
                                                      .withPosition(ItemPosition.END)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("file_folder1_folder2_folder3", result.getNewName());
    }

    // ============================================================================
    // F. Special Characters in Folder Names Tests
    // ============================================================================

    @Test
    void testSpecialCharactersInFolderName_Spaces() {
        // Given - folder with spaces
        FileModel input = createTestFileModelWithPath("/root/My Documents/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("My Documents_file", result.getNewName());
    }

    @Test
    void testSpecialCharactersInFolderName_Symbols() {
        // Given - folder with special chars
        FileModel input = createTestFileModelWithPath("/root/Project@2024/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("Project@2024_file", result.getNewName());
    }

    @Test
    void testSpecialCharactersInFolderName_Unicode() {
        // Given - folder with unicode
        FileModel input = createTestFileModelWithPath("/root/文档/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        assertEquals("文档_file", result.getNewName());
    }

    // ============================================================================
    // G. Error Handling Tests - No Parent Folder
    // ============================================================================

    @Test
    void testNoParentFolder_Error() {
        // Given - file at root with no parent
        FileModel input = createTestFileModelWithPath("/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should error because no parent available
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("No parent folders"));
        assertFalse(result.needsRename());
    }

    // ============================================================================
    // H. Error Handling Tests - Requesting More Parents Than Exist
    // ============================================================================

    @Test
    void testRequestingMoreParentsThanExist() {
        // Given - only 2 parents available, requesting 5
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(5)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should use only available parents
        assertFalse(result.isHasError());
        // Should have parent and root only
        assertTrue(result.getNewName().contains("parent"));
        assertTrue(result.needsRename());
    }

    @Test
    void testRequestingMoreParentsThanExist_OnlyOneAvailable() {
        // Given - only 1 parent available, requesting 3
        FileModel input = createTestFileModelWithPath("/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(3)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then - should use only the one available parent
        assertFalse(result.isHasError());
        assertEquals("parent_file", result.getNewName());
        assertTrue(result.needsRename());
    }

    // ============================================================================
    // I. Transformation Metadata Tests
    // ============================================================================

    @Test
    void testTransformationMetadata_Populated() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertNotNull(result.getTransformationMeta());
        TransformationMetadata metadata = result.getTransformationMeta();
        assertEquals(TransformationMode.USE_PARENT_FOLDER_NAME, metadata.getMode());
        assertNotNull(metadata.getAppliedAt());
        assertNotNull(metadata.getConfig());
    }

    @Test
    void testTransformationMetadata_ConfigStored() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(2)
                                                      .withPosition(ItemPosition.END)
                                                      .withSeparator("-")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        TransformationMetadata metadata = result.getTransformationMeta();
        Map<String, Object> configMap = metadata.getConfig();
        assertEquals(2, configMap.get("numberOfParentFolders"));
        assertEquals("END", configMap.get("position"));
        assertEquals("-", configMap.get("separator"));
    }

    // ============================================================================
    // J. Additional Error Handling Tests
    // ============================================================================

    @Test
    void testErrorHandling_NullInput() {
        // Given
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When/Then
        assertThrows(NullPointerException.class, () -> {
            transformer.transform(null, config);
        });
    }

    @Test
    void testErrorHandling_NullConfig() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");

        // When
        PreparedFileModel result = transformer.transform(input, null);

        // Then - should return error result
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertTrue(result.getErrorMessage().isPresent());
        assertTrue(result.getErrorMessage().get().contains("Transformer configuration must not be null"));
        assertNull(result.getTransformationMeta());
        assertFalse(result.needsRename());
    }

    @Test
    void givenNullConfig_whenTransform_thenErrorResultReturned() {
        FileModel input = createTestFileModelWithPath("/test/path/document.txt");
        PreparedFileModel result = transformer.transform(input, null);
        assertNotNull(result);
        assertTrue(result.isHasError());
        assertFalse(result.getErrorMessage().isEmpty());
    }

    // ============================================================================
    // K. Integration and Multiple Scenarios Tests
    // ============================================================================

    @Test
    void testMultipleFiles_DifferentConfigurations() {
        // Test that transformer handles different files correctly
        FileModel file1 = createTestFileModelWithPath("/root/folder1/doc1.txt");
        FileModel file2 = createTestFileModelWithPath("/root/folder2/subfolder/doc2.pdf");
        FileModel file3 = createTestFileModelWithPath("/root/folder3/doc3.jpg");

        ParentFolderConfig config1 = ParentFolderConfig.builder()
                                                       .withNumberOfParentFolders(1)
                                                       .withPosition(ItemPosition.BEGIN)
                                                       .withSeparator("_")
                                                       .build();

        ParentFolderConfig config2 = ParentFolderConfig.builder()
                                                       .withNumberOfParentFolders(2)
                                                       .withPosition(ItemPosition.BEGIN)
                                                       .withSeparator("_")
                                                       .build();

        PreparedFileModel result1 = transformer.transform(file1, config1);
        PreparedFileModel result2 = transformer.transform(file2, config2);
        PreparedFileModel result3 = transformer.transform(file3, config1);

        assertEquals("folder1_doc1", result1.getNewName());
        assertEquals("folder2_subfolder_doc2", result2.getNewName());
        assertEquals("folder3_doc3", result3.getNewName());
    }

    @Test
    void testExtensionPreservation() {
        // Verify that extension is always preserved correctly
        FileModel input = createTestFileModelWithPath("/root/parent/file.custom_ext");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertEquals("custom_ext", result.getNewExtension());
        assertEquals(input.getExtension(), result.getNewExtension());
    }

    @Test
    void testOriginalFilePreservation() {
        // Verify that original file reference is preserved
        FileModel input = createTestFileModelWithPath("/root/parent/original.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        PreparedFileModel result = transformer.transform(input, config);

        assertSame(input, result.getOriginalFile());
        assertEquals("original", result.getOriginalFile().getName());
    }

    @Test
    void testNeedsRename_True() {
        // Given
        FileModel input = createTestFileModelWithPath("/root/parent/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(1)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertTrue(result.needsRename());
        assertNotEquals(result.getOldFullName(), result.getNewFullName());
    }

    @Test
    void testComplexPathHierarchy() {
        // Given - deeply nested path
        FileModel input = createTestFileModelWithPath("/home/user/Documents/Work/Projects/2024/Q1/file.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                                                      .withNumberOfParentFolders(4)
                                                      .withPosition(ItemPosition.BEGIN)
                                                      .withSeparator("_")
                                                      .build();

        // When
        PreparedFileModel result = transformer.transform(input, config);

        // Then
        assertFalse(result.isHasError());
        // Should have Work, Projects, 2024, Q1 (furthest to closest)
        assertEquals("Work_Projects_2024_Q1_file", result.getNewName());
        assertTrue(result.needsRename());
    }

    @Test
    void testZeroParentFolders_Error() {
        // Config validation now rejects numberOfParentFolders < 1 at construction time
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            ParentFolderConfig.builder()
                              .withNumberOfParentFolders(0)
                              .withPosition(ItemPosition.BEGIN)
                              .withSeparator("_")
                              .build()
        );
        assertTrue(ex.getMessage().contains("numberOfParentFolders must be >= 1"));
    }
}
