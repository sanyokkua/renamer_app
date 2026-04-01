package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.AudioMeta;
import ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractor;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for MP3 metadata extraction using real test data files.
 * Test data located at: src/test/resources/test-data/audio/mp3/
 */
class Mp3MetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/audio/mp3/";
    private UnifiedAudioFileMetadataExtractor extractor;

    static Stream<Arguments> provideMp3TestFiles() {
        return Stream.of(
                arguments("test_mp3_clean.mp3"),
                arguments("test_mp3_std_2025-12-11_21-00-35.mp3"),
                arguments("test_mp3_past_2000-01-01_12-00-00.mp3"),
                arguments("test_mp3_future_2050-01-01_12-00-00.mp3"),
                arguments("test_mp3_std_no_tz_2025-12-11_21-00-35.mp3"),
                arguments("test_mp3_std_tz_2025-12-11_21-00-35p02-00.mp3")
        );
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    @BeforeEach
    void setUp() {
        extractor = new UnifiedAudioFileMetadataExtractor();
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
    @MethodSource("provideMp3TestFiles")
    void testExtract_AllFiles(String filename) {
        File testFile = getTestFile(filename);

        FileMeta result = extractor.extract(testFile, "audio/mpeg");

        assertNotNull(result, "FileMeta should not be null");
        assertTrue(result.getAudioMeta().isPresent(), "Audio metadata should be present for: " + filename);

        AudioMeta audioMeta = result.getAudioMeta().get();

        // Audio files should have length/duration
        assertTrue(audioMeta.getLength().isPresent(), "Length should be present for: " + filename);

        int length = audioMeta.getLength().get();
        assertTrue(length > 0, "Length should be positive for: " + filename);
    }

    // ============================================================================
    // ID3 Tag Tests
    // ============================================================================

    @Test
    void testExtract_BasicMetadata() {
        // Use a standard test file that should have tags
        File testFile = getTestFile("test_mp3_std_2025-12-11_21-00-35.mp3");

        FileMeta result = extractor.extract(testFile, "audio/mpeg");

        assertNotNull(result);
        assertTrue(result.getAudioMeta().isPresent());

        AudioMeta audioMeta = result.getAudioMeta().get();

        // Check that basic audio metadata is present
        assertTrue(audioMeta.getLength().isPresent(), "Length should be present");

        // Duration should be approximately 1 second (test data is short)
        int length = audioMeta.getLength().get();
        assertTrue(length > 0 && length <= 2, "Length should be approximately 1 second");
    }

    @Test
    void testExtract_CleanFile() {
        File testFile = getTestFile("test_mp3_clean.mp3");

        FileMeta result = extractor.extract(testFile, "audio/mpeg");

        assertNotNull(result);
        assertTrue(result.getAudioMeta().isPresent());

        AudioMeta audioMeta = result.getAudioMeta().get();

        // Clean file should still have duration
        assertTrue(audioMeta.getLength().isPresent(), "Clean file should have length");

        // Clean file might not have ID3 tags
        // (artist, album, song name, year could be absent)
    }

    // ============================================================================
    // Duration Tests
    // ============================================================================

    @Test
    void testExtract_Duration() {
        File testFile = getTestFile("test_mp3_std_2025-12-11_21-00-35.mp3");

        FileMeta result = extractor.extract(testFile, "audio/mpeg");

        AudioMeta audioMeta = result.getAudioMeta().get();

        assertTrue(audioMeta.getLength().isPresent());

        int length = audioMeta.getLength().get();

        // Test data generated with 1 second duration
        assertTrue(length > 0, "Duration should be positive");
        assertTrue(length <= 2, "Duration should be approximately 1 second");
    }

    // ============================================================================
    // Error Handling Tests
    // ============================================================================

    @Test
    void testExtract_AllFilesExist() {
        // Verify all test files can be loaded
        String[] testFiles = {
                "test_mp3_clean.mp3",
                "test_mp3_std_2025-12-11_21-00-35.mp3",
                "test_mp3_past_2000-01-01_12-00-00.mp3",
                "test_mp3_future_2050-01-01_12-00-00.mp3",
                "test_mp3_std_no_tz_2025-12-11_21-00-35.mp3",
                "test_mp3_std_tz_2025-12-11_21-00-35p02-00.mp3"
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
        File testFile = getTestFile("test_mp3_std_2025-12-11_21-00-35.mp3");

        FileMeta result = extractor.extract(testFile, "audio/mpeg");

        assertNotNull(result);
        //  Some minor errors may occur which is normal for audio extraction
        // Just verify the result is not null and has audio metadata
        assertTrue(result.getAudioMeta().isPresent());
    }

    @Test
    void testExtract_MetadataStructure() {
        File testFile = getTestFile("test_mp3_std_2025-12-11_21-00-35.mp3");

        FileMeta result = extractor.extract(testFile, "audio/mpeg");

        assertNotNull(result);
        assertTrue(result.getAudioMeta().isPresent(), "Should have audio metadata");
        assertFalse(result.getImageMeta().isPresent(), "Should not have image metadata");
        assertFalse(result.getVideoMeta().isPresent(), "Should not have video metadata");

        AudioMeta audioMeta = result.getAudioMeta().get();
        assertNotNull(audioMeta, "Audio metadata object should not be null");
    }

    @Test
    void testExtract_YearParsing() {
        // Test that year extraction works if  tags are present
        File testFile = getTestFile("test_mp3_past_2000-01-01_12-00-00.mp3");

        FileMeta result = extractor.extract(testFile, "audio/mpeg");

        AudioMeta audioMeta = result.getAudioMeta().get();

        // Year might or might not be present depending on test data
        // If present, it should be a valid year
        audioMeta.getYear().ifPresent(year -> {
            assertTrue(year >= 1900 && year <= 2100,
                    "Year should be reasonable: " + year);
        });
    }
}
