package ua.renamer.app.core.v2.mapper.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.v2.mapper.strategy.format.image.JpegFileMetadataExtractor;
import ua.renamer.app.core.v2.model.meta.FileMeta;
import ua.renamer.app.core.v2.model.meta.category.ImageMeta;
import ua.renamer.app.core.v2.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for JPEG metadata extraction using real test data files.
 * Test data located at: src/test/resources/test-data/image/jpg/
 */
class JpegMetadataExtractorIntegrationTest {

    private JpegFileMetadataExtractor extractor;
    private static final String TEST_DATA_PATH = "test-data/image/jpg/";

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new JpegFileMetadataExtractor(dateTimeConverter);
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
    // Test Data Providers
    // ============================================================================

    static Stream<Arguments> provideJpegTestFiles() {
        return Stream.of(
                // filename, expectedDate, hasDate, hasDimensions
                arguments("test_jpg_clean.jpg", null, false, true),
                arguments("test_jpg_std_2025-12-11_21-00-35.jpg",
                         LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true),
                arguments("test_jpg_past_2000-01-01_12-00-00.jpg",
                         LocalDateTime.of(2000, 1, 1, 12, 0, 0), true, true),
                arguments("test_jpg_future_2050-01-01_12-00-00.jpg",
                         LocalDateTime.of(2050, 1, 1, 12, 0, 0), true, true),
                arguments("test_jpg_std_no_tz_2025-12-11_21-00-35.jpg",
                         LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true),
                arguments("test_jpg_std_tz_2025-12-11_21-00-35p02-00.jpg",
                         LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true)
        );
    }

    // ============================================================================
    // Basic Extraction Tests
    // ============================================================================

    @Test
    void testExtract_CleanJpeg() {
        File testFile = getTestFile("test_jpg_clean.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent(), "Image metadata should be present");

        ImageMeta imageMeta = result.getImageMeta().get();

        // Clean file should have no creation date
        assertTrue(imageMeta.getContentCreationDate().isEmpty(),
                  "Clean file should not have creation date");

        // But should have dimensions (from the actual image data)
        assertTrue(imageMeta.getWidth().isPresent(), "Width should be present");
        assertTrue(imageMeta.getHeight().isPresent(), "Height should be present");

        // Dimensions should be 320x240 (from test data generator)
        assertEquals(320, imageMeta.getWidth().get());
        assertEquals(240, imageMeta.getHeight().get());
    }

    @ParameterizedTest
    @MethodSource("provideJpegTestFiles")
    void testExtract_VariousScenarios(String filename, LocalDateTime expectedDate,
                                      boolean hasDate, boolean hasDimensions) {
        File testFile = getTestFile(filename);

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result, "FileMeta should not be null");
        assertTrue(result.getImageMeta().isPresent(), "Image metadata should be present");

        ImageMeta imageMeta = result.getImageMeta().get();

        // Check creation date
        if (hasDate) {
            assertTrue(imageMeta.getContentCreationDate().isPresent(),
                      "Creation date should be present for: " + filename);
            assertEquals(expectedDate, imageMeta.getContentCreationDate().get(),
                        "Creation date mismatch for: " + filename);
        } else {
            assertTrue(imageMeta.getContentCreationDate().isEmpty(),
                      "Creation date should not be present for: " + filename);
        }

        // Check dimensions
        if (hasDimensions) {
            assertTrue(imageMeta.getWidth().isPresent(), "Width should be present");
            assertTrue(imageMeta.getHeight().isPresent(), "Height should be present");
            assertEquals(320, imageMeta.getWidth().get());
            assertEquals(240, imageMeta.getHeight().get());
        }
    }

    // ============================================================================
    // GPS Data Tests
    // ============================================================================

