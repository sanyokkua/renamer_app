package ua.renamer.app.metadata.extractor.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link GenericFileMetadataExtractor}.
 *
 * <p>This extractor is the fallback for file categories that have no dedicated
 * metadata extraction strategy. It always returns {@link FileMeta#empty()}.
 */
class GenericFileMetadataExtractorTest {

    @TempDir
    Path tempDir;

    private GenericFileMetadataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new GenericFileMetadataExtractor();
    }

    // ============================================================================
    // Happy path
    // ============================================================================

    @Test
    void extract_withExistingFile_returnsEmptyFileMeta() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.txt"));

        FileMeta result = extractor.extract(file.toFile(), "text/plain");

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(FileMeta.empty());
    }

    @Test
    void extract_withNullMimeType_returnsEmptyFileMeta() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.bin"));

        FileMeta result = extractor.extract(file.toFile(), null);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(FileMeta.empty());
    }

    @Test
    void extract_withEmptyMimeType_returnsEmptyFileMeta() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.bin"));

        FileMeta result = extractor.extract(file.toFile(), "");

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(FileMeta.empty());
    }

    @Test
    void extract_returnsFileMetaWithNoImageMeta() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.txt"));

        FileMeta result = extractor.extract(file.toFile(), "text/plain");

        assertThat(result.getImageMeta()).isEmpty();
    }

    @Test
    void extract_returnsFileMetaWithNoAudioMeta() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.txt"));

        FileMeta result = extractor.extract(file.toFile(), "text/plain");

        assertThat(result.getAudioMeta()).isEmpty();
    }

    @Test
    void extract_returnsFileMetaWithNoVideoMeta() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.txt"));

        FileMeta result = extractor.extract(file.toFile(), "text/plain");

        assertThat(result.getVideoMeta()).isEmpty();
    }

    @Test
    void extract_returnsFileMetaWithNoErrors() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.txt"));

        FileMeta result = extractor.extract(file.toFile(), "text/plain");

        assertThat(result.getErrors()).isEmpty();
    }

    // ============================================================================
    // No-throw contract
    // ============================================================================

    @Test
    void extract_withNonExistentFile_doesNotThrow() {
        File nonExistent = tempDir.resolve("does_not_exist.txt").toFile();

        assertThatCode(() -> extractor.extract(nonExistent, "text/plain"))
                .doesNotThrowAnyException();
    }

    @Test
    void extract_withNonExistentFile_returnsEmptyFileMeta() {
        File nonExistent = tempDir.resolve("does_not_exist.txt").toFile();

        FileMeta result = extractor.extract(nonExistent, "text/plain");

        assertThat(result).isEqualTo(FileMeta.empty());
    }

    @Test
    void extract_calledMultipleTimes_alwaysReturnsSameEmpty() throws IOException {
        Path file = Files.createFile(tempDir.resolve("test.txt"));
        File f = file.toFile();

        FileMeta first = extractor.extract(f, "text/plain");
        FileMeta second = extractor.extract(f, "application/octet-stream");
        FileMeta third = extractor.extract(f, "video/mp4");

        assertThat(first).isEqualTo(FileMeta.empty());
        assertThat(second).isEqualTo(FileMeta.empty());
        assertThat(third).isEqualTo(FileMeta.empty());
    }
}
