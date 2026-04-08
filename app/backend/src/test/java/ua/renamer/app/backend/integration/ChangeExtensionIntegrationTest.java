package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.ExtensionChangeConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeExtensionIntegrationTest extends BaseRealMetadataIntegrationTest {

    @Test
    void changeExtension_toMd_changesExtension() throws Exception {
        File file = copyTestResource("flat/document.txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension("md")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_EXTENSION, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("document.md");
        assertRenamed("document.txt", "document.md");
    }

    @Test
    void changeExtension_jpegToPng_changesExtension() throws Exception {
        File file = copyTestResource("media/photo_1920x1080.jpg");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension("png")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_EXTENSION, config);

        // Bytes remain JPEG — valid test of renaming, not file conversion
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("photo_1920x1080.png");
        assertRenamed("photo_1920x1080.jpg", "photo_1920x1080.png");
    }

    @Test
    void changeExtension_fileWithNoExtension_addsExtension() throws Exception {
        File file = copyTestResource("flat/no_extension");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension("txt")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_EXTENSION, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("no_extension.txt");
        assertRenamed("no_extension", "no_extension.txt");
    }

    @Test
    void changeExtension_emptyString_configValidationThrows() {
        // ExtensionChangeConfigBuilder.build() rejects blank extensions
        assertThatThrownBy(() -> ExtensionChangeConfig.builder()
                .withNewExtension("")
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeExtension_sameExtension_skipped() throws Exception {
        File file = copyTestResource("flat/document.txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension("txt")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_EXTENSION, config);

        // Old and new full names are identical → orchestrator returns SKIPPED
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("document.txt");
    }

    @Test
    void changeExtension_batchOf5Csv_allSucceed() throws Exception {
        List<File> csvFiles = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            csvFiles.add(createPlainFile("data_" + i + ".csv", "col1,col2"));
        }

        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension("json")
                .build();

        List<RenameResult> results = orchestrator.execute(csvFiles, TransformationMode.CHANGE_EXTENSION, config,
                noOpCallback());

        assertThat(results).hasSize(5);
        for (int i = 1; i <= 5; i++) {
            assertThat(results.get(i - 1).getStatus()).isEqualTo(RenameStatus.SUCCESS);
            assertRenamed("data_" + i + ".csv", "data_" + i + ".json");
        }
    }

    @Test
    void changeExtension_uppercaseExtension_casePreservedInOutputName() throws Exception {
        File file = copyTestResource("flat/document.txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension("TXT")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_EXTENSION, config);

        // The transformer must preserve case in the output filename string — no lowercasing.
        // Disk assertion is intentionally omitted: on case-insensitive filesystems (macOS APFS,
        // Windows NTFS) renaming .txt → .TXT is a no-op at the OS level, so assertRenamed would
        // fail even though the transformer behaved correctly.
        assertThat(result.getNewFileName()).isEqualTo("document.TXT");
        assertThat(result.getStatus()).isNotIn(
                RenameStatus.ERROR_EXTRACTION, RenameStatus.ERROR_TRANSFORMATION, RenameStatus.ERROR_EXECUTION);
    }

    @Test
    void changeExtension_withLeadingDot_normalizedCorrectly() throws Exception {
        File file = copyTestResource("flat/document.txt");
        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension(".md")
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_EXTENSION, config);

        // Transformer strips leading dot: ".md" → "md" → result is "document.md", not "document..md"
        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("document.md");
        assertRenamed("document.txt", "document.md");
    }

    @Test
    void changeExtension_fullTree_bakExtension_allFilesChanged() throws Exception {
        List<File> allFiles = copyAndExpandFullTree();
        assertThat(allFiles).isNotEmpty();

        ExtensionChangeConfig config = ExtensionChangeConfig.builder()
                .withNewExtension("bak")
                .build();

        List<RenameResult> results = orchestrator.execute(allFiles, TransformationMode.CHANGE_EXTENSION, config,
                noOpCallback());

        assertThat(results).hasSameSizeAs(allFiles);
        // None of the test files already have .bak → all should be SUCCESS
        assertDiskStateForBatch(results);
    }
}
