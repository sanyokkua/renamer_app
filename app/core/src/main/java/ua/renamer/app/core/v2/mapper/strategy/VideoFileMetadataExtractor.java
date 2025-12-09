package ua.renamer.app.core.v2.mapper.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.renamer.app.core.v2.enums.AppMimeTypes;
import ua.renamer.app.core.v2.interfaces.FileMetadataExtractor;
import ua.renamer.app.core.v2.mapper.strategy.format.video.AviFileMetadataExtractor;
import ua.renamer.app.core.v2.mapper.strategy.format.video.Mp4FileMetadataExtractor;
import ua.renamer.app.core.v2.mapper.strategy.format.video.QuickTimeFileMetadataExtractor;
import ua.renamer.app.core.v2.model.meta.FileMeta;

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