    @Test
    void testExtract_WithGPS() {
        File testFile = getTestFile("test_jpg_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should have creation date
        assertTrue(imageMeta.getContentCreationDate().isPresent());
        assertEquals(LocalDateTime.of(2025, 12, 11, 21, 0, 35),
                    imageMeta.getContentCreationDate().get());

        // Should have dimensions
        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());

        // Note: GPS coordinates would be in metaInfo map if we were testing that
        // For now, we just verify the image metadata extracts correctly
    }

    // ============================================================================
    // Date Parsing Tests
    // ============================================================================

    @Test
    void testExtract_StandardDate() {
        File testFile = getTestFile("test_jpg_std_2025-12-11_21-00-35.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        ImageMeta imageMeta = result.getImageMeta().get();
        LocalDateTime creationDate = imageMeta.getContentCreationDate().get();

        assertEquals(2025, creationDate.getYear());
        assertEquals(12, creationDate.getMonthValue());
        assertEquals(11, creationDate.getDayOfMonth());
        assertEquals(21, creationDate.getHour());
        assertEquals(0, creationDate.getMinute());
        assertEquals(35, creationDate.getSecond());
    }

    @Test
    void testExtract_PastDate() {
        File testFile = getTestFile("test_jpg_past_2000-01-01_12-00-00.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        ImageMeta imageMeta = result.getImageMeta().get();
        LocalDateTime creationDate = imageMeta.getContentCreationDate().get();

        assertEquals(2000, creationDate.getYear());
        assertEquals(1, creationDate.getMonthValue());
        assertEquals(1, creationDate.getDayOfMonth());
    }

    @Test
    void testExtract_FutureDate() {
        File testFile = getTestFile("test_jpg_future_2050-01-01_12-00-00.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        ImageMeta imageMeta = result.getImageMeta().get();
        LocalDateTime creationDate = imageMeta.getContentCreationDate().get();

        assertEquals(2050, creationDate.getYear());
        assertEquals(1, creationDate.getMonthValue());
        assertEquals(1, creationDate.getDayOfMonth());
    }

    @Test
    void testExtract_WithTimezone() {
        File testFile = getTestFile("test_jpg_std_tz_2025-12-11_21-00-35p02-00.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should extract the datetime correctly (timezone info handled by parser)
        assertTrue(imageMeta.getContentCreationDate().isPresent());
        LocalDateTime creationDate = imageMeta.getContentCreationDate().get();

        assertEquals(2025, creationDate.getYear());
        assertEquals(12, creationDate.getMonthValue());
        assertEquals(11, creationDate.getDayOfMonth());
    }

    // ============================================================================
    // Dimensions Tests
    // ============================================================================

    @Test
    void testExtract_Dimensions() {
        File testFile = getTestFile("test_jpg_std_2025-12-11_21-00-35.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        ImageMeta imageMeta = result.getImageMeta().get();

        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());

        int width = imageMeta.getWidth().get();
        int height = imageMeta.getHeight().get();

        // Test data generated with 320x240 dimensions
        assertEquals(320, width, "Width should be 320");
        assertEquals(240, height, "Height should be 240");

        // Verify aspect ratio
        double aspectRatio = (double) width / height;
        assertEquals(4.0/3.0, aspectRatio, 0.01, "Aspect ratio should be 4:3");
    }

    // ============================================================================
    // Error Handling Tests
    // ============================================================================

    @Test
    void testExtract_AllFilesExist() {
        // Verify all test files can be loaded
        String[] testFiles = {
            "test_jpg_clean.jpg",
            "test_jpg_std_2025-12-11_21-00-35.jpg",
            "test_jpg_past_2000-01-01_12-00-00.jpg",
            "test_jpg_future_2050-01-01_12-00-00.jpg",
            "test_jpg_std_no_tz_2025-12-11_21-00-35.jpg",
            "test_jpg_std_tz_2025-12-11_21-00-35p02-00.jpg",
            "test_jpg_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.jpg"
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
        File testFile = getTestFile("test_jpg_std_2025-12-11_21-00-35.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result);
        assertTrue(result.getErrors().isEmpty(),
                  "Should not have extraction errors");
    }
}
