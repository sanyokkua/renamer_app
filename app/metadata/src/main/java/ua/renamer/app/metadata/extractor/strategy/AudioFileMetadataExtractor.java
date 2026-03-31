package ua.renamer.app.metadata.extractor.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.AppMimeTypes;
import ua.renamer.app.metadata.extractor.strategy.format.audio.UnifiedAudioFileMetadataExtractor;
import ua.renamer.app.api.interfaces.FileMetadataExtractor;
import ua.renamer.app.api.model.meta.FileMeta;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
public class AudioFileMetadataExtractor implements FileMetadataExtractor {
    private final UnifiedAudioFileMetadataExtractor unifiedAudioFileMetadataExtractor;

    @Override
    public FileMeta extract(File file, String mimeType) {
        log.debug("Extracting audio metadata for file: {}, mimeType: {}", file.getName(), mimeType);

        // UnifiedAudioFileMetadataExtractor handles all audio formats via jaudiotagger library
        // Routes all audio MIME types to the same extractor
        if (AppMimeTypes.AUDIO_MP4.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_MPEG.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_MP2.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_WAV.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_FLAC.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_OGG.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_X_MS_WMA.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_AIFF.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_X_AIFF.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_APE.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_MUSEPACK.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_WAVPACK.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_SPEEX.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_OPUS.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_BASIC.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_DSF.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_X_REALAUDIO.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_X_OPTIMFROG.getMime().equals(mimeType) ||
            AppMimeTypes.AUDIO_X_TTA.getMime().equals(mimeType)) {
            return unifiedAudioFileMetadataExtractor.extract(file, mimeType);
        }

        log.warn("Unsupported audio MIME type: {} for file: {}", mimeType, file.getName());
        return FileMeta.withError("Not Supported File MimeType: " + mimeType);
    }
}
