package ua.renamer.app.api.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An enumeration representing MIME types and their associated file extensions.
 */
@Getter
public enum MimeTypes {
    APPLICATION_POSTSCRIPT("application/postscript", ".eps", ".epsf", ".epsi"),
    AUDIO_MP4("audio/mp4", ".m4a", ".m4b", ".m4p", ".m4r"),
    AUDIO_MPEG("audio/mpeg", ".mp3"),
    AUDIO_WAV("audio/wav", ".wav", ".wave"),
    IMAGE_BMP("image/bmp", ".bmp"),
    IMAGE_GIF("image/gif", ".gif"),
    IMAGE_HEIC("image/heic", ".heic"),
    IMAGE_HEIF("image/heif", ".heif"),
    IMAGE_JPEG("image/jpeg", ".jpg", ".jpeg", ".jpe"),
    IMAGE_PNG("image/png", ".png"),
    IMAGE_TIFF("image/tiff", ".tiff", ".tif"),
    IMAGE_VND_ADOBE_PHOTOSHOP("image/vnd.adobe.photoshop", ".psd"),
    IMAGE_WEBP("image/webp", ".webp"),
    IMAGE_X_ICON("image/x-icon", ".ico"),
    IMAGE_X_PCX("image/x-pcx", ".pcx"),
    VIDEO_MP4("video/mp4", ".mp4", ".m4v"),
    VIDEO_QUICKTIME("video/quicktime", ".mov", ".qt"),
    VIDEO_X_MSVIDEO("video/x-msvideo", ".avi");

    private final String mime;
    private final Set<String> extensions;

    MimeTypes(String mime, String... extensions) {
        this.mime = mime;
        this.extensions = Arrays.stream(extensions).collect(Collectors.toSet());
    }

    /**
     * Returns all extensions associated with the given MIME type string.
     *
     * @param mimeString the MIME type string to look up; may be null or empty
     * @return the set of extensions, or an empty set if not found
     */
    public static Set<String> getExtensionsByMimeString(String mimeString) {
        if (Objects.isNull(mimeString) || mimeString.isEmpty()) {
            return Set.of();
        }
        try {
            return Arrays.stream(MimeTypes.values())
                         .filter(mime -> mime.getMime().equals(mimeString))
                         .findFirst()
                         .map(MimeTypes::getExtensions)
                         .orElse(Set.of());
        } catch (IllegalArgumentException ex) {
            return Set.of();
        }
    }
}
