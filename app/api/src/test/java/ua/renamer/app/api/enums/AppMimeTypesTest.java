package ua.renamer.app.api.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AppMimeTypes#getExtensionsByMimeString(String)}.
 */
class AppMimeTypesTest {

    /**
     * Provide all 43 constants that are defined with at least one extension argument.
     * Each entry is the MIME string of a constant expected to return a non-empty set.
     */
    static Stream<String> constantsWithExtensions() {
        return Stream.of(
                // Original 18
                "application/postscript",
                "audio/mp4",
                "audio/mpeg",
                "audio/wav",
                "image/bmp",
                "image/gif",
                "image/heic",
                "image/heif",
                "image/jpeg",
                "image/png",
                "image/tiff",
                "image/vnd.adobe.photoshop",
                "image/webp",
                "image/x-icon",
                "image/x-pcx",
                "video/mp4",
                "video/quicktime",
                "video/x-msvideo",
                // Audio additions
                "audio/flac",
                "audio/ogg",
                "audio/x-ms-wma",
                "audio/aiff",
                "audio/x-aiff",
                "audio/x-ape",
                "audio/x-musepack",
                "audio/x-wavpack",
                "audio/speex",
                "audio/opus",
                "audio/basic",
                "audio/dsf",
                "audio/mp2",
                "audio/x-pn-realaudio",
                "audio/x-optimfrog",
                "audio/x-tta",
                // Image additions
                "image/avif",
                "image/x-sony-arw",
                "image/x-canon-cr2",
                "image/x-canon-cr3",
                "image/x-nikon-nef",
                "image/x-olympus-orf",
                "image/x-fujifilm-raf",
                "image/x-panasonic-rw2",
                "image/x-adobe-dng"
        );
    }

    @Test
    void getExtensionsByMimeString_whenNullInput_shouldReturnEmptySet() {
        Set<String> result = AppMimeTypes.getExtensionsByMimeString(null);

        assertThat(result).isEmpty();
    }

    @Test
    void getExtensionsByMimeString_whenEmptyString_shouldReturnEmptySet() {
        Set<String> result = AppMimeTypes.getExtensionsByMimeString("");

        assertThat(result).isEmpty();
    }

    @Test
    void getExtensionsByMimeString_whenUnknownMimeString_shouldReturnEmptySet() {
        Set<String> result = AppMimeTypes.getExtensionsByMimeString("application/x-unknown-format");

        assertThat(result).isEmpty();
    }

    @Test
    void getExtensionsByMimeString_whenImageJpeg_shouldReturnThreeExtensions() {
        Set<String> result = AppMimeTypes.getExtensionsByMimeString("image/jpeg");

        assertThat(result)
                .hasSize(3)
                .containsExactlyInAnyOrder(".jpg", ".jpeg", ".jpe");
    }

    @Test
    void getExtensionsByMimeString_whenAudioFlac_shouldReturnFlacExtension() {
        Set<String> result = AppMimeTypes.getExtensionsByMimeString("audio/flac");

        assertThat(result).containsExactly(".flac");
    }

    @Test
    void getExtensionsByMimeString_whenUpperCaseMimeString_shouldReturnSameAsLowerCase() {
        Set<String> lowerResult = AppMimeTypes.getExtensionsByMimeString("image/jpeg");
        Set<String> upperResult = AppMimeTypes.getExtensionsByMimeString("IMAGE/JPEG");

        assertThat(upperResult).isEqualTo(lowerResult);
    }

    @Test
    void getExtensionsByMimeString_whenMixedCaseMimeString_shouldReturnSameAsLowerCase() {
        Set<String> lowerResult = AppMimeTypes.getExtensionsByMimeString("image/jpeg");
        Set<String> mixedResult = AppMimeTypes.getExtensionsByMimeString("Image/Jpeg");

        assertThat(mixedResult).isEqualTo(lowerResult);
    }

    @ParameterizedTest
    @MethodSource("constantsWithExtensions")
    void getExtensionsByMimeString_forConstantsWithExtensionData_shouldReturnNonEmptySet(String mimeString) {
        Set<String> result = AppMimeTypes.getExtensionsByMimeString(mimeString);

        assertThat(result)
                .as("Expected non-empty extensions for MIME type: %s", mimeString)
                .isNotEmpty();
    }
}
