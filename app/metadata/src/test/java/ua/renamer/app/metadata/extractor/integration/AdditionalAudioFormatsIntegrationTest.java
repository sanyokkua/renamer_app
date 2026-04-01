package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
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
 * Integration tests for additional audio formats (WAV, FLAC, OGG).
 */
class AdditionalAudioFormatsIntegrationTest {

    private UnifiedAudioFileMetadataExtractor extractor;

    static Stream<Arguments> provideAudioTestFiles() {
        return Stream.of(
                // WAV files
                arguments("test-data/audio/wav/test_wav_clean.wav", "audio/wav"),
                arguments("test-data/audio/wav/test_wav_std_2025-12-11_21-00-35.wav", "audio/wav"),

                // FLAC files
                arguments("test-data/audio/flac/test_flac_clean.flac", "audio/flac"),
                arguments("test-data/audio/flac/test_flac_std_2025-12-11_21-00-35.flac", "audio/flac"),

                // OGG files
                arguments("test-data/audio/ogg/test_ogg_clean.ogg", "audio/ogg"),
                arguments("test-data/audio/ogg/test_ogg_std_2025-12-11_21-00-35.ogg", "audio/ogg")
        );
    }

    @BeforeEach
    void setUp() {
        extractor = new UnifiedAudioFileMetadataExtractor();
    }

    private File getTestFile(String path) {
        URL resource = getClass().getClassLoader().getResource(path);
        assertNotNull(resource, "Test file not found: " + path);
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            fail("Failed to load test file: " + path);
            return null;
        }
    }

    @ParameterizedTest
    @MethodSource("provideAudioTestFiles")
    void testExtract_VariousFormats(String filePath, String mimeType) {
        File testFile = getTestFile(filePath);

        FileMeta result = extractor.extract(testFile, mimeType);

        assertNotNull(result, "FileMeta should not be null for: " + filePath);
        assertTrue(result.getAudioMeta().isPresent(),
                "Audio metadata should be present for: " + filePath);

        AudioMeta audioMeta = result.getAudioMeta().get();

        // All audio files should have length/duration
        assertTrue(audioMeta.getLength().isPresent(),
                "Length should be present for: " + filePath);

        int length = audioMeta.getLength().get();
        assertTrue(length > 0, "Length should be positive for: " + filePath);
    }
}
