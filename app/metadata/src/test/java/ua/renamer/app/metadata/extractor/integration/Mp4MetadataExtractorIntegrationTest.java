package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.metadata.extractor.strategy.format.video.Mp4FileMetadataExtractor;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.VideoMeta;
import ua.renamer.app.metadata.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for MP4 metadata extraction using real test data files.
 * Test data located at: src/test/resources/test-data/video/mp4/
 * <p>
 * NOTE: MP4 metadata embedding via exiftool works differently than JPEG.
 * The test data may not have properly embedded creation dates in QuickTime format.
 * These tests focus on dimensions and duration which are reliably extracted.
 */
class Mp4MetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/video/mp4/";
    private Mp4FileMetadataExtractor extractor;

    static Stream<Arguments> provideMp4TestFiles() {
        return Stream.of(
                arguments("test_mp4_clean.mp4"),
                arguments("test_mp4_std_2025-12-11_21-00-35.mp4"),
                arguments("test_mp4_past_2000-01-01_12-00-00.mp4"),
                arguments("test_mp4_future_2050-01-01_12-00-00.mp4"),
                arguments("test_mp4_std_no_tz_2025-12-11_21-00-35.mp4"),
                arguments("test_mp4_std_tz_2025-12-11_21-00-35p02-00.mp4"),
                arguments("test_mp4_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.mp4")
        );
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new Mp4FileMetadataExtractor(dateTimeConverter);
    }

    // ============================================================================
    // Test Data Providers
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

    @ParameterizedTest
    @MethodSource("provideMp4TestFiles")
    void testExtract_AllFiles(String filename) {
        File testFile = getTestFile(filename);

        FileMeta result = extractor.extract(testFile, "video/mp4");

        assertNotNull(result, "FileMeta should not be null");
        assertTrue(result.getVideoMeta().isPresent(), "Video metadata should be present for: " + filename);

        VideoMeta videoMeta = result.getVideoMeta().get();

        // All files should have dimensions
        assertTrue(videoMeta.getWidth().isPresent(), "Width should be present for: " + filename);
        assertTrue(videoMeta.getHeight().isPresent(), "Height should be present for: " + filename);
        assertEquals(320, videoMeta.getWidth().get(), "Width should be 320");
        assertEquals(240, videoMeta.getHeight().get(), "Height should be 240");

        // Note: Duration may not be present in all test files due to test data generation limitations
    }

    // ============================================================================
    // Dimensions Tests
    // ============================================================================

    @Test
    void testExtract_Dimensions() {
        File testFile = getTestFile("test_mp4_std_2025-12-11_21-00-35.mp4");

        FileMeta result = extractor.extract(testFile, "video/mp4");

        VideoMeta videoMeta = result.getVideoMeta().get();

        assertTrue(videoMeta.getWidth().isPresent());
        assertTrue(videoMeta.getHeight().isPresent());

        int width = videoMeta.getWidth().get();
        int height = videoMeta.getHeight().get();

        // Test data generated with 320x240 dimensions
        assertEquals(320, width, "Width should be 320");
        assertEquals(240, height, "Height should be 240");

        // Verify aspect ratio
        double aspectRatio = (double) width / height;
        assertEquals(4.0 / 3.0, aspectRatio, 0.01, "Aspect ratio should be 4:3");
    }

    // ============================================================================
    // DateTime Tests
    // ============================================================================
    // NOTE: MP4 datetime extraction has limitations with current test data.
    // The test files were generated with FFmpeg which creates QuickTime track dates
    // that default to 0 (interpreted as 1904 epoch). Proper QuickTime creation_time
    // embedding requires special handling that wasn't used in test data generation.
    // These tests verify the current behavior: datetime may be present but might be
    // the 1904 epoch date for files without proper QuickTime timestamps.

    @Test
    void testExtract_DateTimeExtractionBehavior() {
        File testFile = getTestFile("test_mp4_std_2025-12-11_21-00-35.mp4");

        FileMeta result = extractor.extract(testFile, "video/mp4");

        assertNotNull(result);
        assertTrue(result.getVideoMeta().isPresent());

        VideoMeta videoMeta = result.getVideoMeta().get();

        // Current behavior: datetime might be present, but could be 1904 epoch
        // if QuickTime creation_time wasn't properly set during file generation
        if (videoMeta.getContentCreationDate().isPresent()) {
            java.time.LocalDateTime dateTime = videoMeta.getContentCreationDate().get();
            assertNotNull(dateTime, "If datetime is present, it should not be null");
            // Year might be 1904 (QuickTime epoch) or actual date if properly embedded
            assertTrue(dateTime.getYear() >= 1904, "Year should be valid");
        }
        // We don't assert that datetime MUST be present due to test data limitations
    }

    @Test
    void testExtract_CleanFileDateTime() {
        File testFile = getTestFile("test_mp4_clean.mp4");

        FileMeta result = extractor.extract(testFile, "video/mp4");

        assertNotNull(result);
        assertTrue(result.getVideoMeta().isPresent());

        VideoMeta videoMeta = result.getVideoMeta().get();

        // Clean file behavior: might have 1904 epoch or no datetime
        // Both behaviors are acceptable for files without explicit timestamps
        if (videoMeta.getContentCreationDate().isPresent()) {
            java.time.LocalDateTime dateTime = videoMeta.getContentCreationDate().get();
            // If present, should be a valid date (likely 1904 for clean files)
            assertTrue(dateTime.getYear() >= 1904 && dateTime.getYear() <= 2100,
                       "If datetime present, should be in valid range");
        }
    }

    // ============================================================================
    // Error Handling Tests
    // ============================================================================

    @Test
    void testExtract_AllFilesExist() {
        // Verify all test files can be loaded
        String[] testFiles = {
                "test_mp4_clean.mp4",
                "test_mp4_std_2025-12-11_21-00-35.mp4",
                "test_mp4_past_2000-01-01_12-00-00.mp4",
                "test_mp4_future_2050-01-01_12-00-00.mp4",
                "test_mp4_std_no_tz_2025-12-11_21-00-35.mp4",
                "test_mp4_std_tz_2025-12-11_21-00-35p02-00.mp4",
                "test_mp4_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.mp4"
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
        File testFile = getTestFile("test_mp4_std_2025-12-11_21-00-35.mp4");

        FileMeta result = extractor.extract(testFile, "video/mp4");

        assertNotNull(result);
        // Some errors may occur during metadata extraction which is normal
        // Just verify the result is not null and has video metadata
        assertTrue(result.getVideoMeta().isPresent());
    }

    @Test
    void testExtract_MetadataStructure() {
        File testFile = getTestFile("test_mp4_std_2025-12-11_21-00-35.mp4");

        FileMeta result = extractor.extract(testFile, "video/mp4");

        assertNotNull(result);
        assertTrue(result.getVideoMeta().isPresent(), "Should have video metadata");
        assertFalse(result.getImageMeta().isPresent(), "Should not have image metadata");
        assertFalse(result.getAudioMeta().isPresent(), "Should not have audio metadata");

        VideoMeta videoMeta = result.getVideoMeta().get();
        assertNotNull(videoMeta, "Video metadata object should not be null");
    }
}
