package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.metadata.extractor.strategy.format.image.GifFileMetadataExtractor;
import ua.renamer.app.metadata.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for GIF metadata extraction using real test data files.
 * Test data located at: src/test/resources/test-data/image/gif/
 * <p>
 * Note: GIF format has limited metadata support:
 * - Dimensions are extracted from GIF header (always present)
 * - NO standard EXIF datetime support
 * - NO GPS metadata support
 */
class GifMetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/image/gif/";
    private GifFileMetadataExtractor extractor;

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new GifFileMetadataExtractor(dateTimeConverter);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private File getTestFile(String filename) {
        URL resource = getClass().getClassLoader().getResource(TEST_DATA_PATH + filename);
        assertNotNull(resource, "Test file not found: " + filename);
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            fail("Failed to load test file: " + filename);
            return null;
        }
    }

    // ============================================================================
    // Basic Extraction Tests
    // ============================================================================

    @Test
    void testExtract_CleanGif() {
        File testFile = getTestFile("test_gif_clean.gif");

        FileMeta result = extractor.extract(testFile, "image/gif");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent(), "Image metadata should be present");

        ImageMeta imageMeta = result.getImageMeta().get();

        // GIF should have dimensions from header
        assertTrue(imageMeta.getWidth().isPresent(), "Width should be present");
        assertTrue(imageMeta.getHeight().isPresent(), "Height should be present");
        assertEquals(800, imageMeta.getWidth().get());
        assertEquals(600, imageMeta.getHeight().get());

        // GIF format does not support EXIF datetime metadata
        assertFalse(imageMeta.getContentCreationDate().isPresent(),
                "GIF format does not support datetime metadata");
    }

    @Test
    void testExtract_Dimensions_1920x1080() {
        File testFile = getTestFile("test_gif_dimensions_1920x1080.gif");

        FileMeta result = extractor.extract(testFile, "image/gif");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());
        assertEquals(1920, imageMeta.getWidth().get());
        assertEquals(1080, imageMeta.getHeight().get());

        // Verify aspect ratio
        double aspectRatio = (double) imageMeta.getWidth().get() / imageMeta.getHeight().get();
        assertEquals(16.0 / 9.0, aspectRatio, 0.01, "Aspect ratio should be 16:9");
    }

    @Test
    void testExtract_AnimatedGif() {
        File testFile = getTestFile("test_gif_animated_640x480.gif");

        FileMeta result = extractor.extract(testFile, "image/gif");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Animated GIFs should still extract dimensions correctly
        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());
        assertEquals(640, imageMeta.getWidth().get());
        assertEquals(480, imageMeta.getHeight().get());

        // Verify aspect ratio
        double aspectRatio = (double) imageMeta.getWidth().get() / imageMeta.getHeight().get();
        assertEquals(4.0 / 3.0, aspectRatio, 0.01, "Aspect ratio should be 4:3");

        // Should not have datetime
        assertFalse(imageMeta.getContentCreationDate().isPresent(),
                "Animated GIF should not have datetime metadata");
    }

    // ============================================================================
    // Error Handling Tests
    // ============================================================================

    @Test
    void testExtract_AllFilesExist() {
        // Verify all test files can be loaded
        String[] testFiles = {
                "test_gif_clean.gif",
                "test_gif_dimensions_1920x1080.gif",
                "test_gif_animated_640x480.gif"
        };

        for (String filename : testFiles) {
            File testFile = getTestFile(filename);
            assertTrue(testFile.exists(), "Test file should exist: " + filename);
            assertTrue(testFile.isFile(), "Should be a file: " + filename);
            assertTrue(testFile.canRead(), "Should be readable: " + filename);
        }
    }

    @Test
    void testExtract_NoErrors() {
        // All test files should extract without errors
        File testFile = getTestFile("test_gif_clean.gif");

        FileMeta result = extractor.extract(testFile, "image/gif");

        assertNotNull(result);
        assertTrue(result.getErrors().isEmpty(),
                "Should not have extraction errors");
    }
}
