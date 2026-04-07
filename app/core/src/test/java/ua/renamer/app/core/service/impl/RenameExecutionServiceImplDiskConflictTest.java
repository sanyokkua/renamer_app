package ua.renamer.app.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.enums.Category;
import ua.renamer.app.api.model.FileModel;
import ua.renamer.app.api.model.PreparedFileModel;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMetadata;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.core.service.validator.impl.NameValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for RenameExecutionServiceImpl disk-conflict resolution.
 *
 * <p>Verifies that when the target file already exists on disk the service
 * retries with " (001)", " (002)", … " (999)" suffixes rather than failing,
 * and that case-change renames on case-insensitive file systems are handled
 * without any suffix.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RenameExecutionServiceImplDiskConflictTest {

    @TempDir
    static Path tempDir;

    private RenameExecutionServiceImpl service;

    @BeforeAll
    void setUpClass() {
        // Mock NameValidator: real implementation calls toRealPath() which throws
        // NoSuchFileException for filenames that do not yet exist on disk.
        NameValidator mockValidator = mock(NameValidator.class);
        when(mockValidator.isValid(anyString())).thenReturn(true);
        service = new RenameExecutionServiceImpl(mockValidator);
    }

    @BeforeEach
    void cleanTempDir() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.debug("Best-effort cleanup failed for '{}': {}", path, e.getMessage());
                        }
                    });
        }
    }

    // ============================================================================
    // Helper methods (mirrors RenameExecutionServiceImplTest)
    // ============================================================================

    private File createTempFile(String name, String extension) throws IOException {
        Path filePath = tempDir.resolve(name + "." + extension);
        Files.writeString(filePath, "test content");
        return filePath.toFile();
    }

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

    private PreparedFileModel createPreparedFile(FileModel fileModel, String newName, String newExtension) {
        return PreparedFileModel.builder()
                .withOriginalFile(fileModel)
                .withNewName(newName)
                .withNewExtension(newExtension)
                .withHasError(false)
                .withErrorMessage(null)
                .withTransformationMeta(
                        TransformationMetadata.builder()
                                .withMode(TransformationMode.ADD_TEXT)
                                .withAppliedAt(LocalDateTime.now())
                                .withConfig(Map.of("test", "data"))
                                .build())
                .build();
    }

    // ============================================================================
    // 1. Single conflict: target exists → suffix (001) applied
    // ============================================================================

    @Test
    void givenTargetFileExistsOnDisk_whenExecute_thenSuffixAppliedAndFileRenamed() throws IOException {
        // Arrange: source file to rename, and a pre-existing file occupying the target name
        File sourceFile = createTempFile("source", "jpg");
        Files.createFile(tempDir.resolve("photo.jpg")); // occupies target slot

        FileModel fileModel = createFileModel(sourceFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "photo", "jpg");

        // Act
        RenameResult result = service.execute(preparedFile);

        // Assert: operation must succeed with suffix applied
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccess());

        // source.jpg must be gone; photo (001).jpg must exist on disk
        assertFalse(Files.exists(sourceFile.toPath()), "source.jpg should have been moved");
        assertTrue(Files.exists(tempDir.resolve("photo (001).jpg")), "photo (001).jpg must exist after conflict resolution");

        // The original photo.jpg (the blocker) must remain untouched
        assertTrue(Files.exists(tempDir.resolve("photo.jpg")), "pre-existing photo.jpg must not be overwritten");
    }

    // ============================================================================
    // 2. Two slots occupied: target + (001) exist → suffix (002) applied
    // ============================================================================

    @Test
    void givenTargetAndSuffix01ExistOnDisk_whenExecute_thenSuffix02Applied() throws IOException {
        // Arrange
        File sourceFile = createTempFile("source", "jpg");
        Files.createFile(tempDir.resolve("photo.jpg"));       // occupies target
        Files.createFile(tempDir.resolve("photo (001).jpg")); // occupies first suffix

        FileModel fileModel = createFileModel(sourceFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "photo", "jpg");

        // Act
        RenameResult result = service.execute(preparedFile);

        // Assert
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccess());

        assertFalse(Files.exists(sourceFile.toPath()), "source.jpg should have been moved");
        assertTrue(Files.exists(tempDir.resolve("photo (002).jpg")), "photo (002).jpg must exist after second conflict resolution");

        // Both prior files must still exist
        assertTrue(Files.exists(tempDir.resolve("photo.jpg")));
        assertTrue(Files.exists(tempDir.resolve("photo (001).jpg")));
    }

    // ============================================================================
    // 3. Case-change rename on case-insensitive filesystem (macOS / Windows)
    // ============================================================================

    /**
     * On a case-insensitive filesystem (macOS HFS+/APFS, Windows NTFS), renaming
     * "photo.jpg" to "PHOTO.JPG" must succeed without appending any suffix because
     * old and new paths resolve to the same inode.
     *
     * <p>The implementation detects this via equalsIgnoreCase on the absolute path
     * strings and routes through {@code File.renameTo()} directly.
     */
    @Test
    @EnabledOnOs({OS.MAC, OS.WINDOWS})
    void givenCaseChangeRename_whenSourceAndTargetSameFile_thenRenameSucceedsWithoutSuffix() throws IOException {
        // Arrange: create photo.jpg
        File sourceFile = createTempFile("photo", "jpg");
        FileModel fileModel = createFileModel(sourceFile);

        // PreparedFileModel targets "PHOTO" with extension "JPG"
        // On a case-insensitive FS: photo.jpg path and PHOTO.JPG path are equal
        // when compared case-insensitively, so isCaseChange == true.
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "PHOTO", "JPG");

        // Act
        RenameResult result = service.execute(preparedFile);

        // Assert: SUCCESS, no suffix appended
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccess());

        // On a case-insensitive FS, "photo.jpg" and "PHOTO.JPG" are the same entry;
        // after the rename the entry should exist under the new casing.
        assertTrue(Files.exists(tempDir.resolve("PHOTO.JPG")),
                "PHOTO.JPG should exist after case-change rename");
    }

    /**
     * On a case-sensitive filesystem (Linux ext4) a rename from "photo.jpg" to
     * "PHOTO.JPG" is NOT a case-change: both paths are distinct. The service
     * should treat it as a normal rename with no pre-existing conflict.
     */
    @Test
    @DisabledOnOs({OS.MAC, OS.WINDOWS})
    void givenCaseChangeRenameOnCaseSensitiveFs_whenNoConflict_thenRenameSucceedsWithoutSuffix() throws IOException {
        // Arrange
        File sourceFile = createTempFile("photo", "jpg");
        FileModel fileModel = createFileModel(sourceFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "PHOTO", "JPG");

        // Act
        RenameResult result = service.execute(preparedFile);

        // Assert: plain rename succeeds; no suffix
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccess());
        assertTrue(Files.exists(tempDir.resolve("PHOTO.JPG")),
                "PHOTO.JPG should exist after rename on case-sensitive FS");
        assertFalse(Files.exists(tempDir.resolve("photo.jpg")),
                "source photo.jpg should be gone");
    }

    // ============================================================================
    // 4. No conflict at all: clean rename, no suffix
    // ============================================================================

    @Test
    void givenNormalRename_whenNoConflict_thenNoSuffixAdded() throws IOException {
        // Arrange: source file exists; photo.jpg does NOT exist
        File sourceFile = createTempFile("source", "jpg");
        assertFalse(Files.exists(tempDir.resolve("photo.jpg")), "pre-condition: photo.jpg must not exist");

        FileModel fileModel = createFileModel(sourceFile);
        PreparedFileModel preparedFile = createPreparedFile(fileModel, "photo", "jpg");

        // Act
        RenameResult result = service.execute(preparedFile);

        // Assert: plain rename; the file lands at exactly photo.jpg
        assertEquals(RenameStatus.SUCCESS, result.getStatus());
        assertTrue(result.isSuccess());

        assertTrue(Files.exists(tempDir.resolve("photo.jpg")),
                "photo.jpg must exist after conflict-free rename");
        assertFalse(Files.exists(tempDir.resolve("source.jpg")),
                "source.jpg should no longer exist");

        // No suffix variants should have been created
        assertFalse(Files.exists(tempDir.resolve("photo (001).jpg")),
                "no suffix should be applied when there is no conflict");
    }
}
