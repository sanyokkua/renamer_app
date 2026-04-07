package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.metadata.extractor.strategy.format.image.JpegFileMetadataExtractor;
import ua.renamer.app.metadata.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for JPEG metadata extraction using real test data files.
 * Test data located at: src/test/resources/test-data/image/jpg/
 */
class JpegMetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/image/jpg/";
    private JpegFileMetadataExtractor extractor;

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
    // Helper Methods
    // ============================================================================

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new JpegFileMetadataExtractor(dateTimeConverter);
    }

    // ============================================================================
    // Test Data Providers
    // ============================================================================

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

        // Verify GPS coordinates are in metaInfo map
        assertNotNull(result.getMetaInfo(), "MetaInfo map should not be null");
        assertFalse(result.getMetaInfo().isEmpty(), "MetaInfo map should contain GPS data");

        // GPS coordinates should be present in the metadata map
        // The metadata-extractor library stores GPS data in the GPS directory
        assertTrue(result.getMetaInfo().keySet().stream().anyMatch(key -> key.contains("GPS")),
                "Should have GPS-related metadata keys");

        // Verify GPS Latitude is present and contains expected values
        String gpsLatKey = result.getMetaInfo().keySet().stream()
                .filter(key -> key.toLowerCase().contains("gps") && key.toLowerCase().contains("latitude"))
                .filter(key -> !key.toLowerCase().contains("ref")) // Exclude LatitudeRef
                .findFirst()
                .orElse(null);
        assertNotNull(gpsLatKey, "GPS Latitude key should be present");
        String gpsLatValue = result.getMetaInfo().get(gpsLatKey);
        assertNotNull(gpsLatValue, "GPS Latitude value should not be null");
        assertTrue(gpsLatValue.contains("48") && gpsLatValue.contains("51"),
                "GPS Latitude should contain degrees (48) and minutes (51): " + gpsLatValue);

        // Verify GPS Longitude is present and contains expected values
        String gpsLonKey = result.getMetaInfo().keySet().stream()
                .filter(key -> key.toLowerCase().contains("gps") && key.toLowerCase().contains("longitude"))
                .filter(key -> !key.toLowerCase().contains("ref")) // Exclude LongitudeRef
                .findFirst()
                .orElse(null);
        assertNotNull(gpsLonKey, "GPS Longitude key should be present");
        String gpsLonValue = result.getMetaInfo().get(gpsLonKey);
        assertNotNull(gpsLonValue, "GPS Longitude value should not be null");
        assertTrue(gpsLonValue.contains("2") && gpsLonValue.contains("21"),
                "GPS Longitude should contain degrees (2) and minutes (21): " + gpsLonValue);
    }

    @Test
    void testExtract_WithoutGPS() {
        // Use a clean file that has no GPS data
        File testFile = getTestFile("test_jpg_std_2025-12-11_21-00-35.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should have creation date and dimensions
        assertTrue(imageMeta.getContentCreationDate().isPresent());
        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());

        // Verify GPS coordinates are NOT present
        if (result.getMetaInfo() != null && !result.getMetaInfo().isEmpty()) {
            // If metaInfo exists, it should not contain GPS latitude/longitude
            boolean hasGpsLat = result.getMetaInfo().keySet().stream()
                    .anyMatch(key -> key.toLowerCase().contains("gps") &&
                            key.toLowerCase().contains("latitude") &&
                            !key.toLowerCase().contains("ref"));
            boolean hasGpsLon = result.getMetaInfo().keySet().stream()
                    .anyMatch(key -> key.toLowerCase().contains("gps") &&
                            key.toLowerCase().contains("longitude") &&
                            !key.toLowerCase().contains("ref"));
            assertFalse(hasGpsLat, "Should not have GPS Latitude for non-GPS file");
            assertFalse(hasGpsLon, "Should not have GPS Longitude for non-GPS file");
        }
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
        assertEquals(4.0 / 3.0, aspectRatio, 0.01, "Aspect ratio should be 4:3");
    }

    // ============================================================================
    // Edge Case Tests
    // ============================================================================

    @Test
    void testExtract_ConflictingDates() {
        // Test file has three different dates in EXIF:
        // DateTimeOriginal: 2020-01-01 10:00:00 (should be selected - earliest)
        // CreateDate: 2021-06-15 14:30:00
        // ModifyDate: 2023-12-25 18:45:00
        File testFile = getTestFile("test_jpg_conflicting_dates.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should extract the earliest date (DateTimeOriginal: 2020-01-01)
        // This verifies the priority logic in BaseImageMetadataExtractor.findMinOrNull()
        assertTrue(imageMeta.getContentCreationDate().isPresent(),
                "Should extract datetime when multiple dates present");

        java.time.LocalDateTime dateTime = imageMeta.getContentCreationDate().get();
        assertEquals(2020, dateTime.getYear(),
                "Should select earliest date (DateTimeOriginal: 2020)");
        assertEquals(1, dateTime.getMonthValue());
        assertEquals(1, dateTime.getDayOfMonth());
        assertEquals(10, dateTime.getHour());
        assertEquals(0, dateTime.getMinute());
        assertEquals(0, dateTime.getSecond());
    }

    @Test
    void testExtract_Pre1970Date_1950() {
        // Test file has date from 1950 (before Unix epoch 1970)
        File testFile = getTestFile("test_jpg_date_1950.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should handle pre-1970 dates correctly
        assertTrue(imageMeta.getContentCreationDate().isPresent(),
                "Should extract pre-1970 dates");

        java.time.LocalDateTime dateTime = imageMeta.getContentCreationDate().get();
        assertEquals(1950, dateTime.getYear(), "Should correctly extract year 1950");
        assertEquals(7, dateTime.getMonthValue());
        assertEquals(15, dateTime.getDayOfMonth());
        assertEquals(12, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
        assertEquals(0, dateTime.getSecond());
    }

    @Test
    void testExtract_Pre1970Date_EdgeOfEpoch() {
        // Test file has date from 1969-12-31 23:59:59 (edge of Unix epoch)
        File testFile = getTestFile("test_jpg_date_1969.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should handle edge of epoch correctly
        assertTrue(imageMeta.getContentCreationDate().isPresent(),
                "Should extract date at edge of Unix epoch");

        java.time.LocalDateTime dateTime = imageMeta.getContentCreationDate().get();
        assertEquals(1969, dateTime.getYear(), "Should correctly extract year 1969");
        assertEquals(12, dateTime.getMonthValue());
        assertEquals(31, dateTime.getDayOfMonth());
        assertEquals(23, dateTime.getHour());
        assertEquals(59, dateTime.getMinute());
        assertEquals(59, dateTime.getSecond());
    }

    @Test
    void testExtract_PartialMetadata_OnlyDimensions() {
        // Clean file has dimensions but no datetime or GPS
        File testFile = getTestFile("test_jpg_clean.jpg");

        FileMeta result = extractor.extract(testFile, "image/jpeg");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        // Should have dimensions (always present from JPEG structure)
        assertTrue(imageMeta.getWidth().isPresent(), "Width should be present");
        assertTrue(imageMeta.getHeight().isPresent(), "Height should be present");
        assertEquals(320, imageMeta.getWidth().get());
        assertEquals(240, imageMeta.getHeight().get());

        // Should NOT have datetime
        assertFalse(imageMeta.getContentCreationDate().isPresent(),
                "Clean file should not have datetime");

        // Should NOT have GPS
        if (result.getMetaInfo() != null && !result.getMetaInfo().isEmpty()) {
            assertFalse(result.getMetaInfo().keySet().stream()
                            .anyMatch(key -> key.toLowerCase().contains("gps")),
                    "Clean file should not have GPS metadata");
        }
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
                "test_jpg_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.jpg",
                "test_jpg_conflicting_dates.jpg",
                "test_jpg_date_1950.jpg",
                "test_jpg_date_1969.jpg"
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
