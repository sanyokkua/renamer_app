package ua.renamer.app.api.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Known MIME types used during metadata extraction and category detection.
 *
 * <p>Constants that have associated file extensions provide them via the varargs constructor.
 * Use {@link #getExtensionsByMimeString(String)} for a case-insensitive lookup by MIME string.
 */
@Getter
public enum AppMimeTypes {
    /**
     * PostScript and Encapsulated PostScript formats.
     */
    APPLICATION_POSTSCRIPT("application/postscript", ".eps", ".epsf", ".epsi"),
    /**
     * MPEG-4 audio container variants.
     */
    AUDIO_MP4("audio/mp4", ".m4a", ".m4b", ".m4p", ".m4r"),
    /**
     * MPEG audio layer III.
     */
    AUDIO_MPEG("audio/mpeg", ".mp3"),
    /**
     * Waveform Audio File Format.
     */
    AUDIO_WAV("audio/wav", ".wav", ".wave"),
    /**
     * Free Lossless Audio Codec.
     */
    AUDIO_FLAC("audio/flac", ".flac"),
    /**
     * Ogg Vorbis audio.
     */
    AUDIO_OGG("audio/ogg", ".ogg"),
    /**
     * Windows Media Audio.
     */
    AUDIO_X_MS_WMA("audio/x-ms-wma", ".wma"),
    /**
     * Audio Interchange File Format.
     */
    AUDIO_AIFF("audio/aiff", ".aiff", ".aif"),
    /**
     * Audio Interchange File Format (alternative MIME).
     */
    AUDIO_X_AIFF("audio/x-aiff", ".aiff", ".aif"),
    /**
     * Monkey's Audio lossless codec.
     */
    AUDIO_APE("audio/x-ape", ".ape"),
    /**
     * Musepack lossy audio codec.
     */
    AUDIO_MUSEPACK("audio/x-musepack", ".mpc"),
    /**
     * WavPack hybrid lossless codec.
     */
    AUDIO_WAVPACK("audio/x-wavpack", ".wv"),
    /**
     * Speex speech codec.
     */
    AUDIO_SPEEX("audio/speex", ".spx"),
    /**
     * Opus interactive audio codec.
     */
    AUDIO_OPUS("audio/opus", ".opus"),
    /**
     * AU audio format.
     */
    AUDIO_BASIC("audio/basic", ".au"),
    /**
     * Direct Stream Digital audio.
     */
    AUDIO_DSF("audio/dsf", ".dsf"),
    /**
     * MPEG audio layer II.
     */
    AUDIO_MP2("audio/mp2", ".mp2"),
    /**
     * RealAudio streaming format.
     */
    AUDIO_X_REALAUDIO("audio/x-pn-realaudio", ".ra"),
    /**
     * OptimFROG lossless audio.
     */
    AUDIO_X_OPTIMFROG("audio/x-optimfrog", ".ofr"),
    /**
     * True Audio lossless codec.
     */
    AUDIO_X_TTA("audio/x-tta", ".tta"),
    /**
     * Windows Bitmap image.
     */
    IMAGE_BMP("image/bmp", ".bmp"),
    /**
     * Graphics Interchange Format.
     */
    IMAGE_GIF("image/gif", ".gif"),
    /**
     * High Efficiency Image Container (HEVC).
     */
    IMAGE_HEIC("image/heic", ".heic"),
    /**
     * High Efficiency Image Format.
     */
    IMAGE_HEIF("image/heif", ".heif"),
    /**
     * Joint Photographic Experts Group image.
     */
    IMAGE_JPEG("image/jpeg", ".jpg", ".jpeg", ".jpe"),
    /**
     * Portable Network Graphics image.
     */
    IMAGE_PNG("image/png", ".png"),
    /**
     * Tagged Image File Format.
     */
    IMAGE_TIFF("image/tiff", ".tiff", ".tif"),
    /**
     * Adobe Photoshop document.
     */
    IMAGE_VND_ADOBE_PHOTOSHOP("image/vnd.adobe.photoshop", ".psd"),
    /**
     * WebP image format.
     */
    IMAGE_WEBP("image/webp", ".webp"),
    /**
     * Windows icon format.
     */
    IMAGE_X_ICON("image/x-icon", ".ico"),
    /**
     * ZSoft PCX bitmap image.
     */
    IMAGE_X_PCX("image/x-pcx", ".pcx"),
    /**
     * AV1 Image File Format.
     */
    IMAGE_AVIF("image/avif", ".avif"),
    /**
     * Sony Alpha Raw image.
     */
    IMAGE_X_SONY_ARW("image/x-sony-arw", ".arw"),
    /**
     * Canon Raw version 2 image.
     */
    IMAGE_X_CANON_CR2("image/x-canon-cr2", ".cr2"),
    /**
     * Canon Raw version 3 image.
     */
    IMAGE_X_CANON_CR3("image/x-canon-cr3", ".cr3"),
    /**
     * Nikon Electronic Format raw image.
     */
    IMAGE_X_NIKON_NEF("image/x-nikon-nef", ".nef"),
    /**
     * Olympus Raw Format image.
     */
    IMAGE_X_OLYMPUS_ORF("image/x-olympus-orf", ".orf"),
    /**
     * Fujifilm RAF raw image.
     */
    IMAGE_X_FUJIFILM_RAF("image/x-fujifilm-raf", ".raf"),
    /**
     * Panasonic RW2 raw image.
     */
    IMAGE_X_PANASONIC_RW2("image/x-panasonic-rw2", ".rw2"),
    /**
     * Adobe Digital Negative raw image.
     */
    IMAGE_X_ADOBE_DNG("image/x-adobe-dng", ".dng"),
    /**
     * MPEG-4 video container.
     */
    VIDEO_MP4("video/mp4", ".mp4", ".m4v"),
    /**
     * Apple QuickTime video.
     */
    VIDEO_QUICKTIME("video/quicktime", ".mov", ".qt"),
    /**
     * Audio Video Interleave video.
     */
    VIDEO_X_MS_VIDEO("video/x-msvideo", ".avi");

    private final String mime;
    private final Set<String> extensions;

    AppMimeTypes(String mime, String... extensions) {
        this.mime = mime;
        this.extensions = Arrays.stream(extensions).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all file extensions associated with the given MIME type string.
     *
     * <p>The lookup is case-insensitive. Constants that were defined without extension
     * arguments return an empty set.
     *
     * @param mimeString the MIME type string to look up; may be {@code null} or empty
     * @return the unmodifiable set of extensions for the matched constant, or {@link Set#of()}
     * if the input is {@code null}, empty, or matches no constant with extensions
     */
    public static Set<String> getExtensionsByMimeString(String mimeString) {
        if (Objects.isNull(mimeString) || mimeString.isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(AppMimeTypes.values())
                .filter(mime -> mime.getMime().equalsIgnoreCase(mimeString))
                .findFirst()
                .map(AppMimeTypes::getExtensions)
                .orElse(Set.of());
    }
}
