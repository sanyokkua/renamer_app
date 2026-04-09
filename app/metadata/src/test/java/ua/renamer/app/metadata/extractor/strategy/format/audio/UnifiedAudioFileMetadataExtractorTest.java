package ua.renamer.app.metadata.extractor.strategy.format.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.api.model.meta.category.AudioMeta;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Unit tests for {@link UnifiedAudioFileMetadataExtractor}.
 *
 * <p>Tests cover: happy path extraction for all supported audio formats, no-tag / clean
 * files, corrupt / empty / non-audio files (no-throw contract), null input, and year
 * parsing boundary conditions.</p>
 */
class UnifiedAudioFileMetadataExtractorTest {

    private static final String MP3_DIR = "test-data/audio/mp3/";
    private static final String WAV_DIR = "test-data/audio/wav/";
    private static final String FLAC_DIR = "test-data/audio/flac/";
    private static final String OGG_DIR = "test-data/audio/ogg/";

    private UnifiedAudioFileMetadataExtractor extractor;

    static Stream<Arguments> allAudioFiles() {
        return Stream.of(
                // MP3 variants — test_mp3_no_tags.mp3 excluded here; it is ~0.26 s so
                // jaudiotagger rounds getTrackLength() to 0, meaning length is absent.
                // It has its own dedicated test in NoTagFiles.
                arguments(MP3_DIR + "test_mp3_clean.mp3", "audio/mpeg"),
                arguments(MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3", "audio/mpeg"),
                arguments(MP3_DIR + "test_mp3_past_2000-01-01_12-00-00.mp3", "audio/mpeg"),
                arguments(MP3_DIR + "test_mp3_future_2050-01-01_12-00-00.mp3", "audio/mpeg"),
                arguments(MP3_DIR + "test_mp3_std_no_tz_2025-12-11_21-00-35.mp3", "audio/mpeg"),
                arguments(MP3_DIR + "test_mp3_std_tz_2025-12-11_21-00-35p02-00.mp3", "audio/mpeg"),
                // WAV variants
                arguments(WAV_DIR + "test_wav_clean.wav", "audio/wav"),
                arguments(WAV_DIR + "test_wav_std_2025-12-11_21-00-35.wav", "audio/wav"),
                arguments(WAV_DIR + "test_wav_past_2000-01-01_12-00-00.wav", "audio/wav"),
                arguments(WAV_DIR + "test_wav_future_2050-01-01_12-00-00.wav", "audio/wav"),
                // FLAC variants
                arguments(FLAC_DIR + "test_flac_clean.flac", "audio/flac"),
                arguments(FLAC_DIR + "test_flac_std_2025-12-11_21-00-35.flac", "audio/flac"),
                arguments(FLAC_DIR + "test_flac_past_2000-01-01_12-00-00.flac", "audio/flac"),
                arguments(FLAC_DIR + "test_flac_future_2050-01-01_12-00-00.flac", "audio/flac"),
                // OGG variants
                arguments(OGG_DIR + "test_ogg_clean.ogg", "audio/ogg"),
                arguments(OGG_DIR + "test_ogg_std_2025-12-11_21-00-35.ogg", "audio/ogg"),
                arguments(OGG_DIR + "test_ogg_past_2000-01-01_12-00-00.ogg", "audio/ogg"),
                arguments(OGG_DIR + "test_ogg_future_2050-01-01_12-00-00.ogg", "audio/ogg")
        );
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    static Stream<Arguments> mp3FilesWithDuration() {
        // Standard MP3 test files were generated at ~1 s; they yield getTrackLength() >= 1
        // test_mp3_no_tags.mp3 is only ~0.26 s — integer rounding makes getTrackLength() == 0
        return Stream.of(
                arguments(MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3", 1, 2),
                arguments(MP3_DIR + "test_mp3_past_2000-01-01_12-00-00.mp3", 1, 2),
                arguments(MP3_DIR + "test_mp3_clean.mp3", 1, 2)
        );
    }

    // =========================================================================
    // Parameterized sources
    // =========================================================================

    @BeforeEach
    void setUp() {
        extractor = new UnifiedAudioFileMetadataExtractor();
    }

    private File getTestFile(String resourcePath) {
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        assertNotNull(resource, "Test resource not found: " + resourcePath);
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Bad URI for resource: " + resourcePath, e);
        }
    }

    // =========================================================================
    // 1. Happy Path — extract() returns non-null FileMeta with AudioMeta
    // =========================================================================

    @Nested
    class HappyPath {

        @ParameterizedTest
        @MethodSource("ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractorTest#allAudioFiles")
        void extract_validAudioFile_returnsNonNullFileMeta(String resourcePath, String mimeType) {
            File file = getTestFile(resourcePath);

            FileMeta result = extractor.extract(file, mimeType);

            assertThat(result).isNotNull();
        }

        @ParameterizedTest
        @MethodSource("ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractorTest#allAudioFiles")
        void extract_validAudioFile_audioMetaPresentInResult(String resourcePath, String mimeType) {
            File file = getTestFile(resourcePath);

            FileMeta result = extractor.extract(file, mimeType);

            assertThat(result.getAudioMeta()).isPresent();
        }

        @ParameterizedTest
        @MethodSource("ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractorTest#allAudioFiles")
        void extract_validAudioFile_noImageOrVideoMeta(String resourcePath, String mimeType) {
            File file = getTestFile(resourcePath);

            FileMeta result = extractor.extract(file, mimeType);

            assertThat(result.getImageMeta()).isEmpty();
            assertThat(result.getVideoMeta()).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractorTest#allAudioFiles")
        void extract_validAudioFile_positiveDuration(String resourcePath, String mimeType) {
            File file = getTestFile(resourcePath);

            FileMeta result = extractor.extract(file, mimeType);

            AudioMeta audioMeta = result.getAudioMeta().get();
            assertThat(audioMeta.getLength()).isPresent();
            assertThat(audioMeta.getLength().get()).isGreaterThan(0);
        }

        @Test
        void extract_mp3StandardFile_durationIsApproximatelyOneSecond() {
            File file = getTestFile(MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3");

            FileMeta result = extractor.extract(file, "audio/mpeg");

            AudioMeta audioMeta = result.getAudioMeta().get();
            assertThat(audioMeta.getLength()).isPresent();
            assertThat(audioMeta.getLength().get()).isBetween(1, 2);
        }

        @Test
        void extract_flacStandardFile_durationIsApproximatelyOneSecond() {
            File file = getTestFile(FLAC_DIR + "test_flac_std_2025-12-11_21-00-35.flac");

            FileMeta result = extractor.extract(file, "audio/flac");

            AudioMeta audioMeta = result.getAudioMeta().get();
            assertThat(audioMeta.getLength()).isPresent();
            assertThat(audioMeta.getLength().get()).isBetween(1, 2);
        }

        @Test
        void extract_mimeTypeIsPassedThrough_doesNotAffectAudioResult() {
            // The extractor does not filter on mimeType; result depends only on file
            File file = getTestFile(MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3");

            FileMeta resultMp3 = extractor.extract(file, "audio/mpeg");
            FileMeta resultMp4a = extractor.extract(file, "audio/mp4");

            assertThat(resultMp3.getAudioMeta()).isPresent();
            assertThat(resultMp4a.getAudioMeta()).isPresent();
        }
    }

    // =========================================================================
    // 2. No-Tag / Clean Files
    // =========================================================================

    @Nested
    class NoTagFiles {

        @Test
        void extract_mp3NoTags_audioMetaPresentWithoutTagFields() {
            File file = getTestFile(MP3_DIR + "test_mp3_no_tags.mp3");

            FileMeta result = extractor.extract(file, "audio/mpeg");

            assertThat(result.getAudioMeta()).isPresent();
            AudioMeta audioMeta = result.getAudioMeta().get();
            // No ID3 tags embedded; tag fields are absent
            assertThat(audioMeta.getArtistName()).isEmpty();
            assertThat(audioMeta.getAlbumName()).isEmpty();
            assertThat(audioMeta.getSongName()).isEmpty();
            assertThat(audioMeta.getYear()).isEmpty();
        }

        @Test
        void extract_mp3NoTagsSubSecondFile_audioMetaPresentLengthAbsent() {
            // test_mp3_no_tags.mp3 is ~0.26 s; jaudiotagger getTrackLength() returns 0
            // so extractDurationSafely() stores null — length Optional is empty
            File file = getTestFile(MP3_DIR + "test_mp3_no_tags.mp3");

            FileMeta result = extractor.extract(file, "audio/mpeg");

            assertThat(result.getAudioMeta()).isPresent();
            AudioMeta audioMeta = result.getAudioMeta().get();
            // Sub-second file: length is absent because integer rounding drops it to 0
            assertThat(audioMeta.getLength()).isEmpty();
        }

        @Test
        void extract_mp3CleanFile_audioMetaPresentWithoutTagFields() {
            File file = getTestFile(MP3_DIR + "test_mp3_clean.mp3");

            FileMeta result = extractor.extract(file, "audio/mpeg");

            assertThat(result.getAudioMeta()).isPresent();
            AudioMeta audioMeta = result.getAudioMeta().get();
            assertThat(audioMeta.getArtistName()).isEmpty();
            assertThat(audioMeta.getAlbumName()).isEmpty();
            assertThat(audioMeta.getSongName()).isEmpty();
        }

        @Test
        void extract_wavCleanFile_audioMetaPresent() {
            File file = getTestFile(WAV_DIR + "test_wav_clean.wav");

            FileMeta result = extractor.extract(file, "audio/wav");

            assertThat(result.getAudioMeta()).isPresent();
        }

        @Test
        void extract_flacCleanFile_audioMetaPresent() {
            File file = getTestFile(FLAC_DIR + "test_flac_clean.flac");

            FileMeta result = extractor.extract(file, "audio/flac");

            assertThat(result.getAudioMeta()).isPresent();
        }

        @Test
        void extract_oggCleanFile_audioMetaPresent() {
            File file = getTestFile(OGG_DIR + "test_ogg_clean.ogg");

            FileMeta result = extractor.extract(file, "audio/ogg");

            assertThat(result.getAudioMeta()).isPresent();
        }
    }

    // =========================================================================
    // 3. No-Throw Contract — corrupt / missing / non-audio files
    // =========================================================================

    @Nested
    class NoThrowContract {

        @Test
        void extract_nonExistentFile_doesNotThrow(@TempDir Path tempDir) {
            File ghost = tempDir.resolve("does_not_exist.mp3").toFile();

            assertThatCode(() -> extractor.extract(ghost, "audio/mpeg"))
                    .doesNotThrowAnyException();
        }

        @Test
        void extract_nonExistentFile_returnsFileMeta(@TempDir Path tempDir) {
            File ghost = tempDir.resolve("does_not_exist.mp3").toFile();

            FileMeta result = extractor.extract(ghost, "audio/mpeg");

            assertThat(result).isNotNull();
        }

        @Test
        void extract_emptyFile_doesNotThrow(@TempDir Path tempDir) throws IOException {
            Path empty = Files.createFile(tempDir.resolve("empty.mp3"));

            assertThatCode(() -> extractor.extract(empty.toFile(), "audio/mpeg"))
                    .doesNotThrowAnyException();
        }

        @Test
        void extract_emptyFile_returnsFileMeta(@TempDir Path tempDir) throws IOException {
            Path empty = Files.createFile(tempDir.resolve("empty.mp3"));

            FileMeta result = extractor.extract(empty.toFile(), "audio/mpeg");

            assertThat(result).isNotNull();
        }

        @Test
        void extract_binaryGarbageFile_doesNotThrow(@TempDir Path tempDir) throws IOException {
            Path garbage = tempDir.resolve("garbage.mp3");
            Files.write(garbage, new byte[]{0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE, 0x7F, 0x3A, 0x5B});

            assertThatCode(() -> extractor.extract(garbage.toFile(), "audio/mpeg"))
                    .doesNotThrowAnyException();
        }

        @Test
        void extract_binaryGarbageFile_returnsFileMeta(@TempDir Path tempDir) throws IOException {
            Path garbage = tempDir.resolve("garbage.mp3");
            Files.write(garbage, new byte[]{0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE, 0x7F, 0x3A, 0x5B});

            FileMeta result = extractor.extract(garbage.toFile(), "audio/mpeg");

            assertThat(result).isNotNull();
        }

        @Test
        void extract_textFileRenamedToMp3_doesNotThrow(@TempDir Path tempDir) throws IOException {
            Path text = tempDir.resolve("fake.mp3");
            Files.writeString(text, "this is not audio data at all, just plain text");

            assertThatCode(() -> extractor.extract(text.toFile(), "audio/mpeg"))
                    .doesNotThrowAnyException();
        }

        @Test
        void extract_directoryPassedAsFile_doesNotThrow(@TempDir Path tempDir) {
            // On some JVM versions AudioFileIO.read(directory) may throw — must be caught
            assertThatCode(() -> extractor.extract(tempDir.toFile(), "audio/mpeg"))
                    .doesNotThrowAnyException();
        }

        @Test
        void extract_nullMimeType_doesNotThrow() {
            File file = getTestFile(MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3");

            assertThatCode(() -> extractor.extract(file, null))
                    .doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // 4. Year Parsing Boundaries
    // =========================================================================

    @Nested
    class YearParsing {

        @Test
        void extract_allMp3Files_yearWhenPresentIsInValidRange() {
            String[] mp3Files = {
                    MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3",
                    MP3_DIR + "test_mp3_past_2000-01-01_12-00-00.mp3",
                    MP3_DIR + "test_mp3_future_2050-01-01_12-00-00.mp3",
                    MP3_DIR + "test_mp3_clean.mp3",
                    MP3_DIR + "test_mp3_no_tags.mp3"
            };

            for (String path : mp3Files) {
                File file = getTestFile(path);
                FileMeta result = extractor.extract(file, "audio/mpeg");
                result.getAudioMeta().ifPresent(meta ->
                        meta.getYear().ifPresent(year ->
                                assertThat(year)
                                        .as("Year from %s should be in valid range", path)
                                        .isBetween(1900, 2100)));
            }
        }

        @Test
        void extract_allFlacFiles_yearWhenPresentIsInValidRange() {
            String[] flacFiles = {
                    FLAC_DIR + "test_flac_past_2000-01-01_12-00-00.flac",
                    FLAC_DIR + "test_flac_future_2050-01-01_12-00-00.flac",
                    FLAC_DIR + "test_flac_std_2025-12-11_21-00-35.flac"
            };

            for (String path : flacFiles) {
                File file = getTestFile(path);
                FileMeta result = extractor.extract(file, "audio/flac");
                result.getAudioMeta().ifPresent(meta ->
                        meta.getYear().ifPresent(year ->
                                assertThat(year)
                                        .as("Year from %s should be in valid range", path)
                                        .isBetween(1900, 2100)));
            }
        }
    }

    // =========================================================================
    // 5. Return Structure Integrity
    // =========================================================================

    @Nested
    class ReturnStructure {

        @Test
        void extract_validMp3_errorsListIsNeverNull() {
            File file = getTestFile(MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3");

            FileMeta result = extractor.extract(file, "audio/mpeg");

            assertThat(result.getErrors()).isNotNull();
        }

        @Test
        void extract_corruptFile_errorsListIsNeverNull(@TempDir Path tempDir) throws IOException {
            Path garbage = tempDir.resolve("corrupt.mp3");
            Files.write(garbage, new byte[]{0x00, 0x01});

            FileMeta result = extractor.extract(garbage.toFile(), "audio/mpeg");

            assertThat(result.getErrors()).isNotNull();
        }

        @Test
        void extract_validMp3_metaInfoIsNeverNull() {
            File file = getTestFile(MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3");

            FileMeta result = extractor.extract(file, "audio/mpeg");

            assertThat(result.getMetaInfo()).isNotNull();
        }

        @Test
        void extract_consecutiveCalls_produceSameResult() {
            File file = getTestFile(MP3_DIR + "test_mp3_std_2025-12-11_21-00-35.mp3");

            FileMeta first = extractor.extract(file, "audio/mpeg");
            FileMeta second = extractor.extract(file, "audio/mpeg");

            assertThat(first.getAudioMeta().isPresent()).isEqualTo(second.getAudioMeta().isPresent());
            assertThat(first.getErrors().isEmpty()).isEqualTo(second.getErrors().isEmpty());
        }

        @ParameterizedTest
        @MethodSource("ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractorTest#allAudioFiles")
        void extract_validAudioFile_audioMetaFieldsAreNeverExceptionThrowingOptionals(
                String resourcePath, String mimeType) {
            File file = getTestFile(resourcePath);

            FileMeta result = extractor.extract(file, mimeType);

            // Accessing every Optional on AudioMeta must not throw
            result.getAudioMeta().ifPresent(audioMeta -> {
                assertThatCode(audioMeta::getArtistName).doesNotThrowAnyException();
                assertThatCode(audioMeta::getAlbumName).doesNotThrowAnyException();
                assertThatCode(audioMeta::getSongName).doesNotThrowAnyException();
                assertThatCode(audioMeta::getYear).doesNotThrowAnyException();
                assertThatCode(audioMeta::getLength).doesNotThrowAnyException();
            });
        }
    }
}
