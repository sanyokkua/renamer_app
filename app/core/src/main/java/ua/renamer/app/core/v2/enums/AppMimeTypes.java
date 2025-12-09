package ua.renamer.app.core.v2.enums;

import lombok.Getter;

@Getter
public enum AppMimeTypes {
    APPLICATION_POSTSCRIPT("application/postscript"),
    AUDIO_MP4("audio/mp4"),
    AUDIO_MPEG("audio/mpeg"),
    AUDIO_WAV("audio/wav"),
    IMAGE_BMP("image/bmp"),
    IMAGE_GIF("image/gif"),
    IMAGE_HEIC("image/heic"),
    IMAGE_HEIF("image/heif"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_TIFF("image/tiff"),
    IMAGE_VND_ADOBE_PHOTOSHOP("image/vnd.adobe.photoshop"),
    IMAGE_WEBP("image/webp"),
    IMAGE_X_ICON("image/x-icon"),
    IMAGE_X_PCX("image/x-pcx"),
    VIDEO_MP4("video/mp4"),
    VIDEO_QUICKTIME("video/quicktime"),
    VIDEO_X_MS_VIDEO("video/x-msvideo");

    private final String mime;

    AppMimeTypes(String mime) {
        this.mime = mime;
    }


}
