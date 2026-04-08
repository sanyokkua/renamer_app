package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.FolderDropOptions;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.AddTextConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the folder-drop user flow (step 1 of user interaction):
 * verifies that when the user drops a folder, the correct files and/or sub-folders
 * are added to the working list based on the chosen {@link FolderDropOptions}.
 *
 * <p>Each test then runs the pipeline to confirm the working list items are
 * correctly renamed on disk.
 */
class FolderDropIntegrationTest extends BaseRealMetadataIntegrationTest {

    /**
     * USE_CONTENTS, non-recursive, includeFoldersAsItems=true:
     * dropping multi_folder/ yields only its two immediate sub-folder children
     * (folder_a, folder_b) — no files, because there are no files at the root
     * of multi_folder.
     */
    @Test
    void folderDrop_useContents_nonRecursive_includeFolders_yieldsDirectSubfolders() throws Exception {
        // Copy multi_folder structure into tempDir preserving paths
        for (String resource : ALL_TEST_RESOURCES) {
            if (resource.startsWith("multi_folder/")) {
                copyTestResourcePreservingPath(resource);
            }
        }
        Path droppedFolder = tempDir.resolve("multi_folder");

        FolderDropOptions options = new FolderDropOptions(
                FolderDropOptions.Action.USE_CONTENTS, false, true);
        List<File> workingList = folderExpansionService.expand(droppedFolder, options).stream()
                .map(Path::toFile)
                .toList();

        // Direct children of multi_folder/ are folder_a/ and folder_b/ (both dirs)
        assertThat(workingList).hasSize(2);
        assertThat(workingList).allMatch(File::isDirectory);
        assertThat(workingList).extracting(File::getName)
                .containsExactlyInAnyOrder("folder_a", "folder_b");

        // Rename the folders: prepend "drop_"
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("drop_")
                .withPosition(ItemPosition.BEGIN)
                .build();
        List<RenameResult> results = orchestrator.execute(workingList, TransformationMode.ADD_TEXT, config, noOpCallback());

        assertThat(results).hasSize(2);
        assertDiskStateForBatch(results);
        assertThat(droppedFolder.resolve("drop_folder_a")).exists();
        assertThat(droppedFolder.resolve("drop_folder_b")).exists();
        assertThat(droppedFolder.resolve("folder_a")).doesNotExist();
        assertThat(droppedFolder.resolve("folder_b")).doesNotExist();
    }

    /**
     * USE_CONTENTS, recursive=true, includeFoldersAsItems=false:
     * dropping nested/ yields all 4 descendant files, no folders.
     */
    @Test
    void folderDrop_useContents_recursive_filesOnly_yieldsAllDescendantFiles() throws Exception {
        for (String resource : ALL_TEST_RESOURCES) {
            if (resource.startsWith("nested/")) {
                copyTestResourcePreservingPath(resource);
            }
        }
        Path droppedFolder = tempDir.resolve("nested");

        FolderDropOptions options = new FolderDropOptions(
                FolderDropOptions.Action.USE_CONTENTS, true, false);
        List<File> workingList = folderExpansionService.expand(droppedFolder, options).stream()
                .map(Path::toFile)
                .toList();

        // nested/ has 4 files total across all subdirectories
        assertThat(workingList).hasSize(4);
        assertThat(workingList).allMatch(File::isFile);
        assertThat(workingList).extracting(File::getName)
                .containsExactlyInAnyOrder("level1_a.txt", "level1_b.txt", "level2_a.txt", "level3_a.txt");

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("rec_")
                .withPosition(ItemPosition.BEGIN)
                .build();
        List<RenameResult> results = orchestrator.execute(workingList, TransformationMode.ADD_TEXT, config, noOpCallback());

        assertThat(results).hasSize(4);
        assertDiskStateForBatch(results);
    }

    /**
     * USE_CONTENTS, recursive=true, includeFoldersAsItems=true:
     * dropping nested/ yields 4 files plus 2 sub-folders (sublevel, deep) = 6 items.
     */
    @Test
    void folderDrop_useContents_recursive_includeFolders_yieldsFilesAndSubfolders() throws Exception {
        for (String resource : ALL_TEST_RESOURCES) {
            if (resource.startsWith("nested/")) {
                copyTestResourcePreservingPath(resource);
            }
        }
        Path droppedFolder = tempDir.resolve("nested");

        FolderDropOptions options = new FolderDropOptions(
                FolderDropOptions.Action.USE_CONTENTS, true, true);
        List<File> workingList = folderExpansionService.expand(droppedFolder, options).stream()
                .map(Path::toFile)
                .toList();

        // 4 files + 2 dirs (sublevel, sublevel/deep) = 6 items; root "nested" itself excluded
        assertThat(workingList).hasSize(6);
        long fileCount = workingList.stream().filter(File::isFile).count();
        long dirCount = workingList.stream().filter(File::isDirectory).count();
        assertThat(fileCount).isEqualTo(4);
        assertThat(dirCount).isEqualTo(2);

        // Rename only files — batching parent directories with their own children in the same
        // rename pass produces unpredictable results (renaming the parent moves all children,
        // invalidating captured child paths). The expansion assertion above already covers
        // the "correct items entered the working list" requirement for this option combination.
        List<File> filesOnly = workingList.stream().filter(File::isFile).toList();
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("all_")
                .withPosition(ItemPosition.BEGIN)
                .build();
        List<RenameResult> results = orchestrator.execute(filesOnly, TransformationMode.ADD_TEXT, config, noOpCallback());

        assertThat(results).hasSize(4);
        assertDiskStateForBatch(results);
    }

    /**
     * USE_AS_ITEM: the dropped folder itself is the single renamable item.
     * The FolderExpansionService is NOT called for this action — the UI layer
     * adds the folder directly to the working list. This test simulates that.
     */
    @Test
    void folderDrop_useAsItem_droppedFolderIsTheSingleRenameableItem() throws Exception {
        for (String resource : ALL_TEST_RESOURCES) {
            if (resource.startsWith("multi_folder/")) {
                copyTestResourcePreservingPath(resource);
            }
        }
        // Simulate USE_AS_ITEM: add the folder itself directly, no expand() call
        File droppedFolder = tempDir.resolve("multi_folder").toFile();
        List<File> workingList = List.of(droppedFolder);

        assertThat(workingList).hasSize(1);
        assertThat(workingList.getFirst()).isDirectory();

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("item_")
                .withPosition(ItemPosition.BEGIN)
                .build();
        List<RenameResult> results = orchestrator.execute(workingList, TransformationMode.ADD_TEXT, config, noOpCallback());

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getNewFileName()).isEqualTo("item_multi_folder");
        assertDiskStateForBatch(results);
        assertThat(tempDir.resolve("item_multi_folder")).exists().isDirectory();
        assertThat(tempDir.resolve("multi_folder")).doesNotExist();
    }
}
