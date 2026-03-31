package ua.renamer.app.core.v2.service.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ua.renamer.app.api.model.*;
import ua.renamer.app.api.enums.Category;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DuplicateNameResolverImpl.
 * Tests duplicate detection, suffix generation, edge cases, and error handling.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DuplicateNameResolverImplTest {

    private DuplicateNameResolverImpl resolver;

    @BeforeAll
    void setUp() {
        resolver = new DuplicateNameResolverImpl();
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Creates a test FileModel with the given name and extension.
     */
    private FileModel createTestFileModel(String name, String extension, String path) {
        return FileModel.builder()
                        .withFile(new File(path))
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

    /**
     * Creates a PreparedFileModel with the given old and new names.
     */
    private PreparedFileModel createPreparedFile(String oldName, String oldExt,
                                                 String newName, String newExt,
                                                 String path) {
        FileModel fileModel = createTestFileModel(oldName, oldExt, path);

        return PreparedFileModel.builder()
                                .withOriginalFile(fileModel)
                                .withNewName(newName)
                                .withNewExtension(newExt)
                                .withHasError(false)
                                .withErrorMessage(null)
                                .withTransformationMeta(TransformationMetadata.builder()
                                                                              .withMode(TransformationMode.ADD_TEXT)
                                                                              .withAppliedAt(LocalDateTime.now())
                                                                              .withConfig(Map.of("test", "data"))
                                                                              .build())
                                .build();
    }

    /**
     * Creates a PreparedFileModel with an error.
     */
    private PreparedFileModel createPreparedFileWithError(String oldName, String oldExt,
                                                          String newName, String newExt,
                                                          String path, String error) {
        FileModel fileModel = createTestFileModel(oldName, oldExt, path);

        return PreparedFileModel.builder()
                                .withOriginalFile(fileModel)
                                .withNewName(newName)
                                .withNewExtension(newExt)
                                .withHasError(true)
                                .withErrorMessage(error)
                                .withTransformationMeta(null)
                                .build();
    }

    // ============================================================================
    // A. Basic Functionality Tests - No Duplicates
    // ============================================================================

    @Test
    void testResolve_NoDuplicates_AllUnique() {
        // Given
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old1", "txt", "new1", "txt", "/test/old1.txt"),
                createPreparedFile("old2", "txt", "new2", "txt", "/test/old2.txt"),
                createPreparedFile("old3", "txt", "new3", "txt", "/test/old3.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(3, result.size());

        // Find by path since order may not be preserved
        PreparedFileModel result1 = findByPath(result, "/test/old1.txt");
        PreparedFileModel result2 = findByPath(result, "/test/old2.txt");
        PreparedFileModel result3 = findByPath(result, "/test/old3.txt");

        assertEquals("new1", result1.getNewName());
        assertEquals("new2", result2.getNewName());
        assertEquals("new3", result3.getNewName());
    }

    @Test
    void testResolve_EmptyList() {
        // Given
        List<PreparedFileModel> models = Collections.emptyList();

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testResolve_SingleFile() {
        // Given
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old", "txt", "new", "txt", "/test/old.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(1, result.size());
        assertEquals("new", result.get(0).getNewName());
    }

    // ============================================================================
    // B. Duplicate Detection Tests - Two Files
    // ============================================================================

    @Test
    void testResolve_TwoFilesSameName() {
        // Given
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old1", "txt", "file", "txt", "/test/old1.txt"),
                createPreparedFile("old2", "txt", "file", "txt", "/test/old2.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(2, result.size());
        assertEquals("file", result.get(0).getNewName());
        assertEquals("file (1)", result.get(1).getNewName());
        assertEquals("txt", result.get(0).getNewExtension());
        assertEquals("txt", result.get(1).getNewExtension());
    }

    @Test
    void testResolve_TwoFilesSameName_DifferentExtensions() {
        // Given - Same name but different extensions should NOT collide
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old1", "txt", "file", "txt", "/test/old1.txt"),
                createPreparedFile("old2", "jpg", "file", "jpg", "/test/old2.jpg")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(2, result.size());

        // Find by path since order may not be preserved
        PreparedFileModel resultTxt = findByPath(result, "/test/old1.txt");
        PreparedFileModel resultJpg = findByPath(result, "/test/old2.jpg");

        assertEquals("file", resultTxt.getNewName());  // No suffix
        assertEquals("file", resultJpg.getNewName());  // No suffix
        assertEquals("txt", resultTxt.getNewExtension());
        assertEquals("jpg", resultJpg.getNewExtension());
    }

    // ============================================================================
    // C. Duplicate Detection Tests - Multiple Files
    // ============================================================================

    @Test
    void testResolve_ThreeFilesSameName() {
        // Given
        List<PreparedFileModel> models = List.of(
                createPreparedFile("a", "txt", "file", "txt", "/test/a.txt"),
                createPreparedFile("b", "txt", "file", "txt", "/test/b.txt"),
                createPreparedFile("c", "txt", "file", "txt", "/test/c.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(3, result.size());
        assertEquals("file", result.get(0).getNewName());
        assertEquals("file (1)", result.get(1).getNewName());
        assertEquals("file (2)", result.get(2).getNewName());
    }

    @Test
    void testResolve_TenFilesSameName() {
        // Given
        List<PreparedFileModel> models = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            models.add(createPreparedFile("old" + i, "txt", "file", "txt",
                                          "/test/old" + i + ".txt"));
        }

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(10, result.size());
        assertEquals("file", result.get(0).getNewName());
        assertEquals("file (01)", result.get(1).getNewName());
        assertEquals("file (02)", result.get(2).getNewName());
        assertEquals("file (03)", result.get(3).getNewName());
        assertEquals("file (04)", result.get(4).getNewName());
        assertEquals("file (05)", result.get(5).getNewName());
        assertEquals("file (06)", result.get(6).getNewName());
        assertEquals("file (07)", result.get(7).getNewName());
        assertEquals("file (08)", result.get(8).getNewName());
        assertEquals("file (09)", result.get(9).getNewName());

    }

    @Test
    void testResolve_HundredFilesSameName() {
        // Given
        List<PreparedFileModel> models = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            models.add(createPreparedFile("old" + i, "txt", "file", "txt",
                                          "/test/old" + i + ".txt"));
        }

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(101, result.size());
        // Should use 3-digit padding: (001), (002), ..., (100)
        assertEquals("file", result.get(0).getNewName());
        assertEquals("file (001)", result.get(1).getNewName());
        assertEquals("file (011)", result.get(11).getNewName());
        assertEquals("file (098)", result.get(98).getNewName());
        assertEquals("file (099)", result.get(99).getNewName());
        assertEquals("file (100)", result.get(100).getNewName());
    }

    // ============================================================================
    // D. Mixed Scenarios - Some Duplicates, Some Unique
    // ============================================================================

    @Test
    void testResolve_MixedDuplicatesAndUnique() {
        // Given
        List<PreparedFileModel> models = List.of(
                createPreparedFile("a", "txt", "file1", "txt", "/test/a.txt"),
                createPreparedFile("b", "txt", "file2", "txt", "/test/b.txt"),  // unique
                createPreparedFile("c", "txt", "file1", "txt", "/test/c.txt"),
                createPreparedFile("d", "txt", "file3", "txt", "/test/d.txt")   // unique
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(4, result.size());

        // Find results by original file path
        PreparedFileModel resultA = findByPath(result, "/test/a.txt");
        PreparedFileModel resultB = findByPath(result, "/test/b.txt");
        PreparedFileModel resultC = findByPath(result, "/test/c.txt");
        PreparedFileModel resultD = findByPath(result, "/test/d.txt");

        assertEquals("file1", resultA.getNewName());
        assertEquals("file2", resultB.getNewName());  // No collision
        assertEquals("file1 (1)", resultC.getNewName());
        assertEquals("file3", resultD.getNewName());  // No collision
    }

    @Test
    void testResolve_MultipleDuplicateGroups() {
        // Given - 2 files named "a" and 3 files named "b"
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old1", "txt", "a", "txt", "/test/old1.txt"),
                createPreparedFile("old2", "txt", "b", "txt", "/test/old2.txt"),
                createPreparedFile("old3", "txt", "a", "txt", "/test/old3.txt"),
                createPreparedFile("old4", "txt", "b", "txt", "/test/old4.txt"),
                createPreparedFile("old5", "txt", "b", "txt", "/test/old5.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(5, result.size());

        PreparedFileModel result1 = findByPath(result, "/test/old1.txt");
        PreparedFileModel result2 = findByPath(result, "/test/old2.txt");
        PreparedFileModel result3 = findByPath(result, "/test/old3.txt");
        PreparedFileModel result4 = findByPath(result, "/test/old4.txt");
        PreparedFileModel result5 = findByPath(result, "/test/old5.txt");

        assertEquals("a", result1.getNewName());
        assertEquals("b", result2.getNewName());
        assertEquals("a (1)", result3.getNewName());
        assertEquals("b (1)", result4.getNewName());
        assertEquals("b (2)", result5.getNewName());
    }

    // ============================================================================
    // E. Error Handling Tests
    // ============================================================================

    @Test
    void testResolve_FilesWithErrors_Skipped() {
        // Given - Files with errors should be preserved as-is
        List<PreparedFileModel> models = List.of(
                createPreparedFile("a", "txt", "file", "txt", "/test/a.txt"),
                createPreparedFileWithError("b", "txt", "file", "txt", "/test/b.txt", "Test error"),
                createPreparedFile("c", "txt", "file", "txt", "/test/c.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(3, result.size());

        PreparedFileModel resultA = findByPath(result, "/test/a.txt");
        PreparedFileModel resultB = findByPath(result, "/test/b.txt");
        PreparedFileModel resultC = findByPath(result, "/test/c.txt");

        assertEquals("file", resultA.getNewName());
        assertEquals("file", resultB.getNewName());  // Error file unchanged
        assertTrue(resultB.isHasError());
        assertEquals("file (1)", resultC.getNewName());
    }

    @Test
    void testResolve_AllFilesHaveErrors() {
        // Given
        List<PreparedFileModel> models = List.of(
                createPreparedFileWithError("a", "txt", "file", "txt", "/test/a.txt", "Error 1"),
                createPreparedFileWithError("b", "txt", "file", "txt", "/test/b.txt", "Error 2"),
                createPreparedFileWithError("c", "txt", "file", "txt", "/test/c.txt", "Error 3")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(3, result.size());
        // All should be unchanged
        assertTrue(result.get(0).isHasError());
        assertTrue(result.get(1).isHasError());
        assertTrue(result.get(2).isHasError());
        assertEquals("file", result.get(0).getNewName());
        assertEquals("file", result.get(1).getNewName());
        assertEquals("file", result.get(2).getNewName());
    }

    // ============================================================================
    // F. Suffix Format Tests
    // ============================================================================

    @Test
    void testResolve_SuffixFormat_SingleDigit() {
        // Given - Less than 10 files
        List<PreparedFileModel> models = List.of(
                createPreparedFile("a", "txt", "file", "txt", "/test/a.txt"),
                createPreparedFile("b", "txt", "file", "txt", "/test/b.txt"),
                createPreparedFile("c", "txt", "file", "txt", "/test/c.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then - Should use single digit (1), (2), (3)
        assertEquals("file", result.get(0).getNewName());
        assertEquals("file (1)", result.get(1).getNewName());
        assertEquals("file (2)", result.get(2).getNewName());
    }

    @Test
    void testResolve_SuffixFormat_DoubleDigit() {
        // Given - 10-99 files
        List<PreparedFileModel> models = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            models.add(createPreparedFile("old" + i, "txt", "file", "txt",
                                          "/test/old" + i + ".txt"));
        }

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then - Should use double digits with leading zero
        assertEquals("file", result.get(0).getNewName());
        assertEquals("file (08)", result.get(8).getNewName());
        assertEquals("file (09)", result.get(9).getNewName());
        assertEquals("file (14)", result.get(14).getNewName());
    }

    @Test
    void testResolve_SuffixFormat_TripleDigit() {
        // Given - 100+ files
        List<PreparedFileModel> models = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            models.add(createPreparedFile("old" + i, "txt", "file", "txt",
                                          "/test/old" + i + ".txt"));
        }

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then - Should use triple digits
        assertEquals("file", result.get(0).getNewName());
        assertEquals("file (098)", result.get(98).getNewName());
        assertEquals("file (099)", result.get(99).getNewName());
        assertEquals("file (149)", result.get(149).getNewName());
    }

    // ============================================================================
    // G. Collision Prevention Tests
    // ============================================================================

    @Test
    void testResolve_PreventReCollision_ExistingNameWithSuffix() {
        // Given - "file (2)" already exists as a target name
        List<PreparedFileModel> models = List.of(
                createPreparedFile("a", "txt", "file", "txt", "/test/a.txt"),
                createPreparedFile("b", "txt", "file (1)", "txt", "/test/b.txt"),  // Pre-existing suffix
                createPreparedFile("c", "txt", "file", "txt", "/test/c.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then - Should skip "file (2)" and use "file (3)"
        assertEquals(3, result.size());

        PreparedFileModel resultA = findByPath(result, "/test/a.txt");
        PreparedFileModel resultB = findByPath(result, "/test/b.txt");
        PreparedFileModel resultC = findByPath(result, "/test/c.txt");

        assertEquals("file", resultA.getNewName());
        assertEquals("file (1)", resultB.getNewName());
        assertEquals("file (2)", resultC.getNewName());
    }

    @Test
    void testResolve_UsedNamesTracking_AvoidNewCollisions() {
        // Given - Complex scenario with multiple groups
        List<PreparedFileModel> models = List.of(
                createPreparedFile("beforeA", "txt", "file", "txt", "/test/beforeA.txt"),
                createPreparedFile("a", "txt", "file", "txt", "/test/a.txt"),
                createPreparedFile("b", "txt", "doc", "txt", "/test/b.txt"),
                createPreparedFile("c", "txt", "file", "txt", "/test/c.txt"),
                createPreparedFile("d", "txt", "doc", "txt", "/test/d.txt"),
                createPreparedFile("e", "txt", "file (2)", "txt", "/test/e.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(6, result.size());

        PreparedFileModel resultBeforeA = findByPath(result, "/test/beforeA.txt");
        PreparedFileModel resultA = findByPath(result, "/test/a.txt");
        PreparedFileModel resultB = findByPath(result, "/test/b.txt");
        PreparedFileModel resultC = findByPath(result, "/test/c.txt");
        PreparedFileModel resultD = findByPath(result, "/test/d.txt");
        PreparedFileModel resultE = findByPath(result, "/test/e.txt");

        assertEquals("file", resultBeforeA.getNewName());
        assertEquals("file (1)", resultA.getNewName());
        assertEquals("doc", resultB.getNewName());
        assertEquals("file (3)", resultC.getNewName());
        assertEquals("doc (1)", resultD.getNewName());
        assertEquals("file (2)", resultE.getNewName());
    }

    // ============================================================================
    // H. Edge Cases
    // ============================================================================

    @Test
    void testResolve_SameNameDifferentPath_SameDirectory() {
        // Given - Both files in same directory with same target name
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old1", "txt", "new", "txt", "/test/old1.txt"),
                createPreparedFile("old2", "txt", "new", "txt", "/test/old2.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then - Should get suffixes
        assertEquals(2, result.size());
        assertEquals("new", result.get(0).getNewName());
        assertEquals("new (1)", result.get(1).getNewName());
    }

    @Test
    void testResolve_VeryLongFileName() {
        // Given
        String longName = "a".repeat(200);
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old1", "txt", longName, "txt", "/test/old1.txt"),
                createPreparedFile("old2", "txt", longName, "txt", "/test/old2.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(2, result.size());
        assertEquals(longName, result.get(0).getNewName());
        assertEquals(longName + " (1)", result.get(1).getNewName());
    }

    @Test
    void testResolve_SpecialCharactersInName() {
        // Given
        String specialName = "file@#$%^&*()";
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old1", "txt", specialName, "txt", "/test/old1.txt"),
                createPreparedFile("old2", "txt", specialName, "txt", "/test/old2.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(2, result.size());
        assertEquals(specialName, result.get(0).getNewName());
        assertEquals(specialName + " (1)", result.get(1).getNewName());
    }

    @Test
    void testResolve_UnicodeCharactersInName() {
        // Given
        String unicodeName = "файл_文件_ファイル";
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old1", "txt", unicodeName, "txt", "/test/old1.txt"),
                createPreparedFile("old2", "txt", unicodeName, "txt", "/test/old2.txt")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then
        assertEquals(2, result.size());
        assertEquals(unicodeName, result.get(0).getNewName());
        assertEquals(unicodeName + " (1)", result.get(1).getNewName());
    }

    @Test
    void testResolve_ExtensionPreserved() {
        // Given
        List<PreparedFileModel> models = List.of(
                createPreparedFile("a", "txt", "file", "md", "/test/a.txt"),
                createPreparedFile("b", "docx", "file", "md", "/test/b.docx")
        );

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then - Extensions preserved
        assertEquals(2, result.size());
        assertEquals("file", result.get(0).getNewName());
        assertEquals("md", result.get(0).getNewExtension());
        assertEquals("file (1)", result.get(1).getNewName());
        assertEquals("md", result.get(1).getNewExtension());
    }

    // ============================================================================
    // I. Immutability Tests
    // ============================================================================

    @Test
    void testResolve_OriginalFileModelUnchanged() {
        // Given
        List<PreparedFileModel> models = List.of(
                createPreparedFile("old", "txt", "new", "txt", "/test/old.txt")
        );
        PreparedFileModel original = models.get(0);

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then - Original should be unchanged (immutability)
        assertEquals("new", original.getNewName());
        assertEquals("new", result.get(0).getNewName());
        // Note: Since there's no collision, the same instance is returned (no need to create new)
        // This is acceptable as no modification occurred
    }

    @Test
    void testResolve_InputListUnchanged() {
        // Given
        List<PreparedFileModel> models = new ArrayList<>(List.of(
                createPreparedFile("a", "txt", "file", "txt", "/test/a.txt"),
                createPreparedFile("b", "txt", "file", "txt", "/test/b.txt")
        ));
        int originalSize = models.size();
        String originalName1 = models.get(0).getNewName();

        // When
        List<PreparedFileModel> result = resolver.resolve(models);

        // Then - Input list unchanged
        assertEquals(originalSize, models.size());
        assertEquals(originalName1, models.get(0).getNewName());
        assertNotSame(models, result);
    }

    // ============================================================================
    // Utility Methods
    // ============================================================================

    private PreparedFileModel findByPath(List<PreparedFileModel> models, String path) {
        return models.stream()
                     .filter(m -> m.getOriginalFile().getAbsolutePath().equals(path))
                     .findFirst()
                     .orElseThrow(() -> new AssertionError("File not found: " + path));
    }
}
