package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.ItemPositionExtended;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.ReplaceTextConfig;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplaceTextIntegrationTest extends BaseRealMetadataIntegrationTest {

    @Test
    void replaceText_everywhere_replacesAllOccurrences() throws Exception {
        File file = createPlainFile("old_name_old.txt", "content");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace("old")
                .withReplacementText("new")
                .withPosition(ItemPositionExtended.EVERYWHERE)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REPLACE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("new_name_new.txt");
        assertRenamed("old_name_old.txt", "new_name_new.txt");
    }

    @Test
    void replaceText_begin_replacesOnlyFirstOccurrence() throws Exception {
        File file = createPlainFile("temp_temp_file.txt", "content");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace("temp")
                .withReplacementText("final")
                .withPosition(ItemPositionExtended.BEGIN)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REPLACE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("final_temp_file.txt");
        assertRenamed("temp_temp_file.txt", "final_temp_file.txt");
    }

    @Test
    void replaceText_end_replacesOnlyLastOccurrence() throws Exception {
        File file = createPlainFile("file_temp_temp.txt", "content");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace("temp")
                .withReplacementText("final")
                .withPosition(ItemPositionExtended.END)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REPLACE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("file_temp_final.txt");
        assertRenamed("file_temp_temp.txt", "file_temp_final.txt");
    }

    @Test
    void replaceText_everywhere_emptyReplacement_deletesMatches() throws Exception {
        File file = createPlainFile("hello_world.txt", "content");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace("_world")
                .withReplacementText("")
                .withPosition(ItemPositionExtended.EVERYWHERE)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REPLACE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("hello.txt");
        assertRenamed("hello_world.txt", "hello.txt");
    }

    @Test
    void replaceText_everywhere_textNotFound_skipsFile() throws Exception {
        File file = createPlainFile("document.txt", "content");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace("MISSING")
                .withReplacementText("x")
                .withPosition(ItemPositionExtended.EVERYWHERE)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REPLACE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("document.txt");
    }

    /**
     * Verifies that the transformer treats the search string as a literal, not a regex.
     * ".v2" must match the literal dot-v-2 sequence, not "any-char followed by v2".
     * Input stem: "file.v2.0" → replace ".v2" with "_v3" → "file_v3.0"
     */
    @Test
    void replaceText_everywhere_specialRegexChars_treatedAsLiteral() throws Exception {
        File file = createPlainFile("file.v2.0.txt", "content");
        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace(".v2")
                .withReplacementText("_v3")
                .withPosition(ItemPositionExtended.EVERYWHERE)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.REPLACE_TEXT, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("file_v3.0.txt");
        assertRenamed("file.v2.0.txt", "file_v3.0.txt");
    }

    /**
     * Full user flow: all test resources copied with structure preserved, expanded via
     * FolderExpansionService (recursive, files only), renamed with REPLACE_TEXT EVERYWHERE "a"→"@".
     * Many filenames contain "a" and will be renamed; those without are skipped.
     * Validates that assertDiskStateForBatch handles mixed results across all subdirectories.
     */
    @Test
    void replaceText_everywhere_fullDirectoryTree_allItemsProcessedCorrectly() throws Exception {
        List<File> allItems = copyAndExpandFullTree();
        assertThat(allItems).isNotEmpty();

        ReplaceTextConfig config = ReplaceTextConfig.builder()
                .withTextToReplace("a")
                .withReplacementText("@")
                .withPosition(ItemPositionExtended.EVERYWHERE)
                .build();

        List<RenameResult> results = orchestrator.execute(allItems, TransformationMode.REPLACE_TEXT, config, noOpCallback());

        assertThat(results).hasSameSizeAs(allItems);
        assertDiskStateForBatch(results);
    }
}
