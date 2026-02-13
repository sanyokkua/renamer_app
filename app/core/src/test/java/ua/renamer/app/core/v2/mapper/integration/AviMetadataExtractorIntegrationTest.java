package ua.renamer.app.core.v2.mapper.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.core.v2.mapper.strategy.format.video.AviFileMetadataExtractor;
import ua.renamer.app.core.v2.model.meta.FileMeta;
import ua.renamer.app.core.v2.model.meta.category.VideoMeta;
import ua.renamer.app.core.v2.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for AVI metadata extraction.
 */
class AviMetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/video/avi/";
    private AviFileMetadataExtractor extractor;

    static Stream<Arguments> provideAviTestFiles() {
        return Stream.of(
                arguments("test_avi_clean.avi"),
                arguments("test_avi_std_2025-12-11_21-00-35.avi"),
                arguments("test_avi_past_2000-01-01_12-00-00.avi"),
                arguments("test_avi_future_2050-01-01_12-00-00.avi"),
                arguments("test_avi_std_no_tz_2025-12-11_21-00-35.avi"),
                arguments("test_avi_std_tz_2025-12-11_21-00-35p02-00.avi"),
                arguments("test_avi_gps_2025-12-11_21-00-35_lat48.8566_lon2.3522.avi")
        );
    }

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new AviFileMetadataExtractor(dateTimeConverter);
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
    @MethodSource("provideAviTestFiles")
    void testExtract_AllFiles(String filename) {
        File testFile = getTestFile(filename);

        FileMeta result = extractor.extract(testFile, "video/x-msvideo");

        assertNotNull(result);
        assertTrue(result.getVideoMeta().isPresent());

        VideoMeta videoMeta = result.getVideoMeta().get();

        // AVI files should have dimensions
        assertTrue(videoMeta.getWidth().isPresent(), "Width should be present for: " + filename);
        assertTrue(videoMeta.getHeight().isPresent(), "Height should be present for: " + filename);
        assertEquals(320, videoMeta.getWidth().get());
        assertEquals(240, videoMeta.getHeight().get());
    }

    @Test
    void testExtract_Dimensions() {
        File testFile = getTestFile("test_avi_std_2025-12-11_21-00-35.avi");

        FileMeta result = extractor.extract(testFile, "video/x-msvideo");
        VideoMeta videoMeta = result.getVideoMeta().get();

        assertEquals(320, videoMeta.getWidth().get());
        assertEquals(240, videoMeta.getHeight().get());
    }

    // ============================================================================
    // DateTime Tests
    // ============================================================================
    // NOTE: AVI format has very limited metadata support.
    // AVI does NOT have standard datetime metadata fields like MP4/MOV.
    // These tests verify that AVI files do not extract datetime information.

    @Test
    void testExtract_NoDateTimeMetadata() {
        File testFile = getTestFile("test_avi_std_2025-12-11_21-00-35.avi");

        FileMeta result = extractor.extract(testFile, "video/x-msvideo");

        assertNotNull(result);
        assertTrue(result.getVideoMeta().isPresent());

        VideoMeta videoMeta = result.getVideoMeta().get();

        // AVI format does not support datetime metadata
        assertFalse(videoMeta.getContentCreationDate().isPresent(),
                    "AVI format does not support datetime metadata");
    }

    @Test
    void testExtract_CleanFileNoDateTime() {
        File testFile = getTestFile("test_avi_clean.avi");

        FileMeta result = extractor.extract(testFile, "video/x-msvideo");

        assertNotNull(result);
        assertTrue(result.getVideoMeta().isPresent());

        VideoMeta videoMeta = result.getVideoMeta().get();

        // Clean AVI file should not have datetime
        assertFalse(videoMeta.getContentCreationDate().isPresent(),
                    "AVI format does not support datetime metadata");
    }
}
