package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.metadata.extractor.strategy.format.image.PngFileMetadataExtractor;
import ua.renamer.app.metadata.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for PNG metadata extraction using real test data files.
 * Test data located at: src/test/resources/test-data/image/png/
 */
class PngMetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/image/png/";
    private PngFileMetadataExtractor extractor;

    static Stream<Arguments> providePngTestFiles() {
        return Stream.of(
                arguments("test_png_clean.png", null, false, true),
                arguments("test_png_std_2025-12-11_21-00-35.png",
                        LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true),
                arguments("test_png_past_2000-01-01_12-00-00.png",
                        LocalDateTime.of(2000, 1, 1, 12, 0, 0), true, true),
                arguments("test_png_future_2050-01-01_12-00-00.png",
                        LocalDateTime.of(2050, 1, 1, 12, 0, 0), true, true),
                arguments("test_png_std_no_tz_2025-12-11_21-00-35.png",
                        LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true),
                arguments("test_png_std_tz_2025-12-11_21-00-35p02-00.png",
                        LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true)
        );
    }

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new PngFileMetadataExtractor(dateTimeConverter);
    }

    private File getTestFile(String filename) {
        URL resource = getClass().getClassLoader().getResource(TEST_DATA_PATH + filename);
        assertNotNull(resource, "Test file not found: " + filename);
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException _) {
            fail("Failed to load test file: " + filename);
            return null;
        }
    }

    @Test
    void testExtract_CleanPng() {
        File testFile = getTestFile("test_png_clean.png");

        FileMeta result = extractor.extract(testFile, "image/png");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent(), "Image metadata should be present");

        ImageMeta imageMeta = result.getImageMeta().get();

        assertTrue(imageMeta.getContentCreationDate().isEmpty(),
                "Clean file should not have creation date");

        assertTrue(imageMeta.getWidth().isPresent(), "Width should be present");
        assertTrue(imageMeta.getHeight().isPresent(), "Height should be present");

        assertEquals(320, imageMeta.getWidth().get());
        assertEquals(240, imageMeta.getHeight().get());
    }

    @ParameterizedTest
    @MethodSource("providePngTestFiles")
    void testExtract_VariousScenarios(String filename, LocalDateTime expectedDate,
                                      boolean hasDate, boolean hasDimensions) {
        File testFile = getTestFile(filename);

        FileMeta result = extractor.extract(testFile, "image/png");

        assertNotNull(result, "FileMeta should not be null");
        assertTrue(result.getImageMeta().isPresent(), "Image metadata should be present");

        ImageMeta imageMeta = result.getImageMeta().get();

        if (hasDate) {
            assertTrue(imageMeta.getContentCreationDate().isPresent(),
                    "Creation date should be present for: " + filename);
            assertEquals(expectedDate, imageMeta.getContentCreationDate().get(),
                    "Creation date mismatch for: " + filename);
        } else {
            assertTrue(imageMeta.getContentCreationDate().isEmpty(),
                    "Creation date should not be present for: " + filename);
        }

        if (hasDimensions) {
            assertTrue(imageMeta.getWidth().isPresent(), "Width should be present");
            assertTrue(imageMeta.getHeight().isPresent(), "Height should be present");
            assertEquals(320, imageMeta.getWidth().get());
            assertEquals(240, imageMeta.getHeight().get());
        }
    }

    @Test
    void testExtract_WithGPS() {
        File testFile = getTestFile("test_png_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.png");

        FileMeta result = extractor.extract(testFile, "image/png");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        assertTrue(imageMeta.getContentCreationDate().isPresent());
        assertEquals(LocalDateTime.of(2025, 12, 11, 21, 0, 35),
                imageMeta.getContentCreationDate().get());

        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());
    }

    @Test
    void testExtract_Dimensions() {
        File testFile = getTestFile("test_png_std_2025-12-11_21-00-35.png");

        FileMeta result = extractor.extract(testFile, "image/png");

        ImageMeta imageMeta = result.getImageMeta().get();

        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());

        int width = imageMeta.getWidth().get();
        int height = imageMeta.getHeight().get();

        assertEquals(320, width, "Width should be 320");
        assertEquals(240, height, "Height should be 240");

        double aspectRatio = (double) width / height;
        assertEquals(4.0 / 3.0, aspectRatio, 0.01, "Aspect ratio should be 4:3");
    }

    @Test
    void testExtract_AllFilesExist() {
        String[] testFiles = {
                "test_png_clean.png",
                "test_png_std_2025-12-11_21-00-35.png",
                "test_png_past_2000-01-01_12-00-00.png",
                "test_png_future_2050-01-01_12-00-00.png",
                "test_png_std_no_tz_2025-12-11_21-00-35.png",
                "test_png_std_tz_2025-12-11_21-00-35p02-00.png",
                "test_png_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.png"
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
        File testFile = getTestFile("test_png_std_2025-12-11_21-00-35.png");

        FileMeta result = extractor.extract(testFile, "image/png");

        assertNotNull(result);
        assertTrue(result.getErrors().isEmpty(),
                "Should not have extraction errors");
    }
}
