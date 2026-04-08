package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.AddTextConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddTextIntegrationTest extends BaseRealMetadataIntegrationTest {

    @Test
    void addText_begin_plainAscii_prependsToName() throws Exception {
        File file = copyTestResource("flat/document.txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("PREFIX_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("PREFIX_document.txt");
        assertRenamed("document.txt", "PREFIX_document.txt");
    }

    @Test
    void addText_end_plainAscii_appendsBeforeExtension() throws Exception {
        File file = copyTestResource("flat/report_final.md");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("_SUFFIX")
                .withPosition(ItemPosition.END)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("report_final_SUFFIX.md");
        assertRenamed("report_final.md", "report_final_SUFFIX.md");
    }

    @Test
    void addText_begin_noExtension_prependsToName() throws Exception {
        File file = copyTestResource("flat/no_extension");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("PRE_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("PRE_no_extension");
        assertRenamed("no_extension", "PRE_no_extension");
    }

    @Test
    void addText_begin_emptyText_skipsFile() throws Exception {
        File file = copyTestResource("flat/document.txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("document.txt");
    }

    @Test
    void addText_begin_unicodeText_prependsUnicode() throws Exception {
        File file = copyTestResource("flat/document.txt");
        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("日本語_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.ADD_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("日本語_document.txt");
        assertRenamed("document.txt", "日本語_document.txt");
    }

    @Test
    void addText_begin_batchOfFive_allSucceed() throws Exception {
        List<File> files = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            files.add(createPlainFile("file_" + i + ".txt", "content " + i));
        }

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("batch_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(files, TransformationMode.ADD_TEXT, config, noOpCallback());

        assertThat(results).hasSize(5);
        for (int i = 0; i < results.size(); i++) {
            RenameResult result = results.get(i);
            assertThat(result.getStatus())
                    .as("result[%d] should be SUCCESS", i)
                    .isEqualTo(RenameStatus.SUCCESS);
            assertThat(result.getNewFileName())
                    .as("result[%d] should start with batch_", i)
                    .startsWith("batch_");
        }
        for (int i = 1; i <= 5; i++) {
            assertRenamed("file_" + i + ".txt", "batch_file_" + i + ".txt");
        }
    }

    /**
     * Full user flow: all test resources copied with structure preserved, expanded via
     * FolderExpansionService (recursive + includeFolders), renamed with ADD_TEXT, then every
     * result verified on disk. Validates that files in all subdirectories are correctly renamed.
     */
    @Test
    void addText_begin_fullDirectoryTree_allItemsRenamedOnDisk() throws Exception {
        List<File> allItems = copyAndExpandFullTree();
        assertThat(allItems).isNotEmpty();

        AddTextConfig config = AddTextConfig.builder()
                .withTextToAdd("tree_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(allItems, TransformationMode.ADD_TEXT, config, noOpCallback());

        assertThat(results).hasSameSizeAs(allItems);
        assertDiskStateForBatch(results);
    }
}
