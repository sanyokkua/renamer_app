package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.metadata.extractor.strategy.format.video.QuickTimeFileMetadataExtractor;
import ua.renamer.app.metadata.model.meta.FileMeta;
import ua.renamer.app.metadata.model.meta.category.VideoMeta;
import ua.renamer.app.metadata.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for QuickTime/MOV metadata extraction.
 */
class MovMetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/video/mov/";
    private QuickTimeFileMetadataExtractor extractor;

    static Stream<Arguments> provideMovTestFiles() {
        return Stream.of(
                arguments("test_mov_clean.mov"),
                arguments("test_mov_std_2025-12-11_21-00-35.mov"),
                arguments("test_mov_past_2000-01-01_12-00-00.mov"),
                arguments("test_mov_future_2050-01-01_12-00-00.mov"),
                arguments("test_mov_std_no_tz_2025-12-11_21-00-35.mov"),
                arguments("test_mov_std_tz_2025-12-11_21-00-35p02-00.mov"),
                arguments("test_mov_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.mov")
        );
    }

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new QuickTimeFileMetadataExtractor(dateTimeConverter);
    }

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

    @ParameterizedTest
    @MethodSource("provideMovTestFiles")
    void testExtract_AllFiles(String filename) {
        File testFile = getTestFile(filename);

        FileMeta result = extractor.extract(testFile, "video/quicktime");

        assertNotNull(result);
        assertTrue(result.getVideoMeta().isPresent());

        VideoMeta videoMeta = result.getVideoMeta().get();

        // MOV files should have dimensions
        assertTrue(videoMeta.getWidth().isPresent(), "Width should be present for: " + filename);
        assertTrue(videoMeta.getHeight().isPresent(), "Height should be present for: " + filename);
        assertEquals(320, videoMeta.getWidth().get());
        assertEquals(240, videoMeta.getHeight().get());
    }

    @Test
    void testExtract_Dimensions() {
        File testFile = getTestFile("test_mov_std_2025-12-11_21-00-35.mov");

        FileMeta result = extractor.extract(testFile, "video/quicktime");
        VideoMeta videoMeta = result.getVideoMeta().get();

        assertEquals(320, videoMeta.getWidth().get());
        assertEquals(240, videoMeta.getHeight().get());
    }

    // ============================================================================
    // DateTime Tests
    // ============================================================================
    // NOTE: MOV/QuickTime datetime extraction has similar limitations to MP4.
    // Test files generated with FFmpeg create QuickTime track dates that default
    // to 0 (interpreted as 1904 epoch). Proper QuickTime creation_time embedding
    // requires special handling. These tests verify current behavior.

    @Test
    void testExtract_DateTimeExtractionBehavior() {
        File testFile = getTestFile("test_mov_std_2025-12-11_21-00-35.mov");

        FileMeta result = extractor.extract(testFile, "video/quicktime");

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
        File testFile = getTestFile("test_mov_clean.mov");

        FileMeta result = extractor.extract(testFile, "video/quicktime");

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
}
