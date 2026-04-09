package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.ParentFolderConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddFolderNameIntegrationTest extends BaseRealMetadataIntegrationTest {

    private File createFileInSubdir(String subDir, String fileName, String content) throws Exception {
        Path dir = tempDir.resolve(subDir);
        Files.createDirectories(dir);
        Path filePath = dir.resolve(fileName);
        Files.writeString(filePath, content);
        return filePath.toFile();
    }

    @Test
    void addFolderName_oneParent_begin_underscore_prependsFolderName() throws Exception {
        File file = createFileInSubdir("myFolder", "file.txt", "content");
        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(1)
                .withPosition(ItemPosition.BEGIN)
                .withSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_FOLDER_NAME, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("myFolder_file.txt");
        assertThat(result.getPreparedFile().getNewPath()).exists();
        assertThat(result.getPreparedFile().getOldPath()).doesNotExist();
    }

    @Test
    void addFolderName_oneParent_end_dash_appendsFolderName() throws Exception {
        File file = createFileInSubdir("myFolder", "file.txt", "content");
        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(1)
                .withPosition(ItemPosition.END)
                .withSeparator("-")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_FOLDER_NAME, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("file-myFolder.txt");
        assertThat(result.getPreparedFile().getNewPath()).exists();
        assertThat(result.getPreparedFile().getOldPath()).doesNotExist();
    }

    @Test
    void addFolderName_twoParents_begin_underscore_includesGrandparent() throws Exception {
        File file = createFileInSubdir(Path.of("grand", "parent").toString(), "file.txt", "content");
        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(2)
                .withPosition(ItemPosition.BEGIN)
                .withSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_FOLDER_NAME, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("grand_parent_file.txt");
        assertThat(result.getPreparedFile().getNewPath()).exists();
        assertThat(result.getPreparedFile().getOldPath()).doesNotExist();
    }

    @Test
    void addFolderName_oneParent_begin_emptySeparator_noSpaceBetween() throws Exception {
        File file = createFileInSubdir("folder", "file.txt", "content");
        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(1)
                .withPosition(ItemPosition.BEGIN)
                .withSeparator("")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_FOLDER_NAME, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("folderfile.txt");
        assertThat(result.getPreparedFile().getNewPath()).exists();
        assertThat(result.getPreparedFile().getOldPath()).doesNotExist();
    }

    @Test
    void addFolderName_oneParent_begin_folderNameWithSpaces_spacesPreserved() throws Exception {
        File file = createFileInSubdir("my folder", "file.txt", "content");
        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(1)
                .withPosition(ItemPosition.BEGIN)
                .withSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_FOLDER_NAME, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("my folder_file.txt");
        assertThat(result.getPreparedFile().getNewPath()).exists();
        assertThat(result.getPreparedFile().getOldPath()).doesNotExist();
    }

    /**
     * Requests 3 parent folders but the file is only 2 levels deep (tempDir/parent/file.txt).
     * Verifies graceful behavior — no exception thrown. Records actual status for documentation.
     */
    @Test
    void addFolderName_requestMoreParentsThanExist_gracefulBehavior() throws Exception {
        File file = createFileInSubdir("parent", "file.txt", "content");
        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(3)
                .withPosition(ItemPosition.BEGIN)
                .withSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_FOLDER_NAME, config);

        // The app handles this gracefully — no exception, result has a defined status.
        // Acceptable outcomes: SUCCESS (uses available parents) or SKIPPED.
        assertThat(result.getStatus()).isIn(RenameStatus.SUCCESS, RenameStatus.SKIPPED);
        if (result.isSuccess()) {
            assertThat(result.getPreparedFile().getNewPath()).exists();
        } else {
            assertThat(result.getPreparedFile().getOldPath()).exists();
        }
    }

    @Test
    void addFolderName_nestedTestResource_immediateParentPrepended() throws Exception {
        File file = copyTestResourcePreservingPath("nested/sublevel/deep/level3_a.txt");
        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(1)
                .withPosition(ItemPosition.BEGIN)
                .withSeparator("_")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_FOLDER_NAME, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("deep_level3_a.txt");
        assertThat(result.getPreparedFile().getNewPath()).exists();
        assertThat(result.getPreparedFile().getOldPath()).doesNotExist();
    }

    @Test
    void addFolderName_fullTree_oneParent_begin_underscore_allFilesGetParentPrepended() throws Exception {
        List<File> allItems = copyAndExpandFullTree();
        assertThat(allItems).isNotEmpty();

        ParentFolderConfig config = ParentFolderConfig.builder()
                .withNumberOfParentFolders(1)
                .withPosition(ItemPosition.BEGIN)
                .withSeparator("_")
                .build();

        List<RenameResult> results = orchestrator.execute(allItems, TransformationMode.ADD_FOLDER_NAME, config,
                noOpCallback());

        assertThat(results).hasSameSizeAs(allItems);
        assertDiskStateForBatch(results);
    }
}
