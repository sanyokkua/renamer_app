package ua.renamer.app.backend.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.model.FolderDropOptions;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.service.FileRenameOrchestrator;
import ua.renamer.app.api.service.ProgressCallback;
import ua.renamer.app.backend.integration.support.GuiceTestHelper;
import ua.renamer.app.backend.integration.support.TestManifest;
import ua.renamer.app.backend.service.FolderExpansionService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Tag("integration")
public abstract class BaseRealMetadataIntegrationTest {

    /**
     * All integration-test-data resource paths relative to the integration-test-data root.
     * Used by {@link #copyAndExpandFullTree()} to reconstruct the full directory structure.
     */
    protected static final List<String> ALL_TEST_RESOURCES = List.of(
            "flat/document.txt",
            "flat/report_final.md",
            "flat/data_export.csv",
            "flat/config_dev.json",
            "flat/no_extension",
            "nested/level1_a.txt",
            "nested/level1_b.txt",
            "nested/sublevel/level2_a.txt",
            "nested/sublevel/deep/level3_a.txt",
            "multi_folder/folder_a/file_x.txt",
            "multi_folder/folder_a/file_y.txt",
            "multi_folder/folder_b/file_p.txt",
            "multi_folder/folder_b/file_q.txt",
            "media/photo_1920x1080.jpg",
            "media/photo_no_exif.jpg",
            "media/image_800x600.png",
            "media/song_with_tags.mp3",
            "media/audio_no_tags.wav"
    );
    protected static FileRenameOrchestrator orchestrator;
    protected static TestManifest manifest;
    protected static FolderExpansionService folderExpansionService;
    @TempDir
    protected Path tempDir;

    @BeforeAll
    static void setUpClass() {
        orchestrator = GuiceTestHelper.getOrchestrator();
        manifest = TestManifest.load();
        folderExpansionService = GuiceTestHelper.getInjector().getInstance(FolderExpansionService.class);
    }

    /**
     * Copies a file from classpath /integration-test-data/{resourceRelativePath} into tempDir (flat —
     * no subdirectory structure, only the filename is used).
     */
    protected File copyTestResource(String resourceRelativePath) throws IOException {
        String classpathPath = "/integration-test-data/" + resourceRelativePath;
        String fileName = Path.of(resourceRelativePath).getFileName().toString();
        Path dest = tempDir.resolve(fileName);
        try (InputStream in = getClass().getResourceAsStream(classpathPath)) {
            assertThat(in).as("Test resource not found: " + classpathPath).isNotNull();
            Files.copy(in, dest);
        }
        return dest.toFile();
    }

    /**
     * Copies a file from classpath into tempDir/{subDir}/{filename}. Creates subDir if absent.
     */
    protected File copyTestResourceTo(String resourceRelativePath, String subDir) throws IOException {
        String classpathPath = "/integration-test-data/" + resourceRelativePath;
        String fileName = Path.of(resourceRelativePath).getFileName().toString();
        Path dir = tempDir.resolve(subDir);
        Files.createDirectories(dir);
        Path dest = dir.resolve(fileName);
        try (InputStream in = getClass().getResourceAsStream(classpathPath)) {
            assertThat(in).as("Test resource not found: " + classpathPath).isNotNull();
            Files.copy(in, dest);
        }
        return dest.toFile();
    }

    /**
     * Copies a file from classpath into tempDir preserving the full relative path structure.
     * E.g. {@code "nested/sublevel/level2_a.txt"} → {@code tempDir/nested/sublevel/level2_a.txt}.
     */
    protected File copyTestResourcePreservingPath(String resourceRelativePath) throws IOException {
        String classpathPath = "/integration-test-data/" + resourceRelativePath;
        Path dest = tempDir.resolve(resourceRelativePath);
        Files.createDirectories(dest.getParent());
        try (InputStream in = getClass().getResourceAsStream(classpathPath)) {
            assertThat(in).as("Test resource not found: " + classpathPath).isNotNull();
            Files.copy(in, dest);
        }
        return dest.toFile();
    }

