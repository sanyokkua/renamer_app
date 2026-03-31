package ua.renamer.app.metadata.enums;

import lombok.Getter;

@Getter
public enum AppMimeTypes {
    APPLICATION_POSTSCRIPT("application/postscript"),
    AUDIO_MP4("audio/mp4"),
    AUDIO_MPEG("audio/mpeg"),
    AUDIO_WAV("audio/wav"),
    AUDIO_FLAC("audio/flac"),
    AUDIO_OGG("audio/ogg"),
    AUDIO_X_MS_WMA("audio/x-ms-wma"),
    AUDIO_AIFF("audio/aiff"),
    AUDIO_X_AIFF("audio/x-aiff"),
    AUDIO_APE("audio/x-ape"),
    AUDIO_MUSEPACK("audio/x-musepack"),
    AUDIO_WAVPACK("audio/x-wavpack"),
    AUDIO_SPEEX("audio/speex"),
    AUDIO_OPUS("audio/opus"),
    AUDIO_BASIC("audio/basic"),  // AU format
    AUDIO_DSF("audio/dsf"),
    AUDIO_MP2("audio/mp2"),
    AUDIO_X_REALAUDIO("audio/x-pn-realaudio"),
    AUDIO_X_OPTIMFROG("audio/x-optimfrog"),
    AUDIO_X_TTA("audio/x-tta"),  // True Audio
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
    IMAGE_AVIF("image/avif"),
    IMAGE_X_SONY_ARW("image/x-sony-arw"),
    IMAGE_X_CANON_CR2("image/x-canon-cr2"),
    IMAGE_X_CANON_CR3("image/x-canon-cr3"),
    IMAGE_X_NIKON_NEF("image/x-nikon-nef"),
    IMAGE_X_OLYMPUS_ORF("image/x-olympus-orf"),
    IMAGE_X_FUJIFILM_RAF("image/x-fujifilm-raf"),
    IMAGE_X_PANASONIC_RW2("image/x-panasonic-rw2"),
    IMAGE_X_ADOBE_DNG("image/x-adobe-dng"),
    VIDEO_MP4("video/mp4"),
    VIDEO_QUICKTIME("video/quicktime"),
    VIDEO_X_MS_VIDEO("video/x-msvideo");

    private final String mime;

    AppMimeTypes(String mime) {
        this.mime = mime;
    }


}
