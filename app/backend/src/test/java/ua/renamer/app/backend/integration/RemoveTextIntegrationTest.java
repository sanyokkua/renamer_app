package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPosition;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.RemoveTextConfig;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RemoveTextIntegrationTest extends BaseRealMetadataIntegrationTest {

    @Test
    void removeText_begin_matchingPrefix_removesPrefix() throws Exception {
        File file = createPlainFile("OLD_document.txt", "content");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("OLD_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REMOVE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("document.txt");
        assertRenamed("OLD_document.txt", "document.txt");
    }

    @Test
    void removeText_end_matchingSuffix_removesSuffix() throws Exception {
        File file = createPlainFile("report_DRAFT.md", "content");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("_DRAFT")
                .withPosition(ItemPosition.END)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REMOVE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("report.md");
        assertRenamed("report_DRAFT.md", "report.md");
    }

    @Test
    void removeText_begin_textNotFound_skipsFile() throws Exception {
        File file = createPlainFile("NOTHERE_file.txt", "content");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("MISSING_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REMOVE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("NOTHERE_file.txt");
    }

    @Test
    void removeText_begin_duplicateOccurrence_removesOnlyFirst() throws Exception {
        File file = createPlainFile("OLD_OLD_file.txt", "content");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("OLD_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REMOVE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("OLD_file.txt");
        assertRenamed("OLD_OLD_file.txt", "OLD_file.txt");
    }

    /**
     * Removing the entire stem produces an extension-only filename (".txt").
     * This is a valid rename — ".txt" is a legitimate filename on Unix/macOS.
     * The pipeline returns SUCCESS and physically renames the file.
     */
    @Test
    void removeText_begin_entireStemRemoved_producesExtensionOnlyName() throws Exception {
        File file = createPlainFile("ONLY_.txt", "content");
        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("ONLY_")
                .withPosition(ItemPosition.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REMOVE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo(".txt");
        assertRenamed("ONLY_.txt", ".txt");
    }

    /**
     * Full user flow: all test resources copied with structure preserved, expanded via
     * FolderExpansionService (recursive, files only), renamed with REMOVE_TEXT BEGIN "level".
     * Files whose names start with "level" are renamed; all others are skipped.
     * Validates that assertDiskStateForBatch handles mixed SUCCESS + SKIPPED across all subdirectories.
     */
    @Test
    void removeText_begin_fullDirectoryTree_allItemsProcessedCorrectly() throws Exception {
        List<File> allItems = copyAndExpandFullTree();
        assertThat(allItems).isNotEmpty();

        RemoveTextConfig config = RemoveTextConfig.builder()
                .withTextToRemove("level")
                .withPosition(ItemPosition.BEGIN)
                .build();

        List<RenameResult> results = orchestrator.execute(allItems, TransformationMode.REMOVE_TEXT, config, noOpCallback());

        assertThat(results).hasSameSizeAs(allItems);
        assertDiskStateForBatch(results);
    }
}