    /**
     * Copies ALL integration test resources into tempDir preserving directory structure, then
     * expands tempDir via {@link FolderExpansionService} with
     * {@code USE_CONTENTS + recursive=true + includeFoldersAsItems=true}.
     *
     * <p>This simulates the real user flow: user drops the root folder onto the app and all
     * files and sub-folders from all subdirectories are added to the working list.
     *
     * @return the full working list (files + sub-folders from all subdirectories)
     */
    protected List<File> copyAndExpandFullTree() throws IOException {
        for (String resource : ALL_TEST_RESOURCES) {
            copyTestResourcePreservingPath(resource);
        }
        // Files only (includeFoldersAsItems=false): avoids parent-dir + child collision in rename batches.
        // The practical use case is "rename all files across every subdirectory".
        FolderDropOptions options = new FolderDropOptions(
                FolderDropOptions.Action.USE_CONTENTS, true, false);
        return folderExpansionService.expand(tempDir, options).stream()
                .map(Path::toFile)
                .toList();
    }

    /**
     * Creates a plain text file in tempDir with the given content.
     */
    protected File createPlainFile(String name, String content) throws IOException {
        Path dest = tempDir.resolve(name);
        Files.writeString(dest, content);
        return dest.toFile();
    }

    /**
     * Asserts a file exists in tempDir at the given path (supports subdirectory paths).
     */
    protected void assertFileExists(String name) {
        assertThat(tempDir.resolve(name)).exists();
    }

    /**
     * Asserts a file does not exist in tempDir at the given path (supports subdirectory paths).
     */
    protected void assertFileNotExists(String name) {
        assertThat(tempDir.resolve(name)).doesNotExist();
    }

    /**
     * Asserts the file was physically renamed on disk: the old name is gone and the new name exists.
     * Both paths are resolved relative to tempDir.
     */
    protected void assertRenamed(String oldName, String newName) {
        assertFileNotExists(oldName);
        assertFileExists(newName);
    }

    /**
     * Verifies disk state for each result in a batch execution:
     * <ul>
     *   <li>ERROR_* status → assertion failure (no errors expected)</li>
     *   <li>SUCCESS → renamed file exists at its new path; original path is gone</li>
     *   <li>SKIPPED → original file still exists at its original path</li>
     * </ul>
     */
    protected void assertDiskStateForBatch(List<RenameResult> results) {
        for (RenameResult result : results) {
            String original = result.getOriginalFileName();
            String errorDetail = result.getErrorMessage().orElse("");
            assertThat(result.getStatus())
                    .as("Unexpected error for '%s': %s", original, errorDetail)
                    .isNotIn(RenameStatus.ERROR_EXTRACTION,
                            RenameStatus.ERROR_TRANSFORMATION,
                            RenameStatus.ERROR_EXECUTION);
            if (result.isSuccess()) {
                assertThat(result.getPreparedFile().getNewPath())
                        .as("Renamed file should exist on disk: %s", result.getNewFileName())
                        .exists();
                assertThat(result.getPreparedFile().getOldPath())
                        .as("Original file should be gone from disk: %s", original)
                        .doesNotExist();
            } else {
                // SKIPPED: file unchanged on disk
                assertThat(result.getPreparedFile().getOldPath())
                        .as("Unchanged file should still exist on disk: %s", original)
                        .exists();
            }
        }
    }

    protected ProgressCallback noOpCallback() {
        return (current, max) -> {
        };
    }

    /**
     * Runs the pipeline on a single file and asserts the result list has exactly 1 entry.
     */
    protected RenameResult executeSingle(File file, TransformationMode mode, Object config) {
        List<RenameResult> results = orchestrator.execute(List.of(file), mode, config, noOpCallback());
        assertThat(results).hasSize(1);
        return results.getFirst();
    }
}
