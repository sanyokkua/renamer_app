package ua.renamer.app.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.model.FolderDropOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FolderExpansionServiceImpl}.
 *
 * <p>Uses a real temporary file system via {@link TempDir} — no mocks needed.
 */
class FolderExpansionServiceImplTest {

    @TempDir
    Path tempDir;

    private FolderExpansionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FolderExpansionServiceImpl();
    }

    // -------------------------------------------------------------------------
    // Non-recursive (flat)
    // -------------------------------------------------------------------------

    @Test
    void expand_nonRecursive_returnsOnlyDirectChildFiles() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("alpha.txt"));
        Path file2 = Files.createFile(tempDir.resolve("beta.txt"));
        Files.createDirectory(tempDir.resolve("subdir"));

        var options = new FolderDropOptions(FolderDropOptions.Action.USE_CONTENTS, false, false);
        List<Path> result = service.expand(tempDir, options);

        assertThat(result).containsExactlyInAnyOrder(file1, file2);
    }

    @Test
    void expand_nonRecursive_includeFolders_returnsFilesAndSubdirs() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("alpha.txt"));
        Path subdir = Files.createDirectory(tempDir.resolve("subdir"));

        var options = new FolderDropOptions(FolderDropOptions.Action.USE_CONTENTS, false, true);
        List<Path> result = service.expand(tempDir, options);

        assertThat(result).containsExactlyInAnyOrder(file1, subdir);
    }

    // -------------------------------------------------------------------------
    // Recursive
    // -------------------------------------------------------------------------

    @Test
    void expand_recursive_returnsAllDescendantFiles() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("alpha.txt"));
        Path subdir = Files.createDirectory(tempDir.resolve("subdir"));
        Path file2 = Files.createFile(subdir.resolve("beta.txt"));

        var options = new FolderDropOptions(FolderDropOptions.Action.USE_CONTENTS, true, false);
        List<Path> result = service.expand(tempDir, options);

        assertThat(result).containsExactlyInAnyOrder(file1, file2);
    }

    @Test
    void expand_recursive_includeFolders_returnsFilesAndSubdirs() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("alpha.txt"));
        Path subdir = Files.createDirectory(tempDir.resolve("subdir"));
        Path file2 = Files.createFile(subdir.resolve("beta.txt"));

        var options = new FolderDropOptions(FolderDropOptions.Action.USE_CONTENTS, true, true);
        List<Path> result = service.expand(tempDir, options);

        assertThat(result).containsExactlyInAnyOrder(file1, subdir, file2);
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    void expand_emptyFolder_returnsEmptyList() {
        var options = new FolderDropOptions(FolderDropOptions.Action.USE_CONTENTS, false, false);
        List<Path> result = service.expand(tempDir, options);

        assertThat(result).isEmpty();
    }

    @Test
    void expand_hiddenFilesExcluded() throws IOException {
        Files.createFile(tempDir.resolve(".hidden_file"));
        Path visible = Files.createFile(tempDir.resolve("visible.txt"));

        var options = new FolderDropOptions(FolderDropOptions.Action.USE_CONTENTS, false, false);
        List<Path> result = service.expand(tempDir, options);

        assertThat(result).containsExactly(visible);
    }

    @Test
    void expand_rootFolderNotInResult() throws IOException {
        Files.createFile(tempDir.resolve("file.txt"));

        var optionsFlat = new FolderDropOptions(FolderDropOptions.Action.USE_CONTENTS, false, true);
        var optionsRecursive = new FolderDropOptions(FolderDropOptions.Action.USE_CONTENTS, true, true);

        assertThat(service.expand(tempDir, optionsFlat)).doesNotContain(tempDir);
        assertThat(service.expand(tempDir, optionsRecursive)).doesNotContain(tempDir);
    }
}
