package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.renamer.app.metadata.extractor.strategy.format.image.TiffFileMetadataExtractor;
import ua.renamer.app.metadata.model.meta.FileMeta;
import ua.renamer.app.metadata.model.meta.category.ImageMeta;
import ua.renamer.app.metadata.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TIFF metadata extraction using real test data files.
 * Test data located at: src/test/resources/test-data/image/tiff/
 * <p>
 * TIFF is a professional format with full EXIF support including:
 * - Dimensions from TIFF/EXIF directories
 * - DateTime from EXIF (DateTimeOriginal, DateTime, DateTimeDigitized)
 * - GPS coordinates
 * - Comprehensive metadata support
 */
class TiffMetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/image/tiff/";
    private TiffFileMetadataExtractor extractor;

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new TiffFileMetadataExtractor(dateTimeConverter);
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
    void testExtract_CleanTiff() {
        File testFile = getTestFile("test_tiff_clean.tiff");

        FileMeta result = extractor.extract(testFile, "image/tiff");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent(), "Image metadata should be present");

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should have dimensions
        assertTrue(imageMeta.getWidth().isPresent(), "Width should be present");
        assertTrue(imageMeta.getHeight().isPresent(), "Height should be present");
        assertEquals(1920, imageMeta.getWidth().get());
        assertEquals(1080, imageMeta.getHeight().get());

        // Clean file should not have creation date
        assertFalse(imageMeta.getContentCreationDate().isPresent(),
                    "Clean file should not have creation date");
    }

    @Test
    void testExtract_WithDateTime() {
        File testFile = getTestFile("test_tiff_std_2025-12-11_21-00-35.tiff");

        FileMeta result = extractor.extract(testFile, "image/tiff");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should have dimensions
        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());

        // Should have creation date
        assertTrue(imageMeta.getContentCreationDate().isPresent(),
                   "TIFF with EXIF should have creation date");

        LocalDateTime dateTime = imageMeta.getContentCreationDate().get();
        assertEquals(2025, dateTime.getYear());
        assertEquals(12, dateTime.getMonthValue());
        assertEquals(11, dateTime.getDayOfMonth());
        assertEquals(21, dateTime.getHour());
        assertEquals(0, dateTime.getMinute());
        assertEquals(35, dateTime.getSecond());
    }

    @Test
    void testExtract_WithGpsCoordinates() {
        File testFile = getTestFile("test_tiff_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.tiff");

        FileMeta result = extractor.extract(testFile, "image/tiff");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should have dimensions
        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());

        // Should have creation date
        assertTrue(imageMeta.getContentCreationDate().isPresent());
        LocalDateTime dateTime = imageMeta.getContentCreationDate().get();
        assertEquals(2025, dateTime.getYear());
        assertEquals(12, dateTime.getMonthValue());
        assertEquals(11, dateTime.getDayOfMonth());

        // Verify GPS coordinates are in metaInfo map
        assertNotNull(result.getMetaInfo(), "MetaInfo map should not be null");
        assertFalse(result.getMetaInfo().isEmpty(), "MetaInfo map should contain GPS data");

        // GPS coordinates should be present in the metadata map
        assertTrue(result.getMetaInfo().keySet().stream().anyMatch(key -> key.contains("GPS")),
                   "Should have GPS-related metadata keys");

        // Verify GPS Latitude is present and contains expected values (48 degrees 51 minutes)
        String gpsLatKey = result.getMetaInfo().keySet().stream()
                                 .filter(key -> key.toLowerCase().contains("gps") && key.toLowerCase().contains("latitude"))
                                 .filter(key -> !key.toLowerCase().contains("ref"))
                                 .findFirst()
                                 .orElse(null);
        assertNotNull(gpsLatKey, "GPS Latitude key should be present");
        String gpsLatValue = result.getMetaInfo().get(gpsLatKey);
        assertNotNull(gpsLatValue, "GPS Latitude value should not be null");
        assertTrue(gpsLatValue.contains("48") && gpsLatValue.contains("51"),
                   "GPS Latitude should contain degrees (48) and minutes (51): " + gpsLatValue);

        // Verify GPS Longitude is present and contains expected values (2 degrees 21 minutes)
        String gpsLonKey = result.getMetaInfo().keySet().stream()
                                 .filter(key -> key.toLowerCase().contains("gps") && key.toLowerCase().contains("longitude"))
                                 .filter(key -> !key.toLowerCase().contains("ref"))
                                 .findFirst()
                                 .orElse(null);
        assertNotNull(gpsLonKey, "GPS Longitude key should be present");
        String gpsLonValue = result.getMetaInfo().get(gpsLonKey);
        assertNotNull(gpsLonValue, "GPS Longitude value should not be null");
        assertTrue(gpsLonValue.contains("2") && gpsLonValue.contains("21"),
                   "GPS Longitude should contain degrees (2) and minutes (21): " + gpsLonValue);
    }

    @Test
    void testExtract_Dimensions() {
        File testFile = getTestFile("test_tiff_dimensions_800x600.tiff");

        FileMeta result = extractor.extract(testFile, "image/tiff");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());
        assertEquals(800, imageMeta.getWidth().get());
        assertEquals(600, imageMeta.getHeight().get());

        // Verify aspect ratio
        double aspectRatio = (double) imageMeta.getWidth().get() / imageMeta.getHeight().get();
        assertEquals(4.0 / 3.0, aspectRatio, 0.01, "Aspect ratio should be 4:3");
    }

    // ============================================================================
    // Error Handling Tests
    // ============================================================================

    @Test
    void testExtract_AllFilesExist() {
        // Verify all test files can be loaded
        String[] testFiles = {
                "test_tiff_clean.tiff",
                "test_tiff_std_2025-12-11_21-00-35.tiff",
                "test_tiff_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.tiff",
                "test_tiff_dimensions_800x600.tiff"
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
        File testFile = getTestFile("test_tiff_std_2025-12-11_21-00-35.tiff");

        FileMeta result = extractor.extract(testFile, "image/tiff");

        assertNotNull(result);
        assertTrue(result.getErrors().isEmpty(),
                   "Should not have extraction errors");
    }
}
