package ua.renamer.app.metadata.extractor.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.ImageMeta;
import ua.renamer.app.metadata.extractor.strategy.format.image.WebPFileMetadataExtractor;
import ua.renamer.app.metadata.util.DateTimeConverter;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for WebP metadata extraction using real test data files.
 */
class WebPMetadataExtractorIntegrationTest {

    private static final String TEST_DATA_PATH = "test-data/image/webp/";
    private WebPFileMetadataExtractor extractor;

    static Stream<Arguments> provideWebPTestFiles() {
        return Stream.of(
                arguments("test_webp_clean.webp", null, false, true),
                arguments("test_webp_std_2025-12-11_21-00-35.webp",
                        LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true),
                arguments("test_webp_past_2000-01-01_12-00-00.webp",
                        LocalDateTime.of(2000, 1, 1, 12, 0, 0), true, true),
                arguments("test_webp_future_2050-01-01_12-00-00.webp",
                        LocalDateTime.of(2050, 1, 1, 12, 0, 0), true, true),
                arguments("test_webp_std_no_tz_2025-12-11_21-00-35.webp",
                        LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true),
                arguments("test_webp_std_tz_2025-12-11_21-00-35p02-00.webp",
                        LocalDateTime.of(2025, 12, 11, 21, 0, 35), true, true)
        );
    }

    @BeforeEach
    void setUp() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter();
        extractor = new WebPFileMetadataExtractor(dateTimeConverter);
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

    @Test
    void testExtract_CleanWebP() {
        File testFile = getTestFile("test_webp_clean.webp");

        FileMeta result = extractor.extract(testFile, "image/webp");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        assertTrue(imageMeta.getContentCreationDate().isEmpty());
        assertTrue(imageMeta.getWidth().isPresent());
        assertTrue(imageMeta.getHeight().isPresent());

        assertEquals(320, imageMeta.getWidth().get());
        assertEquals(240, imageMeta.getHeight().get());
    }

    @ParameterizedTest
    @MethodSource("provideWebPTestFiles")
    void testExtract_VariousScenarios(String filename, LocalDateTime expectedDate,
                                      boolean hasDate, boolean hasDimensions) {
        File testFile = getTestFile(filename);

        FileMeta result = extractor.extract(testFile, "image/webp");

        assertNotNull(result);
        assertTrue(result.getImageMeta().isPresent());

        ImageMeta imageMeta = result.getImageMeta().get();

        if (hasDate) {
            assertTrue(imageMeta.getContentCreationDate().isPresent(),
                    "Creation date should be present for: " + filename);
            assertEquals(expectedDate, imageMeta.getContentCreationDate().get(),
                    "Creation date mismatch for: " + filename);
        } else {
            assertTrue(imageMeta.getContentCreationDate().isEmpty());
        }

        if (hasDimensions) {
            assertTrue(imageMeta.getWidth().isPresent());
            assertTrue(imageMeta.getHeight().isPresent());
            assertEquals(320, imageMeta.getWidth().get());
            assertEquals(240, imageMeta.getHeight().get());
        }
    }

    @Test
    void testExtract_Dimensions() {
        File testFile = getTestFile("test_webp_std_2025-12-11_21-00-35.webp");

        FileMeta result = extractor.extract(testFile, "image/webp");
        ImageMeta imageMeta = result.getImageMeta().get();

        assertEquals(320, imageMeta.getWidth().get());
        assertEquals(240, imageMeta.getHeight().get());
    }
}
