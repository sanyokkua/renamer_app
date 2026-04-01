package ua.renamer.app.metadata.extractor.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.api.enums.AppMimeTypes;
import ua.renamer.app.api.interfaces.FileMetadataExtractor;
import ua.renamer.app.api.model.meta.FileMeta;
import ua.renamer.app.metadata.extractor.strategy.format.video.AviFileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.video.Mp4FileMetadataExtractor;
import ua.renamer.app.metadata.extractor.strategy.format.video.QuickTimeFileMetadataExtractor;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
public class VideoFileMetadataExtractor implements FileMetadataExtractor {
    private final AviFileMetadataExtractor aviFileMetadataExtractor;
    private final Mp4FileMetadataExtractor mp4FileMetadataExtractor;
    private final QuickTimeFileMetadataExtractor quickTimeFileMetadataExtractor;

    @Override
    public FileMeta extract(File file, String mimeType) {
        log.debug("Extracting video metadata for file: {}, mimeType: {}", file.getName(), mimeType);

        if (AppMimeTypes.VIDEO_X_MS_VIDEO.getMime().equals(mimeType)) {
            return aviFileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.VIDEO_MP4.getMime().equals(mimeType)) {
            return mp4FileMetadataExtractor.extract(file, mimeType);
        }
        if (AppMimeTypes.VIDEO_QUICKTIME.getMime().equals(mimeType)) {
            return quickTimeFileMetadataExtractor.extract(file, mimeType);
        }

        log.warn("Unsupported video MIME type: {} for file: {}", mimeType, file.getName());
        return FileMeta.withError("Not Supported File MimeType: " + mimeType);
    }
}
