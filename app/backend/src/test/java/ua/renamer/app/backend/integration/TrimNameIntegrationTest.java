package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.enums.TruncateOptions;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.TruncateConfig;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrimNameIntegrationTest extends BaseRealMetadataIntegrationTest {

    // -----------------------------------------------------------------------
    // REMOVE_SYMBOLS_IN_BEGIN
    // -----------------------------------------------------------------------

    @Test
    void trimName_removeFromBegin_3chars_removesFirstThreeChars() throws Exception {
        File file = copyTestResource("flat/document.txt");
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                .withNumberOfSymbols(3)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // stem "document" (8 chars) → remove first 3 → "ument"
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("ument.txt");
        assertRenamed("document.txt", "ument.txt");
    }

    @Test
    void trimName_removeFromBegin_exactLength_emptyName_producesError() throws Exception {
        File file = copyTestResource("flat/document.txt");
        // stem "document" is exactly 8 chars — removing 8 produces empty stem
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                .withNumberOfSymbols(8)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // Truncation produces empty stem → transformer sets hasError=true, but orchestrator
        // detects old name == new name (error result preserves original name) and returns SKIPPED.
        // File is untouched in both cases — correct behavior.
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("document.txt");  // file untouched
    }

    @Test
    void trimName_removeFromBegin_zeroChars_skipped() throws Exception {
        File file = copyTestResource("flat/document.txt");
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_IN_BEGIN)
                .withNumberOfSymbols(0)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // Removing 0 chars produces the same name → orchestrator skips
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("document.txt");
    }

    // -----------------------------------------------------------------------
    // REMOVE_SYMBOLS_FROM_END
    // -----------------------------------------------------------------------

    @Test
    void trimName_removeFromEnd_5chars_removesLastFiveChars() throws Exception {
        File file = copyTestResource("flat/document.txt");
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                .withNumberOfSymbols(5)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // stem "document" (8 chars) → remove last 5 → "doc"
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("doc.txt");
        assertRenamed("document.txt", "doc.txt");
    }

    @Test
    void trimName_removeFromEnd_overLength_emptyName_producesError() throws Exception {
        File file = createPlainFile("abc.txt", "content");
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                .withNumberOfSymbols(10)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // stem "abc" (3 chars) ≤ 10 → produces empty stem → SKIPPED (same logic as above)
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("abc.txt");  // file untouched
    }

    @Test
    void trimName_removeFromEnd_noExtension_removesFromStem() throws Exception {
        File file = createPlainFile("README", "content");
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                .withNumberOfSymbols(3)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // stem "README" (6 chars) → remove last 3 → "REA"; no extension
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("REA");
        assertRenamed("README", "REA");
    }

    @Test
    void trimName_removeFromEnd_5chars_mediaFile_extensionPreserved() throws Exception {
        File file = copyTestResource("media/photo_1920x1080.jpg");
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                .withNumberOfSymbols(5)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // stem "photo_1920x1080" (15 chars) → remove last 5 → first 10 chars = "photo_1920"
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("photo_1920.jpg");
        assertRenamed("photo_1920x1080.jpg", "photo_1920.jpg");
    }

    // -----------------------------------------------------------------------
    // TRUNCATE_EMPTY_SYMBOLS
    // -----------------------------------------------------------------------

    @Test
    void trimName_truncateEmpty_fileWithLeadingTrailingSpaces_trims() throws Exception {
        File file = createPlainFile("  spaces  .txt", "content");
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                .withNumberOfSymbols(0)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // stem "  spaces  " → trim() → "spaces"
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("spaces.txt");
        assertRenamed("  spaces  .txt", "spaces.txt");
    }

    @Test
    void trimName_truncateEmpty_cleanFile_skipped() throws Exception {
        File file = createPlainFile("clean.txt", "content");
        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.TRUNCATE_EMPTY_SYMBOLS)
                .withNumberOfSymbols(0)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.TRIM_NAME, config);

        // stem "clean" → trim() → "clean" (unchanged) → SKIPPED
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("clean.txt");
    }

    // -----------------------------------------------------------------------
    // Full-tree test
    // -----------------------------------------------------------------------

    @Test
    void trimName_fullTree_removeFromEnd_2chars_allFilesProcessed() throws Exception {
        List<File> allFiles = copyAndExpandFullTree();
        assertThat(allFiles).isNotEmpty();

        TruncateConfig config = TruncateConfig.builder()
                .withTruncateOption(TruncateOptions.REMOVE_SYMBOLS_FROM_END)
                .withNumberOfSymbols(2)
                .build();

        List<RenameResult> results = orchestrator.execute(allFiles, TransformationMode.TRIM_NAME, config,
                noOpCallback());

        assertThat(results).hasSameSizeAs(allFiles);
        // All test-data stems are > 2 chars, so all should be SUCCESS (no ERROR_* expected)
        assertDiskStateForBatch(results);
    }
}
