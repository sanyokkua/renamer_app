package ua.renamer.app.metadata.extractor.strategy.format;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link MetadataCommons#buildMetadataMap(Metadata)}.
 *
 * <p>Tests construct Metadata from real JPEG test files on the classpath to
 * exercise both the null-value filtering branch and the happy path where tags
 * with non-null descriptions are collected into the map.
 */
class MetadataCommonsTest {

    private static final String JPEG_WITH_META = "test-data/image/jpg/test_jpg_std_2025-12-11_21-00-35.jpg";
    private static final String JPEG_CLEAN = "test-data/image/jpg/test_jpg_clean.jpg";

    @TempDir
    Path tempDir;

    // ============================================================================
    // Happy path — real metadata from JPEG files
    // ============================================================================

    @Test
    void buildMetadataMap_withJpegContainingExif_returnsNonEmptyMap() throws Exception {
        File jpegFile = loadTestFile(JPEG_WITH_META);
        Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

        Map<String, String> result = MetadataCommons.buildMetadataMap(metadata);

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void buildMetadataMap_withJpegContainingExif_keysHaveDirectoryPrefix() throws Exception {
        File jpegFile = loadTestFile(JPEG_WITH_META);
        Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

        Map<String, String> result = MetadataCommons.buildMetadataMap(metadata);

        // All keys must have "." separator (directory.tag format)
        assertThat(result.keySet()).allMatch(key -> key.contains("."),
                "All keys must be in 'DirectoryName.TagName' format");
    }

    @Test
    void buildMetadataMap_withJpegContainingExif_valuesAreNotNull() throws Exception {
        File jpegFile = loadTestFile(JPEG_WITH_META);
        Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

        Map<String, String> result = MetadataCommons.buildMetadataMap(metadata);

        // The method filters null-description tags — all map values must be non-null
        assertThat(result.values()).doesNotContainNull();
    }

    @Test
    void buildMetadataMap_withCleanJpeg_returnsMap() throws Exception {
        File jpegFile = loadTestFile(JPEG_CLEAN);
        Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

        Map<String, String> result = MetadataCommons.buildMetadataMap(metadata);

        assertThat(result).isNotNull();
        // Clean JPEG may have fewer tags but should still have file-type tags
    }

    // ============================================================================
    // Empty Metadata — covers the "no directories" branch
    // ============================================================================

    @Test
    void buildMetadataMap_withEmptyMetadata_returnsEmptyMap() {
        Metadata emptyMetadata = new Metadata();

        Map<String, String> result = MetadataCommons.buildMetadataMap(emptyMetadata);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    // ============================================================================
    // No-throw contract
    // ============================================================================

    @Test
    void buildMetadataMap_withEmptyMetadata_doesNotThrow() {
        Metadata emptyMetadata = new Metadata();

        assertThatCode(() -> MetadataCommons.buildMetadataMap(emptyMetadata))
                .doesNotThrowAnyException();
    }

    @Test
    void buildMetadataMap_withRealMetadata_doesNotThrow() throws Exception {
        File jpegFile = loadTestFile(JPEG_WITH_META);
        Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

        assertThatCode(() -> MetadataCommons.buildMetadataMap(metadata))
                .doesNotThrowAnyException();
    }

    // ============================================================================
    // Helper
    // ============================================================================

    private File loadTestFile(String resourcePath) {
        var url = getClass().getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("Test resource not found: " + resourcePath);
        }
        try {
            return new File(url.toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load test resource: " + resourcePath, e);
        }
    }
}
