package ua.renamer.app.backend.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import ua.renamer.app.api.enums.TextCaseOptions;
import ua.renamer.app.api.model.RenameResult;
import ua.renamer.app.api.model.RenameStatus;
import ua.renamer.app.api.model.TransformationMode;
import ua.renamer.app.api.model.config.CaseChangeConfig;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeCaseIntegrationTest extends BaseRealMetadataIntegrationTest {

    @Test
    @DisabledOnOs({OS.MAC, OS.WINDOWS})
        // case-only rename: old path still resolves on case-insensitive filesystems
    void changeCase_lowercase_uppercaseInput_convertsToLower() throws Exception {
        File file = createPlainFile("UPPER_CASE.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("upper_case.txt");
        assertRenamed("UPPER_CASE.txt", "upper_case.txt");
    }

    @Test
    @DisabledOnOs({OS.MAC, OS.WINDOWS})
        // case-only rename: old path still resolves on case-insensitive filesystems
    void changeCase_uppercase_lowercaseInput_convertsToUpper() throws Exception {
        File file = createPlainFile("lower_case.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.UPPERCASE)
                .withCapitalizeFirstLetter(false)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("LOWER_CASE.txt");
        assertRenamed("lower_case.txt", "LOWER_CASE.txt");
    }

    @Test
    void changeCase_camelCase_snakeInput_convertsToCamel() throws Exception {
        File file = createPlainFile("hello_world_test.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("helloWorldTest.txt");
        assertRenamed("hello_world_test.txt", "helloWorldTest.txt");
    }

    @Test
    void changeCase_pascalCase_snakeInput_convertsToPascal() throws Exception {
        File file = createPlainFile("hello_world_test.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.PASCAL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("HelloWorldTest.txt");
        assertRenamed("hello_world_test.txt", "HelloWorldTest.txt");
    }

    /**
     * Input is already in snake_case — transformer should detect no change is needed and skip.
     * Cross-platform safe: SKIPPED means no rename attempted, so no case-sensitivity issues.
     */
    @Test
    void changeCase_snakeCase_alreadySnakeInput_skipsFile() throws Exception {
        File file = createPlainFile("already_snake.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SKIPPED);
        assertFileExists("already_snake.txt");
    }

    @Test
    @DisabledOnOs({OS.MAC, OS.WINDOWS})
        // case-only rename: old path still resolves on case-insensitive filesystems
    void changeCase_snakeCaseScreaming_lowercaseInput_convertsToUpperSnake() throws Exception {
        File file = createPlainFile("hello_world.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.SNAKE_CASE_SCREAMING)
                .withCapitalizeFirstLetter(false)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("HELLO_WORLD.txt");
        assertRenamed("hello_world.txt", "HELLO_WORLD.txt");
    }

    @Test
    void changeCase_kebabCase_snakeInput_convertsToKebab() throws Exception {
        File file = createPlainFile("hello_world_test.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.KEBAB_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("hello-world-test.txt");
        assertRenamed("hello_world_test.txt", "hello-world-test.txt");
    }

    @Test
    @DisabledOnOs({OS.MAC, OS.WINDOWS})
        // case-only rename: old path still resolves on case-insensitive filesystems
    void changeCase_titleCase_spacedInput_convertsToTitle() throws Exception {
        File file = createPlainFile("hello world test.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.TITLE_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("Hello World Test.txt");
        assertRenamed("hello world test.txt", "Hello World Test.txt");
    }

    @Test
    @DisabledOnOs({OS.MAC, OS.WINDOWS})
        // case-only rename: old path still resolves on case-insensitive filesystems
    void changeCase_lowercase_capitalizeFirstLetter_appliesCapital() throws Exception {
        File file = createPlainFile("HELLO.txt", "content");
        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.LOWERCASE)
                .withCapitalizeFirstLetter(true)
                .build();

        RenameResult result = executeSingle(file, TransformationMode.CHANGE_CASE, config);

        assertThat(result.getStatus()).isEqualTo(RenameStatus.SUCCESS);
        assertThat(result.getNewFileName()).isEqualTo("Hello.txt");
        assertRenamed("HELLO.txt", "Hello.txt");
    }

    /**
     * Full user flow: all test resources copied with structure preserved, expanded via
     * FolderExpansionService (recursive, files only), renamed with CHANGE_CASE CAMEL_CASE.
     *
     * <p>Uses CAMEL_CASE (not UPPERCASE) intentionally: CAMEL_CASE replaces underscores/separators
     * with camelCase letters, producing structurally different names (e.g., level1_a → level1A).
     * This ensures the old path and new path are distinct even on case-insensitive filesystems
     * (macOS APFS, Windows NTFS), avoiding false positives in assertDiskStateForBatch.
     */
    @Test
    void changeCase_camelCase_fullDirectoryTree_allItemsProcessedCorrectly() throws Exception {
        List<File> allItems = copyAndExpandFullTree();
        assertThat(allItems).isNotEmpty();

        CaseChangeConfig config = CaseChangeConfig.builder()
                .withCaseOption(TextCaseOptions.CAMEL_CASE)
                .withCapitalizeFirstLetter(false)
                .build();

        List<RenameResult> results = orchestrator.execute(allItems, TransformationMode.CHANGE_CASE, config, noOpCallback());

        assertThat(results).hasSameSizeAs(allItems);
        assertDiskStateForBatch(results);
    }
}
