package ua.renamer.app.core.v2.mapper.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.enums.AppMimeTypes;
import ua.renamer.app.core.v2.interfaces.FileMetadataExtractor;
import ua.renamer.app.core.v2.mapper.strategy.format.audio.UnifiedAudioFileMetadataExtractor;
import ua.renamer.app.core.v2.model.meta.FileMeta;

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
            AppMimeTypes.AUDIO_WAV.getMime().equals(mimeType)) {
            return unifiedAudioFileMetadataExtractor.extract(file, mimeType);
        }

        log.warn("Unsupported audio MIME type: {} for file: {}", mimeType, file.getName());
        return FileMeta.withError("Not Supported File MimeType: " + mimeType);
    }
}
